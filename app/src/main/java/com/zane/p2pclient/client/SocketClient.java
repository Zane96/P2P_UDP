package com.zane.p2pclient.client;

import android.os.Handler;
import android.util.Log;

import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.HeartbeatDispatcher;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageDispatcher;
import com.zane.p2pclient.comman.MessageFilter;
import com.zane.p2pclient.comman.MessageQueue;
import com.zane.p2pclient.comman.parse.AbstractParseMan;
import com.zane.p2pclient.comman.parse.ConnectMan;
import com.zane.p2pclient.comman.parse.HeartbeatMan;
import com.zane.p2pclient.comman.parse.SendMan;
import com.zane.p2pclient.comman.parse.ServerConnectMan;
import com.zane.p2pclient.comman.parse.UDPChannelMan;
import com.zane.p2pclient.comman.receive.IMessageReceiver;
import com.zane.p2pclient.comman.receive.TCPMessageReceiver;
import com.zane.p2pclient.comman.receive.UDPMessageReceiver;
import com.zane.p2pclient.comman.send.TCPMessageSend;
import com.zane.p2pclient.comman.send.UDPMessageSend;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by Zane on 2017/6/17.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class SocketClient {
    private DatagramSocket clientSocket;
    private Socket socket;
    private UDPMessageReceiver udpMessageReceiver;
    private UDPMessageSend udpMessageSend;
    private TCPMessageSend tcpMessageSend;
    private TCPMessageReceiver tcpMessageReceiver;
    private AbstractParseMan headParser;
    private HeartbeatDispatcher heartbeatDispatcher;
    private MessageDispatcher dispatcher;
    private MessageQueue queue;
    private int byteLength;

    private Flowable<String> connectFlowable;
    private Flowable<String> udpChannelFlowable;
    private Flowable<String> sendFlowable;
    private Flowable<String> serverConnectFlowable;

    private OnSocketInitListener listener;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    tcpMessageSend = new TCPMessageSend(socket);
                    heartbeatDispatcher = new HeartbeatDispatcher();
                    initTCPReiver();
                    initParser();
                    dispatcher = new MessageDispatcher(headParser);
                    dispatcher.start();
                    listener.initSuccess();

                    break;
                case 1:
                    listener.initFailed((IOException) msg.obj);
            }
        }
    };

    public interface OnSocketInitListener {
        void initSuccess();

        void initFailed(IOException e);
    }

    public void setOnSocketInitListener(OnSocketInitListener listener) {
        this.listener = listener;
    }

    public SocketClient(int byteLegth, int port) {
        try {
            clientSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(Config.SERVER_HOST, Config.SERVER_PORT);
                    handler.sendEmptyMessage(0);
                } catch (IOException e) {
                    android.os.Message message = android.os.Message.obtain();
                    message.what = 1;
                    message.obj = e;
                    handler.sendMessage(message);
                }
            }
        }).start();

        this.byteLength = byteLegth;
        udpMessageSend = new UDPMessageSend(clientSocket);

        initUDPReciver();

        queue = MessageQueue.getInstance();
    }

    /**
     * 将要发送的Message添加到队列中，然后通过一个分发队列去分发Message
     *
     * @param message
     * @throws Exception
     */
    public void send(Message message) throws Exception {
        MessageFilter.filterSendMessage(message);
    }

    public Flowable<String> getConnectFlowable() {
        return connectFlowable.observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<String> getSendFlowable() {
        return sendFlowable.observeOn(AndroidSchedulers.mainThread());
    }

    public Flowable<String> getServerConnectFlowable() {
        return serverConnectFlowable.observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 释放资源
     */
    public void close() {
        clientSocket.close();
        udpMessageReceiver.close();
        tcpMessageReceiver.close();
        dispatcher.finish();
        heartbeatDispatcher.stop();
        try {
            socket.close();
        } catch (IOException e) {
            Log.i("socketclient", "TCPSocket close error: " + e.getMessage());
        }
    }

    private void initUDPReciver() {
        udpMessageReceiver = new UDPMessageReceiver(clientSocket, byteLength);
        udpMessageReceiver.setOnReceiverFailedListener(new IMessageReceiver.OnReceiverListener() {
            @Override
            public void onFailed() {
                initUDPReciver();
            }
        });
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
        AbstractParseMan udpChannelMan = new UDPChannelMan(udpMessageSend,udpMessageReceiver,heartbeatDispatcher);
        AbstractParseMan heartbeatMan = new HeartbeatMan(udpMessageSend);
        AbstractParseMan sendMan = new SendMan(udpMessageSend);
        AbstractParseMan serverConnectMan = new ServerConnectMan(tcpMessageSend, tcpMessageReceiver);

        headParser.nextParseMan = udpChannelMan;
        udpChannelMan.nextParseMan = heartbeatMan;
        heartbeatMan.nextParseMan = sendMan;
        sendMan.nextParseMan = serverConnectMan;

        connectFlowable = headParser.getFlowable();
        udpChannelFlowable = udpChannelMan.getFlowable();
        sendFlowable = sendMan.getFlowable();
        serverConnectFlowable = serverConnectMan.getFlowable();
    }

}
