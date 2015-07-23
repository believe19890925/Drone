package com.autodesk.drone.iw.asdk.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by dongjiawei on 4/1/15.
 */
public class NetUtil {
    public static String getWifiLocalIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//        if (!wifiManager.isWifiEnabled()) {
//            wifiManager.setWifiEnabled(true);
//        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            int ipAddress = wifiInfo.getIpAddress();
            return (ipAddress & 0xFF) + "." + ((ipAddress >> 8) & 0xFF) + "." + ((ipAddress >> 16) & 0xFF) + "." + ((ipAddress >> 24) & 0xFF);
        }
        return null;
    }

    public static String getHostIp() {
        try {
            for (Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces(); enumeration.hasMoreElements(); ) {
                NetworkInterface networkInterface = enumeration.nextElement();
                for (Enumeration<InetAddress> ipAddr = networkInterface.getInetAddresses(); ipAddr.hasMoreElements();) {
                    InetAddress address = ipAddr.nextElement();
                    if (!address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        }catch (Exception ex) {
        }
        return null;
    }
}
