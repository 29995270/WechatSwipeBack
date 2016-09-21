package com.wq.freeze.wechatswipe;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

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
    public static Handler handler = new Handler(Looper.getMainLooper());
}
