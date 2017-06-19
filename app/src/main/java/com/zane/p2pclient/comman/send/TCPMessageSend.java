package com.zane.p2pclient.comman.send;

import com.google.gson.Gson;
import com.zane.p2pclient.comman.Message;

import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class TCPMessageSend implements IMessageSend{

    private OutputStream os;
    private Gson gson;

    public TCPMessageSend(OutputStream os){
        this.os = os;
        gson = new Gson();
    }

    @Override
    public void sendMessage(Message message) throws Exception{
        PrintWriter pw = new PrintWriter(os);
        pw.print(gson.toJson(message));
        pw.flush();
    }
}
