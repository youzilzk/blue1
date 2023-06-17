package com.youzi.blue.net.client.manager;

import android.content.Context;
import android.content.res.AssetManager;

import com.youzi.blue.server.DataPackList;

public class Manager {
    private static AssetManager assetManager;

    private static Context aContext;

    private static DataPackList dataPackList = new DataPackList();

    public static DataPackList getDataPackList() {
        return dataPackList;
    }

    public static AssetManager getAssetManager() {
        return assetManager;
    }

    public static Context getContext() {
        return aContext;
    }

    public static void initAssetManager(AssetManager manager) {
        assetManager = manager;
    }

    public static void initContext(Context context) {
        aContext = context;
    }
}
