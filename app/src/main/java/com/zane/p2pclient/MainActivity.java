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
        // start the ls command running
        //String[] args =  new String[]{"sh", "-c", command};
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);        //这句话就是shell与高级语言间的调用
        //如果有参数的话可以用另外一个被重载的exec方法
        //实际上这样执行时启动了一个子进程,它没有父进程的控制台
        //也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
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
        //tv.setText(sb.toString());
        //使用exec执行不会等执行成功以后才返回,它会立即返回
        //所以在某些情况下是很要命的(比如复制文件的时候)
        //使用wairFor()可以等待命令执行完成以后才返回
//        try {
//            if (proc.waitFor() != 0) {
//                System.err.println("exit value = " + proc.exitValue());
//            }
//        }
//        catch (InterruptedException e) {
//            System.err.println(e);
//        }
    }

}
