package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:创建播放器对象消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class CreateNewPlayerInstanceMessage extends AbstractPlayerMessage {
    private PlayerMessageState mPlayerMessageState;

    public CreateNewPlayerInstanceMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case CREATING_PLAYER_INSTANCE:
                videoPlayerView.createNewPlayerInstance();
                mPlayerMessageState = PlayerMessageState.IDLE;
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
            case PLAYER_NEW_SET:
                result = PlayerMessageState.CREATING_PLAYER_INSTANCE;
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
        return "name:CreateNewPlayerInstanceMessage";
    }
}
