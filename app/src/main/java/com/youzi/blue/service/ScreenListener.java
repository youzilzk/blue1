package com.youzi.blue.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.youzi.blue.utils.LoggerFactory;

import java.util.List;

public class ScreenListener {
    private final LoggerFactory log = LoggerFactory.getLogger();
    private final Context mContext;
    private final ScreenBroadcastReceiver receiver;
    private ScreenStateListener mScreenStateListener;

    public ScreenListener(Context context) {
        mContext = context;
        receiver = new ScreenBroadcastReceiver();
    }

    public void register(ScreenStateListener screenStateListener) {
        if (screenStateListener != null) {
            mScreenStateListener = screenStateListener;
        }
        if (receiver != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_USER_PRESENT);
            mContext.registerReceiver(receiver, filter);
            log.info("注册屏幕监听!");
        }
    }

    public void unregister() {
        try {
            if (isBroadcastRegister()) {
                log.info("注销屏幕监听!");
                mContext.unregisterReceiver(receiver);
            }
        } catch (Exception e) {
            //亮屏再息屏会出现异常,因为亮屏未注册
            log.error("注销屏幕监听广播异常: {}", e.getMessage());
        }
    }

    public boolean isBroadcastRegister() {
        Intent intent = new Intent(mContext, ScreenBroadcastReceiver.class);
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryBroadcastReceivers(intent, 0);
        return resolveInfos != null && !resolveInfos.isEmpty();
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    if (mScreenStateListener != null) {
                        log.info("用户亮屏!");
                        mScreenStateListener.onScreenOn();
                    }
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    if (mScreenStateListener != null) {
                        log.info("用户息屏!");
                        mScreenStateListener.onScreenOff();
                    }
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    if (mScreenStateListener != null) {
                        log.info("屏幕解锁!");
                        mScreenStateListener.onUserPresent();
                    }
                }
            }
        }
    }

    public interface ScreenStateListener {
        void onScreenOn();

        void onScreenOff();

        void onUserPresent();
    }
}
