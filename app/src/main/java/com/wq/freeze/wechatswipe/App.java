package com.wq.freeze.wechatswipe;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by wangqi on 2016/8/11.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
