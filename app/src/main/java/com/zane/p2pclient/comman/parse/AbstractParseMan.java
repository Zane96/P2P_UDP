package com.zane.p2pclient.comman.parse;

import com.zane.p2pclient.comman.Message;

/**
 * 处理Message的责任链模式
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public abstract class AbstractParseMan {
    protected AbstractParseMan nextParseMan;

    public abstract void send(Message message);

    public abstract Message receive(Message message);
}
