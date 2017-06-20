package com.zane.p2pclient.comman;

import android.util.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Message队列
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class MessageQueue {
    private BlockingQueue<Message> messaegQueue;

    private MessageQueue() {
        messaegQueue = new LinkedBlockingQueue<Message>(10);
    }

    private static final class SingletonHolder{
        private static final MessageQueue instance = new MessageQueue();
    }

    public static MessageQueue getInstance() {
        return SingletonHolder.instance;
    }

    public void put(Message message) {
        try {
            messaegQueue.put(message);
        } catch (InterruptedException e) {
            Log.i("MessageQueue", "Put interrupted: " + e.getMessage());
        }
    }

    /**
     * See @{MessageDispacher}
     * @return
     * @throws InterruptedException
     */
    public Message take() throws InterruptedException{
        return messaegQueue.take();
    }
}
