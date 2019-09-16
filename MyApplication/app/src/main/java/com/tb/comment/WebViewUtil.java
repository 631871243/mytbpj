package com.tb.comment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;
/**
 * webview工具类
 * @author windows
 *
 */
public class WebViewUtil {

    private final static String TAG = "WebViewUtil";

	private static WebViewUtil instance;
	private Context context;
	public static WebViewUtil getInstance(Context context) {
		if (null == instance) {
			synchronized (WebViewUtil.class) {
				instance = new WebViewUtil(context);
			}
		}
		return instance;
	}

	private WebViewUtil() {

	}

	private WebViewUtil(Context context) {
		this.context = context;
	}

	/**
	 * 获取assets文件
	 * @param fileName
	 * @return
	 */
	public String loadJs(String fileName) {
		String wholeJS = null;
		try {
			InputStream fis = context.getAssets().open(fileName);
			byte buff[] = new byte[1024];
			ByteArrayOutputStream fromFile = new ByteArrayOutputStream();
			do {
				int numread = fis.read(buff);
				if (numread <= 0) {
					break;
				}
				fromFile.write(buff, 0, numread);
			} while (true);
			wholeJS = fromFile.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return wholeJS;
	}
	
	public String getJsString(String fileName) {
		return "javascript:" + loadJs(fileName);
	}

	public void loadJs(WebView wb,String fileName) {
		wb.loadUrl(getJsString(fileName));
	}

}
