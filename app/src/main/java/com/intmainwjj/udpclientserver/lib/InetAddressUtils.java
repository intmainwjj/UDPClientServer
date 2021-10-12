package com.intmainwjj.udpclientserver.lib;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class InetAddressUtils {

    /**
     * 获取广播地址
     *
     */
    public static InetAddress getBroadcastAddress(Context context) throws UnknownHostException {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        if (dhcp == null) {
            return InetAddress.getByName("255.255.255.255");
        }
        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        InetAddress address = InetAddress.getByAddress(quads);
        if (TextUtils.equals(address.getHostAddress(), "0.0.0.0")) {
            return InetAddress.getByName("255.255.255.255");
        }
        return address;
    }
}
