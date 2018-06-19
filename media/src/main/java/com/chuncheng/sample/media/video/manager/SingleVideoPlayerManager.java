package com.chuncheng.sample.media.video.manager;

import android.media.MediaPlayer;
import android.text.TextUtils;

import com.chuncheng.sample.media.utils.log.MediaLog;
import com.chuncheng.sample.media.video.entity.VideoPlayerBean;
import com.chuncheng.sample.media.video.manager.mssage.MessagesHandlerThread;
import com.chuncheng.sample.media.video.manager.mssage.player.ClearAbstractPlayerInstanceMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.CreateNewPlayerInstanceMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.PauseMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.PrepareMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.ReleaseMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.ResetMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.SeekToMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.SetNewViewForPlaybackMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.SetUrlDataSourceMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.StartMessage;
import com.chuncheng.sample.media.video.manager.mssage.player.StopMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;
import com.chuncheng.sample.media.video.ui.wrapper.MediaPlayerWrapper;

import java.io.File;

/**
 * Description:VideoPlayerManager实现类
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class SingleVideoPlayerManager implements VideoPlayerManager,
        VideoPlayerManagerCallback, MediaPlayerWrapper.MediaPlayerStateListener {
    private static final String TAG = "Manager";
    /** 校验url 正则表达式 */
    private static final String REGULAR_URL = "(http|https)+://[^\\s]*";
    /** 当前播放器 */
    private VideoPlayerView mPlayerView = null;
    /** 当前播放器状态 */
    private PlayerMessageState mPlayerMessageState = PlayerMessageState.IDLE;
    /** 线程池 处理视频消息 */
    private final MessagesHandlerThread mPlayerHandler = new MessagesHandlerThread();

    @Override
    public void playVideo(VideoPlayerView view, VideoPlayerBean bean) {
        try {
            MediaLog.getInstance().i(TAG, ">>",
                    "SingleVideoPlayerManager",
                    "playVideo");
            MediaLog.getInstance().i(TAG, "bean -> " + bean.toString() + ", state " + mPlayerMessageState,
                    "SingleVideoPlayerManager",
                    "playVideo");
            //1. 暂停队列处理
            mPlayerHandler.pauseQueueProcessing(TAG);

            //2.校验播放器
            if (view != null) {
                //检查当前播放器是否有效
                boolean currentPlayerIsActive = mPlayerView == view;
                //当前播放的资源地址是否一致
                boolean currentUrlIsSame = mPlayerView != null
                        && bean.getUrl().equals(mPlayerView.getVideoUrlDataSource());
                //当前是否正在播放并且播放地址一致
                boolean currentStateIsPlayingAndUrlIsSame = isInPlaybackState() && currentUrlIsSame;
                MediaLog.getInstance().i(TAG, "currentPlayerIsActive " + currentPlayerIsActive
                                + ", currentUrlIsSame " + currentUrlIsSame
                                + ", currentStateIsPlayingAndUrlIsSame "
                                + currentStateIsPlayingAndUrlIsSame,
                        "SingleVideoPlayerManager",
                        "playVideo");
                //如果当前view和将要播放的view一致
                //并且为正在播放状态 并且播放的资源地址也一致 并且不是重新播放
                //则不执行任何操作
                //否则执行播放视频操作
                if (currentPlayerIsActive && currentStateIsPlayingAndUrlIsSame && !bean.isReplay()) {
                    MediaLog.getInstance().i(TAG, "mPlayerView is already in state " + mPlayerMessageState,
                            "SingleVideoPlayerManager",
                            "playVideo");
                } else {
                    //开启新的播放器
                    //播放前验证资源地址是否正确
                    if (!TextUtils.isEmpty(bean.getUrl())) {
                        //校验URL是否正确 http/https链接 本地文件链接
                        boolean urlIsCorrect = false;
                        if (bean.getUrl().matches(REGULAR_URL)) {
                            urlIsCorrect = true;
                        } else {
                            File file = new File(bean.getUrl());
                            if (file.exists()) {
                                urlIsCorrect = true;
                            }
                        }
                        MediaLog.getInstance().i(TAG, "urlIsCorrect " + urlIsCorrect +
                                        ", mCurrentPlayerState " + mPlayerMessageState,
                                "SingleVideoPlayerManager",
                                "playVideo");
                        //如果资源地址校验正确
                        //则创建视频播放器
                        if (urlIsCorrect) {
                            MediaLog.getInstance().i(TAG, "mCurrentPlayerState " + mPlayerMessageState,
                                    "SingleVideoPlayerManager",
                                    "playVideo");
                            //设置监听
                            view.addMediaPlayerListener(this);
                            //清除线程消息
                            mPlayerHandler.clearAllPendingMessages(TAG);
                            if (mPlayerView != null) {
                                MediaLog.getInstance().i(TAG, "mPlayerView is not null clear player "
                                                + "instance message, mCurrentPlayerState "
                                                + mPlayerMessageState,
                                        "SingleVideoPlayerManager",
                                        "playVideo");
                                //停止
                                mPlayerHandler.addMessage(new StopMessage(mPlayerView, this));
                                //重置
                                mPlayerHandler.addMessage(new ResetMessage(mPlayerView, this));
                                //释放
                                mPlayerHandler.addMessage(new ReleaseMessage(mPlayerView, this));
                                //清除
                                //当前视频播放器
                                mPlayerHandler.addMessage(new ClearAbstractPlayerInstanceMessage(mPlayerView, this));
                            }
                            MediaLog.getInstance().i(TAG, "set new player instance message,"
                                            + " mCurrentPlayerState " + mPlayerMessageState,
                                    "SingleVideoPlayerManager",
                                    "playVideo");
                            //设置新的播放器
                            mPlayerHandler.addMessage(new SetNewViewForPlaybackMessage(view, this));
                            //创建视频播放器
                            mPlayerHandler.addMessage(new CreateNewPlayerInstanceMessage(view, this));
                            //设置数据源
                            mPlayerHandler.addMessage(new SetUrlDataSourceMessage(view, bean.getUrl(), this));
                            //准备视频
                            mPlayerHandler.addMessage(new PrepareMessage(view, this));
                            MediaLog.getInstance().i(TAG, "bean.isSeekTo " + bean.isSeekTo()
                                            + ", bean.isPlayer " + bean.isPlayer()
                                            + ", mCurrentPlayerState " + mPlayerMessageState,
                                    "SingleVideoPlayerManager",
                                    "playVideo");
                            //定位视频播放位置
                            if (bean.isSeekTo()) {
                                mPlayerHandler.addMessage(new SeekToMessage(view, bean.getPosition(), this));
                            }
                            //启动视频开始播放
                            if (bean.isPlayer()) {
                                mPlayerHandler.addMessage(new StartMessage(view, this));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "error bean " + bean.toString()
                            + ", exception " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "SingleVideoPlayerManager",
                    "playVideo");
        } finally {
            //3. 恢复停止队列
            mPlayerHandler.resumeQueueProcessing(TAG);
            MediaLog.getInstance().i(TAG, "videoUrl " + bean.getUrl(),
                    "SingleVideoPlayerManager",
                    "playVideo");
            MediaLog.getInstance().i(TAG, "<<",
                    "SingleVideoPlayerManager",
                    "playVideo");
        }
    }

    @Override
    public void startVideo(VideoPlayerView view) {
        try {
            MediaLog.getInstance().i(TAG, ">>",
                    "SingleVideoPlayerManager",
                    "startVideo");
            MediaLog.getInstance().i(TAG, "mPlayerMessageState " + mPlayerMessageState,
                    "SingleVideoPlayerManager",
                    "startVideo");
            //1. 暂停队列处理
            mPlayerHandler.pauseQueueProcessing(TAG);
            //2.校验播放器
            if (mPlayerView != null) {
                mPlayerHandler.addMessage(new StartMessage(mPlayerView, this));
            } else {
                MediaLog.getInstance().e(TAG, "mPlayerView is null"
                                + ", mPlayerMessageState " + mPlayerMessageState,
                        "SingleVideoPlayerManager",
                        "startVideo");
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "error, mPlayerMessageState " + mPlayerMessageState
                            + ", exception " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "SingleVideoPlayerManager",
                    "startVideo");
        } finally {
            //3. 恢复停止队列
            mPlayerHandler.resumeQueueProcessing(TAG);
            MediaLog.getInstance().i(TAG, "mPlayerMessageState " + mPlayerMessageState,
                    "SingleVideoPlayerManager",
                    "startVideo");
            MediaLog.getInstance().i(TAG, "<<",
                    "SingleVideoPlayerManager",
                    "startVideo");
        }
    }

    @Override
    public void pauseVideo(VideoPlayerView view) {
        try {
            MediaLog.getInstance().i(TAG, ">>",
                    "SingleVideoPlayerManager",
                    "pauseVideo");
            MediaLog.getInstance().i(TAG, "mPlayerMessageState " + mPlayerMessageState,
                    "SingleVideoPlayerManager",
                    "pauseVideo");
            //1. 暂停队列处理
            mPlayerHandler.pauseQueueProcessing(TAG);
            //2.校验播放器
            if (mPlayerView != null) {
                mPlayerHandler.addMessage(new PauseMessage(mPlayerView, this));
            } else {
                MediaLog.getInstance().e(TAG, "mPlayerView is null"
                                + ", mPlayerMessageState " + mPlayerMessageState,
                        "SingleVideoPlayerManager",
                        "pauseVideo");
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "error mPlayerMessageState " + mPlayerMessageState
                            + ", exception " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "SingleVideoPlayerManager",
                    "pauseVideo");
        } finally {
            //3. 恢复停止队列
            mPlayerHandler.resumeQueueProcessing(TAG);
            MediaLog.getInstance().i(TAG, "mPlayerMessageState " + mPlayerMessageState,
                    "SingleVideoPlayerManager",
                    "pauseVideo");
            MediaLog.getInstance().i(TAG, "<<",
                    "SingleVideoPlayerManager",
                    "pauseVideo");
        }
    }

    @Override
    public void seekToVideo(VideoPlayerView view, VideoPlayerBean bean) {
        try {
            MediaLog.getInstance().i(TAG, ">>", "SingleVideoPlayerManager",
                    "seekToVideo");
            MediaLog.getInstance().i(TAG, "mPlayerMessageState " + mPlayerMessageState
                            + ", position " + bean.getPosition()
                            + ", duration " + view.getDuration(),
                    "SingleVideoPlayerManager",
                    "seekToVideo");
            //1. 暂停队列处理
            mPlayerHandler.pauseQueueProcessing(TAG);
            //2.校验播放器
            if (mPlayerView != null) {
                mPlayerHandler.addMessage(new SeekToMessage(mPlayerView, bean.getPosition(), this));
            } else {
                MediaLog.getInstance().e(TAG, "mPlayerView is null"
                                + ", mPlayerMessageState " + mPlayerMessageState
                                + ", position" + bean.getPosition(),
                        "SingleVideoPlayerManager",
                        "seekToVideo");
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "error mPlayerMessageState " + mPlayerMessageState
                            + ", position" + bean.getPosition()
                            + ", exception " + e.getMessage(),
                    "SingleVideoPlayerManager",
                    "seekToVideo");
        } finally {
            //3. 恢复停止队列
            mPlayerHandler.resumeQueueProcessing(TAG);
            MediaLog.getInstance().i(TAG, "mPlayerMessageState " + mPlayerMessageState
                            + ", position" + bean.getPosition(),
                    "SingleVideoPlayerManager",
                    "seekToVideo");
            MediaLog.getInstance().i(TAG, "<<",
                    "SingleVideoPlayerManager",
                    "seekToVideo");

        }
    }

    @Override
    public void stopVideo(VideoPlayerView view) {
        try {
            MediaLog.getInstance().i(TAG, ">>",
                    "SingleVideoPlayerManager",
                    "stopVideo");
            MediaLog.getInstance().i(TAG, "mPlayerMessageState " + mPlayerMessageState,
                    "SingleVideoPlayerManager",
                    "stopVideo");
            //1. 暂停队列处理
            mPlayerHandler.pauseQueueProcessing(TAG);
            //2. 清除线程消息
            mPlayerHandler.clearAllPendingMessages(TAG);
            //3. 校验播放器
            if (mPlayerView != null) {
                //移除监听
                view.removeMediaPlayerListener(this);
                //停止
                mPlayerHandler.addMessage(new StopMessage(mPlayerView, this));
                //重置
                mPlayerHandler.addMessage(new ResetMessage(mPlayerView, this));
                //释放
                mPlayerHandler.addMessage(new ReleaseMessage(mPlayerView, this));
                //清除
                //当前视频播放器
                mPlayerHandler.addMessage(new ClearAbstractPlayerInstanceMessage(mPlayerView, this));
            } else {
                MediaLog.getInstance().e(TAG, "mPlayerView is null"
                                + ", mPlayerMessageState " + mPlayerMessageState,
                        "SingleVideoPlayerManager",
                        "stopVideo");
            }
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "error mPlayerMessageState " + mPlayerMessageState
                            + ", exception " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "SingleVideoPlayerManager",
                    "stopVideo");
        } finally {
            //4. 恢复停止队列
            mPlayerHandler.resumeQueueProcessing(TAG);
            MediaLog.getInstance().i(TAG, "mPlayerMessageState " + mPlayerMessageState,
                    "SingleVideoPlayerManager",
                    "stopVideo");
            MediaLog.getInstance().i(TAG, "<<",
                    "SingleVideoPlayerManager",
                    "stopVideo");
        }
    }

    @Override
    public boolean isInPlaybackState() {
        boolean isPlaying = mPlayerMessageState == PlayerMessageState.STARTED
                || mPlayerMessageState == PlayerMessageState.STARTING;
        MediaLog.getInstance().i(TAG, "isInPlaybackState: " + isPlaying,
                "SingleVideoPlayerManager",
                "isInPlaybackState");
        return isPlaying;
    }

    /**
     * 清除线程中待执行的消息
     */
    private void clearAllMessage() {
        MediaLog.getInstance().i(TAG, ">>",
                "SingleVideoPlayerManager",
                "clearAllMessage");
        try {
            mPlayerHandler.pauseQueueProcessing(TAG);
            mPlayerHandler.clearAllPendingMessages(TAG);
        } catch (Exception e) {
            MediaLog.getInstance().e(TAG, "error"
                            + ", exception " + e.getMessage()
                            + ", " + MediaLog.getInstance().getErrorContent(e),
                    "SingleVideoPlayerManager",
                    "clearAllMessage");
        } finally {
            mPlayerHandler.resumeQueueProcessing(TAG);
            MediaLog.getInstance().i(TAG, "<<",
                    "SingleVideoPlayerManager",
                    "clearAllMessage");
        }
    }

    /*----------------------------VideoPlayerManagerCallback-开始---------------------------------*/

    @Override
    public void setCurrentView(VideoPlayerView newPlayerView) {
        MediaLog.getInstance().i(TAG, "current player state " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "setCurrentView");
        mPlayerView = newPlayerView;
    }

    @Override
    public void setVideoPlayerState(VideoPlayerView newPlayerView,
                                    PlayerMessageState playerMessageState) {
        MediaLog.getInstance().i(TAG, "current player state " + mPlayerMessageState
                        + ", new player state " + playerMessageState,
                "SingleVideoPlayerManager",
                "setVideoPlayerState");
        if (playerMessageState != null) {
            mPlayerMessageState = playerMessageState;
        }
    }

    @Override
    public PlayerMessageState getCurrentPlayerState() {
        MediaLog.getInstance().i(TAG, "current player state " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "getCurrentPlayerState");
        return mPlayerMessageState;
    }

    /*----------------------------VideoPlayerManagerCallback-结束---------------------------------*/

    /*----------------------------MediaPlayerWrapper.MediaPlayerStateListener-开始----------------*/

    @Override
    public void onErrorCallback(MediaPlayer mp, int what, int extra) {
        MediaLog.getInstance().i(TAG, "what " + what + ", extra " + extra
                        + ", mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onErrorCallback");
        mPlayerMessageState = PlayerMessageState.ERROR;
        clearAllMessage();
    }

    @Override
    public void onInfoCallback(MediaPlayer mp, int what, int extra) {
        MediaLog.getInstance().i(TAG, "what " + what + ", extra " + extra
                        + ", mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onInfoCallback");
    }

    @Override
    public void onBufferingUpdateCallback(MediaPlayer mp, int percent) {
        /*MediaLog.getInstance().i(TAG, "percent " + percent +
                        ", mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onBufferingUpdateCallback");*/
    }

    @Override
    public void onCompletionCallback(MediaPlayer mp) {
        MediaLog.getInstance().i(TAG, "percent, mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onCompletionCallback");
        mPlayerMessageState = PlayerMessageState.PLAYBACK_COMPLETED;
    }

    @Override
    public void onSeekCompleteCallback(MediaPlayer mp) {
        MediaLog.getInstance().i(TAG, "seekTo complete, mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onSeekCompleteCallback");
    }

    @Override
    public void onVideoSizeChangedCallback(MediaPlayer mp, int width, int height) {
        MediaLog.getInstance().i(TAG, "width " + width + ", height " + height +
                        ", mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onVideoSizeChangedCallback");
    }

    @Override
    public void onPreparedCallback(MediaPlayer mp) {
        MediaLog.getInstance().i(TAG, "media player is prepared, mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onPreparedCallback");
    }

    @Override
    public void onVideoStartCallback() {
        MediaLog.getInstance().i(TAG, "video started playing, mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onVideoStartCallback");
    }

    @Override
    public void onVideoStoppedCallback() {
        MediaLog.getInstance().i(TAG, "video stopped playing, mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onVideoStoppedCallback");
    }

    @Override
    public void onVideoPauseCallback() {
        MediaLog.getInstance().i(TAG, "video is paused, mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onVideoPauseCallback");
    }

    @Override
    public void onCurrentPositionCallback(int position) {
    }

    @Override
    public void onDurationCallback(int duration) {
        MediaLog.getInstance().i(TAG, "video duration " + duration
                        + ", mPlayerMessageState " + mPlayerMessageState,
                "SingleVideoPlayerManager",
                "onDurationCallback");
    }

    /*----------------------------MediaPlayerWrapper.MediaPlayerStateListener-结束----------------*/
}
