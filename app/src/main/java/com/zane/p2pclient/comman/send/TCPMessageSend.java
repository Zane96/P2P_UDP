package com.zane.p2pclient.comman.send;

import com.google.gson.Gson;
import com.zane.p2pclient.comman.Message;

import java.net.Socket;

/**
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class TCPMessageSend implements IMessageSend{

    private Socket socket;
    private Gson gson;

    public TCPMessageSend(Socket socket) {
        this.socket = socket;
        gson = new Gson();
    }


    @Override
    public void sendMessage(Message message) {
        
    }
}
