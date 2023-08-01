package com.youzi.blue.network.client.handlers;


import com.youzi.blue.service.BlueService;
import com.youzi.blue.utils.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;


public class IdleCheckHandler extends IdleStateHandler {
    private final LoggerFactory log = LoggerFactory.getLogger();

    public static final int READ_IDLE_TIME = 18;
    public static final int WRITE_IDLE_TIME = 15;

    public IdleCheckHandler() {
        super(READ_IDLE_TIME, WRITE_IDLE_TIME, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws InterruptedException {
        if (IdleState.READER_IDLE == evt.state()) {
            Channel channel = ctx.channel();
            channel.close().sync();
            log.info("超时无心跳,重连网络!");
            BlueService.instace.tryReConnected();
        }
    }
}
