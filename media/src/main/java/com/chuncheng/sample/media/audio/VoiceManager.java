package com.chuncheng.sample.media.audio;

import android.content.Context;
import android.media.AudioManager;

/**
 * Description: 音频管理
 *
 * @author: zhangchuncheng
 * @date: 2017/2/15
 */

public class VoiceManager {
    private Context mContext;
    private AudioManager mAudioManager;

    public VoiceManager(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 返回指定音频类型的音量
     *
     * @param streamType 音频类型
     *                   STREAM_ALARM	警报的音频流
     *                   STREAM_DTMF	DTMF音频的音频流
     *                   STREAM_MUSIC	用于音乐播放的音频流
     *                   STREAM_NOTIFICATION	用于通知的音频流
     *                   STREAM_RING	电话铃声的音频流
     *                   STREAM_SYSTEM	系统声音的音频流
     *                   STREAM_VOICE_CALL	电话的音频流
     * @return 当前音量
     */
    public int getStreamVolume(int streamType) {
        return mAudioManager.getStreamVolume(streamType);
    }

    /**
     * 返回指定音频类型的百分比
     *
     * @param streamType 音频类型
     *                   STREAM_ALARM	警报的音频流
     *                   STREAM_DTMF	DTMF音频的音频流
     *                   STREAM_MUSIC	用于音乐播放的音频流
     *                   STREAM_NOTIFICATION	用于通知的音频流
     *                   STREAM_RING	电话铃声的音频流
     *                   STREAM_SYSTEM	系统声音的音频流
     *                   STREAM_VOICE_CALL	电话的音频流
     * @return 当前音量的百分比
     */
    public int getStreamVolumeByPercent(int streamType) {
        return (mAudioManager.getStreamVolume(streamType) * 100) / mAudioManager.getStreamMaxVolume(streamType);
    }

    /**
     * 返回指定音频类型的最大音量
     *
     * @param streamType 音频类型
     *                   STREAM_ALARM	警报的音频流
     *                   STREAM_DTMF	DTMF音频的音频流
     *                   STREAM_MUSIC	用于音乐播放的音频流
     *                   STREAM_NOTIFICATION	用于通知的音频流
     *                   STREAM_RING	电话铃声的音频流
     *                   STREAM_SYSTEM	系统声音的音频流
     *                   STREAM_VOICE_CALL	电话的音频流
     * @return 最大音量
     */
    public int getStreamMaxVolume(int streamType) {
        return mAudioManager.getStreamMaxVolume(streamType);
    }

    /**
     * 设定指定音频类型的音量
     *
     * @param streamType 音频类型
     *                   STREAM_ALARM	警报的音频流
     *                   STREAM_DTMF	DTMF音频的音频流
     *                   STREAM_MUSIC	用于音乐播放的音频流
     *                   STREAM_NOTIFICATION	用于通知的音频流
     *                   STREAM_RING	电话铃声的音频流
     *                   STREAM_SYSTEM	系统声音的音频流
     *                   STREAM_VOICE_CALL	电话的音频流
     * @param index      要设置的卷索引
     * @param flags      标记
     *                   FLAG_PLAY_SOUND 在更改音量时播放声音
     *                   FLAG_SHOW_UI 调整时显示音量条
     *                   FLAG_REMOVE_SOUND_AND_VIBRATE 无振动无声音
     */
    public void setStreamVolume(int streamType, int index, int flags) {
        mAudioManager.setStreamVolume(streamType, index, flags);
    }

    /**
     * 增加音量
     */
    public void increaseVolume() {
        increaseVolume(1f);
    }

    /**
     * 增加音量
     *
     * @param length 追加量
     */
    public void increaseVolume(float length) {
        int currentVolume = getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < maxVolume) {
            int volume = (int) (currentVolume + length);
            if (volume == (currentVolume + length)) {
                setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }

        }
    }

    /**
     * 减小音量
     */
    public void decreaseVolume() {
        decreaseVolume(1);
    }

    /**
     * 减小音量
     *
     * @param length 追加量
     */
    public void decreaseVolume(float length) {
        int currentVolume = getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            int volume = (int) (currentVolume - length);
            if (volume == (currentVolume - length)) {
                setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
        }
    }
}
