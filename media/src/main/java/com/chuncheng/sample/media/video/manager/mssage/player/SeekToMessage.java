package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:定位播放位置消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/23
 */

public class SeekToMessage extends AbstractPlayerMessage {
    private PlayerMessageState mPlayerMessageState;
    private int seekToPosition;

    public SeekToMessage(VideoPlayerView playerView, int position, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
        seekToPosition = position;
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case PREPARED:
            case STARTED:
            case PAUSED:
            case PLAYBACK_COMPLETED:
                videoPlayerView.seekToForThread(seekToPosition);
                mPlayerMessageState = currentState;
                break;
            default:
                break;
        }
    }

    @Override
    protected PlayerMessageState stateBefore() {
        PlayerMessageState currentState = getCurrentState();
        return currentState;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return mPlayerMessageState;
    }

    @Override
    protected String getName() {
        return "name:SeekToMessage";
    }
}
