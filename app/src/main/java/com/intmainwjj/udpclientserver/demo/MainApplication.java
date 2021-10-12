package com.intmainwjj.udpclientserver.demo;

import android.app.Application;

/**
 * @author
 * @date 2021/10/11.
 *
 * <p>
 * description：
 * </p>
 */
public class MainApplication extends Application {
    private static MainApplication instance;

    public static MainApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
    }
}
