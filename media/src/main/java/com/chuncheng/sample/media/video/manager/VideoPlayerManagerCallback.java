package com.chuncheng.sample.media.video.manager;

import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:videoPlayerManagerCallback
 * 1.设置当前操作的view
 * 2.设置当前view的状态
 * 3.返回当前view的状态
 *
 * @author: chuncheng
 * @date: 2017/3/22
 */

public interface VideoPlayerManagerCallback {

    /**
     * 设置显示view
     *
     * @param newPlayerView view
     */
    void setCurrentView(VideoPlayerView newPlayerView);

    /**
     * 设置vie状态
     *
     * @param newPlayerView      view
     * @param playerMessageState 状态
     */
    void setVideoPlayerState(VideoPlayerView newPlayerView, PlayerMessageState playerMessageState);

    /**
     * 获取当前状态
     *
     * @return 状态
     */
    PlayerMessageState getCurrentPlayerState();
}
