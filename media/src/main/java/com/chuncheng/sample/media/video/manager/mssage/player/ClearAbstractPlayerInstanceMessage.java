package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:清除播放器实例消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class ClearAbstractPlayerInstanceMessage extends AbstractPlayerMessage {
    private PlayerMessageState mPlayerMessageState;

    public ClearAbstractPlayerInstanceMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        videoPlayerView.clearPlayerInstance();
        mPlayerMessageState = PlayerMessageState.PLAYER_INSTANCE_CLEARED;
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.CLEARING_PLAYER_INSTANCE;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return mPlayerMessageState;
    }

    @Override
    protected String getName() {
        return "name:ClearPlayerInstanceMessage";
    }
}
