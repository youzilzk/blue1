package com.youzi.blue.net.client.handlers;


import com.youzi.blue.net.common.protocol.Constants;
import com.youzi.blue.net.common.protocol.Message;
import com.youzi.blue.net.common.utils.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;


/**
 * 客户端和服务端连接处理
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<Message> {
    private static LoggerFactory log = LoggerFactory.getLogger();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) {
        switch (message.getType()) {
            case RELAY:
                handleTransferMessage(ctx, message);
                break;
            case HEARTBEAT:
                handleHeartbeatMessage(ctx, message);
                break;
            default:
                break;
        }

    }

    private void handleTransferMessage(ChannelHandlerContext ctx, Message message) {
        Channel toChannel = ctx.channel().attr(Constants.TOWARD_CHANNEL).get();
        assert toChannel != null;

        byte[] data = message.getData();

        log.info("收到服务器响应信息, 数据长度: {} [byte]", data.length);
        ByteBuf buf = ctx.alloc().buffer(data.length);
        buf.writeBytes(data);

        //转发消息
        toChannel.writeAndFlush(buf);
    }

    private void handleHeartbeatMessage(ChannelHandlerContext ctx, Message message) {
        log.info("心跳回复[{}]", ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        channel.close().sync();
        log.info("链路关闭[channelId={}], message: {}", channel.id(), cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }

}