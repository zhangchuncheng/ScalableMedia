package com.chuncheng.sample.media.video.manager.mssage.player;

import android.content.Context;

import com.chuncheng.sample.media.video.manager.PlayerMessageState;
import com.chuncheng.sample.media.video.manager.VideoPlayerManagerCallback;
import com.chuncheng.sample.media.video.ui.VideoPlayerView;

/**
 * Description:设置Uri视频数据源消息
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class SetUriDataSourceMessage extends AbstractSetDataSourceMessage {
    private String mUriPath;
    private Context mContext;

    public SetUriDataSourceMessage(Context context, VideoPlayerView playerView, String uriPath, VideoPlayerManagerCallback callback) {
        super(playerView, callback);
        mUriPath = uriPath;
        mContext = context;
    }

    @Override
    protected void performAction(VideoPlayerView videoPlayerView) {
        PlayerMessageState currentState = getCurrentState();
        switch (currentState) {
            case SETTING_DATA_SOURCE:
                videoPlayerView.setDataSource(mContext, mUriPath);
                mPlayerMessageState = PlayerMessageState.INITIALIZED;
                break;
            default:
                break;
        }
    }

    @Override
    protected String getName() {
        return "name:SetUriDataSourceMessage";
    }
}
