package com.youzi.blue.net.common;


import android.content.res.AssetManager;

import com.youzi.blue.net.client.manager.Manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final Config instance = new Config();

    public static Config getInstance() {
        return instance;
    }

    private Map<String, String> data = new HashMap<>();


    public void init() throws IOException {
        InputStream inputStream = null;
        try {
            AssetManager manager = Manager.getAssetManager();
            inputStream = manager.open("config.properties");
            InputStreamReader inputReader = new InputStreamReader(inputStream);
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            while ((line = bufReader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("#") && !line.equals("")) {
                    line = line.contains("#") ? line.substring(0, line.indexOf("#")) : line;
                    String[] kv = line.split("=");
                    if (kv.length == 2) {
                        data.put(kv[0], kv[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            inputStream.close();
        }
    }

    public String getProperty(String name) {
        return (String) data.get(name);
    }

    public String getStringValue(String key) {
        return this.getProperty(key);
    }

    public int getIntValue(String key) {
        return LangUtil.parseInt(data.get(key));
    }

    public Boolean getBooleanValue(String key) {
        return LangUtil.parseBoolean(this.getProperty(key));
    }


}
