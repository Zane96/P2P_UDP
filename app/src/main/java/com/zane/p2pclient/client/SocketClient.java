package com.zane.p2pclient.client;

import com.zane.p2pclient.comman.parse.HeartbeatMan;
import com.zane.p2pclient.comman.MessageReciver;
import com.zane.p2pclient.comman.send.UDPMessageSend;
import com.zane.p2pclient.comman.Message;

import java.net.DatagramSocket;

/**
 * Created by Zane on 2017/6/17.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class SocketClient {
    private DatagramSocket clientSocket;
    private MessageReciver messageReciver;
    private UDPMessageSend UDPMessageSend;
    private HeartbeatMan heartbeat;
    private int byteLength;

    public SocketClient(int byteLegth, int port) throws Exception{
        clientSocket = new DatagramSocket(port);
        this.byteLength = byteLegth;
        UDPMessageSend = new UDPMessageSend(clientSocket);
        heartbeat = new HeartbeatMan(UDPMessageSend);
        initReciver();
    }

    /**
     * 发送数据到指定的主机
     * @param message
     * @throws Exception
     */
    public void send(Message message) throws Exception{
        UDPMessageSend.sendMessage(message);
    }

    /**
     * 释放资源
     */
    public void close() {
        clientSocket.close();
        messageReciver.finish();
        heartbeat.finish();
    }

    private void initReciver() {
        messageReciver = new MessageReciver(clientSocket, byteLength);
        messageReciver.setOnReceiverFailedListener(new MessageReciver.OnReceiverListener() {

            //接收消息的通道断裂
            @Override
            public void onFailed() {
                restartReceiver();
            }

            //接收到尝试
            @Override
            public void onConnectP() {

            }

            @Override
            public void onConnectResult(String content, String extraNet, String intraNet) {

            }

            //通道成功建立
            @Override
            public void onConnectPResult() {

            }
        });
        messageReciver.start();
    }

    private void restartReceiver() {
        initReciver();
    }
}
