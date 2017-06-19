package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.send.UDPMessageSend;

/**
 * 负责建立连接的逻辑
 * udp的通道需要两个P都互发connect消息之后才能建立起来，不然发送的消息会被抛弃
 *
 * 策略：
 * 1. P1 2 S Login
 * 2. S 2 P1, S 2 P2 传送数据
 * 3. P1 2 P2, P2 2 P1 传送请求连接建立的数据
 * 4. P1, P2分别等待P2P请求连接信息，等待1s，如果没等到转到1（暴力）
 * 5. 打通UDP通道，开始相互心跳包，相互发送数据
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class ConnectMan extends AbstractParseMan{

    public ConnectMan(AbstractParseMan nextParseMan, UDPMessageSend sendMan) {
        this.nextParseMan = nextParseMan;
    }

    @Override
    public void send(Message message) {
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_LOGIN.equals(messageType) || )
    }

    @Override
    public Message receive(Message message) {
        return null;
    }
}
