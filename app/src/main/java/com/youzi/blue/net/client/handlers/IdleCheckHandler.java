package com.youzi.blue.net.client.handlers;


import com.youzi.blue.net.common.protocol.Message;
import com.youzi.blue.net.common.utils.LoggerFactory;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;


public class IdleCheckHandler extends IdleStateHandler {
    private static final LoggerFactory log = LoggerFactory.getLogger();
    private static final Message heartBeatMessage = new Message(Message.TYPE.HEARTBEAT);

    public static final int READ_IDLE_TIME = 8;
    public static final int WRITE_IDLE_TIME = 5;

    public IdleCheckHandler() {
        super(READ_IDLE_TIME, WRITE_IDLE_TIME, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        Channel channel = ctx.channel();

        if (IdleState.WRITER_IDLE == evt.state()) {
            log.info("客户端写闲置, 发送心跳[{}]", channel.id());
            ctx.channel().writeAndFlush(heartBeatMessage);
        }
    }
}
