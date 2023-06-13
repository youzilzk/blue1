package com.youzi.blue.net.client;


import com.youzi.blue.net.client.config.ClientProperties;
import com.youzi.blue.net.client.work.ClientStarter;
import com.youzi.blue.net.common.Config;

import java.io.IOException;

public class ClientApplication {

    public static void start(String host, Integer port) {
        try {
            Config.getInstance().init();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (host != null) {
            ClientProperties.getInstance().setServerHost(host);
        }
        if (port != null) {
            ClientProperties.getInstance().setServerPort(port);
        }
        ClientStarter.start();
    }

    public static void stop() {
        ClientStarter.stop();
    }

}
