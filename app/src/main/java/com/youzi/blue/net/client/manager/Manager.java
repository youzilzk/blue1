package com.youzi.blue.net.client.manager;

import android.annotation.SuppressLint;
import android.content.Context;

import com.youzi.blue.server.DataPackList;

public class Manager {

    @SuppressLint("StaticFieldLeak")
    private static Context aContext;

    private static final DataPackList dataPackList = new DataPackList();

    public static DataPackList getDataPackList() {
        return dataPackList;
    }


    public static Context getContext() {
        return aContext;
    }


    public static void initContext(Context context) {
        aContext = context;
    }
}
