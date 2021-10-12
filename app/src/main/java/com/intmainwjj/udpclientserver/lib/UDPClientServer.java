package com.intmainwjj.udpclientserver.lib;

import android.content.Context;

/**
 * @author intmainwjj
 *
 * <p>
 * description：同时作为服务端和客户端
 * </p>
 */
public class UDPClientServer {
    private UDPSendThread udpSendThread;
    private UDPReceiveThread udpReceiveThread;

    private Context applicationContext;

    private UDPClientServer(Context applicationContext) {
        this.applicationContext = applicationContext;
        udpSendThread = new UDPSendThread(applicationContext);
        udpReceiveThread = new UDPReceiveThread();
    }

    public static UDPClientServer newInstance(Context applicationContext) {
        return new UDPClientServer(applicationContext);
    }

    public UDPClientServer setTargetPort(int targetPort) {
        udpSendThread.setTargetPort(targetPort);

        return this;
    }

    public UDPClientServer setTargetIp(String targetIp) {
        udpSendThread.setTargetIp(targetIp);

        return this;
    }

    public UDPClientServer setReceivePort(int receivePort) {
        udpReceiveThread.setReceivePort(receivePort);

        return this;
    }

    public UDPClientServer setReceiveTimeOut(long receiveTimeOut) {
        udpReceiveThread.setReceiveTimeOut(receiveTimeOut);

        return this;
    }

    public UDPClientServer setSendCallback(UDPSendCallback sendCallback) {
        udpSendThread.setSendCallback(sendCallback);

        return  this;
    }

    public UDPClientServer setReceiveCallback(UDPReceiveCallback receiveCallback) {
        udpReceiveThread.setReceiveCallback(receiveCallback);

        return this;
    }

    public boolean sendBroadcast(byte[] instruction) {
        if (null == udpSendThread) {
            udpSendThread = new UDPSendThread(applicationContext);
        }
        if (null == udpReceiveThread) {
            udpReceiveThread = new UDPReceiveThread();
        }

        if (!udpReceiveThread.isRunning()) {
            udpReceiveThread.start();
        }
        if (!udpSendThread.isRunning()) {
            udpSendThread.start();
        }

        return udpSendThread.sendBroadcast(instruction);
    }

    public void stop() {
        if (null != udpSendThread) {
            udpSendThread.setSendCallback(null);
            udpSendThread.stopThread();
        }
        if (null != udpReceiveThread) {
            udpReceiveThread.setReceiveCallback(null);
            udpReceiveThread.stopThread();
        }
    }
}
