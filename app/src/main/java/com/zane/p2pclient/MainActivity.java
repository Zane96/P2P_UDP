package com.zane.p2pclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
            findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
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
            findViewById(R.id.btn_quit).setOnClickListener(new View.OnClickListener() {
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

            findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Message message = new Message.Builder().setMessageType(Config.MESSAGE_TYPE_CONNECT)
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
            socketClient = new SocketClient(1024);
        } catch (Exception e) {
            flushInfo("GetIntraNet error: " + e.getMessage());
        }
    }

    private void flushInfo(String info) {
        sb.append(info).append("\n");
        textInfo.setText(sb.toString());
    }
}
