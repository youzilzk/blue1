package com.youzi.blue.net.common;

import java.lang.reflect.Field;

import okhttp3.FormBody;

public class FormData<T> {

    public FormBody get(T t) throws IllegalAccessException {
        FormBody.Builder builder = new FormBody.Builder();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            String name = field.getName();
            Object value = field.get(t);
            if (value != null) {
                builder.add(name, value.toString());
            }
        }
        return builder.build();
    }
}
