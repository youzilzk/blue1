package com.youzi.blue.service;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.youzi.blue.net.common.utils.LoggerFactory;

public class AlarmHeartReceiver extends BroadcastReceiver {
    private final LoggerFactory log = LoggerFactory.getLogger();

    @Override
    public void onReceive(Context context, Intent intent) {
        log.warn("时钟消息, 检测网络是否正常!");
        BlueService.instace.checkNetwork();
    }
}