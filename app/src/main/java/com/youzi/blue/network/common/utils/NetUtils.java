package com.youzi.blue.network.common.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetUtils {
    private static List<String> ipList;

    public static List<String> getLocalIPList() {
        if (ipList == null) {
            ipList = new ArrayList<>();
            try {
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                Enumeration<InetAddress> inetAddresses;
                InetAddress inetAddress;
                while (networkInterfaces.hasMoreElements()) {
                    inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        inetAddress = inetAddresses.nextElement();
                        if (inetAddress instanceof Inet4Address) { // IPV4
                            ipList.add(inetAddress.getHostAddress());
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        return ipList;
    }
}
