package com.youzi.blue.net.client.manager;

import android.content.Context;
import android.content.res.AssetManager;

public class Manager {
    private static AssetManager assetManager;

    private static Context aContext;

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
