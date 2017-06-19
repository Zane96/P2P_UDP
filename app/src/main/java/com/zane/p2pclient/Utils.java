package com.zane.p2pclient;

import android.util.Log;

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

    public static String getIntrxNet() throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec("netcfg");

        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        // read the output
        String intraNet = "";
        while ((intraNet = bufferedreader.readLine()) != null) {
            if (intraNet.startsWith("wlan")) {
                break;
            }
        }
        String host = intraNet.substring(intraNet.indexOf("P") + 1, intraNet.indexOf("0x")).trim().replace("/", ":");
        return host;
    }
}
