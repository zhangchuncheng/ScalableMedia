package com.chuncheng.sample.media.video.ui.wrapper;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.RequiresApi;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.chuncheng.sample.media.utils.log.MediaLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Description: MediaPlayer管理类
 * <p>
 * 封装公共操作功能
 *
 * @author: zhangchuncheng
 * @date: 2017/2/14
 */
public class MediaPlayerWrapper implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "MediaPlayerWrapper";
    /** error 标识 */
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_INIT = 96100;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_SET_DATA_SOURCE = 96101;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_SET_SURFACE = 96102;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_SEEK_TO = 96103;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_RESET = 96104;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_RELEASE = 96105;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_CLEAR_ALL = 96106;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_PREPARE = 96107;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_START = 96108;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_PAUSE = 96109;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_STOP = 96110;
    public static final int MEDIA_PLAYER_WRAPPER_ERROR_THREAD = 96111;

    /** 媒体管理 */
    private MediaPlayer mMediaPlayer;
    /** 状态监听 */
    private MediaPlayerStateListener mListener;
    /** 定时器 */
    private ScheduledFuture<?> mFuture;
    /** 定时器线程池 */
    private ScheduledExecutorService mScheduledExecutorService = Executors.newScheduledThreadPool(1);

    /** 主线程回调handler */
    private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());

    public void setListener(MediaPlayerStateListener listener) {
        mListener = listener;
    }

    /**
     * 捕捉异常
     * 设置数据源抛出的错误
     */
    private final Runnable mOnInitErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_INIT,
                        MEDIA_PLAYER_WRAPPER_ERROR_INIT);
            }
            setCurrentState(State.ERROR);
        }
    };

    public MediaPlayerWrapper(MediaPlayer mMediaPlayer) {
        MediaLog.getInstance().i(TAG, "constructor, main Looper " + Looper.getMainLooper() +
                        ", my Looper " + Looper.myLooper(),
                "MediaPlayerWrapper",
                "MediaPlayerWrapper");
        if (Looper.myLooper() != null) {
            MediaLog.getInstance().e(TAG, "myLooper not null, "
                            + "a bug in some MediaPlayer implementation cause that "
                            + "listeners are not called at all. Please use a thread without Looper",
                    "MediaPlayerWrapper",
                    "MediaPlayerWrapper");
            mMainThreadHandler.post(mOnInitErrorMainMessage);
            throw new RuntimeException("myLooper not null, " +
                    "a bug in some MediaPlayer implementation cause that listeners " +
                    "are not called at all. Please use a thread without Looper");
        }

        this.mMediaPlayer = mMediaPlayer;
        mState.set(State.IDLE);
        //媒体源准备播放时要调用的回调的接口
        mMediaPlayer.setOnPreparedListener(this);
        //当视频大小首次获取或更新时调用的回调的接口
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        //要调用的回调的接口定义，指示完成查找操作。
        mMediaPlayer.setOnSeekCompleteListener(this);
        //在媒体源的播放完成时调用的回调的接口
        mMediaPlayer.setOnCompletionListener(this);
        //网络媒体资源的缓冲状态监听
        mMediaPlayer.setOnBufferingUpdateListener(this);
        //要调用的回调的接口定义，以传达关于媒体或其回放的一些信息和/或警告。
        mMediaPlayer.setOnInfoListener(this);
        //错误监听
        mMediaPlayer.setOnErrorListener(this);
    }

    /** 当前状态值 AtomicReference处理线程并发 */
    private final AtomicReference<State> mState = new AtomicReference<>();

    /**
     * mediaPlayer 状态枚举
     */
    public enum State {
        /** mediaPlayer state */
        IDLE,
        INITIALIZED,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSED,
        STOPPED,
        PLAYBACK_COMPLETED,
        END,
        ERROR
    }

    /**
     * 获取当前状态
     *
     * @return 当前状态值
     */
    public State getCurrentState() {
        synchronized (mState) {
            return mState.get();
        }
    }

    /**
     * 设置当前状态
     */
    public void setCurrentState(State state) {
        synchronized (mState) {
            mState.set(state);
        }
    }

    /**
     * 捕捉异常
     * 设置数据源抛出的错误
     */
    private final Runnable mOnSetDataSourceErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_SET_DATA_SOURCE,
                        MEDIA_PLAYER_WRAPPER_ERROR_SET_DATA_SOURCE);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 设置要使用的数据源（文件路径或http / rtsp URL）。
     *
     * @param filePath 视频播放url
     */
    public void setDataSource(String filePath) {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "setDataSource");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", filePath " + filePath + ", mState " + mState,
                "MediaPlayerWrapper",
                "setDataSource");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Idle}*/
                    case IDLE:
                        mMediaPlayer.setDataSource(filePath);
                        mState.set(State.INITIALIZED);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            //写日志
            //IOException IllegalStateException
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", filePath " + filePath + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "setDataSource");
            mMainThreadHandler.post(mOnSetDataSourceErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "setDataSource");
    }

    /**
     * 设置要使用的数据源（文件路径或http / rtsp URL）。
     *
     * @param context context
     * @param uri     视频播放uri
     */
    public void setDataSource(Context context, Uri uri) {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "setDataSource");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", context is it null? " + (context == null)
                        + ", uri " + uri + ", mState " + mState,
                "MediaPlayerWrapper",
                "setDataSource");
        try {
            if (mMediaPlayer == null || context == null) {
                return;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Idle}*/
                    case IDLE:
                        mMediaPlayer.setDataSource(context, uri);
                        mState.set(State.INITIALIZED);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", uri " + uri + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "setDataSource");
            mMainThreadHandler.post(mOnSetDataSourceErrorMainMessage);
        }
    }

    /**
     * 捕捉异常
     * 设置显示view抛出的错误
     */
    private final Runnable mOnSetSurfaceErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_SET_SURFACE,
                        MEDIA_PLAYER_WRAPPER_ERROR_SET_SURFACE);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 设置SurfaceHolder用于显示媒体的视频部分。如果需要显示器或视频接收器，则必须设置表面夹具或表面。
     * 不调用此方法或setSurface(Surface) 播放视频时将只会播放音轨。零表面保持器或表面将仅导致正在播放的音轨。
     *
     * @param sh SurfaceHolder
     */
    public void setDisplay(SurfaceHolder sh) {
        try {
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setDisplay(sh);
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "setDisplay");
            mMainThreadHandler.post(mOnSetSurfaceErrorMainMessage);
        }
    }

    /**
     * 设置Surface要用作媒体视频部分的接收器。
     * 这是类似的setDisplay(SurfaceHolder)，但不支持setScreenOnWhilePlaying(boolean)。
     * 设置表面将取消设置先前设置的任何Surface或SurfaceHolder。
     * 空表面将仅导致正在播放的音轨。
     * 如果Surface向a发送帧SurfaceTexture，则从中返回的时间戳getTimestamp()将具有未指定的零点。
     * 这些时间戳不能在不同媒体源，同一媒体源的不同实例或同一节目的多个运行之间直接比较。
     * 时间戳通常是单调增加的，并且不受时钟调整的影响，但是当位置被设置时它被重置。
     *
     * @param surfaceTexture surfaceTexture
     */
    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "setSurfaceTexture");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", surfaceTexture is it null? " + (surfaceTexture == null)
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "setSurfaceTexture");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            if (surfaceTexture != null) {
                Surface surface = new Surface(surfaceTexture);
                mMediaPlayer.setSurface(surface);
            } else {
                mMediaPlayer.setSurface(null);
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "setSurfaceTexture");
            mMainThreadHandler.post(mOnSetSurfaceErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "setSurfaceTexture");
    }

    /**
     * 设置视频缩放模式。要在播放过程中使目标视频缩放模式有效，必须在设置数据源后调用此方法。
     * 如果未调用，默认的视频缩放模式为VIDEO_SCALING_MODE_SCALE_TO_FIT。
     * <p>
     * VIDEO_SCALING_MODE_SCALE_TO_FIT  1
     * 指定视频缩放模式。
     * 内容被拉伸到表面渲染区域。
     * 当表面具有与内容相同的纵横比时，保持内容的纵横比;
     * 否则，当正在呈现视频时，不保持内容的宽高比。
     * 不像VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING，这种视频缩放模式没有内容裁剪。
     * <p>
     * VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING  2
     * 指定视频缩放模式。
     * 内容缩放，保持其长宽比。
     * 总是使用整个表面积。
     * 当内容的宽高比与表面相同时，不裁剪任何内容; 否则，内容被裁剪以适合表面。
     *
     * @param mode 目标视频缩放模式。大多数是支持的视频缩放模式之一;
     *             否则，将抛出IllegalArgumentException。
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setVideoScalingMode(int mode) {
        try {
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setVideoScalingMode(mode);
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "setVideoScalingMode");
            mMainThreadHandler.post(mOnSetSurfaceErrorMainMessage);
        }
    }

    /**
     * 捕捉异常
     * 调整播放位置时抛出的错误
     */
    private final Runnable mOnSeekToErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_SEEK_TO,
                        MEDIA_PLAYER_WRAPPER_ERROR_SEEK_TO);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 寻求指定的时间位置。
     * 调整播放位置可以通过调用seekTo(int)。
     * <p>
     * seekTo(int)也可以在其他状态下调用，如Prepared，Paused和PlaybackCompleted 状态。
     * <p>
     * 可以通过调用来检索实际当前播放位置getCurrentPosition()，
     * 这对于需要跟踪播放进度的诸如音乐播放器的应用是有帮助的。
     * <p>
     * 虽然异步seekTo(int) 调用以正确的方式返回，
     * 但实际的寻道操作可能需要一段时间才能完成，特别是对于正在流传输的音频/视频。
     * 当实际的搜索操作完成时，如果事先通过注册了OnSeekCompleteListener，
     * 则内部播放器引擎调用用户提供的OnSeekComplete.onSeekComplete()
     * setOnSeekCompleteListener(OnSeekCompleteListener)。
     *
     * @param position 从开始到搜索的偏移量（以毫秒为单位）
     */
    public void seekTo(int position) {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "seekTo");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", position " + position
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "seekTo");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Prepared, Started, Paused, PlaybackCompleted}*/
                    case PREPARED:
                    case STARTED:
                    case PAUSED:
                    case PLAYBACK_COMPLETED:
                        mMediaPlayer.seekTo(position);
                        mMainThreadHandler.post(mOnVideoPositionUpdatedMainMessage);
                        break;
                    default:
                        break;
                }
            }
        } catch (IllegalStateException e) {
            MediaLog.getInstance().e(TAG, "IllegalStateException if the internal player engine has not been initialized"
                            + ", catch exception called in state " + mState
                            + ", position " + position
                            + ", duration " + getDuration()
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "seekTo");
            mMainThreadHandler.post(mOnSeekToErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "seekTo");
    }

    /**
     * 检查MediaPlayer是否正在播放。
     *
     * @return true如果当前播放，否则为false
     */
    private boolean isPlaying() {
        boolean isPlaying = false;
        try {
            isPlaying = mMediaPlayer != null && mState.get() != State.ERROR && mMediaPlayer.isPlaying();
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "isPlaying");
        }
        return isPlaying;
    }

    /**
     * 检查MediaPlayer是循环还是非循环。
     *
     * @return 如果MediaPlayer当前正在循环，则返回true，否则返回false
     */
    public boolean isLooping() {
        boolean looping = false;
        try {
            looping = mMediaPlayer != null && mMediaPlayer.isLooping();
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "isLooping");
        }
        return looping;
    }

    /**
     * 返回视频的宽度
     * 视频的宽度，如果没有视频，没有设置显示表面，或者宽度尚未确定，则为0。
     * OnVideoSizeChangedListener可以通过 setOnVideoSizeChangedListener(OnVideoSizeChangedListener)
     * 在宽度可用时提供通知进行注册。
     */
    public int getVideoWidth() {
        int width = 0;
        try {
            width = mMediaPlayer == null ? 0 : mMediaPlayer.getVideoWidth();
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "getVideoWidth");
        }
        return width;
    }

    /**
     * 返回视频的高度
     * 视频的高度，如果没有视频，没有设置显示表面，或高度尚未确定，则为0。
     * OnVideoSizeChangedListener可以通过 setOnVideoSizeChangedListener(OnVideoSizeChangedListener)
     * 在高度可用时提供通知进行注册。
     */
    public int getVideoHeight() {
        int height = 0;
        try {
            height = mMediaPlayer == null ? 0 : mMediaPlayer.getVideoHeight();
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "getVideoHeight");
        }
        return height;
    }

    /**
     * 获取资源的总时间。
     * 时间以毫秒为单位，如果没有持续时间可用（例如，如果流式传输直播内容），则返回-1。
     */
    public int getDuration() {
        int duration = 0;
        try {
            if (mMediaPlayer == null) {
                return 0;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Prepared, Started, Paused, Stopped, PlaybackCompleted}*/
                    case PREPARED:
                    case STARTED:
                    case PAUSED:
                    case STOPPED:
                    case PLAYBACK_COMPLETED:
                        duration = mMediaPlayer.getDuration();
                        break;
                    default:
                        duration = 0;
                        break;
                }
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "getDuration");
        }
        return duration;
    }

    /**
     * 获取当前播放位置
     * 当前位置（毫秒）
     */
    public int getCurrentPosition() {
        int currentDuration = 0;
        try {
            if (mMediaPlayer == null) {
                return 0;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted}*/
                    case STARTED:
                    case PAUSED:
                    case STOPPED:
                    case PLAYBACK_COMPLETED:
                        currentDuration = mMediaPlayer.getCurrentPosition();
                        break;
                    default:
                        currentDuration = 0;
                        break;
                }
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "getCurrentPosition");
        }
        return currentDuration;
    }

    /**
     * 将播放器设置为循环或非循环
     *
     * @param looping true-循环 false-不循环
     */
    public void setLooping(boolean looping) {
        try {
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setLooping(looping);
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", looping " + looping
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "setLooping");
        }
    }

    /**
     * 设置此播放器上的音量。
     * 建议使用此API来平衡应用程序中音频流的输出。
     * 除非您正在编写一个应用程序来控制用户设置，
     * 否则应优先使用此API来 setStreamVolume(int, int, int)设置特定类型的所有流的音量。
     * 请注意，传递的卷值是范围0.0到1.0中的原始标量。UI控件应按对数进行缩放。
     *
     * @param leftVolume  左声道
     * @param rightVolume 右声道
     */
    public void setVolume(float leftVolume, float rightVolume) {
        try {
            if (mMediaPlayer == null) {
                return;
            }
            mMediaPlayer.setVolume(leftVolume, rightVolume);
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", leftVolume in " + leftVolume + ",rightVolume in " + rightVolume
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "setVolume");
        }
    }

    /**
     * 设置视频播放速率
     * 例如 rate=1.5 播放速度按照正常速度的1.5倍播放
     * 但是只支持6.0以上的系统
     * @param speed 播放速率
     */
    public void setPlayerSpeed(float speed) {
        try {
            if (mMediaPlayer == null) {
                return;
            }
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                PlaybackParams playbackParams = mMediaPlayer.getPlaybackParams();
                playbackParams.setSpeed(speed);
                mMediaPlayer.setPlaybackParams(playbackParams);
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", rate in " + speed
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "setPlayerRate");
        }
    }

    /**
     * 捕捉异常
     * 重置时抛出的错误
     */
    private final Runnable mOnResetErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_RESET,
                        MEDIA_PLAYER_WRAPPER_ERROR_RESET);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 将MediaPlayer重置为其未初始化状态。
     * 调用此方法后，您将必须通过设置数据源和调用prepare（）来重新初始化它。
     */
    public void reset() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "reset");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "reset");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Idle, Initialized, Prepared, Started, Paused, Stopped, PlaybackCompleted, Error}*/
                    case IDLE:
                    case INITIALIZED:
                    case PREPARED:
                    case STARTED:
                    case PAUSED:
                    case STOPPED:
                    case PLAYBACK_COMPLETED:
                    case ERROR:
                        mMediaPlayer.reset();
                        mState.set(State.IDLE);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "reset");
            mMainThreadHandler.post(mOnResetErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "reset");
    }

    /**
     * 捕捉异常
     * 释放资源时抛出的错误
     */
    private final Runnable mOnReleaseErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_RELEASE,
                        MEDIA_PLAYER_WRAPPER_ERROR_RELEASE);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 释放与此MediaPlayer对象相关联的资源
     * 每当应用程序的Activity被暂停（调用其onPause（）方法）或停止（调用其onStop（）方法）时，
     * 应该调用此方法来释放MediaPlayer对象，除非应用程序有特殊需要保持对象周围。
     * release()，对象不再可用
     */
    public void release() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "release");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "release");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            synchronized (mState) {
                mMediaPlayer.release();
                mState.set(State.END);
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "release");
            mMainThreadHandler.post(mOnReleaseErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "release");
    }

    /**
     * 捕捉异常
     * 清除所有时抛出的错误
     */
    private final Runnable mOnClearAllErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_CLEAR_ALL,
                        MEDIA_PLAYER_WRAPPER_ERROR_CLEAR_ALL);
            }
            setCurrentState(State.ERROR);
        }
    };


    /**
     * 移除MediaPlayer中使用到的所有监听
     */
    public void clearAll() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "clearAll");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "clearAll");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            stopPositionUpdateNotifier();
            synchronized (mState) {
                mMediaPlayer.setOnVideoSizeChangedListener(null);
                mMediaPlayer.setOnCompletionListener(null);
                mMediaPlayer.setOnErrorListener(null);
                mMediaPlayer.setOnBufferingUpdateListener(null);
                mMediaPlayer.setOnInfoListener(null);
                mMediaPlayer = null;
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "clearAll");
            mMainThreadHandler.post(mOnClearAllErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "clearAll");
    }

    /**
     * 准备完成后通知
     */
    private final Runnable mOnVideoPreparedMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onPreparedCallback(mMediaPlayer);
            }
        }
    };

    /**
     * 捕捉异常
     * 准备资源时抛出的错误
     */
    private final Runnable mOnPrepareErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_PREPARE,
                        MEDIA_PLAYER_WRAPPER_ERROR_PREPARE);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 准备播放器同步播放。
     * 设置数据源和显示表面后，需要调用prepare（）或prepareAsync（）。
     * 对于文件，可以调用prepare（），它会阻塞，直到MediaPlayer准备好进行播放。
     */
    public void prepare() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "prepare");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "prepare");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Initialized, Stopped}*/
                    case INITIALIZED:
                    case STOPPED:
                        MediaLog.getInstance().i(TAG, ">> run",
                                "MediaPlayerWrapper",
                                "prepare");
                        mMediaPlayer.prepare();
                        MediaLog.getInstance().i(TAG, "<< run",
                                "MediaPlayerWrapper",
                                "prepare");
                        mState.set(State.PREPARED);
                        mMainThreadHandler.post(mOnVideoPreparedMainMessage);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "prepare");
            mMainThreadHandler.post(mOnPrepareErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "prepare");
    }

    /**
     * 捕捉异常
     * 开始播放资源时抛出的错误
     */
    private final Runnable mOnStartErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_START,
                        MEDIA_PLAYER_WRAPPER_ERROR_START);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 开始播放通知
     */
    private final Runnable mOnVideoStartMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onVideoStartCallback();
                mListener.onDurationCallback(getDuration());
            }
        }
    };

    /**
     * 开始或继续播放。
     * 如果播放先前已暂停，播放将从暂停位置继续播放。
     * 如果播放从未从前开始，播放将从开始播放。
     * 此方法不适用于停止播放后重新播放 重新播放请调用play()
     * 调用start()对已处于Started状态的MediaPlayer对象没有影响。
     * 调用start()为暂停的MediaPlayer对象恢复播放，恢复的播放位置与暂停的位置相同。
     * 当调用 start()返回时，暂停的MediaPlayer对象返回到已启动状态。
     */
    public void start() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "start");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "start");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Prepared, Started, Paused, PlaybackCompleted}*/
                    case PREPARED:
                    case STARTED:
                    case PAUSED:
                    case PLAYBACK_COMPLETED:
                        mMediaPlayer.start();
                        mState.set(State.STARTED);
                        startPositionUpdateNotifier();
                        mMainThreadHandler.post(mOnVideoStartMainMessage);
                        break;
                    default:
                        break;
                }
            }
        } catch (IllegalStateException e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "start");
            mMainThreadHandler.post(mOnStartErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "start");
    }

    /**
     * 捕捉异常
     * 开始播放资源时抛出的错误
     */
    private final Runnable mOnPauseErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_PAUSE,
                        MEDIA_PLAYER_WRAPPER_ERROR_PAUSE);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 视频暂停播放的通知
     */
    private final Runnable mOnVideoPauseMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onVideoPauseCallback();
            }
        }
    };

    /**
     * 暂停播放。
     * 调用pause()对已处于已暂停状态的MediaPlayer对象没有影响。
     * 如果内部播放器引擎尚未初始化。会抛异常
     */
    public void pause() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "pause");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "pause");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Started, Paused, PlaybackCompleted}*/
                    case STARTED:
                    case PAUSED:
                    case PLAYBACK_COMPLETED:
                        mMediaPlayer.pause();
                        mState.set(State.PAUSED);
                        mMainThreadHandler.post(mOnVideoPauseMainMessage);
                        break;
                    default:
                        break;
                }
            }
        } catch (IllegalStateException e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "pause");
            mMainThreadHandler.post(mOnPauseErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "pause");
    }

    /**
     * 捕捉异常
     * 停止播放资源时抛出的错误
     */
    private final Runnable mOnStopErrorMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onErrorCallback(null,
                        MEDIA_PLAYER_WRAPPER_ERROR_STOP,
                        MEDIA_PLAYER_WRAPPER_ERROR_STOP);
            }
            setCurrentState(State.ERROR);
        }
    };

    /**
     * 视频停止的通知
     */
    private final Runnable mOnVideoStopMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null) {
                mListener.onVideoStoppedCallback();
            }
        }
    };

    /**
     * 播放停止或暂停后停止播放。
     * 调用 stop()停止播放并使处于Started，Paused，Prepared
     * 或PlaybackCompleted状态的MediaPlayer进入 Stopped状态。
     * 一旦处于停止状态，播放将无法启动，
     * 直到prepare()或被prepareAsync()调用以再次将MediaPlayer对象设置为“ 准备”状态。
     * 调用stop()对已处于已停止状态的MediaPlayer对象没有影响。
     */
    public void stop() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper", "stop");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null)
                        + ", mState " + mState,
                "MediaPlayerWrapper",
                "stop");
        try {
            if (mMediaPlayer == null) {
                return;
            }
            synchronized (mState) {
                switch (mState.get()) {
                    /*{Prepared, Started, Stopped, Paused, PlaybackCompleted}*/
                    case PREPARED:
                    case STARTED:
                    case STOPPED:
                    case PAUSED:
                    case PLAYBACK_COMPLETED:
                        stopPositionUpdateNotifier();
                        mMediaPlayer.stop();
                        mState.set(State.STOPPED);
                        mMainThreadHandler.post(mOnVideoStopMainMessage);
                        break;
                    default:
                        break;
                }
            }
        } catch (IllegalStateException e) {
            MediaLog.getInstance().e(TAG, "catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "stop");
            mMainThreadHandler.post(mOnStopErrorMainMessage);
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper", "stop");
    }

    /**
     * 当前播放位置通知
     */
    private final Runnable mOnVideoPositionUpdatedMainMessage = new Runnable() {
        @Override
        public void run() {
            if (mListener != null && mState.get() == State.STARTED) {
                mListener.onCurrentPositionCallback(getCurrentPosition());
            }
        }
    };

    /**
     * 当前播放时间戳回调
     */
    private final Runnable mNotifyPositionUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mMainThreadHandler.post(mOnVideoPositionUpdatedMainMessage);
        }
    };

    /**
     * 启动当前播放时间回调
     */
    private void startPositionUpdateNotifier() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper",
                "startPositionUpdateNotifier");
        MediaLog.getInstance().i(TAG, "ScheduledFuture mFuture is it null? " + (mFuture == null),
                "MediaPlayerWrapper",
                "startPositionUpdateNotifier");
        try {
            if (mFuture != null) {
                mFuture.cancel(true);
                mFuture = null;
            }
            mFuture = mScheduledExecutorService.scheduleAtFixedRate(mNotifyPositionUpdateRunnable,
                    0,
                    1000,
                    TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "position update notifier error"
                            + ", catch exception called in state " + mState
                            + ", e.getMessage " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "MediaPlayerWrapper",
                    "startPositionUpdateNotifier");
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper",
                "startPositionUpdateNotifier");
    }

    /**
     * 停止当前播放时间回调
     */
    private void stopPositionUpdateNotifier() {
        MediaLog.getInstance().i(TAG, ">>", "MediaPlayerWrapper",
                "stopPositionUpdateNotifier");
        MediaLog.getInstance().i(TAG, "ScheduledFuture mFuture is it null? " + (mFuture == null),
                "MediaPlayerWrapper",
                "stopPositionUpdateNotifier");
        if (mFuture != null) {
            mFuture.cancel(true);
            mFuture = null;
        }
        MediaLog.getInstance().i(TAG, "<<", "MediaPlayerWrapper",
                "stopPositionUpdateNotifier");
    }

    /**
     * MediaPlayer回调状态
     * 准备完成是回调
     *
     * @param mp MediaPlayer
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        MediaLog.getInstance().i(TAG, "media player prepared, state " + mState,
                "MediaPlayerWrapper",
                "onPrepared");
        /*if (mListener != null) {
            mListener.onPreparedCallback(mp);
        }*/
    }

    /**
     * 当视频大小改变时的回调
     *
     * @param mp     MediaPlayer
     * @param width  宽度
     * @param height 高度
     */
    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        MediaLog.getInstance().i(TAG, "width " + width + ", height " + height + ", state " + mState,
                "MediaPlayerWrapper",
                "onVideoSizeChanged");
        if (mListener != null) {
            mListener.onVideoSizeChangedCallback(mp, width, height);
        }
    }

    /**
     * 快进快退完成时回调
     *
     * @param mp MediaPlayer
     */
    @Override
    public void onSeekComplete(MediaPlayer mp) {
        MediaLog.getInstance().i(TAG, "state " + mState,
                "MediaPlayerWrapper",
                "onSeekComplete");
        if (mListener != null) {
            mListener.onSeekCompleteCallback(mp);
        }
    }

    /**
     * 播放完成时回调
     *
     * @param mp MediaPlayer
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        MediaLog.getInstance().i(TAG, "state " + mState,
                "MediaPlayerWrapper",
                "onCompletion");
        synchronized (mState) {
            mState.set(State.PLAYBACK_COMPLETED);
        }
        MediaLog.getInstance().i(TAG, "state " + mState,
                "MediaPlayerWrapper",
                "onCompletion");
        if (mListener != null) {
            mListener.onCompletionCallback(mp);
        }
    }

    /**
     * 缓存进度回调
     *
     * @param mp      MediaPlayer
     * @param percent 缓存进度
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        /*MediaLog.getInstance().i(TAG, "percent " + percent + ", state " + mState,
                "MediaPlayerWrapper",
                "onBufferingUpdate");*/
        if (mListener != null) {
            mListener.onBufferingUpdateCallback(mp, percent);
        }
    }

    /**
     * 详细信息回调
     *
     * @param mp    MediaPlayer
     * @param what  what
     * @param extra extra
     * @return false-处理过
     */
    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        MediaLog.getInstance().i(TAG, "what " + what + " extra " + extra + ", state " + mState,
                "MediaPlayerWrapper",
                "onInfo");
        if (mListener != null) {
            mListener.onInfoCallback(mp, what, extra);
        }
        return false;
    }

    /**
     * 当错误是回调
     *
     * @param mp    MediaPlayer
     * @param what  what
     * @param extra extra
     * @return 如果返回false，则调用OnCompletionListener
     */
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        MediaLog.getInstance().i(TAG, "what " + what + " extra " + extra + ", state " + mState,
                "MediaPlayerWrapper",
                "onError");
        synchronized (mState) {
            mState.set(State.ERROR);
        }
        //停止计时器
        stopPositionUpdateNotifier();
        if (mListener != null) {
            mListener.onErrorCallback(mp, what, extra);
        }
        return true;
    }

    /**
     * MediaPlayer状态监听回调接口
     * 最终是在主线程中是用
     * 所以必须转入主线程中调用
     */
    public interface MediaPlayerStateListener {
        /**
         * 异常回调
         *
         * @param mp    MediaPlayer
         * @param what  错误类型
         *              MEDIA_ERROR_UNKNOWN     1       未指定的媒体播放器错误。
         *              MEDIA_ERROR_SERVER_DIED 100     媒体服务对象失效。在这种情况下，该应用程序必须释放MediaPlayer对象和实例化新的。
         * @param extra MEDIA_ERROR_IO          -1004   文件或网络相关的操作错误。
         *              MEDIA_ERROR_MALFORMED   -1007   比特流不符合相关的编码标准或文件规范。
         *              MEDIA_ERROR_UNSUPPORTED -1010   比特流符合相关的编码标准或文件规范，但媒体框架不支持该功能。
         *              MEDIA_ERROR_TIMED_OUT   -110    有些操作需要很长时间才能完成，通常比3-5秒钟。
         *              如果方法处理错误，则为True，否则为false。返回false，或根本没有OnErrorListener，将导致OnCompletionListener被调用。
         */
        void onErrorCallback(MediaPlayer mp, int what, int extra);

        /**
         * 详情和警告回调
         *
         * @param mp    MediaPlayer
         * @param what  详情或警告的类型。
         *              MEDIA_INFO_UNKNOWN              1   未指定的媒体播放器的信息。
         *              MEDIA_INFO_VIDEO_TRACK_LAGGING  700 视频对于解码器来说太复杂了：它不能足够快地解码帧。可能只有音频在这个阶段可以播放。
         *              MEDIA_INFO_VIDEO_RENDERING_START 3  开始渲染视频的第一帧。
         *              MEDIA_INFO_BUFFERING_START      701 MediaPlayer正在暂时以缓冲更多的数据暂停回放内部。
         *              MEDIA_INFO_BUFFERING_END        702 MediaPlayer正在填充缓冲区后继续播放。
         *              MEDIA_INFO_BAD_INTERLEAVING     800 操作错误 糟糕的交错意味着媒体已经交错不当或不交错所有，如拥有所有的视频样本首先那么所有的声音的。播放视频时，但大量的磁盘寻道可能会发生。
         *              MEDIA_INFO_NOT_SEEKABLE         801 媒体无法查找关键帧无法定位
         *              MEDIA_INFO_METADATA_UPDATE      802 新数据源 一组新的元数据是可用的。
         *              MEDIA_INFO_UNSUPPORTED_SUBTITLE 901 字幕轨道不是由媒体框架支撑。
         *              MEDIA_INFO_SUBTITLE_TIMED_OUT   902 读取字幕轨需要的时间过长。超时
         * @param extra extra
         *              如果方法处理信息，则为True，否则为false。返回false，或根本没有OnErrorListener，将导致信息被丢弃。
         */
        void onInfoCallback(MediaPlayer mp, int what, int extra);

        /**
         * 调用以更新缓冲通过渐进式HTTP下载接收的媒体流的状态。
         * 接收的缓冲百分比指示已经缓冲或播放了多少内容。
         * 例如，当已经播放一半内容时，缓冲更新80％指示要播放的内容的下一个30％已经被缓冲。
         *
         * @param mp      MediaPlayer
         * @param percent 到目前为止缓冲或播放的内容的百分比（0-100）
         */
        void onBufferingUpdateCallback(MediaPlayer mp, int percent);

        /**
         * 调用回调表示对象现在处于 PlaybackCompleted状态。
         * 当处于PlaybackCompleted 状态时，呼叫start()可以从音频/视频源的开始重新开始播放。
         *
         * @param mp MediaPlayer
         */
        void onCompletionCallback(MediaPlayer mp);

        /**
         * 调用以指示完成查找操作
         *
         * @param mp MediaPlayer
         */
        void onSeekCompleteCallback(MediaPlayer mp);

        /**
         * 调用以指示视频大小如果没有视频，没有设置显示表面或尚未确定值，则视频大小（宽度和高度）可以为0。
         *
         * @param mp     MediaPlayer
         * @param width  宽
         * @param height 高
         */
        void onVideoSizeChangedCallback(MediaPlayer mp, int width, int height);

        /**
         * 当媒体文件准备播放时调用
         * 如果设置过自动播放，这个方法内将自动调用start方法。
         *
         * @param mp 当前mediaPlayer对象
         */
        void onPreparedCallback(MediaPlayer mp);

        /**
         * 开始播放时的回调
         */
        void onVideoStartCallback();

        /**
         * 停止播放后的回调
         */
        void onVideoStoppedCallback();

        /**
         * 暂停播放后的回调
         */
        void onVideoPauseCallback();

        /**
         * 返回视频当前时间戳
         *
         * @param position position
         */
        void onCurrentPositionCallback(int position);

        /**
         * 返回当前视频的总时长
         *
         * @param duration duration
         */
        void onDurationCallback(int duration);
    }

    /**
     * MediaPlayerStateListener 抽象类
     */
    public static abstract class AbstractMediaPlayerStateListener implements MediaPlayerStateListener {

        @Override
        public void onErrorCallback(MediaPlayer mp, int what, int extra) {
            errorCallback(mp, what, extra);
        }

        @Override
        public void onInfoCallback(MediaPlayer mp, int what, int extra) {
            infoCallback(mp, what, extra);
        }

        @Override
        public void onBufferingUpdateCallback(MediaPlayer mp, int percent) {
            bufferingUpdateCallback(mp, percent);
        }

        @Override
        public void onCompletionCallback(MediaPlayer mp) {
            completionCallback(mp);
        }

        @Override
        public void onSeekCompleteCallback(MediaPlayer mp) {
            seekCompleteCallback(mp);
        }

        @Override
        public void onVideoSizeChangedCallback(MediaPlayer mp, int width, int height) {
            videoSizeChangedCallback(mp, width, height);
        }

        @Override
        public void onPreparedCallback(MediaPlayer mp) {
            preparedCallback(mp);
        }

        @Override
        public void onVideoStartCallback() {
            videoStartCallback();
        }

        @Override
        public void onVideoStoppedCallback() {
            videoStoppedCallback();
        }

        @Override
        public void onVideoPauseCallback() {
            videoPauseCallback();
        }

        @Override
        public void onCurrentPositionCallback(int position) {
            currentPositionCallback(position);
        }

        @Override
        public void onDurationCallback(int duration) {
            durationCallback(duration);
        }


        public void errorCallback(MediaPlayer mp, int what, int extra) {

        }

        public void infoCallback(MediaPlayer mp, int what, int extra) {

        }

        public void bufferingUpdateCallback(MediaPlayer mp, int percent) {

        }

        public abstract void completionCallback(MediaPlayer mp);

        public void seekCompleteCallback(MediaPlayer mp) {

        }

        public void videoSizeChangedCallback(MediaPlayer mp, int width, int height) {

        }

        public void preparedCallback(MediaPlayer mp) {

        }

        public abstract void videoStartCallback();

        public abstract void videoStoppedCallback();

        public abstract void videoPauseCallback();

        public void currentPositionCallback(int position) {

        }

        public void durationCallback(int duration) {

        }
    }
}
