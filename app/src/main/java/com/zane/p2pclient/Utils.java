package com.zane.p2pclient;

import android.util.Log;

import com.zane.p2pclient.comman.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class Utils {

    //获取udp通道的内网address
    public static String getIntrxNet(){
        Runtime runtime = Runtime.getRuntime();
        Process proc = null;
        try {
            proc = runtime.exec("netcfg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        // read the output
        String intraNet = null;
        try {
            while ((intraNet = bufferedreader.readLine()) != null) {
                if (intraNet.startsWith("wlan")) {
                    String address = intraNet.substring(intraNet.indexOf("P") + 1, intraNet.indexOf("0x")).trim();
                    return address.substring(0, address.indexOf("/")) + ":" + Config.PHONE_PORT;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return intraNet;
    }

    public static String getHost(String address) {
        return address.substring(0, address.indexOf(":"));
    }

    public static int getPort(String address) {
        return Integer.valueOf(address.substring(address.indexOf(":") + 1, address.length()));
    }
}
