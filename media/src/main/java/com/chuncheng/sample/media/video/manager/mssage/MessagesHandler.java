package com.chuncheng.sample.media.video.manager.mssage;

import android.os.Handler;
import android.os.Message;

import java.util.List;

/**
 * Description: 消息处理handler
 *
 * @author: zhangchuncheng
 * @date: 2017/3/22
 */

public class MessagesHandler extends Handler {
    private static final String TAG = "MessagesHandler";
    @Override
    public void handleMessage(Message msg) {
        if (msg.obj != null) {
            InvokeMessage invokeMessage = (InvokeMessage) msg.obj;
            invokeMessage.polledFromQueue();
            invokeMessage.runMessage();
            invokeMessage.messageFinished();
        }
    }

    /**
     * 发送消息
     *
     * @param list 消息集合
     */
    public void addMessage(List<InvokeMessage> list) {
        for (InvokeMessage invokeMessage : list) {
            Message message = new Message();
            message.what = 1;
            message.obj = invokeMessage;
            sendMessage(message);

        }
    }

    /**
     * 发送单调消息
     *
     * @param invokeMessage 消息
     */
    public void addMessage(InvokeMessage invokeMessage) {
        Message message = new Message();
        message.what = 1;
        message.obj = invokeMessage;
        sendMessage(message);
    }

}

