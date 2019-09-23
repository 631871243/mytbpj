package com.tb.comment.util;

import android.text.TextUtils;

public class TbUtil {

    private static final String TARGET_HTTP_START = "http";
    private static final String TARGET_HTTP_END = "点击链接";

    public static String parserProductUrl(String url){
        if(TextUtils.isEmpty(url) == false){
            if(url.startsWith(TARGET_HTTP_START)){
                return url;
            }else{
                return parserTbPasswork(url);
            }
        }
        return url.trim();
    }

    private static String parserTbPasswork(String url){
        if(url.contains(TARGET_HTTP_START) && url.contains(TARGET_HTTP_END)){
            int index = url.indexOf(TARGET_HTTP_START);
            int end = url.indexOf(TARGET_HTTP_END);
            return url.substring(index,end);
        }
        return url;
    }

}
