package com.noregret;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ChannelHandler.Sharable
@Slf4j
public class ServerHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private ProcessMsg processMsg;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.debug("Server received message: {}", msg);
        if (msg instanceof String response) {
            processMsg.init(ctx);
            processMsg.sendResponse(response);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (ProcessMsg.isExist(ctx)) {
            String username = ProcessMsg.online2.get(ctx);
            log.info("用户 {} 下线!", username);
            ProcessMsg.remove(ctx);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.debug(cause.getMessage(), cause);
    }
}
