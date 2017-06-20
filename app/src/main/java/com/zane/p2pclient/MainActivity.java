package com.zane.p2pclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zane.p2pclient.client.SocketClient;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SocketClient socketClient;
    private TextView textInfo;
    private StringBuilder sb;
    private String intraNet;

    private Button btnLogin;
    private Button btnQuit;
    private Button btnConnect;
    private Button btnDisConnect;
    private Button btnSend;
    private EditText editMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInfo = (TextView) findViewById(R.id.text_message);
        sb = new StringBuilder("Init!~");
        init();

        if (socketClient != null && intraNet != null) {
            final String finalIntraNet = intraNet;

            //login
            btnLogin = (Button) findViewById(R.id.btn_login);
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Message message = new Message.Builder()
                                              .setMessageType(Config.MESSAGE_TYPE_LOGIN)
                                              .setIntraNet(finalIntraNet)
                                              .setHost(Config.SERVER_HOST)
                                              .setPort(Config.SERVER_PORT)
                                              .setContent("Zane")
                                              .build();
                    try {
                        socketClient.send(message);
                    } catch (Exception e) {
                        flushInfo("Send LoginMessage error: " + e.getMessage());
                    }
                }
            });

            //quit
            btnQuit = (Button) findViewById(R.id.btn_quit);
            btnQuit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Message message = new Message.Builder()
                                              .setMessageType(Config.MESSAGE_TYPE_QUIT)
                                              .setHost(Config.SERVER_HOST)
                                              .setPort(Config.SERVER_PORT)
                                              .setContent("Zane")
                                              .build();
                    try {
                        socketClient.send(message);
                    } catch (Exception e) {
                        flushInfo("Send QuitMessage error: " + e.getMessage());
                    }
                }
            });

            //连接
            btnConnect = (Button) findViewById(R.id.btn_connect);
            btnConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Message message = new Message.Builder()
                                              .setMessageType(Config.MESSAGE_TYPE_CONNECT)
                                              .setHost(Config.SERVER_HOST)
                                              .setPort(Config.SERVER_PORT)
                                              .setContent("SB")
                                              .build();

                    try {
                        socketClient.send(message);
                    } catch (Exception e) {
                        flushInfo("Send ConnectMessage error: " + e.getMessage());
                    }
                }
            });

            //断开连接
            btnDisConnect = (Button) findViewById(R.id.btn_disconnect);
            btnDisConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message = new Message.Builder()
                                              .setMessageType(Config.MESSAGE_TYPE_DISCONNECT)
                                              .setHost(MyPreferences.getInstance().getHost())
                                              .setPort(MyPreferences.getInstance().getPort())
                                              .setContent("disconnect")
                                              .build();

                    try {
                        socketClient.send(message);
                    } catch (Exception e) {
                        flushInfo("Send DisconnectMessage error: " + e.getMessage());
                    }
                }
            });

            //发送数据
            btnSend = (Button) findViewById(R.id.btn_send);
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Message message = new Message.Builder()
                                              .setMessageType(Config.MESSAGE_TYPE_SEND)
                                              .setHost(MyPreferences.getInstance().getHost())
                                              .setPort(MyPreferences.getInstance().getPort())
                                              .setContent(editMessage.getText().toString())
                                              .build();

                    try {
                        socketClient.send(message);
                    } catch (Exception e) {
                        flushInfo("Send SendMessage error: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void init() {
        try {
            intraNet = Utils.getIntrxNet();
        } catch (IOException e) {
            flushInfo("GetIntraNet error: " + e.getMessage());
        }

        try {
            socketClient = new SocketClient(1024, Config.PHONE_PORT);
        } catch (Exception e) {
            flushInfo("GetIntraNet error: " + e.getMessage());
        }
    }

    private void flushInfo(String info) {
        sb.append(info).append("\n");
        textInfo.setText(sb.toString());
    }
}
