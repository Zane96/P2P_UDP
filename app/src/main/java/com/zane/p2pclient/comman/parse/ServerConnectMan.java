package com.zane.p2pclient.comman.parse;

import android.util.Log;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.Utils;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageQueue;
import com.zane.p2pclient.comman.receive.TCPMessageReceiver;
import com.zane.p2pclient.comman.send.TCPMessageSend;

import java.io.IOException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.PublishSubject;

/**
 * 负责建立连接的逻辑
 * udp的通道需要两个P都互发connect消息之后才能建立起来，不然发送的消息会被抛弃
 *
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class ServerConnectMan extends AbstractParseMan{

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
    public void send(Message message) throws IOException{
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_CONNECT.equals(messageType) || Config.MESSAGE_TYPE_QUIT.equals(messageType)) {
            sendMan.sendMessage(message);
            receiver.read();
        } else if (Config.MESSAGE_TYPE_LOGIN.equals(messageType)) {
            //发送一个打通和服务端UDP通道的包
            Message messageUdp = new Message.Builder()
                                         .setMessageType(Config.MESSAGE_TYPE_SERVER_UDP)
                                         .setHost(Config.SERVER_HOST)
                                         .setPort(Config.SERVER_PORT)
                                         .setContent(Utils.getIntrxNet())
                                         .build();
            messageUdp.setType("send");
            MessageQueue.getInstance().put(messageUdp);

            sendMan.sendMessage(message);
            receiver.read();
        } else {
            nextParseMan.send(message);
        }
    }

    @Override
    public void receive(Message message) throws NoMatchParserMan{
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_LOGIN_RESULT.equals(messageType) || Config.MESSAGE_TYPE_QUIT_RESULT.equals(messageType)) {
            subject.onNext(message);
        } else if (Config.MESSAGE_TYPE_CONNECT_RESULE.equals(messageType)) {
            //判断发送来的对端和自己是不是在一个NAT下
            String extraNet = message.getExtraNet();
            String intraNet = message.getIntraNet();
            String content = message.getContent();
            String host = "";
            int port = -1;

            if (content.equals(Utils.getHost(extraNet))) {
                //一个NAT下，用内网通信
                host = Utils.getHost(intraNet);
                port = Utils.getPort(intraNet);
            } else {
                host = Utils.getHost(extraNet);
                port = Utils.getPort(extraNet);
            }

            MyPreferences.getInstance().putHost(host);
            MyPreferences.getInstance().putPort(port);
            //发送端对端连接请求
            Message messageSend = new Message.Builder()
                                          .setMessageType(Config.MESSAGE_TYPE_CONNECT_P)
                                          .setHost(host)
                                          .setPort(port)
                                          .build();
            messageSend.setType("send");
            MessageQueue.getInstance().put(messageSend);

            subject.onNext(message);
        } else if (Config.MESSAGE_TYPE_NOTFOUND.equals(messageType)) {
            subject.onNext(message);
        } else {
            throw new NoMatchParserMan("No Match ParseMan!!!~");
        }
    }
}
