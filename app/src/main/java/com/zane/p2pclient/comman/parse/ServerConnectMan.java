package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.receive.TCPMessageReceiver;
import com.zane.p2pclient.comman.send.TCPMessageSend;

import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

/**
 * 负责建立连接的逻辑
 * udp的通道需要两个P都互发connect消息之后才能建立起来，不然发送的消息会被抛弃
 * <p>
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class ServerConnectMan extends AbstractParseMan {

    private Flowable<String> flowable;
    private PublishSubject<Message> subject;

    private TCPMessageReceiver receiver;

    public ServerConnectMan(TCPMessageSend sendMan, TCPMessageReceiver receiver) {
        this.sendMan = sendMan;
        this.receiver = receiver;
        subject = PublishSubject.create();
        flowable = subject.toFlowable(BackpressureStrategy.LATEST).map(new Function<Message, String>() {
            @Override
            public String apply(@NonNull Message message) throws Exception {
                return message.getMessageType();
            }
        });
    }

    public Flowable<String> getFlowable() {
        return flowable;
    }

    @Override
    public void send(Message message) throws IOException {
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_CONNECT.equals(messageType) || Config.MESSAGE_TYPE_QUIT.equals(messageType)) {
            sendMan.sendMessage(message);
            receiver.start();
        } else {
            nextParseMan.send(message);
        }
    }

    @Override
    public void receive(Message message) throws NoMatchParserMan {
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_QUIT_RESULT.equals(messageType)) {
            subject.onNext(message);
        } else if (Config.MESSAGE_TYPE_NOTFOUND.equals(messageType)) {
            subject.onNext(message);
        } else {
            throw new NoMatchParserMan("No Match ParseMan!!!~");
        }
    }
}
