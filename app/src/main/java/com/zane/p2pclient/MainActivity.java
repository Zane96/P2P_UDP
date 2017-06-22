package com.zane.p2pclient;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zane.p2pclient.client.SocketClient;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.PublishSubject;

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
        sb = new StringBuilder();
        flushInfo("Init~");

        init();

        if (socketClient != null && intraNet != null) {
            initLogic(intraNet);
        }
    }

    private void connect() {
        btnSend.setEnabled(true);
        btnQuit.setEnabled(false);
        btnDisConnect.setEnabled(true);
        btnConnect.setEnabled(false);
        btnLogin.setEnabled(false);
    }

    private void disconnect() {
        btnSend.setEnabled(false);
        btnQuit.setEnabled(true);
        btnDisConnect.setEnabled(false);
        btnConnect.setEnabled(true);
        btnLogin.setEnabled(false);
    }

    private void login() {
        btnSend.setEnabled(false);
        btnQuit.setEnabled(true);
        btnDisConnect.setEnabled(false);
        btnConnect.setEnabled(true);
        btnLogin.setEnabled(false);
    }

    private void quit() {
        btnSend.setEnabled(false);
        btnQuit.setEnabled(false);
        btnDisConnect.setEnabled(false);
        btnConnect.setEnabled(false);
        btnLogin.setEnabled(true);
        socketClient.close();
    }

    private void init() {
        try {
            intraNet = Utils.getIntrxNet();
        } catch (IOException e) {
            flushInfo("GetIntraNet error1: " + e.getMessage());
        }

        socketClient = new SocketClient(1024, Config.PHONE_PORT);
        socketClient.setOnSocketInitListener(new SocketClient.OnSocketInitListener() {
            @Override
            public void initSuccess() {
                registListener();
            }

            @Override
            public void initFailed(IOException e) {
                flushInfo("Init TCP Socket error: " + e.getMessage());
            }
        });
    }

    private void initLogic(final String finalIntraNet) {
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

        btnSend.setEnabled(false);
        btnQuit.setEnabled(false);
        btnDisConnect.setEnabled(false);
        btnConnect.setEnabled(false);
        btnLogin.setEnabled(true);
    }

    private void registListener() {
        socketClient.getServerConnectFlowable().subscribe(new Subscriber<String>() {
            Subscription subscription;
            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                s.request(Integer.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                if (s.equals(Config.MESSAGE_TYPE_LOGIN_RESULT)) {
                    login();
                    flushInfo("登陆成功");
                } else if (s.equals(Config.MESSAGE_TYPE_QUIT_RESULT)) {
                    quit();
                    flushInfo("退出成功");
                } else if (s.equals(Config.MESSAGE_TYPE_CONNECT_RESULE)) {
                    flushInfo("获取对端信息成功");
                }
            }

            @Override
            public void onError(Throwable t) {
                flushInfo("ServerConnectFloawable error: " + t.getMessage());
                subscription.cancel();
            }

            @Override
            public void onComplete() {

            }
        });

        socketClient.getSendFlowable().subscribe(new Subscriber<String>() {
            Subscription subscription;
            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                s.request(Integer.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                flushInfo("收到消息: " + s);
            }

            @Override
            public void onError(Throwable t) {
                flushInfo("SendFlowable error: " + t.getMessage());
                subscription.cancel();
            }

            @Override
            public void onComplete() {

            }
        });

        socketClient.getConnectFlowable().subscribe(new Subscriber<String>() {
            Subscription subscription;
            @Override
            public void onSubscribe(Subscription s) {
                subscription = s;
                s.request(Integer.MAX_VALUE);
            }

            @Override
            public void onNext(String s) {
                if (s.equals(Config.MESSAGE_TYPE_CONNECT_P)) {
                    flushInfo("通道建立成功");
                    connect();
                } else if (s.equals(Config.MESSAGE_TYPE_DISCONNECT)) {
                    flushInfo("通道断裂");
                    disconnect();
                }
            }

            @Override
            public void onError(Throwable t) {
                flushInfo("ConnectFlowable error: " + t.getMessage());
                subscription.cancel();
            }

            @Override
            public void onComplete() {

            }
        });
    }

    private void flushInfo(String info) {
        sb.append(info).append("\n");
        textInfo.setText(sb.toString());
    }
}
