package com.youzi.blue.net.client.handlers;


import com.youzi.blue.net.client.manager.ChannelManager;
import com.youzi.blue.net.client.manager.FutureManager;
import com.youzi.blue.net.common.protocol.Constants;
import com.youzi.blue.net.common.protocol.Message;
import com.youzi.blue.net.common.utils.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import java.net.InetSocketAddress;

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
        String clientId = channel.attr(Constants.CLIENT_ID).get();

        InetSocketAddress address = (InetSocketAddress) (ctx.channel().remoteAddress());
        if (clientId != null) {
            //如果不空则为客户端链路
            log.info("客户端异常[{}:{}, clientId={},channelId={}], message: {}", address.getHostString(), address.getPort(), clientId, channel.id(), cause.getMessage());
            ChannelManager.closeAll();
            FutureManager.closeAll();

            channel.close().sync();
        } else {
            //否则为隧道链路
            log.info("链路关闭[{}:{}, channelId={}], message: {}", address.getHostString(), address.getPort(), channel.id(), cause.getMessage());
            Channel towardChannel = channel.attr(Constants.TOWARD_CHANNEL).get();
            towardChannel.close().sync();
            channel.close().sync();
        }
        super.exceptionCaught(ctx, cause);
    }

}