package com.zane.p2pclient.comman.receive;

import android.util.Log;

import com.google.gson.Gson;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.MessageQueue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.subjects.AsyncSubject;

/**
 * 循环接收数据
 * Created by Zane on 2017/6/17.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class UDPMessageReciver extends Thread implements IMessageReceiver{

    private DatagramSocket socket;
    private int byteLength;
    private Gson gson;
    private OnReceiverListener listener;

    public void setOnReceiverFailedListener(OnReceiverListener listener) {
        this.listener = listener;
    }

    public UDPMessageReciver(DatagramSocket socket, int byteLength) {
        this.socket = socket;
        this.byteLength = byteLength;
        gson = new Gson();
    }

    public void finish() {
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[byteLength], byteLength);
                Log.i("server", "start listen udp package");
                //socket.setSoTimeout(1000);
                socket.receive(packet);
                byte[] responseData = packet.getData();

                Message message = gson.fromJson(new String(responseData), Message.class);
                message.setType("receive");
                Log.i("server", "receive udp: " + message.toString());
                MessageQueue.getInstance().put(message);
            } catch (IOException e) {
                finish();
                if (listener != null) {
                    listener.onFailed();
                }
            }
        }
    }
}
