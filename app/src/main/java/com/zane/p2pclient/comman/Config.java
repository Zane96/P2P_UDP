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
//    public static final String MESSAGE_TYPE_SERVER_UDP = "server_udp"; //打通和服务端的udp通道 发送内网地址用来给服务器映射udp的外网地址
//    public static final String MESSAGE_TYPE_SERVER_UDP_RESULT = "server_udp_result"; //打通和服务端的udp通道 发送内网地址用来给服务器映射udp的外网地址
    public static final String MESSAGE_TYPE_QUIT = "quit";
    public static final String MESSAGE_TYPE_QUIT_RESULT = "quit_result";
    public static final String MESSAGE_TYPE_CONNECT = "connect"; //向服务器请求连接信息
    public static final String MESSAGE_TYPE_CONNECT_RESULT = "connect_result"; //服务器反馈
    public static final String MESSAGE_TYPE_NOTFOUND = "not_found";
    public static final String MESSAGE_TYPE_CONNECT_P = "connect_p"; //端连接
    public static final String MESSAGE_TYPE_DISCONNECT = "disconnect"; //端对端断开连接
    public static final String MESSAGE_TYPE_SEND = "send";
    public static final String MESSAGE_TYPE_HEART = "heart";

    public static final int CODE_SUCCESS = 200;
    public static final String MESSAGE_SUCCESS = "success";
    public static final int CODE_FAILED = 403;
    public static final String MESSAGE_FAILED = "failed";


    public static final int CLOSED = 0; //关闭状态
    public static final int SYN_SENT = 1; //连接请求报文已发送
    public static final int LISTEN = 2; //监听状态
    public static final int SYN_RCVD = 3; //连接请求报文已收到
    public static final int ESTABLISHED = 4; //连接已建立
//    public static final int P2P_CONNECT = 5; //P2P连接

    //标志位
    public static final String MESSAGE_TYPE_REQUEST_CONNECTION = "request_connect";
    public static final String MESSAGE_TYPE_SYN = "syn"; //同步
    public static final String MESSAGE_TYPE_SYN_ACK = "syn_ack";
    public static final String MESSAGE_TYPE_SYN_ACK_S = "syn_ack_s"; //
    public static final String MESSAGE_TYPE_ACK = "ack"; //确认位
    public static final String MESSAGE_TYPE_RST = "rst"; //复位
    public static final String MESSAGE_TYPE_MSG = "message";
    public static final String MESSAGE_TYPE_MESSAGE_ACK = "message_ack";
    public static final String MESSAGE_TYPE_TIME_OUT_RESEND = "time_out_reSend";
//    public static final String MESSAGE_TYPE_TIME_OUT_P2P = "time_out_p2p";
    public static final String MESSAGE_TYPE_CONNECT_FAILED = "connect_failed";
    public static final String MESSAGE_TYPE_P2P_CONNECT_FAILED = "🎫p2p_connect_failed";
    public static final String MESSAGE_TYPE_CHANNEL_ESTABLISHED = "established";

    public static int hostStatus = CLOSED; //主机连接状态
    public static boolean isP2PConnect = false;
    public static boolean isReliableTrans = false; //可靠传输
    public static boolean isReliableChannel = false;
    public static boolean activeOpen = true; //主动打开
    public static boolean passiveOpen = false; //被动打开

    public static String connectContent="";

    public static int reTransTimeOut = 2000; //超时重传时间RTO

    public static int retriesTime = 5; //超时重传次数
}
