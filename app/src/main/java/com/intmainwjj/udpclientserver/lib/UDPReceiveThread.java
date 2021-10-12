package com.intmainwjj.udpclientserver.lib;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Arrays;

public class UDPReceiveThread extends Thread{
    private static final String TAG = "udp_debug";

    private DatagramSocket server;

    private Object lock;
    private volatile boolean isRunning;

    //UDP 接收超时时间，默认10秒
    private long receiveTimeOut = 10 * 1000;
    //接收端口号
    private int receivePort = 8001;

    private UDPReceiveCallback receiveCallback;

    private static final int WHAT_UDP_RECEIVE_THREAD_START = 1;
    private static final int WHAT_UDP_RECEIVE_THREAD_CLOSED = 2;
    private static final int WHAT_UDP_RECEIVE_DATA_SUCCESS = 3;
    private static final int WHAT_UDP_RECEIVE_DATA_FAILED = 4;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case WHAT_UDP_RECEIVE_THREAD_START:
                    isRunning = true;
                    if (null != receiveCallback) {
                        receiveCallback.onStart();
                    }
                    break;
                case WHAT_UDP_RECEIVE_THREAD_CLOSED:
                    isRunning = false;
                    if (null != receiveCallback) {
                        receiveCallback.onClosed();
                    }
                    break;
                case WHAT_UDP_RECEIVE_DATA_SUCCESS:
                    if (null != receiveCallback) {
                        try {
                            receiveCallback.onReceiveDataSuccess((UDPResult)msg.obj);
                        } catch (Exception e) {

                        }
                    }
                    break;
                case WHAT_UDP_RECEIVE_DATA_FAILED:
                    if (null != receiveCallback) {
                        try {
                            receiveCallback.onReceiveDataFailed((Throwable) msg.obj);
                        } catch (Exception e) {

                        }
                    }
                    break;
            }
        }
    };

    public UDPReceiveThread() {
        lock = new Object();
    }

    @Override
    public void run() {
        handler.sendEmptyMessage(WHAT_UDP_RECEIVE_THREAD_START);
        isRunning = true;

        byte[] buff = new byte[1024];
        DatagramPacket pack = new DatagramPacket(buff, buff.length);

        Log.d(TAG, "start receive pack");
        while (isRunning) {
            try {
                getServerSocket().receive(pack);

                byte[] res = Arrays.copyOf(buff, pack.getLength());
                Log.d(TAG, "received pack hex string is:" + ByteUtils.bytesToHexString(res));

                UDPResult udpResult = new UDPResult();
                udpResult.setResultData(res);
                Message msg = handler.obtainMessage();
                msg.obj = udpResult;
                msg.what = WHAT_UDP_RECEIVE_DATA_SUCCESS;
                handler.sendMessage(msg);
            } catch (IOException e) {
                Log.e(TAG, "UDP receive thread run error:" + e.getMessage());

                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }

                Message msg = handler.obtainMessage();
                msg.obj = e;
                msg.what = WHAT_UDP_RECEIVE_DATA_FAILED;
                handler.sendMessage(msg);
            }
        }
    }

    private DatagramSocket getServerSocket() {
        if (null != server) {
            return server;
        }

        synchronized (lock) {
            if (server != null) {
                return server;
            }

            try {
                server = new DatagramSocket(null);
                server.setReuseAddress(true);
                server.setBroadcast(true);
                server.setSoTimeout((int)receiveTimeOut);
                server.bind(new InetSocketAddress(receivePort));
            } catch (Exception e) {
                Log.e(TAG, "getServerSocket error:" + e.getMessage());
            }
        }

        return server;
    }

    public void stopThread() {
        if (isRunning) {
            isRunning = false;
            handler.sendEmptyMessage(WHAT_UDP_RECEIVE_THREAD_CLOSED);
        }
        try {
            if (server != null && server.isConnected()) {
                server.close();
                server = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "stop udp receive thread error:" + e.getMessage());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setReceiveTimeOut(long receiveTimeOut) {
        this.receiveTimeOut = receiveTimeOut;
    }

    public void setReceivePort(int receivePort) {
        this.receivePort = receivePort;
    }

    public void setReceiveCallback(UDPReceiveCallback receiveCallback) {
        this.receiveCallback = receiveCallback;
    }
}
