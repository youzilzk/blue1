package com.youzi.blue.network.common.protocol;
 

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.Charset;
 
public class MessageDecoder extends LengthFieldBasedFrameDecoder {
    /**
     * 拆包参数
     */
    private static final int MAX_FRAME_LENGTH = 1024 * 1024;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int INITIAL_BYTES_TO_STRIP = 0;
    private static final int LENGTH_ADJUSTMENT = 0;
    /**
     * 消息解码参数
     */
    private static final byte HEADER_SIZE = 4;
    private static final int TYPE_SIZE = 1;
    private static final int CONTENT_LENGTH_SIZE = 1;

    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");

    /**
     * @param maxFrameLength
     * @param lengthFieldOffset
     * @param lengthFieldLength
     * @param lengthAdjustment
     * @param initialBytesToStrip
     */
    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
                          int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    public MessageDecoder() {
        this(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP);
    }

    /**
     * @param maxFrameLength
     * @param lengthFieldOffset
     * @param lengthFieldLength
     * @param lengthAdjustment
     * @param initialBytesToStrip
     * @param failFast
     */
    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
                          int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected Message decode(ChannelHandlerContext ctx, ByteBuf in2) throws Exception {
        /**
         * 消息结构  | 总长度-4byte | action-1byte | 序列号-8byte | text长度-1byte | text | data-剩余长度 |
         */
        ByteBuf in = (ByteBuf) super.decode(ctx, in2);
        if (in == null) {
            return null;
        }
        //
        if (in.readableBytes() < HEADER_SIZE) {
            return null;
        }

        int frameLength = in.readInt();
        if (in.readableBytes() < frameLength) {
            return null;
        }
        Message msg = new Message();
        byte type = in.readByte();

        msg.setType(Message.TYPE.UNDEFINED.byValue(type));

        byte contentLength = in.readByte(); //因为是byte类型，所以文字最大长度是127字节，UTF8每个汉字3字节
        byte[] contentBytes = new byte[contentLength];
        in.readBytes(contentBytes);
        msg.setContent(new String(contentBytes, CHARSET_UTF8));

        byte[] data = new byte[frameLength - TYPE_SIZE - CONTENT_LENGTH_SIZE - contentLength];
        in.readBytes(data);
        msg.setData(data);

        in.release();

        return msg;
    }
}