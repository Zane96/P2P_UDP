package com.zane.p2pclient.comman.receive;

import com.google.gson.Gson;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;

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
    private InputStream is;
    private BufferedReader br;
    private Flowable<Message> responseFlowable;
    private AsyncSubject<String> subject;
    private Gson gson;
    private OnReceiverListener listener;

    public void setOnReceiverFailedListener(OnReceiverListener listener) {
        this.listener = listener;
    }

    public TCPMessageReceiver(Socket socket) {
        this.socket = socket;
        gson = new Gson();
        subject = AsyncSubject.create();
        responseFlowable = subject.toFlowable(BackpressureStrategy.LATEST).map(new Function<String, Message>() {
            @Override
            public Message apply(@NonNull String data) throws Exception {
                return gson.fromJson(data, Message.class);
            }
        });
    }

    public void finish() {
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            try {
                is = socket.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));
                String line = "";
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                subject.onNext(sb.toString());
            } catch (IOException e) {
                finish();
                if (listener != null) {
                    listener.onFailed();
                }
            }
        }
    }
}
