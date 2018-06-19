package com.chuncheng.sample.media.video.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.chuncheng.sample.media.utils.log.MediaLog;

/**
 * Description: handlerThread
 *
 * @author: zhangchuncheng
 * @date: 2017/7/31
 */
public class HandlerThreadExtension extends HandlerThread {
    private static final String TAG = "HandlerThreadExtension";

    private Handler mHandler;
    private final Object mStart = new Object();

    /**
     * @param name                  name
     * @param setupExceptionHandler setupExceptionHandler
     */
    public HandlerThreadExtension(String name, boolean setupExceptionHandler) {
        super(name);
        if (setupExceptionHandler) {
            setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    ex.printStackTrace();
                    System.exit(0);
                }
            });
        }
    }

    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (mStart) {
                    mStart.notifyAll();
                }
            }
        });
    }

    public void post(Runnable r) {
        boolean successfullyAddedToQueue = mHandler.post(r);
    }

    public void postAtFrontOfQueue(Runnable r) {
        mHandler.postAtFrontOfQueue(r);
    }

    public void startThread() {
        synchronized (mStart) {
            start();
            try {
                mStart.wait();
            } catch (InterruptedException e) {
                MediaLog.getInstance().e(TAG, "wait erroe",
                        "HandlerThreadExtension",
                        "startThread");
            }
        }

    }

    public void postQuit() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                //noinspection ConstantConditions
                Looper.myLooper().quit();
            }
        });
    }

    public void remove(Runnable runnable) {
        mHandler.removeCallbacks(runnable);
    }
}

