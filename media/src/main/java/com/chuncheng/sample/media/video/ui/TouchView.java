package com.chuncheng.sample.media.video.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.chuncheng.sample.media.audio.VoiceManager;
import com.chuncheng.sample.media.utils.ScreenUtils;

/**
 * Description:手指触摸view
 * 1、上下滑动处理
 * 2、左右滑动处理
 * 3、点击处理
 *
 * @author: zhangchuncheng
 * @date: 2017/5/22
 */

public class TouchView extends View implements View.OnTouchListener {
    private static final String TAG = "TouchView";

    /** 默认空操作 */
    private static final int GESTURE_MODIFY_NONE = 0;
    /** 音量 */
    private static final int GESTURE_MODIFY_VOLUME = 1;
    /** 亮度 */
    private static final int GESTURE_MODIFY_BRIGHT = 2;
    /** 快进 */
    private static final int GESTURE_MODIFY_PROGRESS = 3;
    /** 区别当前手势是亮度、音量 还是快进 */
    private int gestureFlag = GESTURE_MODIFY_NONE;
    /** 每次触摸屏幕后，第一次scroll的标志 */
    private boolean firstScroll;
    /** 协调音量滑动时的步长，避免每次滑动都改变，导致改变过快 */
    private static final float STEP_X = 0.1f;
    /** 协调快进滑动时的步长，避免每次滑动都改变，导致改变过快 */
    private static final float STEP_Y = 0.1f;
    /** 亮度位置出点起始值 */
    private float recordLight = 0;
    /** 系统亮度值 */
    private int screenBrightness = 0;
    /** 最大音量 */
    private int maxVolume;
    /** 当前音量 */
    private int currentVolume;
    /** 快进到的时间点位置 */
    private int speedPosition;
    /** 当前播放时间 */
    private int currentPosition;
    /** 视频总长度 */
    private int duration;
    /** 是否启用手势 */
    private boolean isGestureDetectorValid;
    /** 音量管理类 */
    private VoiceManager mVoiceManager;
    /** 手势操作类 */
    private GestureDetector mGestureDetector;
    /** 手势相关回调 */
    private VideoGestureDetectorUiOperationListener mUIOperationListener;

    public boolean isGestureDetectorValid() {
        return isGestureDetectorValid;
    }

    public void setGestureDetectorValid(boolean gestureDetectorValid) {
        isGestureDetectorValid = gestureDetectorValid;
    }

    public void setUIOperationListener(VideoGestureDetectorUiOperationListener uiOperationListener) {
        mUIOperationListener = uiOperationListener;
    }

    public TouchView(Context context) {
        super(context);
        initView();
    }

