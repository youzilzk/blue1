package com.youzi.blue.service;


import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.youzi.blue.net.client.work.Net;
import com.youzi.blue.net.common.utils.LoggerFactory;

import io.netty.channel.Channel;

public class AlarmHeartReceiver extends BroadcastReceiver {
    private final LoggerFactory log = LoggerFactory.getLogger();

    @Override
    public void onReceive(Context context, Intent intent) {
        log.warn("时钟消息, 检测网络是否正常!");
        Channel clientChannel = WorkAccessibilityService.instace.getClientChannel();
        if (clientChannel == null || !clientChannel.isActive()) {
            log.warn("网络重连!");
            String username = context.getSharedPreferences("user", MODE_PRIVATE).getString("username", null);
            assert username != null;
            WorkAccessibilityService.instace.updateChannel(new Net(username).start());
        }
    }
}