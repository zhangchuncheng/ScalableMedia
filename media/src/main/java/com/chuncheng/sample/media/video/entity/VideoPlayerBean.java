package com.chuncheng.sample.media.video.entity;

import android.text.TextUtils;

/**
 * Description:视频播放参数
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class VideoPlayerBean {

    /** 视频url */
    private String url;
    /** 播放位置 */
    private int position;
    /** 是否播放视频 */
    private boolean isPlayer;
    /** 是否定位到指定位置 */
    private boolean isSeekTo;
    /** 是否重新播放 */
    private boolean isReplay;

    public VideoPlayerBean() {
        this.isPlayer = true;
        this.isSeekTo = true;
    }

    public VideoPlayerBean(String url) {
        this.isPlayer = true;
        this.url = url;
    }

    public VideoPlayerBean(int position) {
        this.isPlayer = true;
        this.isSeekTo = true;
        this.position = position;
    }
    
    public String getUrl() {
        if (TextUtils.isEmpty(url)) {
            return "";
        } else {
            return url;
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getPosition() {
        return position + 1 < 0 ? 1 : position + 1;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isPlayer() {
        return isPlayer;
    }

    public void setPlayer(boolean player) {
        isPlayer = player;
    }

    public boolean isSeekTo() {
        return isSeekTo;
    }

    public void setSeekTo(boolean seekTo) {
        isSeekTo = seekTo;
    }

    public boolean isReplay() {
        return isReplay;
    }

    public void setReplay(boolean replay) {
        isReplay = replay;
    }

    @Override
    public String toString() {
        return "VideoPlayerBean{" +
                "url ='" + url + '\'' +
                ", position =" + position +
                ", isPlayer =" + isPlayer +
                ", isSeekTo =" + isSeekTo +
                ", isReplay =" + isReplay +
                '}';
    }
}
