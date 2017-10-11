package com.zane.p2pclient.comman.receive;

import android.util.Log;

import com.zane.p2pclient.comman.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Zane on 2017/6/19
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class TCPMessageReceiver extends BlockingThread implements IMessageReceiver {

    public TCPMessageReceiver(Socket socket) {
        super(socket);
    }

    @Override
    public void run() {
        super.run();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = null;
            line = br.readLine();

            if (line != null) {
                Message message = gson.fromJson(line, Message.class);
                filterMessage(message);
            }

        } catch (IOException e) {
            Log.i("server", "failed " + e.getMessage());
            //finish();
            
            if (onReceiverListener != null) {
                onReceiverListener.onFailed();
            }
        }
    }
}
