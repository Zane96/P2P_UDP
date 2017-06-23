package com.zane.p2pclient.comman.receive;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageQueue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.AsyncSubject;

/**
 * Created by Zane on 2017/6/19
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class TCPMessageReceiver implements IMessageReceiver{

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

//    public void finish() {
//        interrupt();
//    }

    public void read() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            line = br.readLine();

            if (line != null) {
                Message message = gson.fromJson(line, Message.class);
                message.setType("receive");
                MessageQueue.getInstance().put(message);
            }

        } catch (IOException e) {
            Log.i("server", "failed " + e.getMessage());
            //finish();
            if (listener != null) {
                listener.onFailed();
            }
        }

    }

//    @Override
//    public void run() {
//        super.run();
//        while (!isInterrupted()) {
//
//        }
//    }
}
