package com.chuncheng.sample.media.video.ui;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Description:优化surfaceView 处理视频的宽高比，防止图像拉伸
 *
 * @author: zhangchuncheng
 * @date: 2017/2/16
 */

public class OptimizeSurfaceView extends SurfaceView {
    /** 按照宽度进项缩放 */
    public static final int SCALE_TYPE_WIDTH = 1;
    /** 按照高度进项缩放 */
    public static final int SCALE_TYPE_HEIGHT = 2;
    /** 默认缩放模式根据控件的宽高比 */
    public static final int SCALE_TYPE_DEFAULT = 0;
    /** 宽高 */
    private Point mPoint;
    /** 缩放模式 */
    private int scaleType;

    public OptimizeSurfaceView(Context context) {
        super(context);
        initView();
    }

    public OptimizeSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public OptimizeSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    /**
     * 初始化宽高
     */
    private void initView() {
        mPoint = new Point(0, 0);
    }

    /**
     * 调节布局大小
     *
     * @param videoSize videoSize x-宽 y-高
     */
    public void setVideoSize(Point videoSize) {
        if (videoSize != null && !mPoint.equals(videoSize)) {
            this.mPoint = videoSize;
            requestLayout();
        }
    }

    /**
     * 设置缩放模式
     *
     * @param scaleType scaleType
     */
    public void setViewScaleType(int scaleType) {
        this.scaleType = scaleType;
    }

    /**
     * 作用是为了使view的宽高比和视频的宽高比相同。
     *
     * @param widthMeasureSpec  view自身的宽度
     * @param heightMeasureSpec view自身的高度
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //要显示视频的宽高
        int videoWidth = mPoint.x;
        int videoHeight = mPoint.y;
        /*MeasureSpec封装了父布局传递给子布局的布局要求，每个MeasureSpec代表了一组宽度和高度的要求
         MeasureSpec由size和mode组成。
         三种Mode：
         1.UNSPECIFIED
         父不没有对子施加任何约束，子可以是任意大小（也就是未指定）
         (UNSPECIFIED在源码中的处理和EXACTLY一样。当View的宽高值设置为0的时候或者没有设置宽高时，模式为UNSPECIFIED
         2.EXACTLY
         父决定子的确切大小，子被限定在给定的边界里，忽略本身想要的大小。
         (当设置width或height为match_parent时，模式为EXACTLY，因为子view会占据剩余容器的空间，所以它大小是确定的)
         3.AT_MOST
         子最大可以达到的指定大小
         (当设置为wrap_content时，模式为AT_MOST, 表示子view的大小最多是多少，这样子view会根据这个上限来设置自己的尺寸)
         MeasureSpecs使用了二进制去减少对象的分配。*/

        //getDefaultSize 作用是返回一个默认的值，
        //如果MeasureSpec没有强制限制的话则使用提供的大小.否则在允许范围内可任意指定大小
        //第一个参数size为提供的默认大小，第二个参数为测量的大小
        int width = getDefaultSize(videoWidth, widthMeasureSpec);
        int height = getDefaultSize(videoHeight, heightMeasureSpec);
        if (videoWidth > 0 && videoHeight > 0) {
            //获取宽的设置模式
            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            //获取宽的测量大小
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
            switch (scaleType) {
                case SCALE_TYPE_WIDTH:
                    width = widthSpecSize;
                    height = width * videoHeight / videoWidth;
//                    if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecMode) {
//                        //如果视频高度大于控件的高度，设控件的高度设为默认大小
//                        height = heightSpecSize;
//                        width = height * videoWidth / videoHeight;
//                    }
                    break;
                case SCALE_TYPE_HEIGHT:
                    height = heightSpecSize;
                    width = height * videoWidth / videoHeight;
                    if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                        width = widthSpecSize;
                        height = width * videoHeight / videoWidth;
                    }
                    break;
                case SCALE_TYPE_DEFAULT:
                default:
                    if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                        // 显示视频面板的宽高固定时
                        width = widthSpecSize;
                        height = heightSpecSize;
                        //根据比例调整大小
                        if (videoWidth * height < width * videoHeight) {
                            //竖向视频
                            width = height * videoWidth / videoHeight;
                        } else {
                            //横向视频
                            height = width * videoHeight / videoWidth;
                        }
                    } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                        //只固定宽度 没有固定高度时
                        width = widthSpecSize;
                        height = width * videoHeight / videoWidth;
                        if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecMode) {
                            //如果视频高度大于控件的高度，设控件的高度设为默认大小
                            height = heightSpecSize;
                            width = height * videoWidth / videoHeight;
                        }
                    } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                        //只固定高度
                        height = heightSpecSize;
                        width = height * videoWidth / videoHeight;
                        if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                            width = widthSpecSize;
                            height = width * videoHeight / videoWidth;
                        }
                    } else {
                        //宽高都没指定的情况
                        width = videoWidth;
                        height = videoHeight;
                        if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                            height = heightSpecSize;
                            width = height * videoHeight / videoWidth;
                        }
                        if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                            width = widthSpecSize;
                            height = width * videoHeight / videoWidth;
                        }
                    }
                    break;
            }
        }
        //必须调用这个方法，来存储测量的宽高值。
        setMeasuredDimension(width, height);
    }
}
