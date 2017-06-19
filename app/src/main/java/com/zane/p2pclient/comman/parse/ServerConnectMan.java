package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.send.IMessageSend;
import com.zane.p2pclient.comman.send.TCPMessageSend;
import com.zane.p2pclient.comman.send.UDPMessageSend;

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

    public ServerConnectMan(TCPMessageSend sendMan) {
        this.sendMan = sendMan;
    }

    @Override
    public void send(Message message) throws Exception{
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_LOGIN.equals(messageType) || Config.MESSAGE_TYPE_CONNECT.equals(messageType)
                || Config.MESSAGE_TYPE_QUIT.equals(messageType)) {
            sendMan.sendMessage(message);
        } else {
            nextParseMan.send(message);
        }
    }

    @Override
    public Message receive(Message message) {
        return null;
    }
}
