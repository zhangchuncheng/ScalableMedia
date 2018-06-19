package com.example.chuncheng.media;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.chuncheng.sample.media.utils.log.MediaLog;
import com.chuncheng.sample.media.video.entity.VideoPlayerBean;
import com.chuncheng.sample.media.video.manager.SeekBarManager;
import com.chuncheng.sample.media.video.manager.SingleVideoPlayerManager;
import com.chuncheng.sample.media.video.manager.VideoPlayerManager;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;
import com.chuncheng.sample.media.video.ui.wrapper.MediaPlayerWrapper;

import java.lang.ref.WeakReference;

/**
 * @author chuncheng
 */
public class PlayerActivity extends AppCompatActivity implements
        VideoPlayerView.VideoGestureDetectorUiOperationListener,
        SeekBarManager.SeekBarChangeListener {
    private static final String TAG = "PlayerActivity";
    /** 延迟屏幕重力感应message */
    private static final int SCREEN_ORIENTATION_EVENT_DELAY_CARRIED_OUT_MESSAGE = 2;
    private int height;
    private String url;
    private String urlAdvertising;
    private int mDuration;
    private boolean isPause;
    private boolean isShowAdvertising;

    private RelativeLayout mRelativeLayout;
    private VideoPlayerView mPlayerView;
    private VideoPlayerView mPlayerViewAdvertising;
    private SeekBarManager mSeekBarManager;
    private Button mButtonPlayer;
    private Button mButtonStart;
    private Button mButtonPause;
    private Button mButtonStop;
    private Button mButtonSeekTo;
    private Button mButtonFullScreen;
    private Button mButtonJump;
    private Button mButtonAdvertisingPlayer;
    private Button mButtonSpeed;
    private EditText mEditTextSpeed;
    private EditText mEditTextUrl;
    private VideoPlayerManager mPlayerManager;
    private VideoPlayerManager mPlayerManagerAdvertising;
    private RelativeLayout.LayoutParams mLayoutParamsVideoContent;

    /** 是否已打开自动选择屏幕 0-未打开 */
    private int openAutoOrientation;
    /** 屏幕重力感应handler */
    private OrientationHandler mOrientationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_activity);
        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        urlAdvertising = bundle.getString("urlAdvertising");

        mRelativeLayout = (RelativeLayout) findViewById(R.id.relative_layout_player);
        mPlayerView = (VideoPlayerView) findViewById(R.id.video_player_view);
        mPlayerViewAdvertising = (VideoPlayerView) findViewById(R.id.video_player_view_advertising);
        mButtonPlayer = (Button) findViewById(R.id.btn_player);
        mButtonStart = (Button) findViewById(R.id.btn_start);
        mButtonPause = (Button) findViewById(R.id.btn_pause);
        mButtonStop = (Button) findViewById(R.id.btn_stop);
        mButtonSeekTo = (Button) findViewById(R.id.btn_seek_to);
        mButtonFullScreen = (Button) findViewById(R.id.btn_full_screen);
        mButtonJump = (Button) findViewById(R.id.btn_jump);
        mButtonAdvertisingPlayer = (Button) findViewById(R.id.btn_advertising_player);
        mButtonSpeed = (Button) findViewById(R.id.btn_player_speed);
        mEditTextSpeed = (EditText) findViewById(R.id.et_player_speed);
        mEditTextUrl = (EditText) findViewById(R.id.et_url);
        SeekBar seekBar = (SeekBar) findViewById(R.id.seek_bar);
        mSeekBarManager = new SeekBarManager(this, seekBar, this);
        mPlayerManager = new SingleVideoPlayerManager();
        mPlayerView.setGestureDetectorValid(false);
        mPlayerView.setKeepScreenOn(true);
        mPlayerView.setUIOperationListener(this);
        mPlayerView.addMediaPlayerListener(new AbstractMediaPlayerStateListener());

        //广告
        mPlayerManagerAdvertising = new SingleVideoPlayerManager();
        mPlayerViewAdvertising.setGestureDetectorValid(false);
        mPlayerViewAdvertising.setKeepScreenOn(true);
        mPlayerViewAdvertising.addMediaPlayerListener(new MediaPlayerWrapper.AbstractMediaPlayerStateListener() {

            @Override
            public void completionCallback(MediaPlayer mp) {
                mPlayerViewAdvertising.setVisibility(View.GONE);
                mPlayerManagerAdvertising.stopVideo(mPlayerViewAdvertising);

                mPlayerView.setVisibility(View.VISIBLE);
                mPlayerManager.startVideo(mPlayerView);
            }

            @Override
            public void videoStartCallback() {

            }

            @Override
            public void videoStoppedCallback() {

            }

            @Override
            public void videoPauseCallback() {

            }
        });

        // 横竖屏切换
        ScreenOrientationUtil.getInstance();
        try {
            openAutoOrientation = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        //屏幕方向变化handler
        enterActivityOfLandscape();
        /* 屏幕方向变化handler */
        ChangeOrientationHandler changeOrientationHandler = new ChangeOrientationHandler(this);
        /* 屏幕方向变化传感器管理 */
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        /* 屏幕方向变化传感器 */
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        /* 屏幕方向变化Listener */
        OrientationSensorListener orientationSensorListener = new OrientationSensorListener(changeOrientationHandler);
        sensorManager.registerListener(orientationSensorListener, sensor, SensorManager.SENSOR_DELAY_UI);
        //屏幕旋转handler 延迟执行
        mOrientationHandler = new OrientationHandler(this);

        addListener();
        mPlayerView.setVisibility(View.VISIBLE);
        mPlayerViewAdvertising.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        if (isPause) {
            mPlayerManager.startVideo(mPlayerView);
            isPause = false;
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        mPlayerManager.pauseVideo(mPlayerView);
        isPause = true;
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        // 如果是横屏，则设置为竖屏
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (height == 0) {
            height = mRelativeLayout.getHeight();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                //横屏
                Window window = getWindow();
                WindowManager.LayoutParams param = window.getAttributes();
                param.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
                window.setAttributes(param);
                mLayoutParamsVideoContent = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                mRelativeLayout.setLayoutParams(mLayoutParamsVideoContent);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                //竖屏
                mLayoutParamsVideoContent = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, height);
                mRelativeLayout.setLayoutParams(mLayoutParamsVideoContent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mPlayerManager.stopVideo(mPlayerView);
        mPlayerManagerAdvertising.stopVideo(mPlayerViewAdvertising);
        MediaLog.getInstance().close();
        super.onDestroy();
    }

    private void addListener() {
        mButtonPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPlayerView.setVisibility(View.VISIBLE);
                mPlayerViewAdvertising.setVisibility(View.GONE);
                String s = mEditTextUrl.getText().toString();
                if (!TextUtils.isEmpty(s)) {
                    url = s;
                }
                VideoPlayerBean bean = new VideoPlayerBean();
                bean.setUrl(url);
                bean.setPosition(0);
                bean.setPlayer(true);
                bean.setSeekTo(true);
                mPlayerManager.playVideo(mPlayerView, bean);
            }
        });
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerManager.startVideo(mPlayerView);
            }
        });
        mButtonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerManager.pauseVideo(mPlayerView);
            }
        });
        mButtonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerManager.stopVideo(mPlayerView);
            }
        });
        mButtonSeekTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayerManager.seekToVideo(mPlayerView, new VideoPlayerBean(mDuration / 2 - 5000));
            }
        });
        mButtonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScreenOrientation();
            }
        });
        mButtonJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(PlayerActivity.this, TouchTestActivity.class);
                startActivity(intent);
            }
        });
        mButtonAdvertisingPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoPlayerBean bean = new VideoPlayerBean();
                bean.setPlayer(true);
                bean.setUrl(urlAdvertising);
                mPlayerManagerAdvertising.playVideo(mPlayerViewAdvertising, bean);
            }
        });
        mButtonSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = mEditTextSpeed.getText().toString();
                if (TextUtils.isEmpty(s)) {
                    mPlayerView.setSpeed(1.0f);
                } else {
                    Float aFloat = Float.valueOf(s);
                    mPlayerView.setSpeed(aFloat);
                }
            }
        });
    }

    /**
     * 更改屏幕方向
     */
    private void changeScreenOrientation() {
        switch (this.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                //横屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                //竖屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            default:
                break;
        }
        if (openAutoOrientation == 1) {
            mOrientationHandler.sendEmptyMessageDelayed(SCREEN_ORIENTATION_EVENT_DELAY_CARRIED_OUT_MESSAGE, 3000);
        }
    }

    /**
     * 用户直接横屏进入终端页需要调用这个方法（不然会有横屏却不是充满整个屏幕的bug）
     */
    public void enterActivityOfLandscape() {
        if (openAutoOrientation == 1) {
            //横屏
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Window window = getWindow();
                WindowManager.LayoutParams param = window.getAttributes();
                param.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
                window.setAttributes(param);
                mLayoutParamsVideoContent = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mRelativeLayout.setLayoutParams(mLayoutParamsVideoContent);
            }
        }
    }

    @Override
    public void onRefreshUIBySeekManagerCallback() {

    }

    @Override
    public void onStartTrackingTouchCallback(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouchCallback(SeekBar seekBar) {

    }

    @Override
    public void onProgressChangedCallback(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onSuperTouch(View v, MotionEvent event) {

    }

    @Override
    public void onGestureDetectorByScreenLuminance(boolean screenType, int number, int percent) {

    }

    @Override
    public void onGestureDetectorByVolume(boolean volumeType, int number, int percent) {

    }

    @Override
    public void onGestureDetectorByProgress(boolean progressType, int number, int percent) {

    }

    @Override
    public boolean onGestureDetectorSetProgress(int position) {
        return false;
    }

    @Override
    public void onGestureDetectorCancel(int gestureType) {

    }

    /**
     * 旋转屏幕方向handle
     */
    private static class OrientationHandler extends Handler {

        private WeakReference<PlayerActivity> mWeakReference;

        OrientationHandler(PlayerActivity videoTerminalActivity) {
            mWeakReference = new WeakReference<>(videoTerminalActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayerActivity t = mWeakReference.get();
            if (t != null) {
                switch (msg.what) {
                    case SCREEN_ORIENTATION_EVENT_DELAY_CARRIED_OUT_MESSAGE:
                        t.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private class AbstractMediaPlayerStateListener extends MediaPlayerWrapper.AbstractMediaPlayerStateListener {
        @Override
        public void errorCallback(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, "onErrorCallback, what: " + what + " extra: " + extra);
        }

        @Override
        public void completionCallback(MediaPlayer mp) {

        }

        @Override
        public void videoStartCallback() {
            mPlayerView.setGestureDetectorValid(true);
        }

        @Override
        public void videoStoppedCallback() {

        }

        @Override
        public void videoPauseCallback() {

        }

        @Override
        public void currentPositionCallback(int position) {
            mSeekBarManager.setProgress(position);
            if (!isShowAdvertising && position >= (mDuration / 2) ) {
                isShowAdvertising = true;
                mPlayerManager.pauseVideo(mPlayerView);
                mPlayerView.setVisibility(View.GONE);

                mPlayerViewAdvertising.setVisibility(View.VISIBLE);
                VideoPlayerBean bean = new VideoPlayerBean();
                bean.setPlayer(true);
                bean.setUrl(urlAdvertising);
                mPlayerManagerAdvertising.playVideo(mPlayerViewAdvertising, bean);
            }
        }

        @Override
        public void durationCallback(int duration) {
            mDuration = duration;
            mSeekBarManager.setMax(duration);
        }
    }
}
