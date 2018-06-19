package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:暂停播放消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class PauseMessage extends AbstractPlayerMessage {
    private PlayerMessageState mPlayerMessageState;

    public PauseMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case PAUSING:
                videoPlayerView.pause();
                mPlayerMessageState = PlayerMessageState.PAUSED;
                break;
            default:
                break;
        }
    }

    @Override
    protected PlayerMessageState stateBefore() {
        PlayerMessageState currentState = getCurrentState();
        PlayerMessageState result = null;
        switch (currentState) {
            case STARTED:
            case PAUSED:
                result = PlayerMessageState.PAUSING;
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
        return "name:PauseMessage";
    }
}
