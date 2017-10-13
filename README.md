### 写在前面
> 本文仅针对有一定volley基础的朋友们。如果你之前完全没有接触过volley，推荐去看下郭霖大神的blog。[Android Volley完全解析(一)，初识Volley的基本用法](http://blog.csdn.net/guolin_blog/article/details/17482095)

> 本文心法传授于群里大佬，大佬QQ:864009106，让我对于volley以及封装有了新的认识，十分感谢。

---

### 准备工作



1. Manifest.xml配置网络权限

    ```
    <!-- 网络请求权限 -->
    <uses-permission android:name="android.permission.INTERNET" />
    ```

2. JSON处理

    本文采用fastjson进行解析

    ```
    //fastJson
    compile 'com.alibaba:fastjson:1.2.33'
    ```

3. 进度条显示(可选)

    本文采用SweetAlertDialog

    ```
    //sweet-alert-dialog
    compile 'cn.pedant.sweetalert:library:1.3'
    ```

> 导入SweetAlertDialog时，项目会报Manifest merge failed错误，在Manifest的application中加入tools:replace="android:icon"即可解决。

### 初始化

1. 确保RequestQueue单例。

    新建RequestQueueUtil工具类， 单例获取RequestQueue对象。
    ```
    public class RequestQueueUtil {

        private static RequestQueue sRequestQueue;

        public static RequestQueue getRequestQueue(Context context) {
            if (sRequestQueue == null) {
                synchronized (RequestQueue.class) {
                    if (sRequestQueue == null) {
                    sRequestQueue = Volley.newRequestQueue(context);
                    }
                }
            }
            return sRequestQueue;
        }

    }
    ```

2. 在Application中初始化请求队列

    ```
    public class VolleyApplication extends Application {

        public static RequestQueue sRequestQueue;

        @Override
        public void onCreate() {
            super.onCreate();
            sRequestQueue = RequestQueueUtil.getRequestQueue(this);
        }
        
    }
    ```
    
### 开始封装

> 开始之前，我们先来想一想，一个好的网络请求框架需要哪些东西？

> 首先要有可拓展性，可以根据项目的情况灵活修改。

> 其次要有统一的数据处理以及错误处理。

> 最后一定要结构整齐美观，便于后期维护。

咱们先来看一个基本的Request用法：
```
private void getInfo() {

    StringRequest request = new StringRequest(Request.Method.GET, "url", new Response.Listener<String>() {
        @Override
        public void onResponse(String s) {
            Bean bean = JSON.parseObject(s, Bean.class);
            switch (bean.getStatus()) {
                //成功逻辑处理
                case 1000:
                    //do something...
                    break;
                //case xxxx:
                //其他逻辑处理...
                //break;
            }
        }
    }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError volleyError) {
            Toast.makeText(MainActivity.this, "error", Toast.LENGTH_SHORT).show();
        }
    });
    request.setRetryPolicy(new DefaultRetryPolicy(
            5 * 1000,//链接超时时间
            0,//重新尝试连接次数
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
    ));
    requestQueue.addRequest(request);

}    
```
    
再来看一个我们封装好的用法：

```
private void getNewInfo() {

    NetworkUtil.getInstance().get(MainActivity.this, "url", map, new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAG:
                    Log.i("result", msg.obj.toString());
                    //do something...
                    break;
            }
        }
    }, TAG);
    
}
```
    
比较起来，封装之后是不是看起来更加的清晰明了呢？话不多说，咱们正式开始。

1. 为了更好的应对项目的变化，我们来建立一个工具类来对请求进行封装：

    ```
    public class NetworkUtil{

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
    
    }
    ```

2. 确定BaseBean：

    所有请求发送成功之后的返回json都应该有着相同的结构，例如我们的项目中会返回：
    
    ```
    {
        "status": 1000,
        "desc": "success",
        "data": {
            "key": "value",
            "key2": 100,
            "key3": false
            //xxxx:yyyy ....
            }
    }
    
    ```
    
    为了统一数据，新建BaseBean类：
    
    ```
    public class BaseBean<T> {
        
        //返回码
        private int status;
        
        //返回信息
        private String desc;
        
        //我们需要的数据
        private T data;
        
        //getter and setter here...
    }
    ```

    这里使用了泛型T来对data进行处理，这样不管返回的结果如何，我们都不用再新建类去处理status以及desc，直接解析data即可。
    
3. 完整方法：

    ```
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
                        //case xxxx:
                        //处理其他逻辑,例如签名错误、token失效、toast错误信息等
                        //break;
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

                //如有公参请求头之类的在这里设置就好
                //@Override
                //public Map<String, String> getHeaders() throws AuthFailureError {
                //return super.getHeaders();
                //}

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return map;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                5 * 1000,//链接超时时间
                0,//重新尝试连接次数
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT//曲线增长因子
        ));
        mRequestQueue.add(request);
    }
    
    ```
    
    先贴出一个完整的post请求方法，接下来我们逐步分析：
    
    1. 参数
    
        ```
        /**
         * @param activity 当前Activity Context
         * @param url 请求地址
         * @param map 请求参数
         * @param handler 返回结果处理
         * @param tag 请求标识
         */
        ```
        
        1. activity:
            
            传入当前Activity作为Context，由于存在progress，必须依托在ActivityContext中，所以不能使用一般Context。另外在Log时也可以通过打印activity.getClass().getSimpleName()进行追踪。
            
        2. url：
            
            请求的链接地址
            
        3. map：
            
            请求参数，通过Map<String,String>进行参数处理
            
        4. handler：
            
            通过handler.sendMessage()发送数据，在Activity中拿到msg.obj进行数据处理。
            
        5. tag：
        
            当前请求标识。由于存有一个页面发起多个请求的情况，用一个自定义tag来对请求进行标记，在Activity的handler中，通过判断tag来确保请求不发生混乱。
            
    2. 进度显示
        
        此项为可选项，但从交互角度考虑需要加上进度条显示。这里使用了SweetAlertDialog，大家也可以选用自定义进度条或者其他控件。在请求发起时显示，请求成功或失败后关闭，并做对应的逻辑处理。
        
    3. 发送请求
    
        一个基本的StringRequest，如果项目需求公参或者Header，在这里统一设置。~~(最开始嫌麻烦没有封装，项目中期要求加公参，真的是一个一个改。。。都是泪)~~此外由于用户操作，界面响应等多种因素，会造成发送多次请求的情况，所以在将请求添加到队列之前，使用setRetryPolicy来统一设置，确保不会重复发送。
        
    4. 数据处理
    
        利用Message传递数据，成功响应后，将tag放入msg.what中，并将解析后的data放入msg.obj中，再由handler发送。其中具体的逻辑按照具体的项目来修改。在失败响应中，根据volleyError做了统一处理，直接在UI界面中Toast出相应信息。
        
### 具体使用

一个基本的Button点击发送请求，成功后在TextView中进行显示。由于没找到合适的post请求链接，就从网上找了个天气api接口，但是不晓得为嘛返回乱码。。。get请求的封装稍后会在源码中放出。
```
public class MainActivity extends AppCompatActivity {

    private Button mButton;

    private TextView mContent;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButton = (Button) findViewById(R.id.test_button);

        mContent = (TextView) findViewById(R.id.test_content);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkUtil.getInstance().get(MainActivity.this, "http://wthrcdn.etouch.cn/weather_mini?citykey=101010100", null, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        switch (msg.what) {
                            case WEATHER_TAG:
                                Log.i("result", msg.obj.toString());
                                mContent.setText(msg.obj.toString());
                                break;
                        }
                    }
                }, WEATHER_TAG);
            }
        });
    }
}
```

### 效果演示
![gif](https://wx4.sinaimg.cn/mw690/006qw6wCgy1fkgg4lvwk7g30a30g3ww8.gif)
