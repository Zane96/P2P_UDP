package com.zane.p2pclient.comman.receive;

import android.app.admin.DeviceAdminInfo;

import com.google.gson.Gson;
import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageFilter;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.LockSupport;

/**
 * 功能：实现两个线程并发
 * 描述：利用阻塞队列和生产者消费者模型实现对两个串行线程的封装
 * Created by Helldefender on 2017/9/26.
 */

public class BlockingThread extends Thread {

    protected IMessageReceiver.OnReceiverListener onReceiverListener;

    protected DatagramSocket datagramSocket;

    protected Socket socket;

    protected Gson gson;

    public BlockingThread(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
        gson = new Gson();
    }


    public BlockingThread(Socket socket) {
        this.socket = socket;
        gson = new Gson();
    }

    public void setOnReceiverFailedListener(IMessageReceiver.OnReceiverListener listener) {
        this.onReceiverListener = listener;
    }

    public void lock() {
        lock(0);
    }

    public void lock(long nanos) {
        //调用native方法阻塞当前线程，最多不超过nanos纳秒
        if (nanos > 0) {
            LockSupport.parkNanos(nanos);    // TODO: 2017/9/28 注意线程阻塞的时间问题，如果长时间无法唤醒， 尝试唤醒线程或是直接关闭线程(设置最长中断时长)
        } else {
            LockSupport.park();
        }
    }

    public void unLock() {
        //唤醒不为中断状态下的线程
        if (Thread.currentThread().isInterrupted()) {
            LockSupport.unpark(this); //唤醒线程
        }
    }

    public void close() {
        if (!Thread.currentThread().isInterrupted())   // TODO: 2017/10/12   判断条件
            interrupt();
    }

    protected void generateMessage(byte[] responseData) {
        String rawData = new String(responseData);
        String host = Config.isP2PConnect ? MyPreferences.getInstance().getHost() : Config.SERVER_HOST;
        int port = Config.isP2PConnect ? MyPreferences.getInstance().getPort() : Config.SERVER_PORT;

        Message message = gson.fromJson(rawData.substring(0, rawData.lastIndexOf("}") + 1), Message.class);
//        Message message = gson.fromJson(line, Message.class);
        message.setHost(host);
        message.setPort(port);

        filterMessage(message);
    }

    protected void generateMessage(String messageType) {
        String host = Config.isP2PConnect ? MyPreferences.getInstance().getHost() : Config.SERVER_HOST;
        int port = Config.isP2PConnect ? MyPreferences.getInstance().getPort() : Config.SERVER_PORT;

        Message message = new Message.Builder().setMessageType(messageType).build();
        message.setHost(host);
        message.setPort(port);

        filterMessage(message);
    }

    protected void filterMessage(Message message) {
        MessageFilter.filterReceiverMessage(message);
    }
}
