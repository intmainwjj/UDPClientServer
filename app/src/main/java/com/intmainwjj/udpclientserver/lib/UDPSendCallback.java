package com.intmainwjj.udpclientserver.lib;

public abstract class UDPSendCallback {

    public void onStart() {

    }

    public void onClosed() {

    }

    public void onSendInstructionFailed(Throwable throwable) {

    }

    public void onSendInstructionSuccess(String instruction) {

    }
}
