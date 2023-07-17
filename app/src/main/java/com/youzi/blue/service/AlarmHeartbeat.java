package com.youzi.blue.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.youzi.blue.utils.LoggerFactory;

public class AlarmHeartbeat extends BroadcastReceiver {
    private final LoggerFactory log = LoggerFactory.getLogger();

    @Override
    public void onReceive(Context context, Intent intent) {
        log.warn("时钟消息, 将检测网络!");
        BlueService.instace.checkNetwork();
    }
}