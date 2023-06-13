package com.youzi.blue.net.client.config;


import com.youzi.blue.net.common.Config;

/**
 * @Author: youzi
 * @Date: 2023-04-10
 * @Description: 配置
 */
public class ClientProperties {

    private static ClientProperties instance = null;
    private static Config config = null;

    private String clientId;   //客户端凭据
    private boolean sslEnable;  //是否启用SSL加密连接
    private String sslJksPath;  //SSL证书路径
    private String sslKeyStorePassword;  //SSL证书密码
    private String serverHost;  //服务器地址或域名
    private int serverPort;  //服务器端口


    public static ClientProperties getInstance() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    config = Config.getInstance();
                }
            }
        }
        if (instance == null) {
            synchronized (ClientProperties.class) {
                if (instance == null) {
                    instance = new ClientProperties();
                    instance.clientId = config.getStringValue("client.id");
                    instance.serverHost = config.getStringValue("server.host");
                    instance.serverPort = config.getIntValue("server.port");
                    instance.sslEnable = config.getBooleanValue("ssl.enable");
                    instance.sslJksPath = config.getStringValue("ssl.jksPath");
                    instance.sslKeyStorePassword = config.getStringValue("ssl.keyStorePassword");

                }
            }
        }

        return instance;
    }

    public ClientProperties() {
    }


    public static void setInstance(ClientProperties instance) {
        ClientProperties.instance = instance;
    }

    public static Config getConfig() {
        return config;
    }

    public static void setConfig(Config config) {
        ClientProperties.config = config;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isSslEnable() {
        return sslEnable;
    }

    public void setSslEnable(boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

    public String getSslJksPath() {
        return sslJksPath;
    }

    public void setSslJksPath(String sslJksPath) {
        this.sslJksPath = sslJksPath;
    }

    public String getSslKeyStorePassword() {
        return sslKeyStorePassword;
    }

    public void setSslKeyStorePassword(String sslKeyStorePassword) {
        this.sslKeyStorePassword = sslKeyStorePassword;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }
}
