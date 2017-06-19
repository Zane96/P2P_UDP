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

public class HeartbeatMan extends AbstractParseMan{

    public HeartbeatMan(UDPMessageSend sendMan) {
        this.sendMan = sendMan;
    }

    @Override
    public void send(Message message) throws Exception {

    }

    @Override
    public Message receive(Message message) {
        return null;
    }
}
