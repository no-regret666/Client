package com.noregret;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Server {
    @Autowired
    ServerHandler serverHandler;
    public void init(int port){
        NioEventLoopGroup boss = new NioEventLoopGroup(1);
        NioEventLoopGroup worker = new NioEventLoopGroup(5);
        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch){
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new StringDecoder());
                            //用来判断是不是读/写空闲过长
                            //5s内如果没有收到channel的数据，会触发一个IdleState#READER_IDLE事件
                            pipeline.addLast(new IdleStateHandler(60, 0, 0));
                            //ChannelDuplexHandler 可以同时作为入站和出站处理器
                            pipeline.addLast(new ChannelDuplexHandler(){
                                @Override
                                public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                    IdleStateEvent event = (IdleStateEvent) evt;
                                    //触发了读空闲事件
                                    if(event.state() == IdleState.READER_IDLE){
                                        log.debug("已经5s没有读到数据了");
                                        ctx.channel().close();
                                    }
                                }
                            });
                            pipeline.addLast(serverHandler);
                            pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                        }
                    });
            Channel channel = bootstrap.bind(port).sync().channel();
            channel.config().setOption(ChannelOption.SO_RCVBUF,65535);
            channel.closeFuture().sync();

        }catch (InterruptedException e){
            throw new RuntimeException(e);
        }finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
