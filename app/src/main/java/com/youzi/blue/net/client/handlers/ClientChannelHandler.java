package com.youzi.blue.net.client.handlers;


import com.youzi.blue.net.client.manager.Manager;
import com.youzi.blue.net.common.protocol.Message;
import com.youzi.blue.service.BlueService;
import com.youzi.blue.utils.LoggerFactory;

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
            case LINKCHECK:
                handleLinkCheckMessage(ctx, message);
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
        log.info("心跳回复<<<<<<[{}]", ctx.channel().id());
    }

    private void handleStartRecordMessage(ChannelHandlerContext ctx, Message message) {
        BlueService blueService = BlueService.instace;
        if (!blueService.isRecordRunning()) {
            log.info("启动录屏[{}]", ctx.channel().id());
            blueService.startSendServer();
        } else {
            log.info("已在录屏[{}]", ctx.channel().id());
        }

    }

    private void handleStopRecordMessage(ChannelHandlerContext ctx, Message message) {
        log.info("停止录屏[{}]", ctx.channel().id());
        BlueService.instace.stopRecord();
    }

    private void handleLinkCheckMessage(ChannelHandlerContext ctx, Message message) {
        log.info("检查链路结果[{}]", new String(message.getData()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        channel.close().sync();
        super.exceptionCaught(ctx, cause);
        log.info("链路关闭[channelId={}], message: {}", channel.id(), cause.getMessage());
    }

}