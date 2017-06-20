package com.zane.p2pclient.comman.parse;

import android.util.Log;

import com.zane.p2pclient.MyPreferences;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.send.UDPMessageSend;

import java.io.IOException;

import io.reactivex.Flowable;

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
    public void send(Message message) throws IOException {
        String messageType = message.getMessageType();
        if (Config.MESSAGE_TYPE_HEART.equals(messageType)) {
            sendMan.sendMessage(message);
        } else {
            nextParseMan.send(message);
        }
    }

    @Override
    public void receive(Message message) throws NoMatchParserMan{
        String messageType = message.getMessageType();
        if (!Config.MESSAGE_TYPE_HEART.equals(messageType)) {
            nextParseMan.receive(message);
            Log.i("receive", "Abanbon the heartbeat package-------");
        } else {
            nextParseMan.receive(message);
        }
    }

    @Override
    public Flowable<String> getFlowable() {
        return null;
    }
}
