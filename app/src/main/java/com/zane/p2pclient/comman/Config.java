package com.zane.p2pclient.comman;

/**
 * Created by Zane on 2017/6/17.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public final class Config {
    public static final String SERVER_HOST = "182.254.232.163"; //公网服务器
    //public static final String SERVER_HOST = "52.15.183.149";
    public static final int SERVER_PORT = 9000; //公网服务器端口
    public static final int PHONE_PORT = 9000; //手机端口号


    public static final String MESSAGE_TYPE_LOGIN = "login";
    public static final String MESSAGE_TYPE_LOGIN_RESULT = "login_result";
    public static final String MESSAGE_TYPE_SERVER_UDP = "server_udp"; //打通和服务端的udp通道
    public static final String MESSAGE_TYPE_QUIT = "quit";
    public static final String MESSAGE_TYPE_QUIT_RESULT = "quit_result";
    public static final String MESSAGE_TYPE_CONNECT = "connect"; //向服务器请求连接信息
    public static final String MESSAGE_TYPE_CONNECT_RESULE = "connect_result"; //服务器反馈
    public static final String MESSAGE_TYPE_NOUFOUND = "not_found";
    public static final String MESSAGE_TYPE_CONNECT_P = "connect_p"; //端连接
    public static final String MESSAGE_TYPE_DISCONNECT = "disconnect"; //端对端断开连接
    public static final String MESSAGE_TYPE_SEND = "send";
    public static final String MESSAGE_TYPE_HEART = "heart";

    public static final int CODE_SUCCESS = 200;
    public static final String MESSAGE_SUCCESS = "success";
    public static final int CODE_FAILED = 403;
    public static final String MESSAGE_FAILED = "failed";
}
