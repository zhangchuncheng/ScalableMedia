package com.chuncheng.sample.media.video.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chuncheng.sample.media.R;
import com.chuncheng.sample.media.video.entity.VideoPlayerBean;
import com.chuncheng.sample.media.video.manager.SeekBarManager;
import com.chuncheng.sample.media.video.manager.SingleVideoPlayerManager;
import com.chuncheng.sample.media.video.manager.VideoPlayerManager;
import com.chuncheng.sample.media.video.ui.wrapper.MediaPlayerWrapper;

/**
 * Description: 继承至FrameLayout 组合控件
 * 1.使用VideoPlayerView和seekBar组合
 * 2.需添加横竖屏按钮点击监听
 *
 * @author: zhangchuncheng
 * @date: 2017/3/21
 */

public class VideoPlayerControlView extends FrameLayout implements
        TextureView.SurfaceTextureListener,
        MediaPlayerWrapper.MediaPlayerStateListener,
        VideoPlayerView.VideoGestureDetectorUiOperationListener,
        SeekBarManager.SeekBarChangeListener {
    private static final String TAG = "VideoPlayerControlView";
    private static final int CURRENT_PLAY_STATE_START = 1;
    private static final int CURRENT_PLAY_STATE_PAUSE = 2;
    private static final int CURRENT_PLAY_STATE_STOP = 3;
    private static final int CURRENT_PLAY_STATE_COMPLETION = 4;
    private static final int CURRENT_PLAY_STATE_MOBILE = 5;
    private Context mContext;
    /** 当前进度条显示状态 true-显示 false-隐藏 */
    private boolean isSeekBarShowState = true;
    /** 当前视频是否已准备完成 */
    private boolean isPlayPrepared;
    /** 是否启用手势 */
    private boolean isGestureDetectorValid;
    /** 当前视频播放状态 1-播放 2-暂停 3-停止 4-播放完成 */
    private int currentPlayState;
    /** 当前播放时间位置 */
    private int currentPosition = 0;
    /** 当前视频总时长 */
    private int currentVideoDuration;
    /** 视频view */
    private VideoPlayerView mPlayerView;
    /** 进度条布局 */
    private RelativeLayout mRelativeLayoutSeekBar;
    /** 视频按钮 */
    private ImageView mImageViewPlayer;
    /** 视频播放时间 */
    private TextView mTextViewPlayTime;
    /** 视频进度条 */
    private SeekBar mSeekBar;
    /** 视频剩余时间 */
    private TextView mTextViewSurplusTime;
    /** 屏幕旋转 */
    private ImageView mImageViewScreenChange;
    /** 视频播放器管理类 */
    private VideoPlayerManager mPlayerManager;
    /** 视频播放器进度条管理类 */
    private SeekBarManager mSeekBarManager;
    /** 旋转屏幕点击监听 */
    private PlayViewOnClickListener mPlayViewOnClickListener;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener;
    private MediaPlayerWrapper.MediaPlayerStateListener mMediaPlayerStateListener;
    private VideoPlayerView.VideoGestureDetectorUiOperationListener mVideoGestureDetectorUIOperationListener;
    private SeekBarManager.SeekBarChangeListener mSeekBarChangeCallback;

    public PlayViewOnClickListener getPlayViewOnClickListener() {
        return mPlayViewOnClickListener;
    }

    public void setPlayViewOnClickListener(PlayViewOnClickListener playViewOnClickListener) {
        mPlayViewOnClickListener = playViewOnClickListener;
    }

    public void setSeekBarChangeCallback(SeekBarManager.SeekBarChangeListener seekBarChangeCallback) {
        mSeekBarChangeCallback = seekBarChangeCallback;
    }

    public void setVideoGestureDetectorUIOperationListener(VideoPlayerView.VideoGestureDetectorUiOperationListener videoGestureDetectorUiOperationListener) {
        mVideoGestureDetectorUIOperationListener = videoGestureDetectorUiOperationListener;
    }

    public void setMediaPlayerStateListener(MediaPlayerWrapper.MediaPlayerStateListener mediaPlayerStateListener) {
        mMediaPlayerStateListener = mediaPlayerStateListener;
    }

    public void setSurfaceTextureListener(TextureView.SurfaceTextureListener surfaceTextureListener) {
        mSurfaceTextureListener = surfaceTextureListener;
    }

    public boolean isPlayPrepared() {
        return isPlayPrepared;
    }

    public void setPlayPrepared(boolean playPrepared) {
        isPlayPrepared = playPrepared;
    }

    public boolean isGestureDetectorValid() {
        return isGestureDetectorValid;
    }

    public void setGestureDetectorValid(boolean gestureDetectorValid) {
        isGestureDetectorValid = gestureDetectorValid;
    }

    public VideoPlayerView getPlayerView() {
        return mPlayerView;
    }

    public VideoPlayerControlView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public VideoPlayerControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public VideoPlayerControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayerControlView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    private void init() {
        initView();
        addListener();
        initPlay();
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_contorol_player_video_layout, this);
        mPlayerView = (VideoPlayerView) view.findViewById(R.id.vpv_scalable_media_player);
        mRelativeLayoutSeekBar = (RelativeLayout) view.findViewById(R.id.rl_scalable_media_seek_bar);
        mImageViewPlayer = (ImageView) view.findViewById(R.id.iv_scalable_media_play);
        mTextViewPlayTime = (TextView) view.findViewById(R.id.tv_scalable_media_play_time);
        mSeekBar = (SeekBar) view.findViewById(R.id.sb_scalable_media_seek_bar);
        mTextViewSurplusTime = (TextView) view.findViewById(R.id.tv_scalable_media_surplus_time);
        mImageViewScreenChange = (ImageView) view.findViewById(R.id.iv_scalable_media_screen_change);
    }

    private void addListener() {
        mImageViewPlayer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrPause();
            }
        });

        mImageViewScreenChange.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayViewOnClickListener.screenChangeOnClick();
            }
        });

        mPlayerView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isGestureDetectorValid) {
                    mPlayViewOnClickListener.screenOnClick();
                    displayAndHiddenSeekBar();
                }

            }
        });
    }

    /**
     * 初始化视频播放器
     */
    private void initPlay() {
        mPlayerView.setViewScaleType(OptimizeTextureView.SCALE_TYPE_DEFAULT);
        //设置常亮
        mPlayerView.setKeepScreenOn(true);
        //设置手势
        mPlayerView.setGestureDetectorValid(false);
        //设置手势回调
        mPlayerView.setUIOperationListener(this);
        //设置surfaceTexture状态回调
        mPlayerView.setSurfaceTextureListener(this);
        //设置mediaPlayer状态回调
        mPlayerView.addMediaPlayerListener(this);
        mPlayerManager = new SingleVideoPlayerManager();
        mSeekBar.setEnabled(false);
        mSeekBarManager = new SeekBarManager(mContext, mSeekBar, this);
    }

    /**
     * 开始播放视频
     *
     * @param videoPlayerBean 视频资源实体
     */
    public void playVideo(VideoPlayerBean videoPlayerBean) {
        updateVideoPlayUI();
        VideoPlayerBean mVideoPlayerBean = new VideoPlayerBean();
        mVideoPlayerBean.setPosition(videoPlayerBean.getPosition() + 1);
        mVideoPlayerBean.setUrl(videoPlayerBean.getUrl());
        mVideoPlayerBean.setPlayer(true);
        mPlayerManager.playVideo(mPlayerView, mVideoPlayerBean);
        //设置手势
        mPlayerView.setGestureDetectorValid(false);

    }

    /**
     * 销毁视频
     */
    public void stopVideo() {
        currentPlayState = CURRENT_PLAY_STATE_STOP;
        mPlayerManager.stopVideo(mPlayerView);
        isPlayPrepared = false;
    }

    /**
     * 暂停
     */
    public void pauseVideo() {
        currentPlayState = CURRENT_PLAY_STATE_PAUSE;
        updateVideoPauseUI();
        mPlayerManager.pauseVideo(mPlayerView);
    }

    /**
     * 从暂停状态继续播放
     */
    public void pauseToStartVideo() {
        currentPlayState = CURRENT_PLAY_STATE_START;
        updateVideoPlayUI();
        mPlayerManager.startVideo(mPlayerView);
    }

    /**
     * 获取当前播放位置
     *
     * @return 当前播放位置 单位毫秒
     */
    public int getCurrentPosition() {
        return mPlayerView.getCurrentPosition();
    }

    /**
     * 获取当前总时长
     *
     * @return 当前视频总时长 单位毫秒
     */
    public int getDuration() {
        return mPlayerView.getDuration();
    }

    /**
     * 完成至播放
     */
    private void playBackToStartVideo() {
        currentPlayState = CURRENT_PLAY_STATE_START;
        updateVideoPlayUI();
        mPlayerManager.startVideo(mPlayerView);
    }

    /**
     * 播放暂停
     */
    private void playOrPause() {
        switch (currentPlayState) {
            case CURRENT_PLAY_STATE_START:
                pauseVideo();
                break;
            case CURRENT_PLAY_STATE_PAUSE:
                pauseToStartVideo();
                break;
            case CURRENT_PLAY_STATE_COMPLETION:
                playBackToStartVideo();
                break;
            default:
                break;
        }
    }

    /**
     * 暂停时更新UI
     */
    private void updateVideoPauseUI() {
        mImageViewPlayer.setImageResource(R.drawable.svg_scalable_media_play);
    }

    /**
     * 播放时更新UI
     */
    private void updateVideoPlayUI() {
        mImageViewPlayer.setImageResource(R.drawable.svg_scalable_media_pause);
    }

    /**
     * 显示或隐藏进度条
     */
    public void displayAndHiddenSeekBar() {
        if (isSeekBarShowState) {
            //进度条为显示状态 点击后隐藏
            mRelativeLayoutSeekBar.setVisibility(View.GONE);
            isSeekBarShowState = false;
        } else {
            //进度条为隐藏状态 点击后显示
            mRelativeLayoutSeekBar.setVisibility(View.VISIBLE);
            isSeekBarShowState = true;
        }
    }

    /**
     * 显示进度条
     */
    public void dispalySeekBar() {
        mRelativeLayoutSeekBar.setVisibility(View.VISIBLE);
        isSeekBarShowState = true;
    }

    /**
     * 显示进度条
     */
    public void hiddenSeekBar() {
        mRelativeLayoutSeekBar.setVisibility(View.GONE);
        isSeekBarShowState = false;
    }

    /**
     * 更新屏幕横竖屏按钮 全屏按钮
     */
    public void updateVideoScreenFullUI() {
        mImageViewScreenChange.setImageResource(R.drawable.ic_scalable_media_play_full);
    }

    /**
     * 更新屏幕横竖屏按钮 竖屏按钮
     */
    public void updateVideoScreenSmallUI() {
        mImageViewScreenChange.setImageResource(R.drawable.ic_scalable_media_play_small);
    }

    /**
     * 进度条开始时间结束时间格式化
     *
     * @param currentView     当前时间view
     * @param durationView    总时间view
     * @param currentPosition 当前播放位置
     * @param duration        总时长
     */
    private void updatePlayTimeFormat(TextView currentView, TextView durationView,
                                      int currentPosition, int duration) {
        int currentSecond = Math.round(currentPosition / 1000);
        int durationSecond = Math.round(duration / 1000);
        updateTextViewWithTimeFormat(currentView, currentSecond, false);
        updateTextViewWithTimeFormat(durationView, durationSecond - currentSecond, true);
    }

    /**
     * 修改时间格式
     *
     * @param view   view
     * @param second 秒数
     * @param minus  是否相减
     */
    @SuppressLint("DefaultLocale")
    private void updateTextViewWithTimeFormat(TextView view, int second, boolean minus) {
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String strTemp;
        if (0 != hh) {
            strTemp = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            strTemp = String.format("%02d:%02d", mm, ss);
        }
        if (view != null) {
            if (minus) {
                String v = "-" + strTemp;
                view.setText(v);
            } else {
                view.setText(strTemp);
            }
        }
    }


    public interface PlayViewOnClickListener {
        /**
         * 要求改变屏幕方向点击监听回调
         */
        void screenChangeOnClick();

        /**
         * 屏幕点击事件 在没有设置手势监听的情况下才有效
         */
        void screenOnClick();
    }

    @Override
    public void onRefreshUIBySeekManagerCallback() {
        if (mSeekBarChangeCallback != null) {
            mSeekBarChangeCallback.onRefreshUIBySeekManagerCallback();
        }
    }

    @Override
    public void onStartTrackingTouchCallback(SeekBar seekBar) {
        if (mSeekBarChangeCallback != null) {
            mSeekBarChangeCallback.onStartTrackingTouchCallback(seekBar);
        }
    }

    @Override
    public void onStopTrackingTouchCallback(SeekBar seekBar) {
        VideoPlayerBean bean = new VideoPlayerBean();
        bean.setPosition(seekBar.getProgress());
        mPlayerManager.seekToVideo(mPlayerView, bean);
        if (mSeekBarChangeCallback != null) {
            mSeekBarChangeCallback.onStopTrackingTouchCallback(seekBar);
        }
    }

    @Override
    public void onProgressChangedCallback(SeekBar seekBar, int progress, boolean fromUser) {
        if (mSeekBarChangeCallback != null) {
            mSeekBarChangeCallback.onProgressChangedCallback(seekBar, progress, fromUser);
        }
    }

    @Override
    public void onSuperTouch(View v, MotionEvent event) {

    }

    @Override
    public void onGestureDetectorByScreenLuminance(boolean screenType, int number, int percent) {
        if (mVideoGestureDetectorUIOperationListener != null) {
            mVideoGestureDetectorUIOperationListener.onGestureDetectorByScreenLuminance(screenType, number, percent);
        }
    }

    @Override
    public void onGestureDetectorByVolume(boolean volumeType, int number, int percent) {
        if (mVideoGestureDetectorUIOperationListener != null) {
            mVideoGestureDetectorUIOperationListener.onGestureDetectorByVolume(volumeType, number, percent);
        }
    }

    @Override
    public void onGestureDetectorByProgress(boolean progressType, int number, int percent) {
        if (mVideoGestureDetectorUIOperationListener != null) {
            mVideoGestureDetectorUIOperationListener.onGestureDetectorByProgress(progressType, number, percent);
        }
    }

    @Override
    public boolean onGestureDetectorSetProgress(int position) {
        return false;
    }

    @Override
    public void onGestureDetectorCancel(int gestureType) {
        if (gestureType == 0) {
            displayAndHiddenSeekBar();
        }
        if (mVideoGestureDetectorUIOperationListener != null) {
            mVideoGestureDetectorUIOperationListener.onGestureDetectorCancel(gestureType);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTextureListener != null) {
            mSurfaceTextureListener.onSurfaceTextureAvailable(surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (mSurfaceTextureListener != null) {
            mSurfaceTextureListener.onSurfaceTextureSizeChanged(surface, width, height);
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mSurfaceTextureListener != null) {
            mSurfaceTextureListener.onSurfaceTextureDestroyed(surface);
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (mSurfaceTextureListener != null) {
            mSurfaceTextureListener.onSurfaceTextureUpdated(surface);
        }
    }

    @Override
    public void onErrorCallback(MediaPlayer mp, int what, int extra) {
        if (mMediaPlayerStateListener != null) {
            mMediaPlayerStateListener.onErrorCallback(mp, what, extra);
        }
    }

    @Override
    public void onInfoCallback(MediaPlayer mp, int what, int extra) {
        if (mMediaPlayerStateListener != null) {
            mMediaPlayerStateListener.onInfoCallback(mp, what, extra);
        }
    }

    @Override
    public void onBufferingUpdateCallback(MediaPlayer mp, int percent) {
        if (mMediaPlayerStateListener != null) {
            mMediaPlayerStateListener.onBufferingUpdateCallback(mp, percent);
        }
    }

    @Override
    public void onCompletionCallback(MediaPlayer mp) {
        updateVideoPauseUI();
        currentPlayState = CURRENT_PLAY_STATE_COMPLETION;
        currentPosition = 0;
        //重置播放器播放位置
        mPlayerManager.seekToVideo(mPlayerView, new VideoPlayerBean(0));
        mSeekBarManager.setProgress(0);
        if (mMediaPlayerStateListener != null) {
            mMediaPlayerStateListener.onCompletionCallback(mp);
        }
    }

    @Override
    public void onSeekCompleteCallback(MediaPlayer mp) {
        if (mMediaPlayerStateListener != null) {
            mMediaPlayerStateListener.onSeekCompleteCallback(mp);
        }
    }

    @Override
    public void onVideoSizeChangedCallback(MediaPlayer mp, int width, int height) {
        if (mMediaPlayerStateListener != null) {
            mMediaPlayerStateListener.onVideoSizeChangedCallback(mp, width, height);
        }
    }

    @Override
    public void onPreparedCallback(MediaPlayer mp) {
        if (mPlayerView != null) {
            isPlayPrepared = true;
            if (mMediaPlayerStateListener != null) {
                mMediaPlayerStateListener.onPreparedCallback(mp);
            }
        }

    }

    @Override
    public void onVideoStartCallback() {
        if (mPlayerView != null) {
            currentPlayState = CURRENT_PLAY_STATE_START;
            //设置手势
            mPlayerView.setGestureDetectorValid(isGestureDetectorValid);
            if (mMediaPlayerStateListener != null) {
                mMediaPlayerStateListener.onVideoStartCallback();
            }
        }
    }

    @Override
    public void onVideoStoppedCallback() {
        if (mMediaPlayerStateListener != null) {
            mMediaPlayerStateListener.onVideoStoppedCallback();
        }
    }

    @Override
    public void onVideoPauseCallback() {
        if (mMediaPlayerStateListener != null) {
            mMediaPlayerStateListener.onVideoPauseCallback();
        }
    }

    @Override
    public void onDurationCallback(int duration) {
        currentVideoDuration = duration;
        mSeekBar.setEnabled(true);
        mSeekBarManager.setMax(currentVideoDuration);
    }

    @Override
    public void onCurrentPositionCallback(int position) {
        currentPosition = position;
        mSeekBarManager.setProgress(position);
        updatePlayTimeFormat(mTextViewPlayTime, mTextViewSurplusTime, currentPosition, currentVideoDuration);

    }
}
