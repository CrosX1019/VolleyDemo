package com.crosx.volleydemo;

import android.app.Application;

import com.android.volley.RequestQueue;


/**
 * Created by CrosX on 2017/10/12.
 */

public class VolleyApplication extends Application {

    public static RequestQueue sRequestQueue;

    @Override
    public void onCreate() {
        super.onCreate();
        sRequestQueue = RequestQueueUtil.getRequestQueue(this);
    }
}
