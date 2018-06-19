package com.chuncheng.sample.media.utils.log;

import android.util.Log;

import com.chuncheng.sample.media.utils.log.message.CloseLogMessage;
import com.chuncheng.sample.media.utils.log.message.MessageCallback;
import com.chuncheng.sample.media.utils.log.message.OpenLogMessage;
import com.chuncheng.sample.media.utils.log.message.WriteLogMessage;
import com.chuncheng.sample.media.utils.log.thread.LogHandlerThread;
import com.tencent.mars.xlog.Xlog;

/**
 * Description: 文件日志
 * <p>
 * PRIV_KEY = "abcd11b660bac42a1098dff780a5041a6f8ea93aa87aff42a"
 * PUB_KEY = "9c26adaeee23e0d8f6e7c9d305c4c8f7ea8f1c4dae35ac8fa701ae0c29be841b3fe6f2b74ab60ef1f3570942a08c0b958"
 *
 * @author: zhangchuncheng
 * @date: 2017/11/15
 */

public class MediaLog implements MessageCallback<String> {
    private static final String TAG = "MediaLog";

    public static final String LOG_FILE_NAME_LIST_KEY = "LOG_FILE_NAME_LIST_KEY";
    public static final int LEVEL_ALL = 0;
    public static final int LEVEL_VERBOSE = 0;
    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_INFO = 2;
    public static final int LEVEL_WARNING = 3;
    public static final int LEVEL_ERROR = 4;
    public static final int LEVEL_FATAL = 5;
    public static final int LEVEL_NONE = 6;

    private static class InstanceHolder {
        private static final MediaLog INSTANCE = new MediaLog();
    }

    private MediaLog() {
        Log.e(TAG, "MediaLog: init");
    }

    public static MediaLog getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**xLog*/
    private static Xlog mLog = new Xlog();
    /** 线程池 处理视频消息 */
    private final LogHandlerThread mLogHandler = new LogHandlerThread();
    /** 日志输出等级 */
    private int outputLevel = LEVEL_NONE;
    /** 日志文件名 */
    private String fileName;

    @Override
    public String getParams() {
        return fileName;
    }

    @Override
    public void setParams(String s) {
        fileName = s;
    }

    /**
     * 打开日志
     *
     * @param level      等级
     * @param namePrefix 文件名前缀
     * @param logPath    文件存储路径
     * @param cachePath  缓存目录
     * @param appVersion 应用版本
     * @param other      其他信息
     */
    public void open(int level, String namePrefix, String logPath, String cachePath, String appVersion, String other) {
        try {
            outputLevel = level;
            //1. 暂停队列处理
            mLogHandler.pauseQueueProcessing(TAG);
            //2. 添加消息进入队列处理
            mLogHandler.addMessage(new OpenLogMessage(level, namePrefix, logPath, cachePath, appVersion, other, this));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3. 恢复停止队列
            mLogHandler.resumeQueueProcessing(TAG);
        }
    }

    /**
     * LEVEL_VERBOSE = 0
     *
     * @param tag          标记
     * @param msg          信息
     * @param fileName     文件名
     * @param functionName 方法名
     */
    public void v(String tag, String msg, String fileName, String functionName) {
        if (outputLevel <= LEVEL_VERBOSE) {
            writeLog(LEVEL_VERBOSE, tag, msg, fileName, functionName);
        } else {
            Log.v(tag, fileName + ", " + functionName + ", " + msg);
        }
    }

    /**
     * LEVEL_DEBUG = 1
     *
     * @param tag          标记
     * @param msg          信息
     * @param fileName     文件名
     * @param functionName 方法名
     */
    public void d(String tag, String msg, String fileName, String functionName) {
        if (outputLevel <= LEVEL_DEBUG) {
            writeLog(LEVEL_DEBUG, tag, msg, fileName, functionName);
        } else {
            Log.d(tag, fileName + ", " + functionName + ", " + msg);
        }
    }

    /**
     * LEVEL_INFO = 2
     *
     * @param tag          标记
     * @param msg          信息
     * @param fileName     文件名
     * @param functionName 方法名
     */
    public void i(String tag, String msg, String fileName, String functionName) {
        if (outputLevel <= LEVEL_INFO) {
            writeLog(LEVEL_INFO, tag, msg, fileName, functionName);
        } else {
            Log.e(tag, fileName + ", " + functionName + ", " + msg);
        }
    }

    /**
     * LEVEL_WARNING = 3
     *
     * @param tag          标记
     * @param msg          信息
     * @param fileName     文件名
     * @param functionName 方法名
     */
    public void w(String tag, String msg, String fileName, String functionName) {
        if (outputLevel <= LEVEL_WARNING) {
            writeLog(LEVEL_WARNING, tag, msg, fileName, functionName);
        } else {
            Log.w(tag, fileName + ", " + functionName + ", " + msg);
        }
    }

    /**
     * LEVEL_ERROR = 4
     *
     * @param tag          标记
     * @param msg          信息
     * @param fileName     文件名
     * @param functionName 方法名
     */
    public void e(String tag, String msg, String fileName, String functionName) {
        if (outputLevel <= LEVEL_ERROR) {
            writeLog(LEVEL_ERROR, tag, msg, fileName, functionName);
        } else {
            Log.e(tag, fileName + ", " + functionName + ", " + msg);
        }
    }

    /**
     * 写日志
     *
     * @param level        等级
     * @param tag          标记
     * @param msg          信息
     * @param fileName     文件名
     * @param functionName 方法名
     */
    private void writeLog(int level, String tag, String msg, String fileName, String functionName) {
        try {
            //1. 暂停队列处理
            mLogHandler.pauseQueueProcessing(TAG);
            //2. 添加消息进入队列处理
            mLogHandler.addMessage(new WriteLogMessage(level, tag, msg, fileName, functionName, this));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //3. 恢复停止队列
            mLogHandler.resumeQueueProcessing(TAG);
        }
    }

    /**
     * 关闭日志
     */
    public void close() {
        //1. 暂停队列处理
        mLogHandler.pauseQueueProcessing(TAG);
        //2. 添加消息进入队列处理
        mLogHandler.addMessage(new CloseLogMessage(mLog,this));
        //3. 恢复停止队列
        mLogHandler.resumeQueueProcessing(TAG);
    }

    /**
     * 返回错误内容
     *
     * @param e 错误
     * @return 字符串
     */
    public String getErrorContent(Exception e) {
        StringBuilder buffer = new StringBuilder();
        try {
            if (e != null) {
                StackTraceElement[] stackTrace = e.getStackTrace();
                for (StackTraceElement stackTraceElement : stackTrace) {
                    buffer.append(stackTraceElement).append("\n");
                }
            }
        } catch (Exception e1) {
            buffer.append("error");
        }
        return buffer.toString();
    }
}
