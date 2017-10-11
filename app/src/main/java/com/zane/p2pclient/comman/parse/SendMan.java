package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.send.UDPMessageSend;

import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;

/**
 * 发送消息，接收消息
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class SendMan extends AbstractParseMan {

    private Flowable<String> flowable;
    private PublishSubject<String> subject;

    public SendMan(UDPMessageSend sendMan) {
        this.sendMan = sendMan;
        subject = PublishSubject.create();
        flowable = subject.toFlowable(BackpressureStrategy.LATEST);
    }

    public Flowable<String> getFlowable() {
        return flowable;
    }

    @Override
    public void send(Message message) throws IOException {
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_SEND.equals(messageType)) {
            if (MyPreferences.getInstance().getisConnected()) {
                sendMan.sendMessage(message);
                subject.onNext("发送消息: " + message.getContent());
            } else {
                subject.onNext("未连接～～");
            }
        } else {
            nextParseMan.send(message);
        }
    }

    @Override
    public void receive(Message message) throws NoMatchParserMan {
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_SEND.equals(messageType)) {
            subject.onNext("接收消息： " + message.getContent());
        } else {
            nextParseMan.receive(message);
        }
    }
}
