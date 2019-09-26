package com.tb.comment;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.tb.comment.HttpUtil.HttpUtil;
import com.tb.comment.TemplateView.TemplateView;
import com.tb.comment.util.DateUtil;
import com.tb.comment.util.DecimalUtil;
import com.tb.comment.util.TbUtil;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener{
    private final static String TAG = "MainActivity";
    private Button button,button2,button3,button4;
    private String testurl = "https://rate.tmall.com/list_detail_rate.htm?itemId=592127641658&order=3&currentPage=1&append=0&content=1&tagId=&posi=&picture=";
    private final static String TIME_KEY = "_ksTS";
    private final static String CALL_BACK = "callback";
    private final static String UA = "ua";
    private final static String ISG = "isg";
    private WebView mWebView;
    private final static String LOAD_URL = "https://detail.tmall.com/item.htm?id=586788914676";

    private final static String JS_PRICE = "price.js";
    private final static String JS_TITLE = "title.js";
    private final static String JS_IMAGE = "produceimage.js";
    private final static String JS_SALES = "sales.js";
    private final static String JS_LOGIN = "login.js";
    private final static String JS_FAVOR = "favor.js";
    private final static String JS_NAME = "name.js";

    private final static String JS_CLICK_PINGJIA = "clickpj.js";
    private final static String JS_SHOW_CONTENT_PINGJIA = "readPJ.js";

    private final static int MSG_LOAD_PRICE_JS = 1;
    private final static int MSG_LOAD_IMAGE_JS = 2;
    private final static int MSG_LOAD_SALES_JS = 3;
    private final static int MSG_LOAD__JS = 4;
    private final static int MSG_LOAD_LOGIN_JS = 5;
    private final static int MSG_LOAD_FAVOR_JS = 6;
    private final static int MSG_LOAD_CLICK_PJ_JS= 7;
    private final static int MSG_LOAD_SHOW_PJ_CONTENT = 8;
    private final static int MSG_SHOW_TITLE = 9;
    private final static int MSG_SHOW_PRICE = 10;
    private final static int MSG_SHOW_SALES = 11;
    private final static int MSG_SHOW_IMAGE = 12;
    private final static int MSG_LOAD_PJ_URL = 13;
    private final static int MSG_LOAD_NAME_JS = 14;
    private final static int MSG_SHOW_NAME = 15;

    private RelativeLayout mMainView,mMainPJAll,mMainPJ;
    private TextView mTitle,mPrice,mFavor,mPriceCount,mPrefixPrice,mPrefixFavor,mPrefixPriceCount,mDiscount,mPrefixDeadline,mDeadline,mShopName;
    private ImageView mProduct,mWX,mBg;
    private EditText mEditLoadUrl,mEditPrice;
    private Handler mUIHandler;
    private HandlerThread mUIThread = new HandlerThread("FragmentWorkerThread");

    private View mCurrentView;

    private String price,favor,title,name;
    private TemplateView mTemplateView;
    private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_PRICE_JS:
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_PRICE);
                    break;
                case MSG_LOAD_IMAGE_JS:
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_IMAGE);
                    break;
                case MSG_LOAD_SALES_JS:
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_SALES);
                    break;
                case MSG_LOAD__JS:
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_PRICE);
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_IMAGE);
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_SALES);
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_FAVOR);
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_LOGIN);
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_TITLE);
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_NAME);
                    break;
                case MSG_LOAD_LOGIN_JS:
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_LOGIN);
                    break;
                case MSG_SHOW_TITLE:
                    mTitle.setText((String)msg.obj);
                    break;
                case MSG_SHOW_PRICE:
                    try {
                        String price = (String)msg.obj;
                        if(price.contains("-")){
                            int index = price.indexOf("-");
                            price = price.substring(0,index);
                        }
                        Log.i(TAG, "当前价格 ==="  + price);
                        String priceCount = DecimalUtil.mul(price,"0.5") + "";
                        String priceDisCount = DecimalUtil.mul(price,"0.5") + "";
                        showPrice(price,mPrice);
                        showPrice(priceCount,mPriceCount);
                        showPrice(priceDisCount ,mDiscount);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
//                    Log.i(TAG, "价格显示完成，去点击累计评价按钮 ===" );
//                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_CLICK_PINGJIA);
                    break;
                case MSG_SHOW_SALES:
                    mFavor.setText((String)msg.obj);
                    break;
                case MSG_SHOW_IMAGE:
                    Glide.with(MainActivity.this).load((String)msg.obj).into(mBg);
                    Glide.with(MainActivity.this).load((String)msg.obj).into(mProduct);
                    break;
                case MSG_LOAD_PJ_URL:
                    mWebView.loadUrl((String)msg.obj);
                    break;
                case MSG_LOAD_CLICK_PJ_JS:
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_CLICK_PINGJIA);
                    break;
                case MSG_LOAD_NAME_JS:
                    WebViewUtil.getInstance(MainActivity.this).loadJs(mWebView,JS_NAME);
                    break;
                case MSG_LOAD_SHOW_PJ_CONTENT:

                    break;
                case MSG_SHOW_NAME:
                    name = (String)msg.obj;
                    mShopName.setText(name);
                    break;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_layout);
        initHttpUtil();
        button = (Button)findViewById(R.id.btn_1);
        button2 = (Button)findViewById(R.id.btn_2);
        button3 = (Button)findViewById(R.id.btn_3);
        button4 = (Button)findViewById(R.id.btn_4);
        mWebView = (WebView)findViewById(R.id.webview);
        mWX = (ImageView)findViewById(R.id.wx);
        mEditLoadUrl = (EditText) findViewById(R.id.load_url);
        mEditPrice = (EditText) findViewById(R.id.et_price);
        mUIThread.start();
        mUIHandler = new WorkHandler(Looper.getMainLooper());
        mTemplateView = new TemplateView(this);


        mMainView = (RelativeLayout) findViewById(R.id.main);
        mMainPJAll = (RelativeLayout) findViewById(R.id.main_pj_all);
        mMainPJ = (RelativeLayout) findViewById(R.id.main_pj);
        mTitle = (TextView)findViewById(R.id.title);
        mPrice = (TextView)findViewById(R.id.prive);
        mFavor = (TextView)findViewById(R.id.favor);
        mDiscount = (TextView)findViewById(R.id.discount);
        mProduct = (ImageView)findViewById(R.id.product_image);
        mBg = (ImageView)findViewById(R.id.img);
        mPriceCount = (TextView)findViewById(R.id.prive_count);
        mPrefixDeadline = (TextView)findViewById(R.id.deadline_title);
        mDeadline = (TextView)findViewById(R.id.deadline);
        mShopName = (TextView)findViewById(R.id.shop_title);
        String mDeadLine = null;
        int day = DateUtil.getDay();
        int month = DateUtil.getMonth();
        int year = DateUtil.getYear();
        if(month == 12){
            mDeadLine = (year + 1) + "." + 1 + "." + day + "-" + (year + 1) + "." + 2 + "." + day ;
        }else{
            mDeadLine = year + "." + month + "." + day + "-" + year + "." + (month + 1) + "." + day;
        }
        mDeadline.setText(mDeadLine);
