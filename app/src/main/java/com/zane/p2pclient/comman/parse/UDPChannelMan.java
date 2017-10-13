package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.HeartbeatDispatcher;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageFilter;
import com.zane.p2pclient.comman.receive.BlockingThread;
import com.zane.p2pclient.comman.send.UDPMessageSend;

import java.io.IOException;
import java.util.Random;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by taitadatsune on 2017/10/5.
 */

public class UDPChannelMan extends AbstractParseMan {

    private BlockingThread udpMessageReceiver;
    private HeartbeatDispatcher heartbeatDispatcher;
    private PublishSubject<Message> subject;
    private Flowable<String> flowable;

    private int seq = 0;
    private int ack = 0;
    private int retries = 0;

    public UDPChannelMan(UDPMessageSend sendMan, BlockingThread udpMessageReceiver, HeartbeatDispatcher heartbeatDispatcher) {
        this.sendMan = sendMan;
        this.udpMessageReceiver = udpMessageReceiver;
        this.heartbeatDispatcher = heartbeatDispatcher;
        subject = PublishSubject.create();
        flowable = subject.toFlowable(BackpressureStrategy.LATEST).map(new Function<Message, String>() {
            @Override
            public String apply(@NonNull Message message) throws Exception {
                return message.getMessageType();
            }
        });
    }

    @Override
    public void send(Message message) throws IOException {
        String messageType = message.getMessageType();

        if (messageType.equals(Config.MESSAGE_TYPE_REQUEST_CONNECTION)) {
            initConnectionConfig(message);

            Config.hostStatus = Config.SYN_SENT;
            sendSegment(message, Config.MESSAGE_TYPE_SYN);
            udpMessageReceiver.start();
        } else {
            nextParseMan.send(message);
        }
    }

    @Override
    public void receive(Message message) throws NoMatchParserMan {
        String messageType = message.getMessageType();
        int peerSeq = message.getHostStatus();

        if (Config.MESSAGE_TYPE_SYN.equals(messageType)) {
            switch (Config.hostStatus) {
                case Config.LISTEN:
                    seq = new Random().nextInt();
                    ack = ++peerSeq;
                    Config.hostStatus = Config.SYN_RCVD;
                    sendSegment(message, Config.MESSAGE_TYPE_SYN_ACK);
                    break;
                case Config.SYN_SENT:
                    seq = ++seq;
                    ack = ++peerSeq;
                    Config.hostStatus = Config.SYN_RCVD;
                    sendSegment(message, Config.MESSAGE_TYPE_SYN_ACK_S);
                    break;
                case Config.SYN_RCVD:
                    if (Config.activeOpen) {
                        if (message.getHostStatus() == Config.SYN_SENT) { //对方主机状态
                            sendSegment(message, Config.MESSAGE_TYPE_SYN);
                        } else {
                            sendSegment(message, Config.MESSAGE_TYPE_SYN_ACK_S);
                        }
                    } else {
                        sendSegment(message, Config.MESSAGE_TYPE_SYN_ACK);
                    }
                    break;
                case Config.ESTABLISHED:
                    sendSegment(message, Config.MESSAGE_TYPE_SYN_ACK_S);
                    break;
            }
        } else if (Config.MESSAGE_TYPE_SYN_ACK.equals(messageType)) {
            switch (Config.hostStatus) {
                case Config.SYN_SENT:
                    seq = ++seq;
                    ack = ++peerSeq;
                    Config.hostStatus = Config.ESTABLISHED;
                    sendSegment(message, Config.MESSAGE_TYPE_ACK);
                    break;
                case Config.ESTABLISHED:
                    sendSegment(message, Config.MESSAGE_TYPE_ACK);
                    break;
            }
        } else if (Config.MESSAGE_TYPE_ACK.equals(messageType)) {
            switch (Config.hostStatus) {
                case Config.SYN_RCVD:
                    Config.hostStatus = Config.ESTABLISHED;

                    if (!Config.isReliableTrans) {
                        message.setMessageType(Config.MESSAGE_TYPE_CHANNEL_ESTABLISHED);
                        subject.onNext(message);
                    }
                    break;
                case Config.ESTABLISHED:
                    if (Config.activeOpen) {
                        message.setMessageType(Config.MESSAGE_TYPE_CHANNEL_ESTABLISHED);
                        heartbeatDispatcher.start();
                        subject.onNext(message);
                    }
                    break;
            }
        } else if (Config.MESSAGE_TYPE_SYN_ACK_S.equals(messageType)) {
            if (Config.hostStatus == Config.SYN_RCVD) {
                Config.hostStatus = Config.ESTABLISHED;
            }
        } else if (Config.MESSAGE_TYPE_TIME_OUT_RESEND.equals(messageType)) {
            switch (Config.hostStatus) {
                case Config.SYN_SENT:

                    sendSegment(message, Config.MESSAGE_TYPE_SYN);
                    break;
                case Config.SYN_RCVD:
                    if (Config.activeOpen) {
                        sendSegment(message, Config.MESSAGE_TYPE_SYN);
                    } else {
                        sendSegment(message, Config.MESSAGE_TYPE_SYN_ACK);
                    }
                    break;
                case Config.ESTABLISHED:
                    if (Config.activeOpen) {
                        sendSegment(message, Config.MESSAGE_TYPE_MSG);
                    }
                    break;
            }
        } else if (Config.MESSAGE_TYPE_CHANNEL_ESTABLISHED.equals(messageType)) {
            if (Config.hostStatus == Config.ESTABLISHED) {
                if (Config.isReliableTrans) {
                    sendSegment(message, Config.MESSAGE_TYPE_MSG);
                } else {
                    subject.onNext(message); //UDP通道建立成功
                }
            }
        } else if (Config.MESSAGE_TYPE_MSG.equals(messageType)) {
            if (Config.hostStatus == Config.ESTABLISHED) {
                sendSegment(message, Config.MESSAGE_TYPE_MESSAGE_ACK);
            }
        } else if (Config.MESSAGE_TYPE_CONNECT_FAILED.equals(messageType) || Config.MESSAGE_TYPE_RST.equals(messageType)) {
            if (Config.hostStatus == Config.SYN_RCVD && Config.MESSAGE_TYPE_CONNECT_FAILED.equals(messageType)) {
                sendSegment(message, Config.MESSAGE_TYPE_RST);
            }

            subject.onNext(message);
        } else if (Config.MESSAGE_TYPE_P2P_CONNECT_FAILED.equals(messageType)) {//多次重传P2P连接请求报文失败，向服务器发起P2P连接请求
            // TODO: 2017/10/7 两端同时再次向服务器发起连接？消息阻塞在某一节点一段时间后达到目的主机是否会给目的主机造成处理异常？
            if (retries < Config.retriesTime) {
                retries++;
                Message connectMessage = new Message.Builder()
                        .setMessageType(Config.MESSAGE_TYPE_CONNECT)
                        .setHost(Config.SERVER_HOST)
                        .setPort(Config.SERVER_PORT)
                        .setContent(MyPreferences.getInstance().getHostNames())
                        .build();

                MessageFilter.putMessageIntoQueue(true, connectMessage);
            } else {
                subject.onNext(message);
            }
        } else {
            nextParseMan.receive(message);
        }
    }

