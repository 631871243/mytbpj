package com.tb.comment.HttpUtil;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by jan on 18-1-8.
 */

public interface APIService {

    @GET()
    Call<ResponseBody> getTabData(@Url String url);

    @GET()
    Call<ResponseBody> getModulesByTabId(@Url String url);

    @GET()
    Call<ResponseBody> getModulesDetail(@Url String url);
}
