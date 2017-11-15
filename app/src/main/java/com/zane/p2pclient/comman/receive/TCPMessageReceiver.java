package com.zane.p2pclient.comman.receive;

import android.util.Log;

import com.google.gson.Gson;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageQueue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Zane on 2017/6/19
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class TCPMessageReceiver extends Thread implements IMessageReceiver{

    private InputStream is;
    private Gson gson;
    private OnReceiverListener listener;

    public void setOnReceiverFailedListener(OnReceiverListener listener) {
        this.listener = listener;
    }

    public TCPMessageReceiver(InputStream is) {
        this.is = is;
        gson = new Gson();
    }

    @Override
    public void finish() {
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        BufferedReader br = null;
        try {
            while (!isInterrupted()) {
                br = new BufferedReader(new InputStreamReader(is));
                String line = null;
                line = br.readLine();

                if (line != null) {
                    Message message = gson.fromJson(line, Message.class);
                    message.setType("receive");
                    MessageQueue.getInstance().put(message);
                }
            }
        } catch (IOException e) {
            Log.i("server", "failed " + e.getMessage());
            if (listener != null) {
                listener.onFailed();
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
