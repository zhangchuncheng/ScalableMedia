package com.chuncheng.sample.media.utils.log.message;

import android.os.Build;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.tencent.mars.xlog.Xlog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Description: 打开日志消息
 * <p>
 * PRIV_KEY = "abcd11b660bac42a1098dff780a5041a6f8ea93aa87aff42a"
 * PUB_KEY = "9c26adaeee23e0d8f6e7c9d305c4c8f7ea8f1c4dae35ac8fa701ae0c29be841b3fe6f2b74ab60ef1f3570942a08c0b958"
 *
 * @author: zhangchuncheng
 * @date: 2017/12/28
 */

public class OpenLogMessage extends AbstractMessage<String> {

    /** 日志输出等级 */
    private int mLevel;
    /** 日志名称 */
    private String mNamePrefix;
    /** 日志目录 */
    private String mLogPath;
    /** 日志缓存目录 */
    private String mCachePath;
    /** 日志key */
    private String mPublicKey;
    /** other */
    private String mOther;
    /** app版本号 */
    private String mAppVersion;

    public OpenLogMessage(int level, String namePrefix, String logPath, String cachePath, String appVersion, String other, MessageCallback<String> messageCallback) {
        super(messageCallback);
        this.mLevel = level;
        this.mNamePrefix = namePrefix;
        this.mLogPath = logPath;
        this.mPublicKey = "9c26adaeee23e0d8f6e7c9d305c4c8f7ea8f1c4dae35ac8fa701ae0c29be841b3fe6f2b74ab60ef1f3570942a08c0b958";
        this.mCachePath = cachePath;
        this.mAppVersion = appVersion;
        this.mOther = other;
    }

    @Override
    protected void performAction() {
        try {
            //打开日志
            Xlog.open(true, mLevel, Xlog.AppednerModeAsync, mCachePath, mLogPath, mNamePrefix, mPublicKey);
            //拼接手机参数
            String string = "PhoneInfo:" + Build.MODEL + "_android_" + Build.VERSION.RELEASE + ", appVersion " + mAppVersion + ", other [ " + mOther + " ]";
            Xlog.logWrite2(Xlog.LEVEL_INFO,
                    "Info",
                    "MediaLog",
                    "Open",
                    0,
                    Process.myPid(),
                    Thread.currentThread().getId(),
                    Looper.getMainLooper().getThread().getId(),
                    string);
            //组装文件名
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            String format = simpleDateFormat.format(new Date());
            String fileName = mNamePrefix + "_" + format + ".xlog";
            setCurrentParams(fileName);
            Log.e(TAG, "open: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
