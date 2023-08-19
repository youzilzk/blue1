package com.youzi.blue.network.client.manager;


import com.youzi.blue.io.DataPackList;

public class Manager {

    private static final DataPackList dataPackList = new DataPackList();

    public static DataPackList getDataPackList() {
        return dataPackList;
    }
}
