package com.zane.p2pclient.client;

import android.util.Log;

import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.HeartbeatDispatcher;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageDispatcher;
import com.zane.p2pclient.comman.MessageQueue;
import com.zane.p2pclient.comman.parse.AbstractParseMan;
import com.zane.p2pclient.comman.parse.ConnectMan;
import com.zane.p2pclient.comman.parse.HeartbeatMan;
import com.zane.p2pclient.comman.parse.SendMan;
import com.zane.p2pclient.comman.parse.ServerConnectMan;
import com.zane.p2pclient.comman.receive.IMessageReceiver;
import com.zane.p2pclient.comman.receive.TCPMessageReceiver;
import com.zane.p2pclient.comman.receive.UDPMessageReciver;
import com.zane.p2pclient.comman.send.TCPMessageSend;
import com.zane.p2pclient.comman.send.UDPMessageSend;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

import io.reactivex.Flowable;

/**
 * Created by Zane on 2017/6/17.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class SocketClient {
    private DatagramSocket clientSocket;
    private Socket socket;
    private UDPMessageReciver udpMessageReceiver;
    private UDPMessageSend udpMessageSend;
    private TCPMessageSend tcpMessageSend;
    private TCPMessageReceiver tcpMessageReceiver;
    private AbstractParseMan headParser;
    private HeartbeatDispatcher heartbeatDispatcher;
    private MessageDispatcher dispatcher;
    private MessageQueue queue;
    private int byteLength;

    private Flowable<String> connectFlowable;
    private Flowable<String> sendFlowable;
    private Flowable<String> serverConnectFlowable;

    public SocketClient(int byteLegth, int port) throws Exception{
        clientSocket = new DatagramSocket(port);
        socket = new Socket(Config.SERVER_HOST, Config.SERVER_PORT);
        this.byteLength = byteLegth;
        udpMessageSend = new UDPMessageSend(clientSocket);

        initUDPReciver();
        initTCPReiver();
        initParser();

        dispatcher = new MessageDispatcher(headParser);
        dispatcher.start();
        heartbeatDispatcher = new HeartbeatDispatcher();

        queue = MessageQueue.getInstance();
    }

    /**
     * 将要发送的Message添加到队列中，然后通过一个分发队列去分发Message
     * @param message
     * @throws Exception
     */
    public void send(Message message) throws Exception{
        message.setType("send");
        queue.put(message);
    }

    public Flowable<String> getConnectFlowable() {
        return connectFlowable;
    }

    public Flowable<String> getSendFlowable() {
        return sendFlowable;
    }

    public Flowable<String> getServerConnectFlowable() {
        return serverConnectFlowable;
    }

    /**
     * 释放资源
     */
    public void close() {
        clientSocket.close();
        udpMessageReceiver.finish();
        tcpMessageReceiver.finish();
        dispatcher.finish();
        heartbeatDispatcher.stop();
        try {
            socket.close();
        } catch (IOException e) {
            Log.i("socketclient", "TCPSocket close error: " + e.getMessage());
        }
    }

    private void initUDPReciver() {
        udpMessageReceiver = new UDPMessageReciver(clientSocket, byteLength);
        udpMessageReceiver.setOnReceiverFailedListener(new IMessageReceiver.OnReceiverListener() {
            @Override
            public void onFailed() {
                initUDPReciver();
            }
        });
        udpMessageReceiver.start();
    }

    private void initTCPReiver() {
        tcpMessageReceiver = new TCPMessageReceiver(socket);
        tcpMessageReceiver.setOnReceiverFailedListener(new IMessageReceiver.OnReceiverListener() {
            @Override
            public void onFailed() {
                initTCPReiver();
            }
        });
        tcpMessageReceiver.start();
    }

    //构建初始化责任链
    private void initParser() {
        headParser = new ConnectMan(udpMessageSend, heartbeatDispatcher);
        AbstractParseMan heartbeatMan = new HeartbeatMan(udpMessageSend);
        AbstractParseMan sendMan = new SendMan(udpMessageSend);
        AbstractParseMan serverConnectMan = new ServerConnectMan(tcpMessageSend);

        headParser.nextParseMan = heartbeatMan;
        heartbeatMan.nextParseMan = sendMan;
        sendMan.nextParseMan = serverConnectMan;

        connectFlowable = heartbeatMan.getFlowable();
        sendFlowable = sendMan.getFlowable();
        serverConnectFlowable = serverConnectMan.getFlowable();
    }

}
