package com.youzi.blue.net.common.utils;

import android.util.Log;

public class LoggerFactory {
    private static String tag = "tunnel";

    private static LoggerFactory loggerFactory;

    public static LoggerFactory getLogger() {
        if (loggerFactory == null) {
            loggerFactory = new LoggerFactory();
        }
        return loggerFactory;
    }

    public int info(Object... args) {
        String msg = (String) args[0];
        StringBuilder info = new StringBuilder();
        int index;
        int i = 1;
        if (args.length >= 2) {
            while ((index = msg.indexOf("{}")) != -1) {
                info.append(msg.substring(0, index)).append(args[i]);
                msg = msg.substring(index + 2);
                i++;
                if (i > args.length - 1) {
                    break;
                }
            }
            String tail = msg.contains("{}") ? msg.substring(0, msg.indexOf("{}")) : msg;
            info.append(tail);
        }

        return Log.i(tag, info.toString());
    }

}

