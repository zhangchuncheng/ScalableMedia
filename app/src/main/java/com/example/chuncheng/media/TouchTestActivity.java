package com.example.chuncheng.media;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.chuncheng.sample.media.video.ui.TouchView;

/**
 * Description:测试touchView
 *
 * @author: zhangchuncheng
 * @date: 2017/5/22
 */

public class TouchTestActivity extends AppCompatActivity implements TouchView.VideoGestureDetectorUiOperationListener {
    private static final String TAG = "TouchTestActivity";
    TouchView mTouchView;
    int currentPosition = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player_test_c);
        mTouchView = (TouchView) findViewById(R.id.touch_view);
        mTouchView.setUIOperationListener(this);
    }

    @Override
    public void onGestureDetectorByScreenLuminance(boolean screenType, int number, int percent) {
        Log.e(TAG, "luminance, screenType: " + screenType + " number: " + number + " percent: " + percent);
    }

    @Override
    public void onGestureDetectorByVolume(boolean volumeType, int number, int percent) {
        Log.e(TAG, "volume, volumeType: " + volumeType + " number: " + number + " percent: " + percent);
    }

    @Override
    public void onGestureDetectorByProgress(boolean progressType, int number, int percent) {
        Log.e(TAG, "progress, progressType: " + progressType + " number: " + number + " percent: " + percent);
        currentPosition = number;
    }

    @Override
    public void onGestureDetectorCancel(int gestureType) {
        Log.e(TAG, "cancel, gestureType: " + gestureType);
    }

    @Override
    public int getDuration() {
        return 1000;
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }
}
