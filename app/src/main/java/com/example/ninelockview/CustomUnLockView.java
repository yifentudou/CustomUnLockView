package com.example.ninelockview;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

/**
 * Created by lzr on 2021/3/23.
 * Describe:九宫格解锁视图
 */
public class CustomUnLockView extends View {

    private final static String TAG = CustomUnLockView.class.getSimpleName();
    private int mWidth;
    private int mHeight;
    private Paint mTextTipsPaint;
    private Paint mPressPaint;
    private Paint mNormalPaint;
    private Paint mErrorPaint;
    private @ColorInt
    int mNormalPaintColor = Color.parseColor("#1FE4E4");
    private @ColorInt
    int mPressPaintColor = Color.parseColor("#8BC34A");
    private @ColorInt
    int mErrorPaintColor = Color.parseColor("#F34444");
    private @ColorInt
    int mTextTipsPaintColor = Color.parseColor("#111111");

    //所有点的坐标
    private Point[][] points;
    //已选择点的集合
    private List<Point> mSelectedPoints = new ArrayList<>();
    //当前触摸的view的x轴和y轴
    private float mY;
    private float mX;
    //圆点半径
    private int mRadius;
    //当前九宫格选中的某个点
    private Point selectP;
    //仍在绘制
    private boolean isDraw;
    //提示文本内容
    private String textTips = "请输入解锁图案";
    //提示文本大小
    private float defalut_text_tips_size = 20;
    //整体偏移量
    private int offset;
    //整体view在Y轴上的偏移量
    private int offsetY;
    //正确密码字符串
    private String pwdStr = "";
    //解密绘制得到的密码字符串
    private StringBuffer newPwdStringBuffer = new StringBuffer();
    //最大输入次数
    private int inputCount = 5;
    //一行个数，总圆点个数是mCount*mCount
    private int mCount = 3;
    //输入正确监听
    private OnUnLockListener listener;

    private int phoneWidth;
    private int phoneHeight;


    public CustomUnLockView(Context context) {
        super(context);
        init();
    }

