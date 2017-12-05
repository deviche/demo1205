package com.example.sf.demo1205;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.sf.demo1205.R;

/**
 * Created by sf on 2017/12/5 0005.
 */

public class CountDownView extends View {
    private Context mContext;//上下文
    private Paint mPaintBackGround;//背景画笔
    private Paint mPaintArc;//圆弧画笔
    private Paint mPaintText;//文字画笔
    private int mRetreatType;//圆弧绘制方式（增加还是减少）
    private int mCircleRadius;//圆弧半径
    private float mPaintArcWidth;//最外层圆弧的宽度
    private int mPaintArcColor = Color.parseColor("#3c3f41");//初始值
    private int mPaintBackGroundColor = Color.parseColor("#55B2E5");//初始值
    private int mLoadingTime;//时间，单位秒
    private String mLoadingTimeUnit = "";//时间单位
    private int mTextColor = Color.BLACK;//字体颜色
    private int mTextSize;//字体大小
    private int location;// 从哪个位置开始
    private float startAngle;//开始角度
    private float mmSweepAngleStart;//起点
    private float mmSweepAngleEnd;//终点
    private float mSweepAngle;// 扫过的角度
    private String mText = "";//要绘制的文字
    //控件宽高
    float mWidth;
    float mHeight;

    public CountDownView(Context context) {
        this(context, null);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CountDownView);
        mRetreatType = array.getInt(R.styleable.CountDownView_cd_retreat_type, 1);
        location = array.getInt(R.styleable.CountDownView_cd_location, 1);
        mCircleRadius = (int) array.getDimension(R.styleable.CountDownView_cd_circle_radius, dip2px(context, 25));//默认25dp
        mPaintArcWidth = array.getDimension(R.styleable.CountDownView_cd_arc_width, dip2px(context, 3));//默认3dp
        mPaintArcColor = array.getColor(R.styleable.CountDownView_cd_arc_color, mPaintArcColor);
        mTextSize = (int) array.getDimension(R.styleable.CountDownView_cd_text_size, dip2px(context, 14));//默认14sp
        mTextColor = array.getColor(R.styleable.CountDownView_cd_text_color, mTextColor);
        mPaintBackGroundColor = array.getColor(R.styleable.CountDownView_cd_bg_color, mPaintBackGroundColor);
        mLoadingTime = array.getInteger(R.styleable.CountDownView_cd_animator_time, 3);//默认3秒
        mLoadingTimeUnit = array.getString(R.styleable.CountDownView_cd_animator_time_unit);//时间单位
        if (TextUtils.isEmpty(mLoadingTimeUnit)) {
            mLoadingTimeUnit = "";
        }
        array.recycle();
        /*measure(0,0);
        mWidth = getWidth();
        mHeight = getHeight();*/
        init();
    }

    private void init() {
        //背景设为透明，然后造成圆形view的视觉错觉
        this.setBackground(ContextCompat.getDrawable(mContext, android.R.color.transparent));
        mPaintBackGround = new Paint();
        mPaintBackGround.setStyle(Paint.Style.FILL);
        mPaintBackGround.setAntiAlias(true);
        mPaintBackGround.setColor(mPaintBackGroundColor);

        mPaintArc = new Paint();
        mPaintArc.setStyle(Paint.Style.STROKE);
        mPaintArc.setAntiAlias(true);
        mPaintArc.setColor(mPaintArcColor);
        mPaintArc.setStrokeWidth(mPaintArcWidth);

        mPaintText = new Paint();
        mPaintText.setStyle(Paint.Style.STROKE);
        mPaintText.setAntiAlias(true);
        mPaintText.setColor(mTextColor);
        mPaintText.setTextSize(mTextSize);
        if (mLoadingTime < 0) {
            mLoadingTime = 3;
        }
        if (location == 1) {
            startAngle = -180;
        } else if (location == 2) {
            startAngle = -90;
        } else if (location == 3) {
            startAngle = 0;
        } else if (location == 4) {
            startAngle = 90;
        }

        if (mRetreatType == 1) {
            mmSweepAngleStart = 0f;
            mmSweepAngleEnd = 360f;
        } else {
            mmSweepAngleStart = 360f;
            mmSweepAngleEnd = 0f;
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //获取宽高
        mWidth = w;
        mHeight = h;
    }

    /*public CountDownView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }*/
    private OnLoadingFinishListener loadingFinishListener;

    public interface OnLoadingFinishListener {
        void onFinish();
    }

    public void setOnLoadingFinishListener(OnLoadingFinishListener loadingFinishListener) {
        this.loadingFinishListener = loadingFinishListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mCircleRadius * 2, mCircleRadius * 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //画背景图
        canvas.drawCircle(mWidth / 2, mHeight / 2, mCircleRadius, mPaintBackGround);
        //画圆弧
        RectF rectF = new RectF(0 + mPaintArcWidth / 2, 0 + mPaintArcWidth / 2, mWidth - mPaintArcWidth / 2, mHeight - mPaintArcWidth / 2);
        canvas.drawArc(rectF, startAngle, mSweepAngle, false, mPaintArc);
        //画文字
        float mTextWidth = mPaintText.measureText(mText, 0, mText.length());
        float dx = mWidth / 2 - mTextWidth / 2;
        Paint.FontMetricsInt fontMetricsInt = mPaintText.getFontMetricsInt();
        float dy = (fontMetricsInt.bottom - fontMetricsInt.top) / 2 - fontMetricsInt.bottom;
        float baseLine = mHeight / 2 + dy;
        canvas.drawText(mText, dx, baseLine, mPaintText);
    }

    //根据手机分辨率从dp转换成px像素
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (scale * dpValue + 0.5f);
    }

    public void start() {
        final ValueAnimator animator = ValueAnimator.ofFloat(mmSweepAngleStart, mmSweepAngleEnd);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mSweepAngle = (float) animation.getAnimatedValue();
                //获取到需要绘制的角度，重新绘制
                invalidate();
            }
        });

        //获取时间 赋值
        ValueAnimator  animator1 = ValueAnimator.ofInt(mLoadingTime,0);
        animator1.setInterpolator(new LinearInterpolator());
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int time =(int) animation.getAnimatedValue();
                mText = time + mLoadingTimeUnit;
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator,animator1);
        set.setDuration(mLoadingTime*1000);
        set.setInterpolator(new LinearInterpolator());
        set.start();
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                clearAnimation();
                if (loadingFinishListener !=null){
                    loadingFinishListener.onFinish();
                }
            }
        });
    }

}
