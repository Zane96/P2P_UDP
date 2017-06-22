package com.zane.p2pclient.comman.send;

import android.util.Log;

import com.google.gson.Gson;
import com.zane.p2pclient.comman.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class TCPMessageSend implements IMessageSend{

    private Socket socket;
    private Gson gson;

    public TCPMessageSend(Socket socket){
        this.socket = socket;
        gson = new Gson();
    }

    @Override
    public void sendMessage(Message message) throws IOException{
        PrintWriter pw = new PrintWriter(socket.getOutputStream());
        pw.print(gson.toJson(message));
        Log.i("server", gson.toJson(message));
        pw.flush();
    }
}
