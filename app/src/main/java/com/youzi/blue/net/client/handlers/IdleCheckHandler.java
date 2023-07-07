package com.youzi.blue.net.client.handlers;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.youzi.blue.net.common.protocol.Message;
import com.youzi.blue.net.common.utils.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
        Channel channel = ctx.channel();

        if (IdleState.WRITER_IDLE == evt.state()) {
            String heartId = String.valueOf(LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")));
            log.info("发送心跳[channelId={},heartId={}]", channel.id(), heartId);
            heartBeatMessage.setData(heartId.getBytes());
            ctx.channel().writeAndFlush(heartBeatMessage);
        }
    }
}