    @Override
    public Flowable<String> getFlowable() {
        return flowable;
    }


    private void initConnectionConfig(Message message) {
        seq = new Random().nextInt();
        Config.activeOpen = true;
        Config.isReliableTrans = message.isReliableTrans();
        Config.isReliableChannel = message.isReliableChannel();
    }

    private void sendSegment(Message message, String messageType) {
        message.setSeq(seq);
        message.setAck(ack);
        message.setHostStatus(Config.hostStatus);
        message.setMessageType(messageType);

        try {
            sendMan.sendMessage(message);
        } catch (IOException e) {
            udpMessageReceiver.close();  // TODO: 2017/10/12 UDPMessageReceiver线程此时状态，start方法对于处于中断状态的线程的影响(异常处理目的：关闭线程，重新开启线程，进行监听工作)

            message.setMessageType(Config.MESSAGE_TYPE_MESSAGE_SEND_FAILED);
            subject.onNext(message);
        }
    }


//    private void connectHostStatusChange(Message message) throws NoMatchParserMan {
//
//        String messageType = message.getMessageType();
//        int peerSeq = message.getSeq();
//
//        if (messageType == null) {
//            throw new NoMatchParserMan("");
//        }
//
//        switch (Config.hostStatus) {
//            case Config.LISTEN:
//                if (messageType.equals(Config.MESSAGE_TYPE_SYN)) {
//                    //收到SYN报文
//                    //发送SYN+ACK报文，进入SYN_RCVD状态
//                    seq = new Random().nextInt();
//                    ack = ++peerSeq;
//                    Config.hostStatus = Config.SYN_RCVD;
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN_ACK);
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_CONNECT_FAILED)) {
//                    subject.onNext(message);
//                }
//                break;
//            case Config.SYN_SENT:
//                if (messageType.equals(Config.MESSAGE_TYPE_TIME_OUT_RESEND)) {
//                    //未在指定时间内收到确认应答
//                    //发送SYN报文
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN);
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_SYN)) {
//                    //收到SYN报文段
//                    //发送SYN_ACK_S报文段，进入SYN_RCVD状态
//                    seq = ++seq;
//                    ack = ++peerSeq;
//                    Config.hostStatus = Config.SYN_RCVD;
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN_ACK_S);
//                }
//
//                //如果在该状态下收到SYN_ACK_S报文段，说明对方发送的SYN报文段丢失
//                if (messageType.equals(Config.MESSAGE_TYPE_SYN_ACK_S)) {
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_SYN_ACK)) {
//                    //收到SYN+ACK报文段
//                    //发送ACK报文段，进入ESTABLISHED状态
//                    seq = ++seq;
//                    ack = ++peerSeq;
//                    Config.hostStatus = Config.ESTABLISHED;
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_ACK);
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_CONNECT_FAILED) || messageType.equals(Config.MESSAGE_TYPE_RST)) {
//                    subject.onNext(message);
//                }
//                break;
//            case Config.SYN_RCVD:
//                if (messageType.equals(Config.MESSAGE_TYPE_TIME_OUT_RESEND)) {
//                    //未在指定时间内收到确认应答
//                    //重新发送SYN报文或是发送SYN_ACK报文
//                    if (Config.activeOpen) {
//                        sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN);
//                    } else {
//                        sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN_ACK);
//                    }
//                }
//
//                if (Config.MESSAGE_TYPE_SYN.equals(messageType)) {
//                    //收到SYN报文段
//                    //重新发送SYN报文或是发送SYN_ACK报文
//                    if (Config.activeOpen) {
//                        if (message.getHostStatus() == Config.SYN_SENT) { //对方主机状态
//                            sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN);
//                        } else {
//                            sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN_ACK_S);
//                        }
//                    } else {
//                        sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN_ACK);
//                    }
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_ACK)) {
//                    //收到ACK报文段
//                    //进入ESTABLISHED状态
//                    Config.hostStatus = Config.ESTABLISHED;
//
//                    if (!Config.isReliableTrans) {
//                        message.setMessageType(Config.MESSAGE_TYPE_CHANNEL_ESTABLISHED); //通道建立成功
//                        subject.onNext(message);
//                    }
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_SYN_ACK_S)) {
//                    //收到SYN_ACK_S报文段
//                    Config.hostStatus = Config.ESTABLISHED;
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_RST)) {
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_CONNECT_FAILED)) {
//                    //发送RST报文段，进入CLOSED状态
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_RST);
//                    subject.onNext(message);
//                }
//                break;
//            case Config.ESTABLISHED:
//                if (messageType.equals(Config.MESSAGE_TYPE_TIME_OUT_RESEND) && Config.activeOpen) {
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_MSG);
//                } else if (messageType.equals(Config.MESSAGE_TYPE_TIME_OUT_RESEND) && Config.passiveOpen) {
//                }
//
//                //同时握手，一方ESTABLISHED状态下时，另一方SYN_RCVD状态下等待超时，发送SYN报文段
//                if (messageType.equals(Config.MESSAGE_TYPE_SYN)) {
//                    //收到SYN报文段
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_SYN_ACK_S);
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_ACK) && Config.activeOpen) {
//                    message.setMessageType(Config.MESSAGE_TYPE_CHANNEL_ESTABLISHED);
//                    heartbeatDispatcher.start();
//                    subject.onNext(message); //消息传输成功
//                }
//
//                //单向，一方发送ACK报文段丢失，一方超时重传，发送SYN_ACK报文段
//                if (messageType.equals(Config.MESSAGE_TYPE_SYN_ACK)) {
//                    //收到SYN+ACK报文
//                    //上次发送的ACK报文段丢失或是阻塞在某一节点，重新发送ACK报文段
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_ACK);
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_MSG)) {
//                    sendSegment(seq, ack, Config.MESSAGE_TYPE_MESSAGE_ACK);
//                }
//
//                //连接发送方在一段时间内未收到任何消息，可靠的UDP通道建立完成
//                if (messageType.equals(Config.MESSAGE_TYPE_CHANNEL_ESTABLISHED)) {
//                    //发送端需要可靠传输
//                    if (Config.isReliableTrans) {
//                        sendSegment(seq, ack, Config.MESSAGE_TYPE_MSG);
//                    } else {
//                        subject.onNext(message); //UDP通道建立成功
//                    }
//                }
//
//                if (messageType.equals(Config.MESSAGE_TYPE_CONNECT_FAILED) || messageType.equals(Config.MESSAGE_TYPE_RST)) {
//                    subject.onNext(message); //消息发送失败
//                }
//                break;
//        }
//    }

}
