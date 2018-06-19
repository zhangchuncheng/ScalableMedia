package com.chuncheng.sample.media.video.ui;

import android.util.Pair;

/**
 * Description: 播放指示器
 *
 * @author: zhangchuncheng
 * @date: 2017/7/27
 */

class PlayerIndicator {
    private static final String TAG = "PlayerIndicator";
    private Pair<Integer, Integer> mVideoSize;
    private boolean mSurfaceTextureAvailable;
    private boolean mFailedToPrepareUiForPlayback;

    /**
     * 视频大小是否有效
     *
     * @return 状态值
     */
    private boolean isVideoSizeAvailable() {
        return mVideoSize.first != null && mVideoSize.second != null;
    }

    /**
     * 设置视频大小
     *
     * @param videoHeight 高度
     * @param videoWidth  快递
     */
    void setVideoSize(Integer videoHeight, Integer videoWidth) {
        mVideoSize = new Pair<>(videoHeight, videoWidth);
    }

    /**
     * 当前显示view是否有效
     *
     * @return 有效状态
     */
    boolean isSurfaceTextureAvailable() {
        return mSurfaceTextureAvailable;
    }

    /**
     * 设置view是否有效
     */
    void setSurfaceTextureAvailable(boolean available) {
        mSurfaceTextureAvailable = available;
    }

    /**
     * 当前视频是否准备完成，并且已设置视频大小
     *
     * @return 当前状态
     */
    boolean isFailedToPrepareUiForPlayback() {
        return mFailedToPrepareUiForPlayback;
    }

    /**
     * 设置视频是否准备完成，并且已设置好视频大小
     *
     * @param failed 状态
     */
    void setFailedToPrepareUiForPlayback(boolean failed) {
        mFailedToPrepareUiForPlayback = failed;
    }

    /**
     * 是否可以播放
     *
     * @return 播放状态
     */
    boolean isReadyForPlayback() {
        return isVideoSizeAvailable() && isSurfaceTextureAvailable();
    }

    @Override
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("PlayerIndicator {");
        if (mVideoSize != null) {
            stringBuffer.append("mVideoSize -> [")
                    .append(mVideoSize.first)
                    .append(",")
                    .append(mVideoSize.second)
                    .append("] ");
        } else {
            stringBuffer.append("mVideoSize -> [ null ]");
        }
        stringBuffer.append(", isSurfaceTextureAvailable -> ").append(isSurfaceTextureAvailable());
        stringBuffer.append(", isFailedToPrepareUiForPlayback -> ").append(isFailedToPrepareUiForPlayback());
        stringBuffer.append(", isVideoSizeAvailable -> ").append(isVideoSizeAvailable());
        stringBuffer.append(", isReadyForPlayback -> ").append(isReadyForPlayback());
        stringBuffer.append("}");
        return stringBuffer.toString();
    }
}
