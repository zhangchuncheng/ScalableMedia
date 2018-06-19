package com.chuncheng.sample.media.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.WindowManager;

/**
 * Description:屏幕工具类
 *
 * @author: zhangchuncheng
 * @date: 2017/3/14
 */

public class ScreenUtils {

    /**
     * 设置当前页面亮度值
     *
     * @param context    上下文环境
     * @param brightness 亮度值
     */
    public static void setLight(Context context, int brightness) {
        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
        lp.screenBrightness = brightness * (1f / 255f);
        ((Activity) context).getWindow().setAttributes(lp);
    }

    /**
     * 获取系统亮度值
     *
     * @param activity activity
     * @return 当前亮度值
     */
    public static int getScreenBrightness(Context activity) {
        int value = 0;
        ContentResolver cr = activity.getContentResolver();
        try {
            value = Settings.System.getInt(cr, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context context
     * @return 宽度值
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
