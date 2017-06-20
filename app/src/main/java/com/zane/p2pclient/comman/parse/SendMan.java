package com.zane.p2pclient.comman.parse;

import android.util.Log;

import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.send.UDPMessageSend;

import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.AsyncSubject;

/**
 * 发送消息，接收消息
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class SendMan extends AbstractParseMan{

    private Flowable<String> flowable;
    private AsyncSubject<Message> subject;

    public SendMan(UDPMessageSend sendMan) {
        this.sendMan = sendMan;
        subject = AsyncSubject.create();
        flowable = subject.toFlowable(BackpressureStrategy.LATEST).map(new Function<Message, String>() {
            @Override
            public String apply(@NonNull Message message) throws Exception {
                Log.i("receive", "Receive message: " + message.toString());
                return message.getContent();
            }
        });
    }

    public Flowable<String> getFlowable() {
        return flowable;
    }

    @Override
    public void send(Message message) throws IOException {
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_SEND.equals(messageType)) {
            sendMan.sendMessage(message);
        } else {
            nextParseMan.send(message);
        }
    }

    @Override
    public void receive(Message message) throws NoMatchParserMan{
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_SEND.equals(messageType)) {
            subject.onNext(message);
        } else {
            nextParseMan.receive(message);
        }
    }
}
