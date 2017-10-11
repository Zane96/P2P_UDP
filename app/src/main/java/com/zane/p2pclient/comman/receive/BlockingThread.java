package com.zane.p2pclient.comman.receive;

import com.google.gson.Gson;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageFilter;

import java.net.DatagramSocket;
import java.net.Socket;
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
        interrupt();
    }

    protected void generateMessage(byte[] responseData) {
        String rawData = new String(responseData);
        Message message = gson.fromJson(rawData.substring(0, rawData.lastIndexOf("}") + 1), Message.class);
        filterMessage(message);
    }

    protected void generateMessage(String messageType) {
        Message message = new Message.Builder().setMessageType(messageType).build();
        filterMessage(message);
    }

    protected void filterMessage(Message message) {
        MessageFilter.filterReceiverMessage(message);
    }
}
