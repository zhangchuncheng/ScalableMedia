package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:重置播放消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class ResetMessage extends AbstractPlayerMessage {
    private PlayerMessageState mPlayerMessageState;

    public ResetMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        videoPlayerView.reset();
        mPlayerMessageState = PlayerMessageState.IDLE;
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.RESETTING;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return mPlayerMessageState;
    }

    @Override
    protected String getName() {
        return "name:ResetMessage";
    }
}
