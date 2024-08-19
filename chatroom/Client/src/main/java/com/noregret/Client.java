package com.noregret;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noregret.Service.SendService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class Client {
    public void init(String host, int port) {
        //创建事件循环组
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            //创建启动辅助类
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024, 0, 4, 0, 4));
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new StringDecoder());
                            //用来判断是不是读/写空闲过长
                            //40s内如果没有收到channel的数据，会触发一个IdleState#WRITER_IDLE事件
                            pipeline.addLast(new IdleStateHandler(0, 40, 0));
                            //ChannelDuplexHandler 可以同时作为入站和出站处理器
                            pipeline.addLast(new ChannelDuplexHandler(){
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    //触发了读空闲事件
                                    if(event.state() == IdleState.WRITER_IDLE){
                                        //log.info("3s没有写数据了，发送一个心跳包");
                                        ObjectMapper objectMapper = new ObjectMapper();
                                        ObjectNode node = objectMapper.createObjectNode();
                                        node.put("type",String.valueOf(MsgType.MSG_PING));
                                        byte[] msg = node.toString().getBytes();
                                        int length = msg.length;
                                        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
                                        buf.writeInt(length);
                                        buf.writeBytes(msg);
                                        ctx.writeAndFlush(buf);
                                    }
                                }
                            });
                            pipeline.addLast(new ClientHandler());
                            pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                        }
                    });
            //连接服务器
            Channel channel = bootstrap.connect(host, port).sync().channel();
            channel.config().setOption(ChannelOption.SO_SNDBUF, 65535);
            SendService sendService = new SendService(channel);

            group.submit(() -> {
                try {
                    sendService.menu();
                } catch (InterruptedException | IOException e) {
                    throw new RuntimeException(e);
                }
            });
            channel.closeFuture().sync();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
