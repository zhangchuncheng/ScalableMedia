package com.chuncheng.sample.media.utils.log.message;

import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import com.chuncheng.sample.media.utils.log.MediaLog;
import com.tencent.mars.xlog.Xlog;


/**
 * Description: 写日志消息
 *
 * @author: zhangchuncheng
 * @date: 2017/12/28
 */

public class WriteLogMessage extends AbstractMessage<String> {

    /**日志等级*/
    private int mLevel;
    /**标签*/
    private String mTag;
    /**内容*/
    private String mMsg;
    /**文件名*/
    private String mFileName;
    /**方法名*/
    private String mFunctionName;

    public WriteLogMessage(int level, String tag, String msg, String fileName, String functionName, MessageCallback<String> messageCallback) {
        super(messageCallback);
        this.mLevel = level;
        this.mTag = tag;
        this.mMsg = msg;
        this.mFileName = fileName;
        this.mFunctionName = functionName;
    }

    @Override
    protected void performAction() {
        try {
            Xlog.logWrite2(mLevel,
                    mTag,
                    mFileName,
                    mFunctionName,
                    0,
                    Process.myPid(),
                    Thread.currentThread().getId(),
                    Looper.getMainLooper().getThread().getId(),
                    mMsg);
            if (mLevel == MediaLog.LEVEL_ERROR) {
                //存入SharedPreferences
                String currentParams = getCurrentParams();
                if (!TextUtils.isEmpty(currentParams)) {
                    //处理文件
                    /*String fileNameJson = SavePreferences.getString(MediaLog.LOG_FILE_NAME_LIST_KEY);
                    Set<String> set = new HashSet<>();
                    if (!TextUtils.isEmpty(fileNameJson)) {
                        set = GsonUtils.toSet(fileNameJson, String.class);
                    }
                    set.add(currentParams);
                    String json = GsonUtils.toJson(set);
                    SavePreferences.setData(MediaLog.LOG_FILE_NAME_LIST_KEY, json);*/
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
