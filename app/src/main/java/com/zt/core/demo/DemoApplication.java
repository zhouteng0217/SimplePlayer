package com.zt.core.demo;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by zhouteng on 2019-09-11
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }
}
