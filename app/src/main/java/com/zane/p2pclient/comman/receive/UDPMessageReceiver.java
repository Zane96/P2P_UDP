package com.zane.p2pclient.comman.receive;

import com.zane.p2pclient.comman.Config;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * 循环接收数据
 * Created by Zane on 2017/6/17.
 * Email: zanebot96@gmail.com
 * Blog: zane96.github.io
 */

public class UDPMessageReceiver extends BlockingThread implements IMessageReceiver {

    private int byteLength;

    public static int reTriesTime = Config.retriesTime; //重传次数
    public static int reTransTime = Config.reTransTimeOut; //重传时间
    public static boolean channelEstablished = false;


    public UDPMessageReceiver(DatagramSocket datagramSocket, int byteLength) {
        super(datagramSocket);
        this.byteLength = byteLength;
    }

    @Override
    public void run() {
        super.run();
        reliableMessageReceive();
    }

    private void reliableMessageReceive() {
        while (!isInterrupted()) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[byteLength], byteLength);
            boolean receivedResponse = false;
            int retries = 0;

            while (!receivedResponse && retries < reTriesTime) {
                try {
                    datagramSocket.setSoTimeout(reTransTime);
                    datagramSocket.receive(datagramPacket);

                    InetAddress locAddress = InetAddress.getLocalHost();

//                    datagramPacket.getSocketAddress(); 用户数据报发送方或数据包接收方主机的IP地址
//                    Returns the IP address of the machine to which this datagram is being sent or from which the datagram was received.
//                    datagramPacket.getAddress(); 用户数据报发送方远程主机的SocketAddress(IP地址+端口号)

                    if (!datagramPacket.getAddress().equals(locAddress)) { // TODO: 2017/10/1 判断发送方IP地址和端口是否和主机相同
                        throw new IOException("数据来自未知地址");
                    }

                    receivedResponse = true;
                } catch (SocketException e) {
                    retries++;

                    if (Config.hostStatus == Config.SYN_SENT || Config.hostStatus == Config.SYN_RCVD || Config.hostStatus == Config.ESTABLISHED) {
                        generateMessage(Config.MESSAGE_TYPE_TIME_OUT_RESEND);
                    }

//                    if (Config.hostStatus == Config.P2P_CONNECT) {
//                        generateMessage(Config.MESSAGE_TYPE_TIME_OUT_P2P);
//                    }
                } catch (IOException e) {
                    close();

                    if (onReceiverListener != null) {
                        onReceiverListener.onFailed();
                    }
                }
            }

            if (receivedResponse) {
                byte[] responseData = datagramPacket.getData();
                generateMessage(responseData);
            } else {
                if (Config.hostStatus == Config.SYN_SENT || Config.hostStatus == Config.LISTEN || Config.hostStatus == Config.SYN_RCVD) {
                    if (Config.isP2PConnect) {
                        generateMessage(Config.MESSAGE_TYPE_P2P_CONNECT_FAILED); //P2P通道建立失败
                    } else {
                        generateMessage(Config.MESSAGE_TYPE_CONNECT_FAILED); //UDP通道建立失败
                    }
                    generateMessage(Config.MESSAGE_TYPE_CONNECT_FAILED);
                } else if (Config.hostStatus == Config.ESTABLISHED) {
                    if (!channelEstablished) {
                        channelEstablished = true;
                        if (!Config.isReliableTrans) {
                            generateMessage(Config.MESSAGE_TYPE_CHANNEL_ESTABLISHED); //P2P通道建立成功
                        }
                    } else {
                        generateMessage(Config.MESSAGE_TYPE_CONNECT_FAILED);  //UDP通道建立成功,消息传输失败（多次重传仍收不到来自对方的确认应答）
                    }
                }
            }
        }
    }
}
