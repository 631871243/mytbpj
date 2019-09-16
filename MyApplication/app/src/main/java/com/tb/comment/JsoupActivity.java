package com.tb.comment;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsoupActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private Button button;
    private ImageView imageview,imageview2;
    private String testurl = "https://rate.tmall.com/list_detail_rate.htm?itemId=592127641658&order=3&currentPage=1&append=0&content=1&tagId=&posi=&picture=";
    private final static String TIME_KEY = "_ksTS";
    private final static String CALL_BACK = "callback";
    private final static String UA = "ua";
    private final static String ISG = "isg";
    private WebView mWebView;
    private final static String LOAD_URL = "https://chaoshi.detail.tmall.com/item.htm?spm=a230r.1.14.6.72d13422CGlc2G&id=595970487985&cm_id=140105335569ed55e27b&abbucket=2";
    private Handler mWorkHandler;
    private HandlerThread mWorkerThread = new HandlerThread("FragmentWorkerThread");
    private static final int MSG_START = 301;
    private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    initJsoup();
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        button = (Button)findViewById(R.id.btn_1);
        mWebView = (WebView)findViewById(R.id.webview);
        mWorkerThread.start();
        mWorkHandler = new WorkHandler(mWorkerThread.getLooper());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWorkHandler.sendEmptyMessage(MSG_START);
            }
        });
    }

    private void initJsoup(){
        try {
            Document document = Jsoup.connect(LOAD_URL).get();
//            Log.i(TAG, "the document ===" + document.outerHtml());
            String html = document.outerHtml();
            Log.i(TAG, "html contain ===" + html.contains("tm-price"));
            if(html.contains("tm-price")){
                int start = html.indexOf("tm-price");
                Log.i(TAG, "the content ===" + html.substring(start));
            }
            Log.i(TAG, "title ===" + document.title());
            List<String> list = new ArrayList<>();
            list.add("tm-promo-price");
            list.add("tb-metatit");
            for (int i = 0; i < list.size(); i++) {
                getTagClassSize(document,list.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getTagClassSize(Document document,String className){
        Elements price = document.getElementsByClass(className);
        int size = price.size();
        for (int i = 0; i < size; i++) {
            Log.i(TAG, "className== "+ className + "size ===" + size + "==text==" + price.get(i).text());
        }
    }
}