    public TouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TouchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        super.setOnTouchListener(this);
        super.setLongClickable(true);
        mVoiceManager = new VoiceManager(getContext());
        //初始化触摸处理类
        mGestureDetector = new GestureDetector(getContext(), new VideoPlayGestureDetector());
        //启用长按设置
        mGestureDetector.setIsLongpressEnabled(true);
        setGestureDetectorValid(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (isGestureDetectorValid) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                currentPosition = mUIOperationListener.getCurrentPosition();
                duration = mUIOperationListener.getDuration();
                currentVolume = mVoiceManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                maxVolume = mVoiceManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if (screenBrightness == 0) {
                    screenBrightness = ScreenUtils.getScreenBrightness(getContext());
                }
                recordLight = 0;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                switch (gestureFlag) {
                    case GESTURE_MODIFY_PROGRESS:
                        break;
                    default:
                        break;
                }
                if (mUIOperationListener != null) {
                    mUIOperationListener.onGestureDetectorCancel(gestureFlag);
                }
                gestureFlag = GESTURE_MODIFY_NONE;
            }
            return mGestureDetector.onTouchEvent(event);
        } else {
            return false;
        }
    }


    /** 视频播放view手势操作控制类 */
    private class VideoPlayGestureDetector extends GestureDetector.SimpleOnGestureListener {
        /**
         * 当手指在屏幕滑动时的回调函数
         *
         * @param e1        初始值 触摸到屏幕的事件
         * @param e2        移动值 移动事件
         * @param distanceX x轴移动的距离
         * @param distanceY y轴移动的距离
         * @return 如果被消耗着为true，否则为false
         */
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float firstX = e1.getX(), firstY = e1.getY();
            float laterX = e2.getX(), laterY = e2.getY();
            //以触摸屏幕后第一次滑动为标准，避免在屏幕上操作切换混乱
            //横向的距离变化大则调整进度，纵向的变化大则再次验证是左半部分还是右半部分，调整亮度或音量。
            if (firstScroll) {
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    //x轴上滑动
                    gestureFlag = GESTURE_MODIFY_PROGRESS;
                } else if (Math.abs(distanceX) < Math.abs(distanceY)) {
                    //y轴上滑动
                    if (firstX > getWidth() * 3.0 / 5) {
                        //音量
                        gestureFlag = GESTURE_MODIFY_VOLUME;
                    } else if (firstX < getWidth() * 2.0 / 5) {
                        //亮度
                        gestureFlag = GESTURE_MODIFY_BRIGHT;
                    }
                }
                firstScroll = false;
            }
            //如果每次触摸屏幕后第一次scroll是调节进度，直到离开屏幕执行下一次操作
            switch (gestureFlag) {
                case GESTURE_MODIFY_NONE:
                    break;
                case GESTURE_MODIFY_BRIGHT:
                    //亮度
                    boolean screenType = false;
                    if (distanceY > STEP_Y) {
                        //增加亮度
                        screenType = true;
                    } else if (distanceX < -STEP_Y) {
                        //减小亮度
                        screenType = false;
                    }
                    if (recordLight != 0) {
                        firstY = recordLight;
                    }
                    screenBrightness = (int) ((float) screenBrightness + (firstY - laterY) / 30);
                    if (screenBrightness > 255) {
                        screenBrightness = 255;
                        if (laterY < firstY) {
                            recordLight = laterY;
                        }
                    }
                    if (screenBrightness < 3) {
                        screenBrightness = 3;
                        if (laterY > firstY) {
                            recordLight = laterY;
                        }
                    }
                    ScreenUtils.setLight(getContext(), screenBrightness);
                    if (mUIOperationListener != null) {
                        mUIOperationListener.onGestureDetectorByScreenLuminance(screenType, screenBrightness, (int) (screenBrightness / 2.55));
                    }
                    break;
                case GESTURE_MODIFY_VOLUME:
                    //音量
                    boolean volumeType = false;
                    if (distanceY > STEP_Y) {
                        //增加音量
                        volumeType = true;
                    } else if (distanceX < -STEP_Y) {
                        //减小音量
                        volumeType = false;
                    }
                    float volume = 0;
                    float volumeDistance;
                    if (firstY - laterY > 0) {
                        //增加音量
                        volumeDistance = maxVolume * (firstY - laterY) / getHeight();
                        volume = currentVolume + volumeDistance > maxVolume ? maxVolume : currentVolume + volumeDistance;
                        /*Log.e(TAG, "增加:" + volumeDistance + "-currentVolume:" + currentVolume + "-firstY:" + firstY + "-laterY:" + laterY + "-volume:" + volume);*/
                    } else if (laterY - firstY > 0) {
                        //减小音量
                        volumeDistance = maxVolume * (laterY - firstY) / getHeight();
                        volume = currentVolume - volumeDistance < 0 ? 0 : currentVolume - volumeDistance;
                        /*Log.e(TAG, "减小:" + volumeDistance + "-currentVolume:" + currentVolume + "-firstY:" + firstY + "-laterY:" + laterY + "-volume:" + volume);*/
                    }
                    mVoiceManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    if (mUIOperationListener != null) {
                        mUIOperationListener.onGestureDetectorByVolume(volumeType, (int) volume, (int) ((volume * 100) / maxVolume));
                    }
                    break;
                case GESTURE_MODIFY_PROGRESS:
                    //进度
                    //验证是否是快进还是快退
                    boolean progressType = false;
                    if (distanceX > STEP_X) {
                        //快退
                        progressType = false;

                    } else if (distanceX < -STEP_X) {
                        //快进
                        progressType = true;

                    }
                    //实际播放时间是前进还是后退
                    /* 播放时间移动的距离 */
                    float playDistance;
                    if (firstX - laterX > 0) {
                        //播放后退距离
                        /*Log.e(TAG, "后退-x1:" + currentPosition + "-z:" + duration + "-y:" + (firstX - laterX) + "-h:" + viewWidth + "-x:" + (duration * (firstX - laterX) / viewWidth) + "-playDistance:" + playDistance + "-speedPosition:" + speedPosition);*/
                        playDistance = duration * (firstX - laterX) / getWidth();
                        speedPosition = (int) (currentPosition - playDistance) < 0 ? 0 : (int) (currentPosition - playDistance);
                    } else if (laterX - firstX > 0) {
                        //播放前进距离
                        /*Log.e(TAG, "前进-x1:" + currentPosition + "-z:" + duration + "-y:" + (laterX - firstX) + "-h:" + viewWidth + "-x:" + (duration * (laterX - firstX) / viewWidth) + "-playDistance:" + playDistance + "-speedPosition" + speedPosition);*/
                        playDistance = duration * (laterX - firstX) / getWidth();
                        speedPosition = (int) (currentPosition + playDistance) > duration ? duration - 1 : (int) (currentPosition + playDistance);
                    }
                    if (mUIOperationListener != null) {
                        mUIOperationListener.onGestureDetectorByProgress(progressType, speedPosition, 0);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // 设定是触摸屏幕后第一次scroll的标志
            firstScroll = true;
            return false;
        }
    }

    /**
     * 手势相关操作回调
     */
    public interface VideoGestureDetectorUiOperationListener {

        /**
         * 屏幕亮度调节回调
         *
         * @param screenType true为增加否则为减小
         * @param number     当前数值
         * @param percent    百分比
         */
        void onGestureDetectorByScreenLuminance(boolean screenType, int number, int percent);

        /**
         * 音量调节回调
         *
         * @param volumeType true为增加否则为减小
         * @param number     当前数值
         * @param percent    百分比
         */
        void onGestureDetectorByVolume(boolean volumeType, int number, int percent);

        /**
         * 进度调节回调
         *
         * @param progressType true为增加否则为减小
         * @param number       当前数值
         * @param percent      百分比
         */
        void onGestureDetectorByProgress(boolean progressType, int number, int percent);

        /**
         * 手势结束
         *
         * @param gestureType 手势类型
         */
        void onGestureDetectorCancel(int gestureType);

        /**
         * 获取总长度
         * @return 进度条的总长度
         */
        int getDuration();

        /**
         * 获取当前进度的数值
         * @return 进度条的当前数值
         */
        int getCurrentPosition();
    }
}
