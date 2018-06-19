package com.example.chuncheng.media;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.chuncheng.sample.media.utils.log.MediaLog;

import java.io.File;
import java.util.Calendar;

/**
 * @author chuncheng
 */
public class MainActivity extends AppCompatActivity {
    Button mButtonA;
    Button mButtonB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取动态权限
        //初始化日志输出参数
        String mCachePath = this.getFilesDir() + "/mediaLog";
        String mLogPath = Environment.getExternalStorageDirectory() + File.separator+"Medplus/media/log";
        String mNamePrefix = "MediaPlayer_" + Calendar.getInstance().getTimeInMillis();
        String mAppVersion = "v1.10";
        String mOther = "";
        MediaLog.getInstance().open(MediaLog.LEVEL_NONE, mNamePrefix, mLogPath, mCachePath, mAppVersion, mOther);

        mButtonA = (Button) findViewById(R.id.button_a);
        mButtonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("url", "http://oe39vgywo.bkt.clouddn.com/text001.mp4");
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(MainActivity.this, TestVideoActivity.class);
                startActivity(intent);
            }
        });
        mButtonB = (Button) findViewById(R.id.button_b);
        mButtonB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("url", "http://oe39vgywo.bkt.clouddn.com/text001.mp4");
                /*bundle.putString("url", "https://sgkldfjgkldfjglkdfj");*/
                bundle.putString("urlAdvertising", "http://oe39vgywo.bkt.clouddn.com/text001.mp4");
                Intent intent = new Intent();
                intent.putExtras(bundle);
                intent.setClass(MainActivity.this, PlayerActivity.class);
                startActivity(intent);
            }
        });
    }
}
