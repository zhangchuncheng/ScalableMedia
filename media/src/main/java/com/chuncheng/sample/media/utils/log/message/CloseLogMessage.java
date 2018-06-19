package com.chuncheng.sample.media.utils.log.message;

import com.tencent.mars.xlog.Xlog;

/**
 * Description: 关闭日志消息
 *
 * @author: zhangchuncheng
 * @date: 2017/12/28
 */

public class CloseLogMessage extends AbstractMessage<String> {

    private Xlog mLog;

    public CloseLogMessage(Xlog xlog, MessageCallback<String> mMessageCallback) {
        super(mMessageCallback);
        mLog = xlog;
    }

    @Override
    protected void performAction() {
        try {
            mLog.appenderClose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
