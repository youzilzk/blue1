package com.youzi.blue.net.common.protocol;
 
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static final int TYPE_SIZE = 1;

    private static final int CONTENT_LENGTH_SIZE = 1;
    private static final Charset CHARSET_UTF8 = StandardCharsets.UTF_8;

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int bodyLength = TYPE_SIZE + CONTENT_LENGTH_SIZE;
        byte[] contentBytes = null;
        if (msg.getContent() != null) {
            contentBytes = msg.getContent().getBytes(CHARSET_UTF8);
            bodyLength += contentBytes.length;
        }

        if (msg.getData() != null) {
            bodyLength += msg.getData().length;
        }

        // 标记数据包的长度用于拆包
        out.writeInt(bodyLength);

        out.writeByte(msg.getType().value);

        if (contentBytes != null) {
            out.writeByte((byte) contentBytes.length);
            out.writeBytes(contentBytes);
        } else {
            out.writeByte((byte) 0x00);
        }

        if (msg.getData() != null) {
            out.writeBytes(msg.getData());
        }

    }
}