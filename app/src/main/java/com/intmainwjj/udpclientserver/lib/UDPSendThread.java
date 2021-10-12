package com.intmainwjj.udpclientserver.lib;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;

public class UDPSendThread extends Thread {
    private static final String TAG = "udp_debug";

    private Context applicationContext;

    //LinkedBlockingQueue构造的时候若没有指定大小，则默认大小为Integer.MAX_VALUE
    //用法：https://blog.csdn.net/dfskhgalshgkajghljgh/article/details/51363543
    private LinkedBlockingDeque<byte[]> instructionQueue = new LinkedBlockingDeque<>();

    private DatagramSocket broadcast;

    private Object lock;
    private volatile boolean isRunning;

    //UDP发送线程开始
    private static final int WHAT_UDP_SEND_THREAD_START = 1;

    //UDP发送线程关闭
    private static final int WHAT_UDP_SEND_THREAD_CLOSED = 2;

    //UDP发送数据失败
    private static final int WHAT_UDP_SEND_DATA_FAILED = 10;
    //UDP发送数据成功
    private static final int WHAT_UDP_SEND_DATA_SUCCESS = 11;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case WHAT_UDP_SEND_THREAD_START:
                    isRunning = true;
                    if (null != sendCallback) {
                        sendCallback.onStart();
                    }
                    break;
                case WHAT_UDP_SEND_THREAD_CLOSED:
                    isRunning = false;
                    if (null != sendCallback) {
                        sendCallback.onClosed();
                    }
                    break;
                case WHAT_UDP_SEND_DATA_FAILED:
                    if (null != sendCallback) {
                        try {
                            sendCallback.onSendInstructionFailed((Throwable) msg.obj);
                        } catch (Exception e) {
                        }
                    }
                    break;
                case WHAT_UDP_SEND_DATA_SUCCESS:
                    if (null != sendCallback) {
                        try {
                            sendCallback.onSendInstructionSuccess((String) msg.obj);
                        } catch (Exception e) {
                        }
                    }
                    break;
            }
        }
    };

    //目标端口号
    private int targetPort = 1907;
    //目标ip，默认为广播形式，可指定目标ip发送
    private String targetIp = "255.255.255.255";

    private UDPSendCallback sendCallback;


    public UDPSendThread(Context applicationContext) {
        lock = new Object();
        this.applicationContext = applicationContext;
    }

    @Override
    public void run() {
        handler.sendEmptyMessage(WHAT_UDP_SEND_THREAD_START);
        isRunning = true;

        while (isRunning) {
            try {
                byte[] instruction = instructionQueue.take();//若队列为空，发生阻塞，等待有元素

                DatagramPacket packet;
                if ("255.255.255.255".equals(targetIp)) {
                    //广播
                    packet = new DatagramPacket(instruction, instruction.length,
                            InetAddressUtils.getBroadcastAddress(applicationContext), targetPort);
                } else {
                    //单播
                    packet = new DatagramPacket(instruction, instruction.length,
                            InetAddress.getByName(targetIp), targetPort);
                }
//                packet = new DatagramPacket(instruction, instruction.length,
//                        InetAddressUtils.getBroadcastAddress(MainApplication.getAppContext()), targetPort);

                getBroadcastSocket().send(packet);

                Message msg = Message.obtain();
                msg.what = WHAT_UDP_SEND_DATA_SUCCESS;
                msg.obj = ByteUtils.bytesToHexString(instruction);
                handler.sendMessage(msg);
            } catch (Exception e) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                }

                Message msg = Message.obtain();
                msg.what = WHAT_UDP_SEND_DATA_FAILED;
                msg.obj = e;
                handler.sendMessage(msg);

                Log.e(TAG, "udp send thread error:" + e.getMessage());
                if (e instanceof InterruptedException) {
                    //instructionQueue.take
                } else if (e instanceof UnknownHostException) {
                    //getBroadcastAddress
                } else if (e instanceof IOException) {
                    //getBroadcastSocket().send
                }
            }
        }
    }

    /**
     * UDP 发送指令
     *
     * @param instruction 指令内容
     * @return 如果发现队列已满无法添加的话，会直接返回false
     */
    public boolean sendBroadcast(byte[] instruction) {
        //Log.d(TAG, "call sendBroadcast :" + ByteUtils.bytesToHexString(instruction));
        return instructionQueue.offer(instruction);
    }

    private DatagramSocket getBroadcastSocket() {
        if (null != broadcast) {
            return broadcast;
        }

        synchronized (lock) {
            if (broadcast != null) {
                return broadcast;
            }

            try {
                broadcast = new DatagramSocket();
                broadcast.setBroadcast(true);
            } catch (Exception e) {
                Log.e(TAG, "getBroadcastSocket error:" + e.getMessage());
            }
        }

        return broadcast;
    }

    public void setTargetPort(int targetPort) {
        this.targetPort = targetPort;
    }

    public void setTargetIp(String targetIp) {
        this.targetIp = targetIp;
    }

    public void stopThread() {
        if (isRunning) {
            isRunning = false;
            handler.sendEmptyMessage(WHAT_UDP_SEND_THREAD_CLOSED);
        }
        try {
            if (broadcast != null && broadcast.isConnected()) {
                broadcast.close();
                broadcast = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "stop udp send thread error:" + e.getMessage());
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setSendCallback(UDPSendCallback sendCallback) {
        this.sendCallback = sendCallback;
    }
}
