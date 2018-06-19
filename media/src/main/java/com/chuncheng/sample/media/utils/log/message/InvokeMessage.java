package com.chuncheng.sample.media.utils.log.message;

/**
 * Description: InvokeMessage
 *
 * @author: zhangchuncheng
 * @date: 2017/12/29
 */

public interface InvokeMessage {
    /**
     * 执行消息
     */
    void runMessage();

    /**
     * 获取消息
     */
    void polledFromQueue();

    /**
     * 销毁消息
     */
    void messageFinished();
}
