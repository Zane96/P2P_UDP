package com.zane.p2pclient.comman.send;

import com.zane.p2pclient.comman.Message;

/**
 * TCP/UDP发送数据的工具抽象
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public interface IMessageSend {

    /**
     * 发送的具体操作在责任链中确定
     * @param message
     */
    void sendMessage(Message message) throws Exception;
}
