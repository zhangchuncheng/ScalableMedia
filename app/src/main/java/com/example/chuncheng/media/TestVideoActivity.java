package com.example.chuncheng.media;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.chuncheng.sample.media.video.entity.VideoPlayerBean;
import com.chuncheng.sample.media.video.manager.SeekBarManager;
import com.chuncheng.sample.media.video.ui.VideoPlayerControlView;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;
import com.chuncheng.sample.media.video.ui.wrapper.MediaPlayerWrapper;

import java.lang.ref.WeakReference;

public class TestVideoActivity extends AppCompatActivity implements
        VideoPlayerControlView.PlayViewOnClickListener,
        TextureView.SurfaceTextureListener,
        MediaPlayerWrapper.MediaPlayerStateListener,
        VideoPlayerView.VideoGestureDetectorUiOperationListener,
        SeekBarManager.SeekBarChangeListener {

    private static final int SCREEN_ORIENTATION_EVENT_DELAY_CARRIED_OUT_MESSAGE = 2;//延迟屏幕重力感应message

    /** 当前activity是否已暂停 */
    private boolean isPause;
    private String url;
    /** 是否已打开自动选择屏幕 0-未打开 */
    private int openAutoOrientation;
    /** 屏幕重力感应handler */
    private OrientationHandler mOrientationHandler;
    /** 屏幕方向变化handler */
    private ChangeOrientationHandler mChangeOrientationHandler;
    /** 屏幕方向变化Listener */
    private OrientationSensorListener mOrientationSensorListener;
    /** 屏幕方向变化传感器管理 */
    private SensorManager mSensorManager;
    /** 屏幕方向变化传感器 */
    private Sensor mSensor;
    private VideoPlayerControlView mPlayerControlView;
    /** 视频播放器区域布局 */
    private RelativeLayout mRelativeLayout;
    /** 视频布局layoutParams */
    private RelativeLayout.LayoutParams mLayoutParamsVideoContent;
    /** 视频实体类 */
    private VideoPlayerBean mVideoPlayerBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player_test_b);
        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("url");
        mRelativeLayout = (RelativeLayout) findViewById(R.id.rl_video);
        mPlayerControlView = (VideoPlayerControlView) findViewById(R.id.player);
        // 横竖屏切换
        ScreenOrientationUtil.getInstance();
        try {
            openAutoOrientation = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        //屏幕方向变化handler
        enterActivityOfLandscape();
        mChangeOrientationHandler = new ChangeOrientationHandler(this);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mOrientationSensorListener = new OrientationSensorListener(mChangeOrientationHandler);
        mSensorManager.registerListener(mOrientationSensorListener, mSensor, SensorManager.SENSOR_DELAY_UI);
        //屏幕旋转handler 延迟执行
        mOrientationHandler = new OrientationHandler(this);

        mPlayerControlView.setPlayViewOnClickListener(this);
        mPlayerControlView.setSurfaceTextureListener(this);
        mPlayerControlView.setMediaPlayerStateListener(this);
        mPlayerControlView.setVideoGestureDetectorUIOperationListener(this);
        mPlayerControlView.setSeekBarChangeCallback(this);
        mPlayerControlView.setGestureDetectorValid(true);
        mVideoPlayerBean = new VideoPlayerBean();
        mVideoPlayerBean.setPosition(0);
        mVideoPlayerBean.setUrl(url);
        //mVideoPlayerBean.setUrl("https://vpro01.medplus.net/1469691246_1280_720.mp4");
        mPlayerControlView.playVideo(mVideoPlayerBean);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPause) {
            //获取播放位置
            isPause = false;
            //mPlayerControlView.playVideo(mVideoPlayerBean);
        }
    }

    @Override
    protected void onPause() {
        isPause = true;
        if (!TextUtils.isEmpty(mVideoPlayerBean.getUrl())) {
            //mPlayerControlView.stopVideo();
        }
        mPlayerControlView.setPlayPrepared(false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (!TextUtils.isEmpty(mVideoPlayerBean.getUrl())) {
            mPlayerControlView.stopVideo();
        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                //横屏
                mPlayerControlView.updateVideoScreenSmallUI();
                Window window = getWindow();
                WindowManager.LayoutParams param = window.getAttributes();
                param.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE;
                window.setAttributes(param);
                mLayoutParamsVideoContent = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mRelativeLayout.setLayoutParams(mLayoutParamsVideoContent);
                break;
            case Configuration.ORIENTATION_PORTRAIT:
                //竖屏
                mPlayerControlView.updateVideoScreenFullUI();
                mLayoutParamsVideoContent = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mRelativeLayout.setLayoutParams(mLayoutParamsVideoContent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {// 如果是横屏，则设置为竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 用户直接横屏进入终端页需要调用这个方法（不然会有横屏却不是充满整个屏幕的bug）
     */
    public void enterActivityOfLandscape() {
        if (openAutoOrientation == 1) {
            //横屏
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mPlayerControlView.updateVideoScreenSmallUI();
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

    /*-------------------------------PlayViewOnClickListener-开始---------------------------------*/
    @Override
    public void screenChangeOnClick() {
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

    @Override
    public void screenOnClick() {

    }

    /*-------------------------------PlayViewOnClickListener-结束---------------------------------*/

    /*-------------------------------surfaceTextureListener-开始----------------------------------*/
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    /*-------------------------------surfaceTextureListener-结束----------------------------------*/

    /*-------------------------------MediaPlayerWrapper.MediaPlayerStateListener------------------*/
    @Override
    public void onErrorCallback(MediaPlayer mp, int what, int extra) {

    }

    @Override
    public void onInfoCallback(MediaPlayer mp, int what, int extra) {

    }

    @Override
    public void onBufferingUpdateCallback(MediaPlayer mp, int percent) {

    }

    @Override
    public void onCompletionCallback(MediaPlayer mp) {

    }

    @Override
    public void onSeekCompleteCallback(MediaPlayer mp) {

    }

    @Override
    public void onVideoSizeChangedCallback(MediaPlayer mp, int width, int height) {

    }

    @Override
    public void onPreparedCallback(MediaPlayer mp) {

    }

    @Override
    public void onVideoStartCallback() {

    }

    @Override
    public void onVideoStoppedCallback() {

    }

    @Override
    public void onVideoPauseCallback() {

    }

    @Override
    public void onCurrentPositionCallback(int position) {

    }

    @Override
    public void onDurationCallback(int duration) {

    }

    /*-------------------------------mediaPlayer 状态操作回调-结束---------------------------------*/

    @Override
    public void onSuperTouch(View v, MotionEvent event) {

    }

    /*-------------------------------手势回调-开始-------------------------------------------------*/
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

    /*-------------------------------手势回调-结束-------------------------------------------------*/

    /*-------------------------------SeekBarChangeCallback-开始-----------------------------------*/
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

    /*-------------------------------SeekBarChangeCallback-结束-----------------------------------*/

    /*-------------------------------屏幕旋转操作 handler-开始-------------------------------------*/

    private static class OrientationHandler extends Handler {

        private WeakReference<TestVideoActivity> mWeakReference;

        OrientationHandler(TestVideoActivity videoTerminalActivity) {
            mWeakReference = new WeakReference<>(videoTerminalActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            TestVideoActivity t = mWeakReference.get();
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

    /*-------------------------------屏幕旋转操作 handler-结束-------------------------------------*/
}