/*        mPrefixPrice = (TextView)findViewById(R.id.prefix_price);
        mPrefixFavor = (TextView)findViewById(R.id.prefix_favor);
        mPrefixPriceCount = (TextView)findViewById(R.id.prefix_price_count);*/

        initWebViewSetting();
        initWebViewClient();
        initWebChromeClient();
        mCurrentView = mTitle;
//        mWebView.loadUrl(/*"file:///android_asset/test.html"*/LOAD_URL);
        button.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        mTitle.setOnClickListener(this);
        mPrice.setOnClickListener(this);
        mFavor.setOnClickListener(this);
        mProduct.setOnClickListener(this);
        mPriceCount.setOnClickListener(this);
        mMainPJ.setOnTouchListener(this);
        mWX.setOnTouchListener(this);
        mEditPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if(s.length() > 0){
                    price = s.toString();
                    Message msg = mUIHandler.obtainMessage();
                    msg.what = MSG_SHOW_PRICE;
                    msg.obj = price;
                    mUIHandler.sendMessage(msg);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onResume(){
        ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData data = cm.getPrimaryClip();
        if(data != null){
            ClipData.Item item = data.getItemAt(0);
            String content = item.getText().toString();
            mEditLoadUrl.setText(TbUtil.parserProductUrl(content));
        }
        super.onResume();
    }

    private void rotateAnimHorizonPJ() {
        float centerX = mMainPJ.getWidth()/2.0F;
        float centerY = mMainPJ.getHeight()/2.0F;
        float centerZ = 0;
        Rotate3dAnimation rotate3dAnimationX = new Rotate3dAnimation(0, 45, centerX, centerY, centerZ, Rotate3dAnimation.ROTATE_Y_AXIS, false);
        rotate3dAnimationX.setDuration(1000);
        rotate3dAnimationX.setFillAfter(true);
        mMainPJ.startAnimation(rotate3dAnimationX);
    }

    private void rotateAnimHorizonWX() {
        float centerX = mWX.getWidth();
        float centerY = -mWX.getHeight();
        float centerZ = 100f;
        Rotate3dAnimation rotate3dAnimationX = new Rotate3dAnimation(0, -30, centerX, centerY, centerZ, Rotate3dAnimation.ROTATE_Y_AXIS, false);
        rotate3dAnimationX.setDuration(1000);
        rotate3dAnimationX.setFillAfter(true);
        mWX.startAnimation(rotate3dAnimationX);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_1:
                openAlbum();
                break;
            case R.id.btn_2:
                rotateAnimHorizonPJ();
                break;
            case R.id.btn_3:
                Bitmap map = loadBitmapFromView(mMainPJAll);
                saveBitap(map);
                break;
            case R.id.btn_4:
                price = "";
                String url = mEditLoadUrl.getText().toString();
                Log.i(TAG,"THE URL IS == " +url);
                if(url.startsWith("http") == false){
                    mEditLoadUrl.setText("商品链接格式错误!!!");
                }else{
                    mEditLoadUrl.setText("正在加载商品链接...");
                    mWebView.loadUrl(url);
                }
                break;
            case R.id.title:
                mCurrentView = v;
                break;
            case R.id.prive:
                mCurrentView = v;
                break;
            case R.id.favor:
                mCurrentView = v;
                break;
            case R.id.prive_count:
                mCurrentView = v;
                break;
        }
    }

    private void saveBitap(Bitmap bmp){
        File appDir = new File("/sdcard/", "/mypj/image/");
        if (!appDir.exists()) {
            Log.i(TAG,"创建文件的结果====="+appDir.mkdirs());
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(MainActivity.this.getContentResolver(),
                    file.getAbsolutePath(), fileName, null);
            Log.i(TAG,"保存成功");
            // 最后通知图库更新
            MainActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(file.getAbsolutePath())));
            MediaScannerConnection.scanFile(MainActivity.this,
                    new String[]{file.getAbsolutePath()},
                    new String[]{"image/jpeg"},
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("pgpluginMain","onScanCompleted"+path);
                        }
                    });
            Toast.makeText(MainActivity.this,"保存图片成功",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Log.i(TAG,"保存失败");
            Toast.makeText(MainActivity.this,"保存图片失败",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private float lastX;
    private float lastY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getRawX();
                lastY = event.getRawY();
                return true;
            case MotionEvent.ACTION_MOVE:
                //  不要直接用getX和getY,这两个获取的数据已经是经过处理的,容易出现图片抖动的情况
                float distanceX = lastX - event.getRawX();
                float distanceY = lastY - event.getRawY();
                float nextY = v.getY() - distanceY;
                float nextX = v.getX() - distanceX;
                // 不能移出屏幕
                if (nextY < 0) {
                    nextY = 0;
                } else if (nextY > containerHeight - v.getHeight()) {
                    nextY = containerHeight - v.getHeight();
                }
                if (nextX < 0) {
                    nextX = 0;
                } else if (nextX > containerWidth - v.getWidth()) {
                    nextX = containerWidth - v.getWidth();
                }
                // 属性动画移动
                ObjectAnimator y = ObjectAnimator.ofFloat(v, "y", v.getY(), nextY);
                ObjectAnimator x = ObjectAnimator.ofFloat(v, "x", v.getX(), nextX);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(x, y);
                animatorSet.setDuration(0);
                animatorSet.start();
                lastX = event.getRawX();
                lastY = event.getRawY();
                return false;
        }
        return super.onTouchEvent(event);
    }

    private int containerHeight,containerWidth;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        // 这里来获取容器的宽和高
        if (hasFocus) {
            containerHeight = mMainView.getHeight();
            containerWidth = mMainView.getWidth();
        }
    }

    //自己定义的类
    /**
     * 逻辑处理
     * @author linzewu
     */
    final class InJavaScriptLocalObj {
        @JavascriptInterface
        public void showPrice(String price) {
            getPrice(price);
        }
        @JavascriptInterface
        public void showImage(String image) {
            getImage(image);
        }
        @JavascriptInterface
        public void showTitle(String title) {
            getTitle(title);
        }
        @JavascriptInterface
        public void showSales(String sales) {
            getSales(sales);
        }
        @JavascriptInterface
        public void showFavor(String favor) {
            getFavor(favor);
        }
        @JavascriptInterface
        public void showPJ(String favor) {
            getPJ(favor);
        }
        @JavascriptInterface
        public void readPJ(String favor) {
            showPJContent(favor);
        }
        @JavascriptInterface
        public void readTAG(String favor) {
            parserTag(favor);
        }
        @JavascriptInterface
        public void readName(String favor) {
            getName(favor);
        }
    }

    private void getName(String name){
        Log.i(TAG, "name ===" + name);
        Message msg = mUIHandler.obtainMessage();
        msg.what = MSG_SHOW_NAME;
        msg.obj = name;
        mUIHandler.sendMessage(msg);
    }

    private void getPJ(String favor){
        //已经点击了累计评价按钮，如果地址不为空，则表示获取成功，
        Log.i(TAG, "getPJ ===" + favor);
        if(TextUtils.isEmpty(favor) == false){
            Log.i(TAG, "获取到了累计评价按钮的地址，点击他，然后他会加载评价内容 ===" );
//            Message msg = mUIHandler.obtainMessage();
//            msg.what = MSG_LOAD_SHOW_PJ_CONTENT;
//            msg.obj = favor;
//            mUIHandler.sendMessageDelayed(msg,2 * 1000);
        }
    }

    private void showPJContent(String favor){
        Log.i(TAG, "showPJContent ===" + favor);
        parserContent(favor);
    }

    private void parserTag(String favor){
        Document document = Jsoup.parse(favor);
        Elements elementsTag = document.getElementsByTag("a");
        int size = elementsTag.size();
        List<String> list = new ArrayList<>();
        for (int i = 0;i< size;i ++){
            String tag = elementsTag.get(i).text();
            Log.i(TAG, "获取的评语标签为 ===" + tag);
            list.add(tag);
        }
    }

    private void parserContent(String favor){
        Document document = Jsoup.parse(favor);
        Elements elements = document.getElementsByClass("tm-col-master");
        List<CommentBean> list = new ArrayList<>();
        int size = elements.size();
        for (int i = 0;i< size;i ++){
            list.add(parserTR(elements.get(i)));
        }
        Log.i(TAG, "获取的评语数量为 ===" + list.size());
    }


    /**
     * 解析评语及图片
     * @param elementParent
     */
    private CommentBean parserTR(Element elementParent){
        CommentBean bean = new CommentBean();
        Elements elements = elementParent.getElementsByClass("tm-rate-fulltxt");
        int size = elements.size();
        for (int i = 0;i< size;i ++){
            String src = elements.get(0).text();
            Log.i(TAG, "评语 ===" + src);
            bean.setCommentContent(elements.get(0).text());
        }
        Elements elementsImage = elementParent.getElementsByTag("li");
        List<String> list = new ArrayList<>();
        int sizeImage = elementsImage.size();
        for (int i = 0;i< sizeImage;i ++){
            String src = elementsImage.get(i).attr("data-src");
            src = "http:" + src;
            Log.i(TAG, "图片 ===" + src);
            list.add(src);
        }
        return bean;
    }

    private void getPrice(String salesHtml) {
        if (TextUtils.isEmpty(salesHtml) == false) {
            String target = "class=\"tm-price\"";
            if (salesHtml.contains(target)) {
                int startfirst = salesHtml.indexOf(target);
                int last = salesHtml.lastIndexOf(target);
                Log.i(TAG, "startfirst ===" + startfirst + "==last==" + last);
                if (startfirst != last) {
                    //
                    String a = salesHtml.substring(last);
                    int startIndex = a.indexOf(">");
                    int endIndex = a.indexOf("<");
                    price = a.substring(startIndex + 1, endIndex);
                    Log.i(TAG, "price ===" + price);
                    Message msg = mUIHandler.obtainMessage();
                    msg.what = MSG_SHOW_PRICE;
                    msg.obj = price;
                    mUIHandler.sendMessage(msg);
                } else {
                    int start = salesHtml.indexOf(target);
                    String a = salesHtml.substring(start);
                    int startIndex = a.indexOf(">");
                    int endIndex = a.indexOf("<");
                    price = a.substring(startIndex + 1, endIndex);
                    Log.i(TAG, "price ===" + price);
                    Message msg = mUIHandler.obtainMessage();
                    msg.what = MSG_SHOW_PRICE;
                    msg.obj = price;
                    mUIHandler.sendMessage(msg);
                }
            }
        }else{
            Log.i(TAG, "price ===" );
            mUIHandler.sendEmptyMessageDelayed(MSG_LOAD_PRICE_JS,2000);
        }
    }

    private void getImage(String salesHtml){
        Log.i(TAG, "image ===" + salesHtml);
        Message msg = mUIHandler.obtainMessage();
        msg.what = MSG_SHOW_IMAGE;
        msg.obj = salesHtml;
        mUIHandler.sendMessage(msg);
    }

    private void getSales(String salesHtml){
        Log.i(TAG, "getSales ===" + salesHtml);
        if(TextUtils.isEmpty(salesHtml) == false){
            if(salesHtml.contains("class=\"tm-count\"")){
                int start = salesHtml.indexOf("class=\"tm-count\"");
                String a = salesHtml.substring(start);
                int startIndex = a.indexOf(">");
                int endIndex = a.indexOf("<");
                favor = a.substring(startIndex + 1,endIndex);
                Log.i(TAG, "sales ===" + favor);
                Message msg = mUIHandler.obtainMessage();
                msg.what = MSG_SHOW_SALES;
                msg.obj = favor;
                mUIHandler.sendMessage(msg);
            }
        }else{
            mUIHandler.sendEmptyMessageDelayed(MSG_LOAD_SALES_JS,2000);
        }
    }

    private void getFavor(String salesHtml){
        Log.i(TAG, "favor ===" + salesHtml);
        if(TextUtils.isEmpty(salesHtml)){
            favor = salesHtml;
            mUIHandler.sendEmptyMessageDelayed(MSG_LOAD_FAVOR_JS,2000);
        }else{
            Message msg = mUIHandler.obtainMessage();
            msg.what = MSG_SHOW_SALES;
            msg.obj = salesHtml;
            mUIHandler.sendMessage(msg);
        }
    }

    private void getTitle(String salesHtml){
        if(TextUtils.isEmpty(salesHtml) == false){
            String target = "class=\"tb-detail-hd\"";
            if(salesHtml.contains(target)){
                int start = salesHtml.indexOf(target);
                String a = salesHtml.substring(start + target.length() + 1);
//                Log.i(TAG, "first step ===" + a);
                int startIndex = a.indexOf(">");
                String a1 = a.substring(startIndex);
//                Log.i(TAG, "second step ===" + a1);
                int endIndex = a1.indexOf("<");

                title = a1.substring(1,endIndex).trim();
                Message msg = mUIHandler.obtainMessage();
                msg.what = MSG_SHOW_TITLE;
                msg.obj = title;
                mUIHandler.sendMessage(msg);
                Log.i(TAG, "title ===   " + title);
            }
        }
    }

    private void initWebViewSetting(){
        WebSettings mWebSettings = mWebView.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(mWebView, true);
        }
        mWebSettings.setUserAgentString("Mozilla/5.0(WindowsNT10.0;Win64;x64)AppleWebKit/537.36(KHTML,likeGecko)Chrome/" + 59 + Math.round(Math.random() * 10) + ".0.3497." + Math.round(Math.random() * 100) + "Safari/445.6");
    }

    private void initWebViewClient(){
        mWebView.addJavascriptInterface(new InJavaScriptLocalObj(), "android");
        mWebView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String aaurl) {
                return super.shouldOverrideUrlLoading(view,aaurl);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view,request);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                Log.i(TAG, "onPagestarted ===" + url);
                super.onPageStarted(view,url,favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
//                Log.i(TAG, "onPageFinished ===" + url);
                if(TextUtils.isEmpty(price)){
                    mUIHandler.removeMessages(MSG_LOAD__JS);
                    mUIHandler.sendEmptyMessageDelayed(MSG_LOAD__JS,3 * 1000);
                }
                super.onPageFinished(view,url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
//                Log.i(TAG, "onLoadResource ===" + url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){
//                Log.i(TAG, "onReceivedError ===" + failingUrl);
            }
        });
    }

    private void initWebChromeClient(){
        mWebView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return super.onConsoleMessage(consoleMessage);
            }

            /**
             * 当前 WebView 加载网页进度
             *
             * @param view
             * @param newProgress
             */
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }

            /**
             * Js 中调用 alert() 函数，产生的对话框
             *
             * @param view
             * @param url
             * @param message
             * @param result
             * @return
             */
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
//                Log.i(TAG, "onJsAlert ===" + message + "==url==" + url);
                /**
                 * 这里写你自己的处理方式
                 */
                result.confirm();
                return true;
            }

            /**
             * 处理 Js 中的 Confirm 对话框
             *
             * @param view
             * @param url
             * @param message
             * @param result
             * @return
             */
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
//                Log.i(TAG, "onJsConfirm ===" + message + "==url==" + url);
                return super.onJsConfirm(view, url, message, result);
            }

            /**
             * 处理 JS 中的 Prompt对话框
             *
             * @param view
             * @param url
             * @param message
             * @param defaultValue
             * @param result
             * @return
             */
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
//                Log.i(TAG, "onJsPrompt ===" + message + "==url==" + url);
                return super.onJsPrompt(view, url, message, defaultValue, result);
            }

            /**
             * 接收web页面的icon
             *
             * @param view
             * @param icon
             */
            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
