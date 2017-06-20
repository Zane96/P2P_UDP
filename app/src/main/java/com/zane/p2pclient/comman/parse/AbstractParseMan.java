package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.comman.Message;
import com.zane.p2pclient.comman.send.IMessageSend;

import java.io.IOException;

import io.reactivex.Flowable;

/**
 * 处理Message的责任链模式
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public abstract class AbstractParseMan{
    public AbstractParseMan nextParseMan;
    protected IMessageSend sendMan;

    public abstract void send(Message message) throws IOException;

    public abstract void receive(Message message) throws NoMatchParserMan;

    public abstract Flowable<String> getFlowable();
}
