package com.chuncheng.sample.media.video.manager;

/**
 * Description: 播放器状态控制枚举
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */
public enum PlayerMessageState {
    /**
     * 播放器状态
     */
    SETTING_NEW_PLAYER,
    PLAYER_NEW_SET,
    CREATING_PLAYER_INSTANCE,
    IDLE,
    SETTING_DATA_SOURCE,
    INITIALIZED,
    PREPARING,
    PREPARED,
    STARTING,
    STARTED,
    PAUSING,
    PAUSED,
    STOPPING,
    STOPPED,
    RESETTING,
    RELEASING,
    END,
    PLAYBACK_COMPLETED,
    CLEARING_PLAYER_INSTANCE,
    PLAYER_INSTANCE_CLEARED,
    ERROR,
}
