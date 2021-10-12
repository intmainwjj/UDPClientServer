package com.intmainwjj.udpclientserver.lib;

public abstract class UDPReceiveCallback {
    public void onStart() {}

    public void onClosed() {}

    public abstract void onReceiveDataSuccess(UDPResult result);

    public abstract void onReceiveDataFailed(Throwable throwable);
}
