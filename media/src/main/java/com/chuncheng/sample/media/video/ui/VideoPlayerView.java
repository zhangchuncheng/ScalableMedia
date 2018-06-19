package com.chuncheng.sample.media.video.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;

import com.chuncheng.sample.media.audio.VoiceManager;
import com.chuncheng.sample.media.utils.ScreenUtils;
import com.chuncheng.sample.media.utils.log.MediaLog;
import com.chuncheng.sample.media.video.manager.HandlerThreadExtension;
import com.chuncheng.sample.media.video.ui.wrapper.MediaPlayerWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description: 继承至OptimizeTextureView
 * 1.增加手势处理
 * 2.增加播放状态的控制
 * 3.引入状态指示器 控制播放进度
 * 4.优化mediaPlayer状态的校验方式
 * 5.整理mediaPlayer状态回调继承分发机制
 * 6.增加手势回调抽象实现类，优化手势回调
 *
 * @author: zhangchuncheng
 * @date: 2017/3/21
 */

public class VideoPlayerView extends OptimizeTextureView
        implements TextureView.SurfaceTextureListener,
        View.OnTouchListener, MediaPlayerWrapper.MediaPlayerStateListener {
    private static final String TAG = "View";
    /** MediaPlayer 包装类 */
    private MediaPlayerWrapper mMediaPlayer;
    /** surfaceView 监听 */
    private TextureView.SurfaceTextureListener mLocalSurfaceTextureListener;
    /** 手势相关回调 */
    private VideoGestureDetectorUiOperationListener mUIOperationListener;
    /** MediaPlayer 监听集合 */
    private final Set<MediaPlayerWrapper.MediaPlayerStateListener> mMediaPlayerMainThreadListeners = new HashSet<>();
    /** 状态指示器 */
    private final PlayerIndicator mPlayerIndicator = new PlayerIndicator();
    /** 网络视频 资源地址 */
    private String mPath;
    /** 子线程 handlerThread */
    private HandlerThreadExtension mViewHandlerBackgroundThread;
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
    /** 当前播放时间 */
    private int currentPosition;
    /** 当前音量 */
    private int currentVolume;
    /** 视频总长度 */
    private int duration;
    /** 最大音量 */
    private int maxVolume;
    /** 快进到的时间点位置 */
    private int speedPosition;
    /** 系统亮度值 */
    private int screenBrightness = 0;
    /** 亮度位置出点起始值 */
    private float recordLight = 0;
    /** 是否启用手势 */
    private boolean isGestureDetectorValid;
    /** 是否启用进度手势 默认开启 */
    private boolean isGestureDetectorProgressValid = true;
    /** 手势操作类 */
    private GestureDetector mGestureDetector;
    /** 音量管理类 */
    private VoiceManager mVoiceManager;

    public void setUIOperationListener(VideoGestureDetectorUiOperationListener uiOperationListener) {
        mUIOperationListener = uiOperationListener;
    }

    public void setGestureDetectorValid(boolean gestureDetectorValid) {
        isGestureDetectorValid = gestureDetectorValid;
    }

    public void setGestureDetectorProgressValid(boolean gestureDetectorProgressValid) {
        isGestureDetectorProgressValid = gestureDetectorProgressValid;
    }

    public String getVideoUrlDataSource() {
        if (TextUtils.isEmpty(mPath)) {
            return "";
        }
        return mPath;
    }

    public void addMediaPlayerListener(MediaPlayerWrapper.MediaPlayerStateListener listener) {
        synchronized (mMediaPlayerMainThreadListeners) {
            mMediaPlayerMainThreadListeners.add(listener);
        }
    }

    public void removeMediaPlayerListener(MediaPlayerWrapper.MediaPlayerStateListener listener) {
        synchronized (mMediaPlayerMainThreadListeners) {
            mMediaPlayerMainThreadListeners.remove(listener);
        }
    }

    public VideoPlayerView(Context context) {
        super(context);
        initView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    @Override
    public final void setSurfaceTextureListener(TextureView.SurfaceTextureListener listener) {
        mLocalSurfaceTextureListener = listener;
    }

    @Override
    public boolean isAttachedToWindow() {
        boolean b = mViewHandlerBackgroundThread != null;
        MediaLog.getInstance().i(TAG, "Handler Background Thread is not null " + b,
                "VideoPlayerView",
                "isAttachedToWindow");
        return b;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        boolean isInEditMode = isInEditMode();
        MediaLog.getInstance().i(TAG, "isInEditMode is " + isInEditMode
                        + ", if false, then new Handler and start",
                "VideoPlayerView",
                "onAttachedToWindow");
        if (!isInEditMode) {
            mViewHandlerBackgroundThread =
                    new HandlerThreadExtension(TAG, false);
            mViewHandlerBackgroundThread.startThread();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        boolean isInEditMode = isInEditMode();
        MediaLog.getInstance().i(TAG, "isInEditMode is " + isInEditMode + ", if false, then quit Handler",
                "VideoPlayerView",
                "onDetachedFromWindow");
        if (!isInEditMode) {
            mViewHandlerBackgroundThread.postQuit();
            mViewHandlerBackgroundThread = null;
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        boolean isInEditMode = isInEditMode();
        String vis;
        switch (visibility) {
            case VISIBLE:
                vis = "VISIBLE";
                break;
            case INVISIBLE:
                vis = "INVISIBLE";
                break;
            case GONE:
                vis = "GONE";
                break;
            default:
                vis = "unexpected";
        }
        if (!isInEditMode) {
            switch (visibility) {
                case VISIBLE:
                    break;
                case INVISIBLE:
                case GONE:
                    synchronized (mPlayerIndicator) {
                        // have to notify worker thread in case we exited
                        // this screen without getting ready for playback
                        mPlayerIndicator.notifyAll();
                    }
                    break;
                default:
                    break;
            }
        }
        MediaLog.getInstance().i(TAG, "visibility is " + vis + ", isInEditMode is " + isInEditMode,
                "VideoPlayerView",
                "onVisibilityChanged");
    }

    /**
     * 初始化
     * 1.设置监听
     * 2.设置默认缩放类型
     */
    private void initView() {
        MediaLog.getInstance().i(TAG, "isInEditMode is " + isInEditMode() +
                        "initialize Listener and create voiceManager gestureDetector, " +
                        "set view scale type SCALE_TYPE_FILL",
                "VideoPlayerView",
                "initView");
        if (!isInEditMode()) {
            super.setSurfaceTextureListener(this);
            super.setOnTouchListener(this);
            super.setLongClickable(true);
            mVoiceManager = new VoiceManager(getContext());
            mGestureDetector = new GestureDetector(getContext(),
                    new VideoPlayerView.VideoPlayGestureDetector());
            //启用长按设置
            mGestureDetector.setIsLongpressEnabled(true);
            setViewScaleType(SCALE_TYPE_FILL);
        }
    }

    /**
     * 检查线程
     */
    private void checkThread() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            notifyOnVideoError(null,
                    MediaPlayerWrapper.MEDIA_PLAYER_WRAPPER_ERROR_THREAD,
                    MediaPlayerWrapper.MEDIA_PLAYER_WRAPPER_ERROR_THREAD);
            MediaLog.getInstance().e(TAG, "cannot be in main thread",
                    "VideoPlayerView",
                    "checkThread");
        }
        MediaLog.getInstance().i(TAG, "Looper.myLooper -> "
                        + Looper.myLooper() +
                        ", Looper.getMainLooper -> "
                        + Looper.getMainLooper() +
                        ", Thread.currentThread().getId -> "
                        + Thread.currentThread().getId() +
                        ", Looper.getMainLooper().getThread().getId -> "
                        + Looper.getMainLooper().getThread().getId(),
                "VideoPlayerView",
                "checkThread");
    }

    /**
     * 获取当前状态
     *
     * @return state
     */
    public MediaPlayerWrapper.State getCurrentState() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentState();
        } else {
            return MediaPlayerWrapper.State.END;
        }
    }

    /**
     * 总时长
     *
     * @return int 视频总时间 毫秒点位 以下方法返回时长的单位都为毫秒
     */
    public int getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    /**
     * 当前播放位置
     *
     * @return int 当前播放位置的毫秒数
     */
    public int getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    /**
     * 是否循环播放
     *
     * @return 是-循环
     */
    public boolean isLooping() {
        return mMediaPlayer != null && mMediaPlayer.isLooping();
    }

    /**
     * 设置循环播放
     *
     * @param looping true-设置为循环 false-不循环
     */
    public void setLooping(boolean looping) {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.setLooping(looping);
    }

    /**
     * 设置播放速度
     * 例如 speed = 1.5
     * 则视频播放速度为正常速度的1.5倍
     * @param speed 速度值
     */
    public void setSpeed(float speed){
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.setPlayerSpeed(speed);
    }

    /**
     * 重置
     */
    public void reset() {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "reset");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "reset");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mMediaPlayer.reset();
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "reset");
    }

    /**
     * 释放资源
     */
    public void release() {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "release");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "release");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mMediaPlayer.release();
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "release");
    }

    /**
     * 清理实例
     */
    public void clearPlayerInstance() {
        MediaLog.getInstance().i(TAG, ">>",
                "VideoPlayerView",
                "clearPlayerInstance");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "clearPlayerInstance");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mPlayerIndicator.setVideoSize(null, null);
        mMediaPlayer.clearAll();
        mMediaPlayer = null;
        MediaLog.getInstance().i(TAG, "<<",
                "VideoPlayerView",
                "clearPlayerInstance");
    }

    /**
     * 创建实例
     */
    public void createNewPlayerInstance() {
        MediaLog.getInstance().i(TAG, ">>",
                "VideoPlayerView",
                "createNewPlayerInstance");
        checkThread();
        synchronized (mPlayerIndicator) {
            mMediaPlayer = new MediaPlayerWrapper(new MediaPlayer());
            mPlayerIndicator.setVideoSize(null, null);
            mPlayerIndicator.setFailedToPrepareUiForPlayback(false);
            MediaLog.getInstance().i(TAG, "surface texture is available? "
                            + mPlayerIndicator.isSurfaceTextureAvailable(),
                    "VideoPlayerView",
                    "createNewPlayerInstance");
            if (mPlayerIndicator.isSurfaceTextureAvailable()) {
                SurfaceTexture texture = getSurfaceTexture();
                MediaLog.getInstance().i(TAG, "texture is available? " + (texture == null),
                        "VideoPlayerView",
                        "createNewPlayerInstance");
                if (texture != null) {
                    mMediaPlayer.setSurfaceTexture(texture);
                    mMediaPlayer.setSurfaceTexture(texture);
                }
            }
            mMediaPlayer.setListener(this);
        }
        MediaLog.getInstance().i(TAG, "<<",
                "VideoPlayerView",
                "createNewPlayerInstance");
    }

    /**
     * 同步准备
     */
    public void prepare() {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "prepare");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "prepare");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mMediaPlayer.prepare();
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "prepare");
    }

    /**
     * 停止
     */
    public void stop() {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "stop");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "stop");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mMediaPlayer.stop();
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "stop");
    }

    /**
     * 暂停
     */
    public void pause() {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "pause");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "pause");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mMediaPlayer.pause();
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "pause");
    }

    /**
     * 播放视频
     */
    public void start() {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "start");
        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "start");
        checkThread();
        synchronized (mPlayerIndicator) {
            if (mMediaPlayer == null) {
                return;
            }
            MediaLog.getInstance().i(TAG, "mPlayerIndicator -> " + mPlayerIndicator.toString(),
                    "VideoPlayerView",
                    "start");
            if (mPlayerIndicator.isReadyForPlayback()) {
                MediaLog.getInstance().i(TAG, ">> run",
                        "VideoPlayerView",
                        "start");
                mMediaPlayer.start();
                MediaLog.getInstance().i(TAG, "<< run",
                        "VideoPlayerView",
                        "start");
            } else if (mPlayerIndicator.isSurfaceTextureAvailable()) {
                MediaLog.getInstance().i(TAG, "set surface texture, mMediaPlayer is it null? "
                                + (mMediaPlayer == null)
                                + "mPlayerIndicator setVideoSize width "
                                + getMWidth() + ", height " + getMHeight(),
                        "VideoPlayerView",
                        "start");
                if (mMediaPlayer != null) {
                    mMediaPlayer.setSurfaceTexture(getSurfaceTexture());
                } else {
                    mPlayerIndicator.setVideoSize(null, null);
                }
                mPlayerIndicator.setVideoSize(getMHeight(), getMWidth());
                MediaLog.getInstance().i(TAG, "mPlayerIndicator isReadyForPlayback "
                                + mPlayerIndicator.isReadyForPlayback(),
                        "VideoPlayerView",
                        "start");
                if (mPlayerIndicator.isReadyForPlayback()) {
                    MediaLog.getInstance().i(TAG, ">> setSurfaceTexture to start",
                            "VideoPlayerView",
                            "start");
                    mMediaPlayer.start();
                    MediaLog.getInstance().i(TAG, "<< setSurfaceTexture to start",
                            "VideoPlayerView",
                            "start");
                } else {
                    MediaLog.getInstance().e(TAG, "movie is not ready, "
                                    + "Player become STARTED state, but it will actually don't play",
                            "VideoPlayerView",
                            "start");
                }
            } else {
                if (!mPlayerIndicator.isFailedToPrepareUiForPlayback()) {
                    MediaLog.getInstance().i(TAG, ">> wait",
                            "VideoPlayerView",
                            "start");
                    try {
                        mPlayerIndicator.wait();
                    } catch (InterruptedException e) {
                        MediaLog.getInstance().e(TAG, "start wait error"
                                        + ", " + MediaLog.getInstance().getErrorContent(e),
                                "VideoPlayerView",
                                "start");
                    }
                    MediaLog.getInstance().i(TAG, "<< wait",
                            "VideoPlayerView",
                            "start");
                    if (mPlayerIndicator.isReadyForPlayback()) {
                        MediaLog.getInstance().i(TAG, ">> wait to start",
                                "VideoPlayerView",
                                "start");
                        mMediaPlayer.start();
                        MediaLog.getInstance().i(TAG, "<< wait to start",
                                "VideoPlayerView",
                                "start");
                    } else {
                        MediaLog.getInstance().e(TAG, "movie is not ready, "
                                        + "Player become STARTED state, "
                                        + "but it will actually don't play",
                                "VideoPlayerView",
                                "start");
                    }
                } else {
                    MediaLog.getInstance().e(TAG, "movie is not ready. Video size will not become available",
                            "VideoPlayerView",
                            "start");
                }
            }
        }
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "start");
    }

    /**
     * 快进快退
     *
     * @param position 要快进到的时间点 单位毫秒
     */
    public void seekToForThread(int position) {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "seekToForThread");
        MediaLog.getInstance().i(TAG, "position " + position
                        + ", duration " + mMediaPlayer.getDuration()
                        + ", mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "seekToForThread");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mMediaPlayer.seekTo(position);
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "seekToForThread");
    }

    /**
     * 设置数据源
     *
     * @param path url 视频网络地址或者本地地址
     */
    public void setDataSource(String path) {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "setDataSource");
        MediaLog.getInstance().i(TAG, "path " + path
                        + ", mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "setDataSource");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mMediaPlayer.setDataSource(path);
        mPath = path;
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "setDataSource");
    }

    /**
     * 设置数据源
     *
     * @param context 上下文
     * @param uriPath uir
     */
    public void setDataSource(Context context, String uriPath) {
        MediaLog.getInstance().i(TAG, ">>", "VideoPlayerView", "setDataSource");
        MediaLog.getInstance().i(TAG, "uriPath " + uriPath
                        + ", mMediaPlayer is it null? " + (mMediaPlayer == null),
                "VideoPlayerView",
                "setDataSource");
        if (mMediaPlayer == null) {
            return;
        }
        checkThread();
        mMediaPlayer.setDataSource(context, Uri.parse(uriPath));
        mPath = uriPath;
        MediaLog.getInstance().i(TAG, "<<", "VideoPlayerView", "setDataSource");
    }

    /**
     * 通知当前播放位置
     *
     * @param position position
     */
    private void notifyOnCurrentPosition(int position) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onCurrentPositionCallback(position);
        }
    }

    /**
     * 通知播放总时长
     *
     * @param duration duration
     */
    private void notifyOnDuration(int duration) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onDurationCallback(duration);
        }
    }

    /**
     * 通知surfaceView可用
     */
    private void notifyTextureAvailable() {
        MediaLog.getInstance().i(TAG, ">>",
                "VideoPlayerView",
                "notifyTextureAvailable");
        if (isAttachedToWindow()) {
            mViewHandlerBackgroundThread.post(new Runnable() {
                @Override
                public void run() {
                    MediaLog.getInstance().i(TAG, ">> run",
                            "VideoPlayerView",
                            "notifyTextureAvailable");
                    synchronized (mPlayerIndicator) {
                        MediaLog.getInstance().i(TAG, "mMediaPlayer is it null? " + (mMediaPlayer == null) +
                                        ", is null, cannot set surface texture",
                                "VideoPlayerView",
                                "notifyTextureAvailable");
                        if (mMediaPlayer != null) {
                            mMediaPlayer.setSurfaceTexture(getSurfaceTexture());
                        } else {
                            mPlayerIndicator.setVideoSize(null, null);
                        }
                        mPlayerIndicator.setSurfaceTextureAvailable(true);
                        MediaLog.getInstance().i(TAG, "mPlayerIndicator.isReadyForPlayback() "
                                        + mPlayerIndicator.isReadyForPlayback()
                                        + ", if true, notify ready for playback",
                                "VideoPlayerView",
                                "notifyTextureAvailable");
                        if (mPlayerIndicator.isReadyForPlayback()) {
                            Log.e(TAG, "notify ready for playback");
                            mPlayerIndicator.notifyAll();
                        }
                    }
                    MediaLog.getInstance().i(TAG, "<< run",
                            "VideoPlayerView",
                            "notifyTextureAvailable");
                }
            });
        }
        MediaLog.getInstance().i(TAG, "<<",
                "VideoPlayerView",
                "notifyTextureAvailable");
    }

    /**
     * 通知暂停
     */
    private void notifyOnVideoStart() {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onVideoStartCallback();
        }
    }

    /**
     * 通知暂停
     */
    private void notifyOnVideoPause() {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onVideoPauseCallback();
        }
    }

    /**
     * 通知停止
     */
    private void notifyOnVideoStopped() {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onVideoStoppedCallback();
        }
    }

    /**
     * 通知视频大小改变
     *
     * @param mp     MediaPlayer对象
     * @param width  宽度
     * @param height 高度
     */
    private void notifyOnVideoSizeChanged(MediaPlayer mp, int width, int height) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onVideoSizeChangedCallback(mp, width, height);
        }
    }

    /**
     * 通知视频播放完成
     *
     * @param mp MediaPlayer对象
     */
    private void notifyOnVideoCompletion(MediaPlayer mp) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onCompletionCallback(mp);
        }
    }

    /**
     * 通知视频播放错误
     *
     * @param mp    MediaPlayer对象
     * @param what  参数
     * @param extra 详细参数
     */
    private void notifyOnVideoError(MediaPlayer mp, int what, int extra) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onErrorCallback(mp, what, extra);
        }
    }

    /**
     * 通知视频播放详情
     *
     * @param mp    MediaPlayer对象
     * @param what  参数
     * @param extra 详细参数
     */
    private void notifyOnVideoInfo(MediaPlayer mp, int what, int extra) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onInfoCallback(mp, what, extra);
        }
    }

    /**
     * 通知视频播放缓存进度
     *
     * @param mp      MediaPlayer对象
     * @param percent 百分比
     */
    private void notifyOnVideoBufferingUpdate(MediaPlayer mp, int percent) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onBufferingUpdateCallback(mp, percent);
        }
    }

    /**
     * 通知视频快进快退完成
     *
     * @param mp MediaPlayer对象
     */
    private void notifyOnSeekComplete(MediaPlayer mp) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onSeekCompleteCallback(mp);
        }
    }

    /**
     * 通知视频准备完成
     *
     * @param mp MediaPlayer对象
     */
    private void notifyOnVideoPrepared(MediaPlayer mp) {
        List<MediaPlayerWrapper.MediaPlayerStateListener> listCopy;
        synchronized (mMediaPlayerMainThreadListeners) {
            listCopy = new ArrayList<>(mMediaPlayerMainThreadListeners);
        }
        for (MediaPlayerWrapper.MediaPlayerStateListener listener : listCopy) {
            listener.onPreparedCallback(mp);
        }
    }

    /**
     * 当TextureView的SurfaceTexture准备就绪可以使用时调用。
     *
     * @param surface surface
     * @param width   宽
     * @param height  高
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        MediaLog.getInstance().i(TAG, "width " + width + ", height " + height,
                "VideoPlayerView", "onSurfaceTextureAvailable");
        notifyTextureAvailable();
        if (mLocalSurfaceTextureListener != null) {
            mLocalSurfaceTextureListener.onSurfaceTextureAvailable(surface, width, height);
        }
    }

    /**
     * 在SurfaceTexture的大小更改时调用。
     *
     * @param surface surface
     * @param width   宽
     * @param height  高
     */
    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        MediaLog.getInstance().i(TAG, "width " + width + ", height " + height,
                "VideoPlayerView", "onSurfaceTextureSizeChanged");
        if (mLocalSurfaceTextureListener != null) {
            mLocalSurfaceTextureListener.onSurfaceTextureSizeChanged(surface, width, height);
        }

    }

    /**
     * 当指定的SurfaceTexture即将被销毁时调用。
     *
     * @param surface surface
     * @return 如果返回true，在调用此方法后，表面纹理内不应进行渲染。
     * 如果返回false，客户端需要调用release（）。 大多数应用程序应该返回true。
     */
    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        MediaLog.getInstance().i(TAG, ">>",
                "VideoPlayerView",
                "onSurfaceTextureDestroyed");
        if (isAttachedToWindow()) {
            mViewHandlerBackgroundThread.post(new Runnable() {
                @Override
                public void run() {
                    MediaLog.getInstance().i(TAG, ">> run",
                            "VideoPlayerView",
                            "onSurfaceTextureDestroyed");
                    synchronized (mPlayerIndicator) {
                        mPlayerIndicator.setSurfaceTextureAvailable(false);
                        mPlayerIndicator.notifyAll();
                    }
                    MediaLog.getInstance().i(TAG, "<< run",
                            "VideoPlayerView",
                            "onSurfaceTextureDestroyed");
                }
            });
        }
        if (mLocalSurfaceTextureListener != null) {
            mLocalSurfaceTextureListener.onSurfaceTextureDestroyed(surface);
        }
        // 手动释放surfaceTexture
        surface.release();
        MediaLog.getInstance().i(TAG, "<<",
                "VideoPlayerView",
                "onSurfaceTextureDestroyed");
        return false;
    }

    /**
     * 当通过updateTexImage（）更新指定的SurfaceTexture时调用。
     *
     * @param surface surface
     */
    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        if (mLocalSurfaceTextureListener != null) {
            mLocalSurfaceTextureListener.onSurfaceTextureUpdated(surface);
        }

    }

    @Override
    public void onErrorCallback(MediaPlayer mp, int what, int extra) {
        notifyOnVideoError(mp, what, extra);
    }

    @Override
    public void onInfoCallback(MediaPlayer mp, int what, int extra) {
        notifyOnVideoInfo(mp, what, extra);
    }

    @Override
    public void onBufferingUpdateCallback(MediaPlayer mp, int percent) {
        notifyOnVideoBufferingUpdate(mp, percent);
    }

    @Override
    public void onCompletionCallback(MediaPlayer mp) {
        notifyOnVideoCompletion(mp);

    }

    @Override
    public void onSeekCompleteCallback(MediaPlayer mp) {
        notifyOnSeekComplete(mp);
    }

    @Override
    public void onVideoSizeChangedCallback(MediaPlayer mp, int width, int height) {
        MediaLog.getInstance().i(TAG, "width " + width + ", height " + height,
                "VideoPlayerView",
                "onVideoSizeChangedCallback");
        if (width != 0 && height != 0) {
            //设置视频大小
            try {
                setVideoSize(new Point(width, height));
            } catch (IllegalStateException ignored) {
                MediaLog.getInstance().e(TAG, "setVideoSize error"
                                + ", " + MediaLog.getInstance().getErrorContent(ignored),
                        "VideoPlayerView",
                        "onVideoSizeChangedCallback");
            }
            MediaLog.getInstance().i(TAG, ">> onVideoSizeAvailable",
                    "VideoPlayerView",
                    "onVideoSizeChangedCallback");
            if (isAttachedToWindow()) {
                mViewHandlerBackgroundThread.post(new Runnable() {
                    @Override
                    public void run() {
                        MediaLog.getInstance().i(TAG, ">> run",
                                "VideoPlayerView",
                                "onVideoSizeChangedCallback");
                        synchronized (mPlayerIndicator) {
                            mPlayerIndicator.setVideoSize(getMHeight(), getMWidth());
                            MediaLog.getInstance().i(TAG, "mPlayerIndicator -> "
                                            + mPlayerIndicator.toString()
                                            + ", isReadyForPlayback is true notifyAll.",
                                    "VideoPlayerView",
                                    "onVideoSizeChangedCallback");
                            if (mPlayerIndicator.isReadyForPlayback()) {
                                Log.e(TAG, "run, onVideoSizeAvailable, notifyAll");
                                mPlayerIndicator.notifyAll();
                            }
                        }
                        MediaLog.getInstance().i(TAG, "<< run",
                                "VideoPlayerView",
                                "onVideoSizeChangedCallback");
                    }
                });
            }
            MediaLog.getInstance().i(TAG, "<< onVideoSizeAvailable",
                    "VideoPlayerView",
                    "onVideoSizeChangedCallback");
        } else {
            if (isAttachedToWindow()) {
                mViewHandlerBackgroundThread.post(new Runnable() {
                    @Override
                    public void run() {
                        MediaLog.getInstance().i(TAG, "size 0. Probably will be unable to start video",
                                "VideoPlayerView",
                                "onVideoSizeChangedCallback");
                        synchronized (mPlayerIndicator) {
                            mPlayerIndicator.setFailedToPrepareUiForPlayback(true);
                            mPlayerIndicator.notifyAll();
                        }
                    }
                });
            }
        }
        notifyOnVideoSizeChanged(mp, width, height);

    }

    @Override
    public void onPreparedCallback(MediaPlayer mp) {
        notifyOnVideoPrepared(mp);
    }

    @Override
    public void onVideoStartCallback() {
        notifyOnVideoStart();
    }

    @Override
    public void onVideoStoppedCallback() {
        notifyOnVideoStopped();
    }

    @Override
    public void onVideoPauseCallback() {
        notifyOnVideoPause();
    }

    @Override
    public void onCurrentPositionCallback(int position) {
        notifyOnCurrentPosition(position);
    }

    @Override
    public void onDurationCallback(int duration) {
        notifyOnDuration(duration);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        //是否启用手势
        if (mUIOperationListener != null) {
            mUIOperationListener.onSuperTouch(v, event);
        }
        if (isGestureDetectorValid) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                currentPosition = getCurrentPosition();
                duration = getDuration();
                currentVolume = mVoiceManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                maxVolume = mVoiceManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                if (screenBrightness == 0) {
                    screenBrightness = ScreenUtils.getScreenBrightness(getContext());
                }
                recordLight = 0;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                switch (gestureFlag) {
                    case GESTURE_MODIFY_PROGRESS:
                        if (mMediaPlayer != null && isGestureDetectorProgressValid) {
                            if (mUIOperationListener != null) {
                                if (!mUIOperationListener.onGestureDetectorSetProgress(speedPosition)) {
                                    mMediaPlayer.seekTo(speedPosition);
                                }
                            }
                        }
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

    /**
     * 视频播放view手势操作控制类
     */
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
            //是否启用手势
            if (isGestureDetectorValid && e1 != null && e2 != null) {
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
                        if (firstX > getMWidth() * 3.0 / 5) {
                            //音量
                            gestureFlag = GESTURE_MODIFY_VOLUME;
                        } else if (firstX < getMWidth() * 2.0 / 5) {
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
                            mUIOperationListener.onGestureDetectorByScreenLuminance(screenType,
                                    screenBrightness,
                                    (int) (screenBrightness / 2.55));
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
                            volumeDistance = maxVolume * (firstY - laterY) / getMHeight();
                            volume = currentVolume
                                    + volumeDistance > maxVolume ? maxVolume : currentVolume
                                    + volumeDistance;
                        } else if (laterY - firstY > 0) {
                            //减小音量
                            volumeDistance = maxVolume * (laterY - firstY) / getMHeight();
                            volume = currentVolume
                                    - volumeDistance < 0 ? 0 : currentVolume
                                    - volumeDistance;
                        }
                        mVoiceManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                                (int) volume,
                                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                        if (mUIOperationListener != null) {
                            mUIOperationListener.onGestureDetectorByVolume(volumeType,
                                    (int) volume,
                                    (int) ((volume * 100) / maxVolume));
                        }
                        break;
                    case GESTURE_MODIFY_PROGRESS:
                        //进度
                        //验证是否是快进还是快退
                        if (isGestureDetectorProgressValid) {
                            boolean progressType = false;
                            if (distanceX > STEP_X) {
                                //快退
                                progressType = false;
                            } else if (distanceX < -STEP_X) {
                                //快进
                                progressType = true;
                            }
                            //实际播放时间是前进还是后退
                            //播放时间移动的距离
                            float playDistance;
                            if (firstX - laterX > 0) {
                                //播放后退距离
                                playDistance = duration * (firstX - laterX) / getMWidth();
                                speedPosition = (int) (currentPosition - playDistance)
                                        < 0
                                        ? 0 : (int) (currentPosition - playDistance);
                            } else if (laterX - firstX > 0) {
                                //播放前进距离
                                playDistance = duration * (laterX - firstX) / getMWidth();
                                speedPosition = (int) (currentPosition + playDistance)
                                        > duration
                                        ? duration - 1 : (int) (currentPosition + playDistance);
                            }
                            if (mUIOperationListener != null) {
                                mUIOperationListener.onGestureDetectorByProgress(progressType,
                                        speedPosition,
                                        0);
                            }
                        }
                        break;
                    default:
                        break;
                }
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
         * 释放onTouch事件
         *
         * @param v     view
         * @param event 动作
         */
        void onSuperTouch(View v, MotionEvent event);

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
         * 滑动结束后设置进度回调
         *
         * @param position 播放位置
         * @return true-自己设置播放进度 default-false
         */
        boolean onGestureDetectorSetProgress(int position);

        /**
         * 手势结束
         *
         * @param gestureType 手势类型
         */
        void onGestureDetectorCancel(int gestureType);
    }

    /**
     * 手势超值抽象类
     */
    public static abstract class AbstractVideoGestureDetectorUiOperationListener
            implements VideoGestureDetectorUiOperationListener {

        @Override
        public void onSuperTouch(View v, MotionEvent event) {
            gestureDetectorBySuperTouch(v, event);
        }

        @Override
        public void onGestureDetectorByScreenLuminance(boolean screenType, int number, int percent) {
            gestureDetectorByScreenLuminance(screenType, number, percent);
        }

        @Override
        public void onGestureDetectorByVolume(boolean volumeType, int number, int percent) {
            gestureDetectorByVolume(volumeType, number, percent);
        }

        @Override
        public void onGestureDetectorByProgress(boolean progressType, int number, int percent) {
            gestureDetectorByProgress(progressType, number, percent);
        }

        @Override
        public boolean onGestureDetectorSetProgress(int position) {
            return gestureDetectorSetProgress(position);
        }

        @Override
        public void onGestureDetectorCancel(int gestureType) {
            gestureDetectorCancel(gestureType);
        }

        public void gestureDetectorBySuperTouch(View v, MotionEvent event) {

        }

        public void gestureDetectorByScreenLuminance(boolean screenType, int number, int percent) {

        }

        public void gestureDetectorByVolume(boolean volumeType, int number, int percent) {

        }

        public void gestureDetectorByProgress(boolean progressType, int number, int percent) {

        }

        public boolean gestureDetectorSetProgress(int position) {
            return false;
        }

        public void gestureDetectorCancel(int gestureType) {

        }
    }
}
