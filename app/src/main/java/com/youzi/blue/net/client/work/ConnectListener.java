package com.youzi.blue.net.client.work;

import android.util.Log;

import com.youzi.blue.service.WorkAccessibilityService;

import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;

public class ConnectListener implements ChannelFutureListener {
    private static final String TAG = "ConnectListener";
    private final Net net;

    public ConnectListener(Net net) {
        this.net = net;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        //连接失败发起重连
        if (!channelFuture.isSuccess()) {
            final EventLoop loop = channelFuture.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "连接失败，发起重连");
                    WorkAccessibilityService.instace.updateChannel(net.start());
                }
            }, 5, TimeUnit.SECONDS);
        }
    }
}
