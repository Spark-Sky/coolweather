package com.tdd.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient mClient = new OkHttpClient();
        Request mRequest = new Request.Builder().url(address).build();
        mClient.newCall(mRequest).enqueue(callback);
    }

}
