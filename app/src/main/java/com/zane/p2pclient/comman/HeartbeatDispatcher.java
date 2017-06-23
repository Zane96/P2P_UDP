package com.zane.p2pclient.comman;

import com.zane.p2pclient.MyPreferences;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 定时分发心跳包
 * Created by Zane on 2017/6/20.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class HeartbeatDispatcher {

    private Message heartbeatMessage;
    private Flowable<Long> timer;
    private Disposable disposable;

    public HeartbeatDispatcher() {
        timer = Flowable.interval(20, TimeUnit.SECONDS);
        this.heartbeatMessage = new Message.Builder()
                                        .setMessageType(Config.MESSAGE_TYPE_HEART)
                                        .setHost(MyPreferences.getInstance().getHost())
                                        .setPort(MyPreferences.getInstance().getPort())
                                        .setContent(String.valueOf(System.currentTimeMillis()))
                                        .build();
        this.heartbeatMessage.setType("send");
    }

    public void start() {
        disposable = timer.subscribe(new Consumer<Long>() {
            @Override
            public void accept(@NonNull Long aLong) throws Exception {
                MessageQueue.getInstance().put(heartbeatMessage);
            }
        });
    }

    public void stop() {
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }
}
