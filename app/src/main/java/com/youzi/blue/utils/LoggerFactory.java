package com.youzi.blue.utils;

import android.util.Log;

public class LoggerFactory {
    private final String tag = "blue";

    private static LoggerFactory loggerFactory;

    public static LoggerFactory getLogger() {
        if (loggerFactory == null) {
            loggerFactory = new LoggerFactory();
        }
        return loggerFactory;
    }

    public void info(Object... args) {
        Log.i(tag, formatMessage(args));
    }

    public void error(Object... args) {
        Log.e(tag, formatMessage(args));
    }

    public void warn(Object... args) {
        Log.w(tag, formatMessage(args));
    }

    private String formatMessage(Object... args) {
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
        } else {
            info.append(msg);
        }
        return info.toString();
    }

}

