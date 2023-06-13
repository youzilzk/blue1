package com.youzi.blue.net.common.protocol;

import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public class Constants {

    /**
     * 对接的Channel
     */
    public static AttributeKey<Channel> TOWARD_CHANNEL = AttributeKey.newInstance("toward_channel");
    /**
     * 客户端id
     */
    public static AttributeKey<String> CLIENT_ID = AttributeKey.newInstance("client_id");
    /**
     * 监听端口在服务端上,是为true
     */
    public static AttributeKey<Boolean> CHANNEL_ON_SERVER = AttributeKey.newInstance("channel_on_server");

    public enum STATE {
        SUCCESS("200"),
        FAILED("500"),
        CHECK("400"),
        REQUEST("300");

        public String value;

        STATE(String value) {
            this.value = value;
        }

        public STATE byValue(String value) {
            for (STATE handleEnum : values()) {
                if (handleEnum.value.equals(value)) {
                    return handleEnum;
                }
            }
            throw new RuntimeException("enum value undefined.");
        }

    }
}