    public CustomUnLockView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public CustomUnLockView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.CustomUnLockView);
        if (null != array) {
            mCount = array.getInt(R.styleable.CustomUnLockView_count, mCount);
            inputCount = array.getInt(R.styleable.CustomUnLockView_max_lock_times, inputCount);
            mTextTipsPaintColor = array.getColor(R.styleable.CustomUnLockView_color_text_tips, mTextTipsPaintColor);
            mPressPaintColor = array.getColor(R.styleable.CustomUnLockView_color_press, mPressPaintColor);
            mNormalPaintColor = array.getColor(R.styleable.CustomUnLockView_color_normal, mNormalPaintColor);
            mErrorPaintColor = array.getColor(R.styleable.CustomUnLockView_color_error, mErrorPaintColor);
            array.recycle();
        }
    }

    private void init() {
        mTextTipsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextTipsPaint.setColor(mTextTipsPaintColor);
        mTextTipsPaint.setTextSize(dp2px(defalut_text_tips_size));

        mNormalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mNormalPaint.setColor(mNormalPaintColor);
        mNormalPaint.setStrokeWidth(7);

        mPressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPressPaint.setColor(mPressPaintColor);
        mPressPaint.setStrokeWidth(7);

        mErrorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mErrorPaint.setColor(mErrorPaintColor);
        mErrorPaint.setStrokeWidth(7);

        phoneWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        phoneHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mX = event.getX();
        mY = event.getY();
        if (inputCount == 0) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//              获取触摸点在哪个目标点的绘制范围内
                selectP = getSelectedPointPosition();
                if (selectP != null) {
                    isDraw = true;
                    //被选择的点存入集合
                    mSelectedPoints.add(selectP);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                selectP = getSelectedPointPosition();
                //已经选了的点，不再重新被选择
                if (selectP != null && !mSelectedPoints.contains(selectP)) {
                    mSelectedPoints.add(selectP);
                }
                break;
            case MotionEvent.ACTION_UP:
                isDraw = false;
                //验证密码路径
                verifyPwdPath();
                break;
        }
        invalidate();
        return true;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points == null || points.length <= 0) {
            //当前视图的大小
            mWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            mHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            Log.i(TAG, "init: mWidth =" + mWidth + " mHeight" + mHeight);
            //九宫格需要居中显示，偏移量
            offset = Math.abs(mWidth - mHeight) / 2;
            //x/y轴上的偏移量
            int offsetX = 0;
            offsetY = 0;
            //每个点所占方格的宽度
            int pointItemWidth = 0;
            //横屏的时候
            if (mWidth > mHeight) {
                offsetX = offset;
                offsetY = 0;
                pointItemWidth = mHeight / (mCount + 1);
            }
            //竖屏的时候
            if (mWidth <= mHeight) {
                offsetY = offset;
                offsetX = 0;
                pointItemWidth = mWidth / (mCount + 1);
            }
            //初始化3*3个点
            initNineCirclePoint(mCount, pointItemWidth, offsetX, offsetY);
        }
        //绘制提示语
        drawTextTips(canvas);
        //绘制圆点
        drawPoints(canvas);
        //绘制连线
        drawLines(canvas);
    }


    /**
     * 校验绘制的密码路径
     */
    private void verifyPwdPath() {
        newPwdStringBuffer.setLength(0);
        if (mSelectedPoints != null) {
            for (int i = 0; i < mSelectedPoints.size(); i++) {
                Point point = mSelectedPoints.get(i);
                newPwdStringBuffer.append(point.getNum());
                Log.i(TAG, "已选择的密码路径有序取出为: " + point.getNum());
            }

        }
        if (newPwdStringBuffer != null) {
            Log.i(TAG, "校验密码路径: " + "本次选择的密码路径：" + newPwdStringBuffer.toString() + "pwdStr" + pwdStr);
            if (newPwdStringBuffer.toString().equals(pwdStr)) {
                textTips = "解锁成功";
                //做其他操作
                if (listener != null) {
                    listener.doUnLock();
                }
                //这里就重置所有的点的状态：
                resetPoints();
            } else {

                inputCount--;
                if (inputCount > 0) {
                    showAnimation();
                    textTips = "密码错误，你还可以输入" + inputCount + "次";
                }
                //重新更改点、线状态
                resetPointsWithState(Point.STATE_ERROR);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (inputCount == 0) {
                            textTips = "已锁定，无法解锁";
                        } else {
                            textTips = "请输入解锁图案";
                        }
                        resetPoints();
                        invalidate();
                    }
                }, 1000);
            }
        }
    }

    /**
     * 设置个原本的正确密码，以便用来校对，密码中的每个数字不能超过圆点的个数
     *
     * @param pwdArray
     */
    public void setRightPwdStr(int[] pwdArray) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < pwdArray.length; i++) {
            buffer.append(Integer.toHexString(pwdArray[i]));
            Log.i(TAG, "固定的密码为" + pwdArray[i] + "转为十六进制后：" + Integer.toHexString(pwdArray[i]));
        }
        this.pwdStr = buffer.toString();
    }


    /**
     * 获取选择点的位置
     */
    private Point getSelectedPointPosition() {
        Point point = new Point(mX, mY, mRadius);
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                //判断触摸的点是否在所有目标点的绘制范围内
                if (points[i][j].getInstanceWithPoint(point) < mRadius) {
                    points[i][j].setState(Point.STATE_PRESS);
                    return points[i][j];
                }
            }

        }
        return null;
    }

    /**
     * 清除所有点的状态
     */
    private void resetPoints() {
        if (mSelectedPoints != null) {
            mSelectedPoints.clear();
        }
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                points[i][j].setState(Point.STATE_NORMAL);
            }
        }
    }


    /**
     * 重置所有点的为指定状态
     */
    private void resetPointsWithState(int status) {
        for (int i = 0; i < mSelectedPoints.size(); i++) {
            mSelectedPoints.get(i).setState(status);
        }
    }

    /**
     * 绘制提示语
     *
     * @param canvas
     */
    private void drawTextTips(Canvas canvas) {
        mTextTipsPaint.setTextAlign(Paint.Align.CENTER);
        //字体比例按屏幕比例来，满屏为20dp,越小字越小
        mTextTipsPaint.setTextSize(dp2px((float) mWidth / Math.min(phoneWidth, phoneHeight) * defalut_text_tips_size));
        canvas.drawText(textTips, (float) mWidth / 2, offsetY, mTextTipsPaint);
    }

    /**
     * 绘制两点之间的连线
     *
     * @param canvas
     * @param startP 起始点
     * @param endP   终止点
     */
    private void drawLine(Canvas canvas, Point startP, Point endP) {
        switch (startP.getState()) {
            case Point.STATE_PRESS:
                canvas.drawLine(startP.x, startP.y, endP.x, endP.y, mPressPaint);
                break;
            case Point.STATE_ERROR:
                canvas.drawLine(startP.x, startP.y, endP.x, endP.y, mErrorPaint);
                break;
        }
    }

    /**
     * 绘制几个点的连线
     *
     * @param canvas
     */
    private void drawLines(Canvas canvas) {
        if (mSelectedPoints.size() > 0) {
            Point startP = mSelectedPoints.get(0);
            Point endP;
            for (int i = 1; i < mSelectedPoints.size(); i++) {
                endP = mSelectedPoints.get(i);
                drawLine(canvas, startP, endP);
                //将这个终点最为下一个起点
                startP = endP;
            }

            if (isDraw) {
                drawLine(canvas, startP, new Point(mX, mY, mRadius));
            }
        }

    }

    /**
     * 根据各个点状态进行重绘
     *
     * @param canvas
     */
    private void drawPoints(Canvas canvas) {
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points[i].length; j++) {
                Point point = points[i][j];
                //不同状态的绘制点
                switch (point.getState()) {
                    case Point.STATE_ERROR:
                        canvas.drawCircle(point.x, point.y, point.mRadius, mErrorPaint);
                        break;
                    case Point.STATE_NORMAL:
                        canvas.drawCircle(point.x, point.y, point.mRadius, mNormalPaint);
                        break;
                    case Point.STATE_PRESS:
                        canvas.drawCircle(point.x, point.y, point.mRadius, mPressPaint);
                        break;
                }
            }
        }
    }

    /**
     * 初始化3*3个圆点
     *
     * @param count       多少行
     * @param pItemOffset 每个格子的偏移量
     * @param offsetX     view的x轴偏移量
     * @param offsetY     view的y轴的偏移量
     *                    /**
     *                    * 坐标分布
     *                    * 1(0,0) 2(1,0) 3(2,0)
     *                    *
     *                    * 4(0,1) 5(1,1) 6(2,1)
     *                    *
     *                    * 7(0,2) 8(1,2) 9(2,2)
     *                    *
     *                    * 找出规律为：(point.x +1)  + point.y* 3
     */
    private void initNineCirclePoint(int count, int pItemOffset, int offsetX, int offsetY) {
        Log.i(TAG, "count=" + count + " pItemOffset=" + pItemOffset + " offsetX=" + offsetX + " offsetY=" + offsetY);
        points = new Point[count][count];
        //将格子的偏移量作为圆点的半径
        mRadius = pItemOffset / 3;
        //行
        for (int i = 0; i < count; i++) {
            //列
            for (int j = 0; j < count; j++) {
                points[i][j] = new Point(offsetX + pItemOffset * (i + 1), offsetY + pItemOffset * (j + 1), mRadius);
                points[i][j].setNum(i + 1 + j * count);
                Log.i(TAG, "points[" + i + "][" + j + "]坐标信息是: " + " x=" + offsetX + pItemOffset * (i + 1) + " y=" + offsetY + pItemOffset * (j + 1) + " mRadius=" + mRadius + " \n记录原始密码=" + (i + 1 + j * count) + " 记录十六进制密码 = " + points[i][j].getNum());

            }
        }
    }

    private void showAnimation() {
        //文字加抖动动画，暂未完成
    }

    public interface OnUnLockListener {
        void doUnLock();
    }

    public void setOnUnLockListener(OnUnLockListener lockListener) {
        this.listener = lockListener;
    }


    static int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().getDisplayMetrics());
    }
}

