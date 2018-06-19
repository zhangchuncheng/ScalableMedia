package com.chuncheng.sample.media.video.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.SeekBar;

import java.lang.ref.WeakReference;

/**
 * Description:视频播放器进度条管理类
 *
 * @author: zhangchuncheng
 * @date: 2017/2/15
 */

public class SeekBarManager implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "VideoPlayerTest";
    /** 发起播放视频更新进度传message值 */
    public static final int UI_EVENT_UPDATE_CURRENT_POSITION = 1;

    private Context mContext;
    private SeekBar mSeekBar;
    private SeekBarChangeListener mSeekBarChangeCallback;
    private Handler mHandler;

    /**
     * 初始化进度条管理类
     *
     * @param context          当前Context
     * @param seekBar          seekBar控件
     * @param operatorCallback 回调接口
     */
    public SeekBarManager(Context context, SeekBar seekBar, SeekBarChangeListener operatorCallback) {
        mContext = context;
        mSeekBar = seekBar;
        mSeekBarChangeCallback = operatorCallback;
        mSeekBar.setOnSeekBarChangeListener(this);
        initHandler();
    }

    /**
     * 初始化进度条管理类
     * 默认设置当前进度条的最大值、当前进度值和第二进度值
     *
     * @param context           当期Context
     * @param seekBar           seekBar控件
     * @param max               最大值
     * @param currentProgress   当前进度值
     * @param secondaryProgress 第二进度值
     * @param operatorCallback  回调接口
     */
    public SeekBarManager(Context context, SeekBar seekBar, int max, int currentProgress,
                          int secondaryProgress, SeekBarChangeListener operatorCallback) {
        mContext = context;
        mSeekBar = seekBar;
        mSeekBarChangeCallback = operatorCallback;
        mSeekBar.setOnSeekBarChangeListener(this);
        setMax(max);
        setProgress(currentProgress);
        setSecondaryProgress(secondaryProgress);
        initHandler();
    }

    /**
     * 初始化handler
     */
    private void initHandler() {
        mHandler = new UIHandler<>(mContext, mSeekBarChangeCallback);
    }

    /**
     * 发送消息 默认200毫秒后执行
     *
     * @param what 消息名称
     */
    public void sendHandler(int what) {
        sendHandler(what, 200);
    }

    /**
     * 发送一条在指定时间后执行的消息
     *
     * @param what        消息名称
     * @param delayMillis 执行时间
     */
    public void sendHandler(int what, long delayMillis) {
        mHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    /**
     * 移除Handler中what的消息
     *
     * @param what 消息名称
     */
    public void removerHandler(int what) {
        mHandler.removeMessages(what);
    }

    /**
     * 获取当前进度
     *
     * @return 进度值
     */
    public int getProgress() {
        return mSeekBar.getProgress();
    }

    /**
     * 获取第二进度条的当前进度
     *
     * @return 进度值
     */
    public int getSecondaryProgress() {
        return mSeekBar.getSecondaryProgress();
    }

    /**
     * 设置最大值
     *
     * @param max 最大值
     */
    public void setMax(int max) {
        mSeekBar.setMax(max);
    }

    /**
     * 设置当前进度
     *
     * @param current 当前进度值
     */
    public void setProgress(int current) {
        mSeekBar.setProgress(current);
    }

    /**
     * 设置第二进度
     *
     * @param secondary 第二进度值
     */
    public void setSecondaryProgress(int secondary) {
        mSeekBar.setSecondaryProgress(secondary);
    }


    /**
     * 进度级别已更改的通知。
     * 客户端可以使用fromUser参数来区分用户发起的更改与以编程方式发生的更改。
     *
     * @param seekBar  SeekBar
     * @param progress 当前进度级别。
     *                 这将在0..max的范围内，其中max被设置setMax(int)。
     *                 （max的默认值为100.）
     * @param fromUser 如果进度更改由用户启动，则为true。
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mSeekBarChangeCallback.onProgressChangedCallback(seekBar, progress, fromUser);
    }

    /**
     * 用户已启动触摸手势的通知。
     * 客户端可能想使用此禁用推进seekBar。
     *
     * @param seekBar SeekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mSeekBarChangeCallback.onStartTrackingTouchCallback(seekBar);
    }

    /**
     * 通知用户已完成触摸手势。
     * 客户端可能想使用它来重新启用提前查找栏。
     *
     * @param seekBar SeekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mSeekBarChangeCallback.onStopTrackingTouchCallback(seekBar);
    }

    /**
     * seekBar操作回调
     * 当进度级别更改时通知客户端的回调。
     * 这包括由用户通过触摸手势或箭头键/轨迹球发起的更改以及以编程方式启动的更改。
     */
    public interface SeekBarChangeListener {
        void onRefreshUIBySeekManagerCallback();

        void onStartTrackingTouchCallback(SeekBar seekBar);

        void onStopTrackingTouchCallback(SeekBar seekBar);

        void onProgressChangedCallback(SeekBar seekBar, int progress, boolean fromUser);
    }

    public static abstract class AbstractSeekBarChangeListener implements SeekBarChangeListener {
        @Override
        public void onRefreshUIBySeekManagerCallback() {
            refreshUIBySeekManagerCallback();
        }

        @Override
        public void onStartTrackingTouchCallback(SeekBar seekBar) {
            startTrackingTouchCallback(seekBar);
        }

        @Override
        public void onStopTrackingTouchCallback(SeekBar seekBar) {
            stopTrackingTouchCallback(seekBar);
        }

        @Override
        public void onProgressChangedCallback(SeekBar seekBar, int progress, boolean fromUser) {
            progressChangedCallback(seekBar, progress, fromUser);
        }

        public void refreshUIBySeekManagerCallback() {

        }

        public void startTrackingTouchCallback(SeekBar seekBar) {

        }

        public void stopTrackingTouchCallback(SeekBar seekBar) {

        }

        public void progressChangedCallback(SeekBar seekBar, int progress, boolean fromUser) {

        }
    }

    private static class UIHandler<T> extends Handler {

        private WeakReference<T> mWeakReference;
        private SeekBarChangeListener mOperatorCallback;

        UIHandler(T t, SeekBarChangeListener operatorCallback) {
            mWeakReference = new WeakReference<>(t);
            mOperatorCallback = operatorCallback;

        }

        @Override
        public void handleMessage(Message msg) {
            T t = mWeakReference.get();
            if (t != null) {
                switch (msg.what) {
                    case UI_EVENT_UPDATE_CURRENT_POSITION:
                        mOperatorCallback.onRefreshUIBySeekManagerCallback();
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
