package com.chuncheng.sample.media.video.manager;

import com.chuncheng.sample.media.video.entity.VideoPlayerBean;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:视频管理接口
 * 1.播放视频
 * 2.暂停播放
 * 3.停止播放
 * 4.重置播放器
 * 5.暂停后开始播放
 * 6.从指定位置开始播放
 *
 * @author: zhangchuncheng
 * @date: 2017/3/21
 */

public interface VideoPlayerManager {

    /**
     * 根据参数
     * 创建播放器
     * 开始播放
     *
     * @param view view
     * @param bean 参数
     */
    void playVideo(VideoPlayerView view, VideoPlayerBean bean);

    /**
     * 视频播放
     *
     * @param view view
     */
    void startVideo(VideoPlayerView view);

    /**
     * 视频暂停
     *
     * @param view view
     */
    void pauseVideo(VideoPlayerView view);

    /**
     * 视频定位位置
     *
     * @param view view
     * @param bean 参数
     */
    void seekToVideo(VideoPlayerView view, VideoPlayerBean bean);

    /**
     * 视频停止
     * 停止之后必须重新调用playVideo才能开始播放
     *
     * @param view view
     */
    void stopVideo(VideoPlayerView view);

    /**
     * 判断是否正在播放
     *
     * @return true-正在播放 false-反之
     */
    boolean isInPlaybackState();

}
