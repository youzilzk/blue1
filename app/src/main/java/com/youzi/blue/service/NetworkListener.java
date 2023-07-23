package com.youzi.blue.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.youzi.blue.utils.LoggerFactory;

import java.util.List;
import java.util.Objects;


public class NetworkListener extends BroadcastReceiver {
    private LoggerFactory log = LoggerFactory.getLogger();

    private Context mContext;
    private NetChangeListener listener;
    /**
     * 没有网络
     */
    public final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    public final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    public final int NETWORK_WIFI = 1;

    private int getNetWorkState(Context context) {
        //得到连接管理器对象
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //如果网络连接，判断该网络类型
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;//wifi
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;//mobile
            }
        } else {
            //网络异常
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 如果相等的话就说明网络状态发生了变化
        if (Objects.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            int netWorkState = getNetWorkState(context);
            // 当网络发生变化，判断当前网络状态，并通过NetEvent回调当前网络状态
            if (netWorkState == NETWORK_WIFI) {
                log.info("wifi网络!");
                listener.onWifi();
            } else if (netWorkState == NETWORK_MOBILE) {
                log.info("移动网络!");
                listener.onMobile();
            } else if (netWorkState == NETWORK_NONE) {
                log.info("网络中断!");
                listener.onDisconnected();
            }
        }
    }

    // 自定义接口
    public interface NetChangeListener {
        void onWifi();

        void onMobile();

        void onDisconnected();
    }

    public void register(NetChangeListener changeListener, Context context) {
        mContext = context;
        this.listener = changeListener;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //实例化IntentFilter对象
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            //注册广播接收
            mContext.registerReceiver(this, filter);
            log.info("注册网络监听!");
        }
    }

    public boolean isBroadcastRegister() {
        Intent intent = new Intent(mContext, NetworkListener.class);
        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryBroadcastReceivers(intent, 0);
        return resolveInfos != null && !resolveInfos.isEmpty();
    }

    public void unRegister() {
        try {
            if (isBroadcastRegister()) {
                mContext.unregisterReceiver(this);
                log.info("注销网络监听!");
            }
        } catch (Exception e) {
            //亮屏再息屏会出现异常,因为亮屏未注册
            log.error("注销网络监听广播异常: {}", e.getMessage());
        }
    }

}


