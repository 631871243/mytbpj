package com.tb.comment.HttpUtil;

import android.content.Context;
import android.util.Log;
import android.webkit.WebSettings;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by jan on 18-1-8.
 * 简单封装retrofit
 */

public class HttpUtil {
    private static final String TAG = "NewUiHttpUtil";

    private Retrofit mRetrofit;
    private Context mContext;
    private APIService mApiService;

    public static HttpUtil getInstance() {
        return InstanceHolder.instance;
    }

    private static class InstanceHolder {
        static final HttpUtil instance = new HttpUtil();
    }

    private HttpUtil() {

    }

    public void init(Context mContext){
        this.mContext = mContext;
        mRetrofit = new Retrofit.Builder()
                .client(createOkHttpAndCache())
                .baseUrl(APIContants.API_HOST)
                .build();
        mApiService = mRetrofit.create(APIService.class);
    }
    String ua = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.100 Safari/537.36";
    /**
     * 创建okHttp & 添加缓存
     * @return
     */
    private OkHttpClient createOkHttpAndCache() {

        File mCacheFile = new File(mContext.getCacheDir().getAbsolutePath() + File.separator + "retrofit2_http_cache");
        //判断缓存路径是否存在
        if (!mCacheFile.exists() && !mCacheFile.isDirectory()) {
            mCacheFile.mkdir();
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request()
                        .newBuilder()
                        .removeHeader("User-Agent")//移除旧的
                        .addHeader("User-Agent", ua)
                        .removeHeader("Referer")//移除旧的
                        .addHeader("Referer", "https://chaoshi.detail.tmall.com/item.htm?spm=a220o.7406545.0.0.17ec53a7CS5eYG&pvid=c32120b4-098e-41b6-a5e8-37063bc3e878&pos=2&acm=03194.1003.1.1288497&id=592127641658&scm=1007.12875.82860.100200300000000")
                        .build();
                return chain.proceed(request);
            }
        });
        Cache cache = new Cache(mCacheFile, 1024 * 1024 * 10);
        //给okHttp添加缓存
        builder.cache(cache);
        //设置超时
        builder.connectTimeout(10, TimeUnit.SECONDS);
        builder.readTimeout(20, TimeUnit.SECONDS);
        builder.writeTimeout(20, TimeUnit.SECONDS);
        //错误重连
        builder.retryOnConnectionFailure(true);
        return builder.build();
    }

    private void getBaseData(Call<ResponseBody> call, final IDataListener listener){
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.i(TAG, "call ===" + call.toString());
                Log.i(TAG, "response ===" + response.toString());
                try {
                    if(response.errorBody()!=null){
                        Log.i(TAG,"err info:"+response.errorBody().string());
                    }
                    String data = response.body().string();
                    listener.success(data);
                } catch (Exception e) {
                    Log.i(TAG,"request err message:"+e.getMessage());
                    listener.fail(e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i(TAG,"t-->"+t.getMessage());
                listener.fail(t.getMessage());
            }
        });
    }


    public Call<ResponseBody> getTabData(String url, IDataListener listener){
        Call<ResponseBody> call = mApiService.getTabData(url);
        getBaseData(call,listener);
        return call;
    }

    public Call<ResponseBody> getModulesByTabId(String url, IDataListener listener){
        Call<ResponseBody> call = mApiService.getModulesByTabId(url);
        getBaseData(call,listener);
        return call;
    }

    public Call<ResponseBody> getModulesDetail(String url, IDataListener listener){
        Call<ResponseBody> call = mApiService.getModulesDetail(url);
        getBaseData(call,listener);
        return call;
    }

    public interface IDataListener{
        void success(String data);
        void fail(String message);
    }

}
