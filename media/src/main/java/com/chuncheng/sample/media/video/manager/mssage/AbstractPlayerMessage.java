package com.chuncheng.sample.media.video.manager.mssage;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description: 播放消息基类
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public abstract class AbstractPlayerMessage implements InvokeMessage {
    protected static final String TAG = "Message";
    private VideoPlayerView mPlayerView;
    private VideoPlayerManagerCallback mCallback;

    public AbstractPlayerMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        mPlayerView = playerView;
        mCallback = callback;
    }

    /**
     * 回调接口
     *
     * @return 获取当前播放器状态
     */
    protected PlayerMessageState getCurrentState() {
        return mCallback.getCurrentPlayerState();
    }

    @Override
    public void runMessage() {
        performAction(mPlayerView);
    }

    @Override
    public void polledFromQueue() {
        mCallback.setVideoPlayerState(mPlayerView, stateBefore());
    }

    @Override
    public void messageFinished() {
        mCallback.setVideoPlayerState(mPlayerView, stateAfter());
    }

    /**
     * 执行操作
     *
     * @param videoPlayerView 当前播放器
     */
    protected abstract void performAction(VideoPlayerView videoPlayerView);

    /**
     * 播放前操作
     *
     * @return 当前播放器状态
     */
    protected abstract PlayerMessageState stateBefore();

    /**
     * 播放后操作
     *
     * @return 当前播放器状态
     */
    protected abstract PlayerMessageState stateAfter();

    /**
     * 获取名称
     *
     * @return 名称
     */
    protected abstract String getName();

    @Override
    public String toString() {
        return getName();
    }


}