package com.youzi.blue.net.client.handlers;


import com.youzi.blue.net.client.manager.Manager;
import com.youzi.blue.net.common.protocol.Constants;
import com.youzi.blue.net.common.protocol.Message;
import com.youzi.blue.net.common.utils.LoggerFactory;
import com.youzi.blue.service.WorkAccessibilityService;
import io.netty.channel.*;


/**
 * 客户端和服务端连接处理
 */
public class ClientChannelHandler extends SimpleChannelInboundHandler<Message> {
    private static final LoggerFactory log = LoggerFactory.getLogger();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) {
        switch (message.getType()) {
            case RELAY:
                handleTransferMessage(ctx, message);
                break;
            case HEARTBEAT:
                handleHeartbeatMessage(ctx, message);
                break;
            case STARTRECORD:
                handleStartRecordMessage(ctx, message);
                break;
            case STOPRECORD:
                handleStopRecordMessage(ctx, message);
                break;
            default:
                break;
        }

    }

    private void handleTransferMessage(ChannelHandlerContext ctx, Message message) {
        byte[] data = message.getData();

        log.info("收到服务器响应信息, 数据长度: {} [byte]", data.length);

        Manager.getDataPackList().putDataPack(data);
    }

    private void handleHeartbeatMessage(ChannelHandlerContext ctx, Message message) {
        log.info("心跳回复[{}]", ctx.channel().id());
    }

    private void handleStartRecordMessage(ChannelHandlerContext ctx, Message message) {
        log.info("启动录屏[{}]", ctx.channel().id());
        WorkAccessibilityService.instace.startSendServer();
    }

    private void handleStopRecordMessage(ChannelHandlerContext ctx, Message message) {
        log.info("停止录屏[{}]", ctx.channel().id());
        WorkAccessibilityService.instace.stopRecord();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        channel.close().sync();
        log.info("链路关闭[channelId={}], message: {}", channel.id(), cause.getMessage());
        super.exceptionCaught(ctx, cause);
    }

}