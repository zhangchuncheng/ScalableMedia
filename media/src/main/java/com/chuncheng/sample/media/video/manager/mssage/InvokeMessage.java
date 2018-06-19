package com.chuncheng.sample.media.video.manager.mssage;

/**
 * Description: 消息基类
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public interface InvokeMessage {
    /**
     * 执行消息
     */
    void runMessage();

    /**
     * 查询消息
     */
    void polledFromQueue();

    /**
     * 消息销毁
     */
    void messageFinished();
}
