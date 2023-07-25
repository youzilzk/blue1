package com.youzi.blue.network.common.protocol;

public class Constants {

    public enum STATE {
        SUCCESS("200"),
        FAILED("500"),
        CHECK("400"),
        REQUEST("300");

        public final String value;

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
