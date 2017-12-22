package com.example.tanaka.posts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;


public class MainActivity extends Activity
        implements View.OnClickListener {
    private final static int WC = ViewGroup.LayoutParams.WRAP_CONTENT;
    private final static int MP = ViewGroup.LayoutParams.MATCH_PARENT;
    private final static String TAG_READ = "read";
    private EditText editText;
    private String   text;
    private Handler  handler = new Handler();

    //テキストファイルのURLの指定(1)
    private final static String URL =
            "https://hooks.slack.com/services/~~~~";

    //アクティビティ起動時に呼ばれる
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //レイアウトの生成
        LinearLayout layout = new LinearLayout(this);
        layout.setBackgroundColor(Color.WHITE);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        //エディットテキストの生成
        editText = new EditText(this);
        editText.setText("");
        editText.setLayoutParams(new LinearLayout.LayoutParams(MP, WC));
        layout.addView(editText);

        //ボタンの生成
        layout.addView(makeButton("HTTP通信", TAG_READ));
    }

    //ボタンの生成
    private Button makeButton(String text, String tag) {
        Button button = new Button(this);
        button.setText(text);
        button.setTag(tag);
        button.setOnClickListener(this);
        button.setLayoutParams(new LinearLayout.LayoutParams(WC, WC));
        return button;
    }

    //ボタンクリック時に呼ばれる
    public void onClick(View v) {
        String tag = (String)v.getTag();
        if (TAG_READ.equals(tag)) {
            //スレッドの生成(4)
            Thread thread = new Thread(new Runnable() {public void run() {
                //HTTP通信
                try {
                    http2data(URL);
                } catch (Exception e) {
                    text = null;
                }
                //ハンドラの生成(5)
                handler.post(new Runnable() {public void run() {
                    if (text != null) {
                        editText.setText(text);
                    } else {
                        editText.setText("読み込み失敗しました。");
                    }
                }});
            }});
            thread.start();
        }
    }

    //HTTP通信
    public JSONObject http2data(String path) throws Exception {
        Log.d( "LogCat", "++++++++++++++++++++++++++++++++++" );
        //byte[] w=new byte[1024];
        HttpURLConnection c = null;
        InputStream in = null;
        //ByteArrayOutputStream out = null;
        String JSON = "{\"text\":\"hoge\"}";
        try {
            //HTTP接続のオープン(2)
            Log.d( "LogCat", "----54l;iou--1----------" );
            URL url = new URL(path);
            c = (HttpURLConnection)url.openConnection();
            c.setDoOutput(true);

            // 時間制限
            c.setReadTimeout(10000);
            c.setConnectTimeout(20000);
            c.setRequestMethod("POST");

            // in = c.getInputStream();



            c.setRequestProperty("Content-Type", "application/JSON; charset=utf-8");
            // POSTデータの長さを設定
            c.setRequestProperty("Content-Length", String.valueOf(JSON.length()));
            // リクエストのbodyにJSON文字列を書き込む
            Log.d( "LogCat", "3" );
            OutputStreamWriter out = new OutputStreamWriter(c.getOutputStream());
            Log.d( "LogCat", "4" );
            out.write(JSON);
            Log.d( "LogCat", "5" );
            out.flush();
            Log.d( "LogCat", "6" );
            c.connect();
            /*
            //バイト配列の読み込み
            out = new ByteArrayOutputStream();
            while (true) {
                int size = in.read(w);
                if (size <= 0) break;
                out.write(w, 0, size);
            }
            */

            // HTTPレスポンスコード
            final int status = c.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                // 通信に成功した
                // テキストを取得する
                // final InputStream in = c.getInputStream();
                in = c.getInputStream();
                String encoding = c.getContentEncoding();
                if (null == encoding) {
                    encoding = "UTF-8";
                }
                final InputStreamReader inReader = new InputStreamReader(in, encoding);
                final BufferedReader bufReader = new BufferedReader(inReader);

                bufReader.close();
                inReader.close();
                in.close();
            } else {
                // 通信が失敗した場合のレスポンスコードを表示
                System.out.println(status);
            }



            out.close();

            //HTTP接続のクローズ(3)
            in.close();
            c.disconnect();


        } catch (Exception e) {
            try {
                if (c != null) c.disconnect();
                if (in != null) in.close();
                //if (out != null) out.close();
            } catch (Exception e2) {
            }
            throw e;
        }
        return null;
    }
}