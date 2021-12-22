package com.tdd.coolweather.util;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    private static final String TAG = "HttpUtil";

    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        Log.i(TAG,address);
        OkHttpClient mClient = new OkHttpClient();
        Request mRequest = new Request.Builder().url(address).build();
        mClient.newCall(mRequest).enqueue(callback);//添加回调函数
    }

}
