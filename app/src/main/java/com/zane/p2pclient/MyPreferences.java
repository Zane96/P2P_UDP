package com.zane.p2pclient;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Zane on 2017/6/18.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class MyPreferences {
    private static SharedPreferences sp;
    private static SharedPreferences.Editor editor;

    private MyPreferences(){
        sp = PreferenceManager.getDefaultSharedPreferences(App.getInstance());
        editor = sp.edit();
    }

    private static class SingletonHolder{
        private static final MyPreferences mp = new MyPreferences();
    }

    public static MyPreferences getInstance() {
        return SingletonHolder.mp;
    }

    public void putHost(String host) {
        editor.putString("host", host);
        editor.commit();
    }

    public String getHost() {
        return sp.getString("host", "");
    }

    public void putPort(int port) {
        editor.putInt("port", port);
        editor.commit();
    }

    public int getPort() {
        return sp.getInt("port", 0);
    }
}
