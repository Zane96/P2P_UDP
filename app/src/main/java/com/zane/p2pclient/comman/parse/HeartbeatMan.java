package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.send.UDPMessageSend;

/**
 * 通过心跳包维持UDP通道
 * Created by Zane on 2017/6/18.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class HeartbeatMan extends Thread{

    private UDPMessageSend UDPMessageSend;
    private Message heartPackage;

    public HeartbeatMan(UDPMessageSend UDPMessageSend) {
        this.UDPMessageSend = UDPMessageSend;
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
                UDPMessageSend.sendMessage(heartPackage);
            } catch (Exception e) {
                finish();
            }
        }
    }
}
