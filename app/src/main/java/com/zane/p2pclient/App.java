package com.zane.p2pclient;

import android.app.Application;

/**
 * Created by Zane on 2017/6/18.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class App extends Application{
    private static App context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    public static App getInstance() {
        return context;
    }
}
