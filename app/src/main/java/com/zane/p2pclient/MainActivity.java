package com.zane.p2pclient;

import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.zane.p2pclient.client.SocketClient;
import com.zane.p2pclient.comman.Config;
import com.zane.p2pclient.comman.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private SocketClient socketClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            execCommand("netcfg");
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("testshell", "err");
        }

        TextView textMessage = (TextView) findViewById(R.id.text_message);

        try {
            socketClient = new SocketClient(1024);
        } catch (Exception e) {
            textMessage.setText("SocketClient init error: " + e.getMessage());
        }

        if (socketClient != null) {
            findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Message message = new Message.Builder().setHost(Config.HOST).setPort(Config.PORT)
                    //socketClient.send();
                }
            });
        }
    }

    public void execCommand(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);

        InputStream inputstream = proc.getInputStream();
        InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
        BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
        // read the ls output
        String line = "";
        StringBuilder sb = new StringBuilder(line);
        while ((line = bufferedreader.readLine()) != null) {
            //System.out.println(line);
            sb.append(line);
            sb.append('\n');
        }
        Log.i("testshell", sb.toString());

    }

}
