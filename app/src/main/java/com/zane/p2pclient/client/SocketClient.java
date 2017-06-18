package com.zane.p2pclient.client;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.comman.Heartbeat;
import com.zane.p2pclient.comman.MessageReciver;
import com.zane.p2pclient.comman.MessageSend;
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
    private MessageSend messageSend;
    private Heartbeat heartbeat;
    private int byteLength;

    public SocketClient(int byteLegth) throws Exception{
        clientSocket = new DatagramSocket(9000);
        this.byteLength = byteLegth;
        messageSend = new MessageSend(clientSocket);
        heartbeat = new Heartbeat(messageSend);
        initReciver();
    }

    /**
     * 发送数据到指定的主机
     * @param message
     * @throws Exception
     */
    public void send(Message message) throws Exception{
        messageSend.sendMessaga(message);
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
            @Override
            public void onFailed() {
                restartReceiver();
            }

            @Override
            public void onConnect() {
                heartbeat.start();
            }

            @Override
            public void onConnectResult(String extraNet, String intraNet) {
                //先全部默认存储外网IP
                MyPreferences.getInstance().putHost(extraNet.substring(0, extraNet.indexOf(":") + 1));
                MyPreferences.getInstance().putPort(Integer.valueOf(extraNet.substring(extraNet.indexOf(":"), extraNet.length())));
            }
        });
        messageReciver.start();
    }

    private void restartReceiver() {
        initReciver();
    }
}
