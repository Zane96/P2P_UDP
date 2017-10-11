package com.zane.p2pclient.comman;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.Utils;
import com.zane.p2pclient.comman.receive.UDPMessageReceiver;

/**
 * Created by taitadatsune on 2017/10/7.
 */

public class MessageFilter {
    // TODO: 2017/10/7 通道建立成功是否需要过滤之后来的请求信息，是否需要对消息进行分片处理

    public static void filterSendMessage(Message message) {
        String messageType = message.getMessageType();

        if (Config.MESSAGE_TYPE_LOGIN.equals(messageType) || Config.MESSAGE_TYPE_CONNECT_P.equals(messageType)) {
            message.setMessageType(Config.MESSAGE_TYPE_REQUEST_CONNECTION);
            putMessageIntoQueue(true, message);
        } else if (Config.MESSAGE_TYPE_CONNECT.equals(messageType)) {
            Config.connectContent = message.getContent();
        }
    }

    public static void filterReceiverMessage(Message message) {
        String messageType = message.getMessageType();

        if ((Config.MESSAGE_TYPE_ACK.equals(messageType) && UDPMessageReceiver.channelEstablished)
                || Config.MESSAGE_TYPE_RST.equals(messageType)  // TODO: 2017/10/7 reset
                || Config.MESSAGE_TYPE_CONNECT_FAILED.equals(messageType) || Config.MESSAGE_TYPE_CHANNEL_ESTABLISHED.equals(messageType)
                || Config.MESSAGE_TYPE_P2P_CONNECT_FAILED.equals(messageType)) {
            UDPMessageReceiver.reTransTime = Integer.MAX_VALUE;
            UDPMessageReceiver.reTriesTime = Integer.MAX_VALUE;

            putMessageIntoQueue(false, message);
        }

        if (Config.MESSAGE_TYPE_CONNECT_RESULT.equals(messageType)) {
            UDPMessageReceiver.reTransTime = Config.reTransTimeOut;
            UDPMessageReceiver.reTriesTime = Config.retriesTime;

            parseP2PMessage(message);
        }
    }

    private static void parseP2PMessage(Message message) {
        Config.isP2PConnect = true;

        String extraNet = message.getExtraNet();
        String intraNet = message.getIntraNet();
        String content = message.getContent();
        String host = "";
        int port = -1;

        if (content.equals(Utils.getHost(extraNet))) {
            host = Utils.getHost(intraNet);
            port = Utils.getPort(intraNet);
        } else {
            host = Utils.getHost(extraNet);
            port = Utils.getPort(extraNet);
        }

        MyPreferences.getInstance().putHost(host);
        MyPreferences.getInstance().putPort(port);

        Message messageSend = new Message.Builder()
                .setMessageType(Config.MESSAGE_TYPE_CONNECT_P)
                .setHost(host)
                .setPort(port)
                .isReliableChannel(true)
                .build();

        putMessageIntoQueue(true, messageSend);
    }

    public static void putMessageIntoQueue(boolean isSend, Message message) {
        message.setType(isSend ? "send" : "receive");
        MessageQueue.getInstance().put(message);
    }
}
