package com.infantechar.application.util;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Eldon on 5/24/2017.
 */

public class ApplicationCallService {
    private static AsyncHttpClient client = new AsyncHttpClient();
    private ApplicationCallService(){

    }

    public static void get(Context context, String url, Header[] headers, RequestParams params, AsyncHttpResponseHandler responseHandler){
        client.get(context, url, headers, params,responseHandler);
    }
}
