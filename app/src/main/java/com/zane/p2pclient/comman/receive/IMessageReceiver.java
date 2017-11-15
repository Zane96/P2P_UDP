package com.zane.p2pclient.comman.receive;

/**
 * Created by Zane on 2017/6/19.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public interface IMessageReceiver {
    interface OnReceiverListener {
        void onFailed();
    }

    public void finish();
}
