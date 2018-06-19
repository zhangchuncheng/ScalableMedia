package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.manager.mssage.AbstractPlayerMessage;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:设置数据源消息
 *
 * @author: chuncheng
 * @date: 2017/3/22
 */

abstract class AbstractSetDataSourceMessage extends AbstractPlayerMessage {
    PlayerMessageState mPlayerMessageState;

    AbstractSetDataSourceMessage(VideoPlayerView playerView, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
    }

    @Override
    protected PlayerMessageState stateBefore() {
        PlayerMessageState result = null;
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case IDLE:
                result = PlayerMessageState.SETTING_DATA_SOURCE;
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
}