//                Log.i(TAG, "onReceivedIcon ===" + (Looper.myLooper() == Looper.getMainLooper()));
                super.onReceivedIcon(view, icon);
            }

            /**
             * 接收web页面的 Title
             *
             * @param view
             * @param title
             */
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
//                Log.i(TAG, "onReceivedTitle ===" + title);
            }

        });
    }

    private void getItemUrl(){
        String productUrl = "";
        String ua = "098#E1hvM9vnvP6vUvCkvvvvvjiPRFLv1jDnPFdZljthPmPZtjtbRF5hgj3ERFLO1jnHRphvCvvvphmrvpvEvUmyIH9vvUb9iQhvCvvvpZpPvpvhvv2MMQyCvhACTZXVjXp4Hd8raAd6D46fd3ODNrBl5i97rjlHlnQXHFKz8Z0vQE01+byDCwsIRfUTKFEw9E7re8TJO9VU+boJ+ul1bPshVE+X5jibKphv8vvvphvvvvvvvvCmBQvv9xhvvhNjvvvmjvvvBGwvvv8hvvCj1QvvvIoivpvUvvCCbHCbqi0EvpvVvpCmpnsZvphvC9mvphvvv2yCvvpvvhCv3QhvCvmvphmrvpvp9gmw5M6vvWtD+oYC6XZzRIhCvCB47gdzwn147DIbDJNGvHdS7rdNov==";
        String callback = "jsonp" + getRandomData(1000,9000);
        String time = System.currentTimeMillis() + "_" + getRandomData(100,900);
        String isg = "BOTkQ7EaZSKCKZGCGeyxWGMFtevWfQjn_b-e7P4F9K9xqYVzJoyOd4BLaUkUcUA_";
        Log.i(TAG, "callback ===" + callback + "==time==" + time);
        productUrl = productUrl + "&" + UA + "=" + ua;
        productUrl = productUrl + "&" + TIME_KEY + "=" + time;
        productUrl = productUrl + "&" + CALL_BACK + "=" + callback;
        productUrl = productUrl + "&" + ISG + "=" + isg;
        getData(productUrl);
    }

    private int getRandomData(int min,int next){
        Random random = new Random();
        return random.nextInt(next) + min;
    }

    private void initHttpUtil(){
        HttpUtil.getInstance().init(this);
    }

    private void getData(String url){
        Log.i(TAG, "url ===" + url);
        HttpUtil.getInstance().getTabData(url, new HttpUtil.IDataListener() {
            @Override
            public void success(String data) {
                Log.i(TAG, "success ===" + data);
            }

            @Override
            public void fail(String message) {
                Log.i(TAG, "success ===" + message);
            }
        });
    }

    private void init(){

        String ua = "098#E1hvM9vnvP6vUvCkvvvvvjiPRFLv1jDnPFdZljthPmPZtjtbRF5hgj3ERFLO1jnHRphvCvvvphmrvpvEvUmyIH9vvUb9iQhvCvvvpZpPvpvhvv2MMQyCvhACTZXVjXp4Hd8raAd6D46fd3ODNrBl5i97rjlHlnQXHFKz8Z0vQE01+byDCwsIRfUTKFEw9E7re8TJO9VU+boJ+ul1bPshVE+X5jibKphv8vvvphvvvvvvvvCmBQvv9xhvvhNjvvvmjvvvBGwvvv8hvvCj1QvvvIoivpvUvvCCbHCbqi0EvpvVvpCmpnsZvphvC9mvphvvv2yCvvpvvhCv3QhvCvmvphmrvpvp9gmw5M6vvWtD+oYC6XZzRIhCvCB47gdzwn147DIbDJNGvHdS7rdNov==";
        String callback = "jsonp" + getRandomData(1000,9000);
        String time = System.currentTimeMillis() + "_" + getRandomData(100,900);
        String url = "https://rate.taobao.com/feedRateList.htm?" + "_ksTS=" + time;
        Map<String,String> map = new HashMap<>();
        map.put("auctionNumId","569127696985");
        map.put("userNumId","2840752540");
        map.put("currentPageNum","1");
        map.put("pageSize","20");
        map.put("rateType","");
        map.put("orderType","sort_weight");
        map.put("attribute","");
        map.put("sku","");
        map.put("hasSku","false");
        map.put("folded","0");
        map.put("ua",ua);
//        map.put("_ksTS",time);
        map.put("callback",callback);
        for(String key : map.keySet()){
            url = url +  "&" + key + "=" + map.get(key);
        }
        getData(url);
    }

    private static String getAssetString(Context ctx, String fileName) {
        try {
            return getStringFromInputStream(ctx.getAssets().open(fileName,
                    AssetManager.ACCESS_STREAMING));
        } catch (Exception e) {
            Log.i(TAG, "can't read asset file: " + fileName);
            e.printStackTrace();
            return null;
        }
    }

    public static String getStringFromInputStream(InputStream in) {
        BufferedReader br = null;
        try {
            final StringBuilder sb = new StringBuilder(in.available());
            br = new BufferedReader(new InputStreamReader(in));

            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeIO(in);
            closeIO(br);
        }
        return "";
    }

    public static void closeIO(Closeable io) {
        if (io != null) {
            try {
                io.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showPrice(String price,TextView tv){
        String result = price;
        if(result.contains(".")){
            int index = result.indexOf(".");
            result = result.substring(0,index);
        }
        tv.setText(result);
    }

    private static final int IMAGE_REQUEST_CODE = 1;

    public void openAlbum(){
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // 打开手机相册,设置请求码
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //在相册里面选择好相片之后调回到现在的这个activity中
        switch (requestCode) {
            case IMAGE_REQUEST_CODE://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
                if (resultCode == RESULT_OK) {//resultcode是setResult里面设置的code值
                    try {
                        Uri selectedImage = data.getData(); //获取系统返回的照片的Uri
                        String path = getImagePath(selectedImage,null);
                        displayImage(path);
                    } catch (Exception e) {
                        // TODO Auto-generatedcatch block
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private String getImagePath(Uri uri, String selection){
        String path = null;
        //通过uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.
                        Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath){
        if(imagePath !=null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            mBg.setImageBitmap(bitmap);
        }else{
            Log.e(TAG,"fail to load the album image========");
        }
    }

    /**
     * todo View 转Bitmap
     *
     * @param v
     * @return
     */
    private Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        c.drawColor(Color.WHITE);
        /** 如果不设置canvas画布为白色，则生成透明 */

        v.layout(0, 0, w, h);
        v.draw(c);

        return bmp;
    }
}
