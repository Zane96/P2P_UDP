package com.zane.p2pclient.comman.receive;

import android.util.Log;

import com.google.gson.Gson;
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

public class TCPMessageReceiver extends Thread implements IMessageReceiver{

    private Socket socket;
    private Gson gson;
    private OnReceiverListener listener;

    public void setOnReceiverFailedListener(OnReceiverListener listener) {
        this.listener = listener;
    }

    public TCPMessageReceiver(Socket socket) {
        this.socket = socket;
        gson = new Gson();
    }

    public void finish() {
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            try {
                InputStream is = socket.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = "";
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                MessageQueue.getInstance().put(gson.fromJson(sb.toString(), Message.class));
            } catch (IOException e) {
                finish();
                if (listener != null) {
                    listener.onFailed();
                }
            }
        }
    }
}
