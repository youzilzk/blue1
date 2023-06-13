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

    public static final int READ_IDLE_TIME = 50;
    public static final int WRITE_IDLE_TIME = 45;

    public IdleCheckHandler() {
        super(READ_IDLE_TIME, WRITE_IDLE_TIME, 0);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        Channel channel = ctx.channel();

        if (IdleState.WRITER_IDLE == evt.state()) {
            log.info("客户端写闲置, 发送心跳[{}]", channel.id());
            Message message = new Message();
            message.setType(Message.TYPE.HEARTBEAT);
            ctx.channel().writeAndFlush(message);
        } else {
            log.info("客户端读闲置, 关闭链路[{}]", channel.id());
            channel.close().sync();
        }
    }
}
