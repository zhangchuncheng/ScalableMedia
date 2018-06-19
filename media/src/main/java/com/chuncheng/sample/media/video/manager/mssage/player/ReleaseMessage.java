package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:释放播放器消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class ReleaseMessage extends AbstractPlayerMessage {
    private PlayerMessageState mPlayerMessageState;

    public ReleaseMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        videoPlayerView.release();
        mPlayerMessageState = PlayerMessageState.END;
    }

    @Override
    protected PlayerMessageState stateBefore() {
        return PlayerMessageState.RELEASING;
    }

    @Override
    protected PlayerMessageState stateAfter() {
        return mPlayerMessageState;
    }

    @Override
    protected String getName() {
        return "name:ReleaseMessage";
    }
}
