package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.send.UDPMessageSend;

/**
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class SendMan extends AbstractParseMan{

    public SendMan(UDPMessageSend sendMan) {
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
