package com.chuncheng.sample.media.video.manager.mssage;

import com.chuncheng.sample.media.utils.log.MediaLog;

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
 * @date: 2017/5/16
 */

public class MessagesHandlerThread {
    private static final String TAG = "MessagesHandlerThread";
    /** 初始化消息队列 */
    private final Queue<InvokeMessage> mPlayerMessagesQueue = new ConcurrentLinkedQueue<>();
    /** 初始化线程锁 */
    private final PlayerQueueLock mQueueLock = new PlayerQueueLock();
    /** 初始化线程池 */
    private final Executor mQueueProcessingThread = Executors.newSingleThreadExecutor();

    private AtomicBoolean mTerminated = new AtomicBoolean(false);
    private InvokeMessage mLastMessage;

    public MessagesHandlerThread() {
        mQueueProcessingThread.execute(new Runnable() {
            @Override
            public void run() {
                MediaLog.getInstance().i(TAG, "start worker thread",
                        "MessagesHandlerThread",
                        "run");
                do {
                    mQueueLock.lock(TAG + ", " + mLastMessage);
                    MediaLog.getInstance().i(TAG, "Queue<InvokeMessage> -> " + mPlayerMessagesQueue,
                            "MessagesHandlerThread",
                            "run");
                    if (mPlayerMessagesQueue.isEmpty()) {
                        try {
                            MediaLog.getInstance().i(TAG, "queue is empty, wait for new messages",
                                    "MessagesHandlerThread",
                                    "run");
                            mQueueLock.wait(TAG + ", " + mLastMessage);
                        } catch (InterruptedException e) {
                            MediaLog.getInstance().e(TAG, "error, e.getMessage " + e.getMessage(),
                                    "MessagesHandlerThread",
                                    "run");
                            throw new RuntimeException("InterruptedException");
                        }
                    }
                    mLastMessage = mPlayerMessagesQueue.poll();
                    MediaLog.getInstance().i(TAG, "before, InvokeMessage -> " + mLastMessage.toString(),
                            "MessagesHandlerThread",
                            "run");
                    mLastMessage.polledFromQueue();
                    mQueueLock.unlock(TAG + ", " + mLastMessage);

                    MediaLog.getInstance().i(TAG, "start, InvokeMessage -> " + mLastMessage.toString(),
                            "MessagesHandlerThread",
                            "run");
                    mLastMessage.runMessage();

                    mQueueLock.lock(TAG + ", " + mLastMessage);
                    MediaLog.getInstance().i(TAG, "after, InvokeMessage -> " + mLastMessage.toString(),
                            "MessagesHandlerThread",
                            "run");
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
        mPlayerMessagesQueue.add(message);
        MediaLog.getInstance().i(TAG, "add, InvokeMessage -> " + message.toString(),
                "MessagesHandlerThread",
                "addMessage");
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
        mPlayerMessagesQueue.addAll(messages);
        MediaLog.getInstance().i(TAG, "addAll, List<InvokeMessage> -> " + messages.toString(),
                "MessagesHandlerThread",
                "addMessage");
        mQueueLock.notify(TAG + ", " + messages);
        mQueueLock.unlock(TAG + ", " + messages);
    }

    /**
     * 暂停执行队列中的消息
     *
     * @param outer tag
     */
    public void pauseQueueProcessing(String outer) {
        MediaLog.getInstance().i(TAG, "lock, PlayerQueueLock is locked " + mQueueLock.isLocked(outer),
                "MessagesHandlerThread",
                "pauseQueueProcessing");
        mQueueLock.lock(outer);
    }

    /**
     * 恢复执行队列中的消息
     *
     * @param outer tag
     */
    public void resumeQueueProcessing(String outer) {
        MediaLog.getInstance().i(TAG, "unlock, PlayerQueueLock is locked " + mQueueLock.isLocked(outer),
                "MessagesHandlerThread",
                "resumeQueueProcessing");
        mQueueLock.unlock(outer);
    }

    /**
     * 清除队列中所有的消息
     *
     * @param outer tag
     */
    public void clearAllPendingMessages(String outer) {
        MediaLog.getInstance().i(TAG, "PlayerQueueLock is locked state "+ mQueueLock.isLocked(outer) +
                ", Queue<InvokeMessage> -> " + mPlayerMessagesQueue,
                "MessagesHandlerThread",
                "clearAllPendingMessages");
        if (mQueueLock.isLocked(outer)) {
            mPlayerMessagesQueue.clear();
        } else {
            MediaLog.getInstance().e(TAG, "cannot perform action, you are not holding a lock",
                    "MessagesHandlerThread",
                    "clearAllPendingMessages");
            throw new RuntimeException("cannot perform action, you are not holding a lock");

        }
    }

    /**
     * 停止线程
     */
    public void terminate() {
        mTerminated.set(true);
    }
}
