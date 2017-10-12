package com.crosx.volleydemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import static com.crosx.volleydemo.common.Constant.WEATHER_TAG;

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
                mContent.setText("");
                Map<String, String> map = new HashMap();
                map.put("citykey", "101010100");
                //map.put("xxx","xxx");
                //...
                NetworkUtil.getInstance().get(MainActivity.this, "http://wthrcdn.etouch.cn/weather_mini", map, new Handler() {
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
