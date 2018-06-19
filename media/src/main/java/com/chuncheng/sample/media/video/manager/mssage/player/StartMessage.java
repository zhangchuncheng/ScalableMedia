package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:开始播放消息
 * STARTED PAUSING PAUSED STOPPING STOPPED
 * 以上状态触发播放的情况下待处理
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class StartMessage extends AbstractPlayerMessage {
    private PlayerMessageState mPlayerMessageState;

    public StartMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case STARTING:
                videoPlayerView.start();
                mPlayerMessageState = PlayerMessageState.STARTED;
                break;
            default:
                break;
        }
    }

    @Override
    protected PlayerMessageState stateBefore() {
        PlayerMessageState result = null;
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case PREPARED:
            case PAUSED:
            case PLAYBACK_COMPLETED:
            case STARTED:
                result = PlayerMessageState.STARTING;
                break;
            default:
                break;
        }
        return result;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return mPlayerMessageState;
    }

    @Override
    protected String getName() {
        return "name:StartMessage";
    }
}
