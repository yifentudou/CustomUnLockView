package com.example.ninelockview;

/**
 * Created by lzr on 2021/3/23.
 * Describe:绘制一个圆点，携带x坐标，Y坐标，R半径，状态，代表的值
 */
public class Point {
    // 正常状态
    public static final int STATE_NORMAL = 1;
    // 按下状态
    public static final int STATE_PRESS = 2;
    // 错误状态
    public static final int STATE_ERROR = 3;

    public float x;
    public float y;
    private int num;
    public float mRadius;
    private int state = STATE_NORMAL;

    /**
     * 计算特定点和当前点的距离
     *
     * @param point
     * @return
     */
    public float getInstanceWithPoint(Point point) {
        return (float) Math.sqrt(Math.pow(point.x - x, 2) + Math.pow(point.y - y, 2));
    }


    public String getNum() {
        return Integer.toHexString(num);
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Point(float x, float y, float mRadius) {
        this.x = x;
        this.y = y;
        this.mRadius = mRadius;
    }

    public float getmRadius() {
        return mRadius;
    }

    public void setmRadius(float mRadius) {
        this.mRadius = mRadius;
    }


    public int getState() {
        return state;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setState(int state) {
        this.state = state;
    }
}
