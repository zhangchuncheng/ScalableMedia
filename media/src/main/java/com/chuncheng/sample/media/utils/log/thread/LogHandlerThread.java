package com.chuncheng.sample.media.utils.log.thread;

import com.chuncheng.sample.media.utils.log.message.InvokeMessage;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Description:多线程管理
 *
 * @author: zhangchuncheng
 * @date: 2017/12/28
 */

public class LogHandlerThread {
    private static final String TAG = "LogHandlerThread";
    /** 初始化消息队列 */
    private final Queue<InvokeMessage> mLogMessagesQueue = new ConcurrentLinkedQueue<>();
    /** 初始化线程锁 */
    private final LogQueueLock mQueueLock = new LogQueueLock();
    /** 初始化线程池 */
    private final Executor mQueueProcessingThread = Executors.newSingleThreadExecutor();

    private AtomicBoolean mTerminated = new AtomicBoolean(false);
    private InvokeMessage mLastMessage;

    public LogHandlerThread() {
        mQueueProcessingThread.execute(new Runnable() {
            @Override
            public void run() {
                do {
                    mQueueLock.lock(TAG + ", " + mLastMessage);
                    if (mLogMessagesQueue.isEmpty()) {
                        try {
                            mQueueLock.wait(TAG + ", " + mLastMessage);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            //throw new RuntimeException("LogHandlerThread");
                        }
                    }
                    mLastMessage = mLogMessagesQueue.poll();
                    mLastMessage.polledFromQueue();
                    mQueueLock.unlock(TAG + ", " + mLastMessage);

                    mLastMessage.runMessage();

                    mQueueLock.lock(TAG + ", " + mLastMessage);
                    mLastMessage.messageFinished();
                    mQueueLock.unlock(TAG + ", " + mLastMessage);

                } while (!mTerminated.get());
            }
        });
    }

    /**
     * 向队列中添加消息
     *
     * @param message 消息
     */
    public void addMessage(InvokeMessage message) {
        mQueueLock.lock(TAG + ", " + message);
        mLogMessagesQueue.add(message);
        mQueueLock.notify(TAG + ", " + message);
        mQueueLock.unlock(TAG + ", " + message);
    }

    /**
     * 向队列中添加消息
     *
     * @param messages 消息集合
     */
    public void addMessage(List<? extends InvokeMessage> messages) {
        mQueueLock.lock(TAG + ", " + messages);
        mLogMessagesQueue.addAll(messages);
        mQueueLock.notify(TAG + ", " + messages);
        mQueueLock.unlock(TAG + ", " + messages);
    }

    /**
     * 暂停执行队列中的消息
     *
     * @param outer tag
     */
    public void pauseQueueProcessing(String outer) {
        mQueueLock.lock(outer);
    }

    /**
     * 恢复执行队列中的消息
     *
     * @param outer tag
     */
    public void resumeQueueProcessing(String outer) {
        mQueueLock.unlock(outer);
    }

    /**
     * 清除队列中所有的消息
     *
     * @param outer tag
     */
    public void clearAllPendingMessages(String outer) {
        if (mQueueLock.isLocked(outer)) {
            mLogMessagesQueue.clear();
        }
        /* else {
            throw new RuntimeException("cannot perform action, you are not holding a lock");

        }*/
    }

    /**
     * 停止线程
     */
    public void terminate() {
        mTerminated.set(true);
    }
}
