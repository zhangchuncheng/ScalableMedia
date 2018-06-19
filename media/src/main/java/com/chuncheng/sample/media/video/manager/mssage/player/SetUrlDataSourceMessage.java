package com.chuncheng.sample.media.video.manager.mssage.player;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:设置网络视频数据源消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class SetUrlDataSourceMessage extends AbstractSetDataSourceMessage {
    private String mUrl;

    public SetUrlDataSourceMessage(VideoPlayerView playerView, String url, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
        mUrl = url;
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case SETTING_DATA_SOURCE:
                videoPlayerView.setDataSource(mUrl);
                mPlayerMessageState = PlayerMessageState.INITIALIZED;
                break;
            default:
                break;
        }
    }

    @Override
    protected String getName() {
        return "name:SetUrlDataSourceMessage";
    }
}
