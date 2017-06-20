package com.zane.p2pclient.comman.send;

import com.google.gson.Gson;
import com.zane.p2pclient.comman.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * 默认使用一种拦截方式来读取数据，可扩展
 * Created by Zane on 2017/6/17.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class UDPMessageSend implements IMessageSend{

    private DatagramSocket socket;
    private Gson gson;

    public UDPMessageSend(DatagramSocket socket) {
        this.socket = socket;
        gson = new Gson();
    }

    @Override
    public void sendMessage(Message message) throws IOException{
        if (!socket.isClosed() && socket.isConnected()) {
            String host = message.getHost();
            int port = message.getPort();
            if (!"".equals(host) && port >= 0) {
                byte[] datas = gson.toJson(message).getBytes();
                DatagramPacket packet = new DatagramPacket(datas, datas.length, InetAddress.getByName(host), port);
                socket.send(packet);
            } else {
                throw new IllegalArgumentException("host or port can't be null!");
            }
        }
    }
}
