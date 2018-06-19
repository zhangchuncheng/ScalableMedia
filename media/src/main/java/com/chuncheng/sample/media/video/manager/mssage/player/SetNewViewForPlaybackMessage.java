package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description: 设置新的播放器消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class SetNewViewForPlaybackMessage extends AbstractPlayerMessage {
    private VideoPlayerManagerCallback mCallback;

    public SetNewViewForPlaybackMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
        mCallback = callback;
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        mCallback.setCurrentView(videoPlayerView);
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.SETTING_NEW_PLAYER;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return PlayerMessageState.PLAYER_NEW_SET;
    }

    @Override
    protected String getName() {
        return "name:SetNewViewForPlaybackMessage";
    }
}
