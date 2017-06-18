package com.zane.p2pclient.comman;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.client.SocketClient;

/**
 * 通过心跳包维持UDP通道
 * Created by Zane on 2017/6/18.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class Heartbeat extends Thread{

    private MessageSend messageSend;
    private Message heartPackage;

    public Heartbeat(MessageSend messageSend) {
        this.messageSend = messageSend;
        heartPackage = new Message.Builder()
                               .setMessageType(Config.MESSAGE_TYPE_HEART)
                               .setHost(MyPreferences.getInstance().getHost())
                               .setPort(MyPreferences.getInstance().getPort())
                               .build();
    }

    public void finish() {
        interrupt();
    }

    @Override
    public void run() {
        super.run();
        while (!isInterrupted()) {
            try {
                sleep(20000);
                messageSend.sendMessaga(heartPackage);
            } catch (Exception e) {
                finish();
            }
        }
    }
}
