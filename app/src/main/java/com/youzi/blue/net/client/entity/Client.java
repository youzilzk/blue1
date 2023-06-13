package com.youzi.blue.net.client.entity;


import io.netty.channel.Channel;

import java.io.Serializable;


public class Client implements Serializable {

    private String id;

    private String name;

    private String macAddress;

    private String host;

    private Channel channel;

    public Client() {
    }

    public Client(String id) {
        this.id = id;
    }

    public Client(String id, String macAddress) {
        this.id = id;
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Client(String id, String name, String host) {
        this.id = id;
        this.name = name;
        this.host = host;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}
