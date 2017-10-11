package com.zane.p2pclient.comman;

/**
 * 双方的内网，外网的二元组，请求的消息类型{登陆，退出登陆，连接，断开连接，发送消息}
 * code, message, content
 * Created by Zane on 2017/6/17.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class Message {

    private String extraNet;//183.123.207.128:1234外网二元组
    private String intraNet;//10.0.0.1:9090内网二元组
    private String host;
    private int port;
    private String messageType;
    private String content;
    private int hostStatus;

    private String type;//send or receive
    private int tryTime = 0;//尝试五次
    private boolean isReliableChannel; //可靠通道
    private boolean isReliableTrans; //可靠通信
    private int seq = 0;
    private int ack = 0;

    public Message(String extraNet, String intraNet, String host, int port, String messageType, String content, boolean isReliableChannel, boolean isReliableTrans) {
        this.extraNet = extraNet;
        this.intraNet = intraNet;
        this.host = host;
        this.port = port;
        this.messageType = messageType;
        this.content = content;
        this.isReliableChannel = isReliableChannel;
        this.isReliableTrans = isReliableTrans;
    }

    public String getExtraNet() {
        return extraNet;
    }

    public void setExtraNet(String extraNet) {
        this.extraNet = extraNet;
    }

    public String getIntraNet() {
        return intraNet;
    }

    public void setIntraNet(String intraNet) {
        this.intraNet = intraNet;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getTryTime() {
        return tryTime;
    }

    public void setTryTime(int tryTime) {
        this.tryTime = tryTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isReliableChannel() {
        return isReliableChannel;
    }

    public void setReliableChannel(boolean reliableChannel) {
        isReliableChannel = reliableChannel;
    }

    public boolean isReliableTrans() {
        return isReliableTrans;
    }

    public void setReliableTrans(boolean reliableTrans) {
        isReliableTrans = reliableTrans;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public int getAck() {
        return ack;
    }

    public void setAck(int ack) {
        this.ack = ack;
    }

    public int getHostStatus() {
        return hostStatus;
    }

    public void setHostStatus(int hostStatus) {
        this.hostStatus = hostStatus;
    }

    @Override
    public String toString() {
        return "type: " + type +
                " tryTime: " + tryTime +
                " extraNet: " + extraNet +
                " intraNet: " + intraNet +
                " host: " + host +
                " port : " + port +
                " messageType: " + messageType +
                " content: " + content;
    }

    public static class Builder {
        private String extraNet = "";//183.123.207.128:1234外网二元组
        private String intraNet = "";//10.0.0.1:9090内网二元组
        private String messageType = "";
        private String content = "";
        private String host = "";
        private int port = -1;
        private boolean isReliableChannel = false;
        private boolean isReliableTrans = false;

        public Builder setExtraNet(String extraNet) {
            this.extraNet = extraNet;
            return this;
        }

        public Builder setIntraNet(String intraNet) {
            this.intraNet = intraNet;
            return this;
        }

        public Builder setMessageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder isReliableChannel(boolean reliableChannel) {
            isReliableChannel = reliableChannel;
            return this;
        }

        public Builder isReliableTrans(boolean reliableTrans) {
            isReliableTrans = reliableTrans;
            return this;
        }

        public Message build() {
            return new Message(extraNet, intraNet, host, port, messageType, content, isReliableChannel, isReliableTrans);
        }
    }
}
