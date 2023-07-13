package com.youzi.blue.net.common.protocol;


import java.util.Arrays;

/**
 * 代理客户端与代理服务器消息交换协议
 */
public class Message {

    public enum TYPE {
        UNDEFINED(0),

        LINK(1),
        //关联管道
        RELEVANT(2),

        RELAY(3),

        HEARTBEAT(4),

        STARTRECORD(5),

        STOPRECORD(6),

        LINKCHECK(7);

        public final byte value;

        TYPE(int value) {
            this.value = (byte) value;
        }

        public TYPE byValue(int value) {
            for (TYPE handleEnum : values()) {
                if (handleEnum.value == value) {
                    return handleEnum;
                }
            }
            throw new RuntimeException("enum value undefined.");
        }

    }

    /**
     * 消息类型
     */
    private TYPE type;

    /**
     * 内容
     */
    private String content;

    /**
     * 消息传输数据
     */
    private byte[] data;

    public Message() {
    }

    public Message(TYPE type) {
        this.type = type;
    }

    public Message(TYPE type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public Message(TYPE type, String content) {
        this.type = type;
        this.content = content;
    }

    public Message(TYPE type, String content, byte[] data) {
        this.type = type;
        this.content = content;
        this.data = data;
    }

    public static class Content {
        /**
         * 状态码,200-成功,500-失败
         */
        private Integer code;
        /**
         * 信息
         */
        private String message;

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }


        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Content() {
        }

        public Content(Integer code, String message) {
            this.code = code;
            this.message = message;
        }
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public void clean() {
        this.type = null;
        this.content = null;
        this.data = null;
    }

    @Override
    public String toString() {
        if (data != null && data.length > 50) {
            return String.format("Message [type[%s], content[%s], data(%s)[...]", type, content, data.length);
        }
        return String.format("Message [type[%s], content[%s], data(%s)%s", type, content, data == null ? 0 : data.length, Arrays.toString(data));
    }

}
