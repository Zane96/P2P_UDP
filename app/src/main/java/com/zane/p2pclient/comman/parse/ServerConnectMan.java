package com.zane.p2pclient.comman.parse;

import android.util.Log;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.Utils;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageQueue;
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
 * 策略：
 * 1. P1 2 S, P2 2 S Login TCP
 * 2. S 2 P1 Login反馈，S 2 P2 Login反馈 TCP
 * 3. p1 2 S connectP2请求信息 TCP
 * 3. S 2 P1, S 2 P2 传送数据 TCP
 * 4. P1 2 P2, P2 2 P1 传送请求连接建立的数据 UDP
 * 5. P1, P2分别等待P2P请求连接信息，等待1s，如果没等到转到4（暴力），尝试5次，如果还失败，主动断开连接
 * 6. 打通UDP通道，开始相互心跳包，相互发送数据 UDP
 * 7. 发送disconnect消息，断开连接 UDP，发送之后主动断开连接 UDP
 * 8. 发送退出登陆消息给服务器 TCP
 * 9. 服务器返回退出消息 TCP
 *
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class ServerConnectMan extends AbstractParseMan{

    private Flowable<String> flowable;
    private PublishSubject<Message> subject;

    public ServerConnectMan(TCPMessageSend sendMan) {
        this.sendMan = sendMan;
        subject = PublishSubject.create();
        flowable = subject.toFlowable(BackpressureStrategy.LATEST).map(new Function<Message, String>() {
            @Override
            public String apply(@NonNull Message message) throws Exception {
                Log.i("receive", "Receive message: " + message.toString());
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
        if (Config.MESSAGE_TYPE_LOGIN.equals(messageType) || Config.MESSAGE_TYPE_CONNECT.equals(messageType)
                || Config.MESSAGE_TYPE_QUIT.equals(messageType)) {
            sendMan.sendMessage(message);
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
            //先统一存储对方的内网二元组
            String intraNet = message.getIntraNet();
            String host = Utils.getHost(intraNet);
            int port = Utils.getPort(intraNet);
            MyPreferences.getInstance().putHost(host);
            MyPreferences.getInstance().putPort(port);

            //发送端对端连接请求
            MessageQueue.getInstance().put(new Message.Builder()
                                                   .setMessageType(Config.MESSAGE_TYPE_CONNECT_P)
                                                   .setHost(host)
                                                   .setPort(port)
                                                   .build());
            subject.onNext(message);
        } else {
            throw new NoMatchParserMan("No Match ParseMan!!!~");
        }
    }
}
