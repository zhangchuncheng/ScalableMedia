package com.chuncheng.sample.media.utils.log.thread;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Description:线程锁
 *
 * @author: zhangchuncheng
 * @date: 2017/12/28
 */

class LogQueueLock {
    private static final String TAG = "LogQueueLock";
    private final ReentrantLock mQueueLock = new ReentrantLock();
    private final Condition mProcessQueueCondition = mQueueLock.newCondition();

    void lock(String owner){
        mQueueLock.lock();
    }

    void unlock(String owner){
        mQueueLock.unlock();
    }

    boolean isLocked(String owner){
        return mQueueLock.isLocked();
    }

    void wait(String owner) throws InterruptedException {
        mProcessQueueCondition.await();
    }

    void notify(String owner) {
        mProcessQueueCondition.signal();
    }
}
