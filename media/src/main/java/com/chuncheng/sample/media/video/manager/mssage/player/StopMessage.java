package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:停止播放消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class StopMessage extends AbstractPlayerMessage {
    private PlayerMessageState mPlayerMessageState;

    public StopMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case STOPPING:
                videoPlayerView.stop();
                mPlayerMessageState = PlayerMessageState.STOPPED;
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
            case STARTED:
            case PAUSED:
            case PLAYBACK_COMPLETED:
            case STOPPED:
                result = PlayerMessageState.STOPPING;
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
        return "name:StopMessage";
    }
}
