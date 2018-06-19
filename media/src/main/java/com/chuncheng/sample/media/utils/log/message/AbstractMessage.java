package com.chuncheng.sample.media.utils.log.message;

/**
 * Description: AbstractMessage
 *
 * @author: zhangchuncheng
 * @date: 2017/12/29
 */

public abstract class AbstractMessage<T> implements InvokeMessage {
    protected static final String TAG = "Message";

    private MessageCallback<T> mMessageCallback;

    public AbstractMessage(MessageCallback<T> mMessageCallback) {
        this.mMessageCallback = mMessageCallback;
    }

    @Override
    public void runMessage() {
        performAction();
    }

    @Override
    public void polledFromQueue() {
        stateBefore();
    }

    @Override
    public void messageFinished() {
        stateAfter();
    }

    protected abstract void performAction();

    protected void stateBefore() {

    }

    protected void stateAfter() {

    }

    protected T getCurrentParams() {
        return mMessageCallback.getParams();
    }

    protected void setCurrentParams(T t) {
        mMessageCallback.setParams(t);
    }
}