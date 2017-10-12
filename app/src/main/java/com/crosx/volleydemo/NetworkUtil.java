package com.crosx.volleydemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Iterator;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by CrosX on 2017/10/12.
 */

public class NetworkUtil {

    private static NetworkUtil instance;

    //单例获取工具类
    public static NetworkUtil getInstance() {
        if (instance == null) {
            synchronized (NetworkUtil.class) {
                if (instance == null) {
                    instance = new NetworkUtil();
                }
            }
        }
        return instance;
    }

    //获取请求队列
    private RequestQueue mRequestQueue = VolleyApplication.sRequestQueue;

    public void post(final Activity activity, String url, final Map<String, String> map, final Handler handler, final int tag) {
        final SweetAlertDialog dialog = new SweetAlertDialog(activity);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#66CCFF"));
        dialog.setTitleText("Loading");
        dialog.setCancelable(false);
        if (!dialog.isShowing()) {
            dialog.show();
        }

        final Message message = Message.obtain();
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                BaseBean baseBean = JSON.parseObject(s, BaseBean.class);
                int status = baseBean.getStatus();
                switch (status) {
                    case 1000:
                        message.what = tag;
                        message.obj = baseBean.getData();
                        handler.sendMessage(message);
                        break;
//                    case xxxx:
//                        //处理其他逻辑,例如签名错误、token失效、toast错误信息等
//                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                handlerErrorMessage(activity, volleyError);
            }
        }) {

//            //如有公参请求头之类的在这里设置就好
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                return super.getHeaders();
//            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                5 * 1000,//链接超时时间
                0,//重新尝试连接次数
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        mRequestQueue.add(request);
    }

    public void get(final Activity activity, String url, Map<String, String> map, final Handler handler, final int tag) {
        final SweetAlertDialog dialog = new SweetAlertDialog(activity);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#66CCFF"));
        dialog.setTitleText("Loading");
        dialog.setCancelable(false);
        if (!dialog.isShowing()) {
            dialog.show();
        }

        String getUrl;

        if (map == null) {
            getUrl = url;
        } else {
            StringBuffer sb = new StringBuffer(url);
            sb.append("?");
            Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                String k = (String) entry.getKey();
                Object v = entry.getValue();
                sb.append(k + "=" + v + "&");
            }
            getUrl = sb.toString().substring(0, sb.toString().length() - 1);
        }

        final Message message = Message.obtain();
        StringRequest request = new StringRequest(Request.Method.GET, getUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                BaseBean baseBean = JSON.parseObject(s, BaseBean.class);
                int status = baseBean.getStatus();
                switch (status) {
                    //成功
                    case 1000:
                        message.what = tag;
                        message.obj = baseBean.getData();
                        handler.sendMessage(message);
                        break;
//                    case xxxx:
//                        //处理其他逻辑,例如签名错误、token失效、toast错误信息等
//                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                handlerErrorMessage(activity, volleyError);
            }
        }) {

//            //如有公参请求头之类的在这里设置就好
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                return super.getHeaders();
//            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                5 * 1000,//链接超时时间
                0,//重新尝试连接次数
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        mRequestQueue.add(request);
    }

    private void handlerErrorMessage(Activity context, VolleyError error) {
        if (error instanceof NoConnectionError || error instanceof NetworkError) {
            showToast(context, "网络链接异常");
        } else if (error instanceof TimeoutError) {
            showToast(context, "连接超时");
        } else if (error instanceof AuthFailureError) {
            showToast(context, "身份验证失败！");
        } else if (error instanceof ParseError) {
            showToast(context, "解析错误！");
        } else if (error instanceof ServerError) {
            showToast(context, "服务器响应错误！");
        }
    }

    private void showToast(Activity context, String str) {
        if (!context.isFinishing()) {
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        }
    }
}
