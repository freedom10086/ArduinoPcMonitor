package me.yluo.androidble.color;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import me.yluo.androidble.Utils;


public class ArgbBar extends View {
    private int barWidth;
    private int mBarLength;
    private int mBarPointerRadius;
    private int pointerHoloR;
    private Paint pointPaint;
    private Paint pointHoloPaint;
    private Shader[] shaderARGB = new Shader[4];
    private Paint mBarPaint;
    private int mColor;

    private ColorPicker mPicker = null;

    public ArgbBar(Context context) {
        super(context);
        init(null, 0);
    }

    public ArgbBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ArgbBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        mBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointHoloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointHoloPaint.setColor(Color.BLACK);
        pointHoloPaint.setAlpha(0x50);
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        setColor(0xd5be5cce);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minHeight = Utils.dp2px(getContext(), 140);

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);

        int height;
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            height = minHeight;
        }

        height = Math.min(minHeight, height);
        barWidth = Utils.dp2px(getContext(), 6);
        mBarPointerRadius = Utils.dp2px(getContext(), 8);
        pointerHoloR = mBarPointerRadius + Utils.dp2px(getContext(), 4);
        mBarLength = width - pointerHoloR * 2;
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int len = getHeight() / 8;
        int cx, cy;
        for (int i = 0; i < 4; i++) {
            cx = (int) (pointerHoloR + ((mColor >> ((3 - i) * 8)) & 0xff) * 1.0f / 255 * mBarLength);
            if (cx > pointerHoloR + mBarLength) cx = pointerHoloR + mBarLength;
            cy = (2 * i + 1) * len;
            mBarPaint.setShader(shaderARGB[i]);
            canvas.drawRect(pointerHoloR, cy - barWidth / 2, getWidth() - pointerHoloR, cy + barWidth / 2, mBarPaint);
            canvas.drawCircle(cx, cy, pointerHoloR, pointHoloPaint);
            pointPaint.setColor(mColor | 0xff000000);
            canvas.drawCircle(cx, cy, mBarPointerRadius, pointPaint);
        }
    }

    private boolean mIsMovingPointer;
    private int moveItem = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        getParent().requestDisallowInterceptTouchEvent(true);
        float x = event.getX();
        float y = event.getY();
        int len = getHeight() / 8;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean isok = false;
                if (x >= 0 && x <= mBarLength + pointerHoloR) {
                    for (int i = 0; i < 4; i++) {
                        int cy = (2 * i + 1) * len;
                        if (y >= (cy - pointerHoloR) && y <= cy + pointerHoloR) {
                            barMove(i, Math.round(x));
                            mIsMovingPointer = true;
                            moveItem = i;
                            isok = true;
                            break;
                        }
                    }
                }

                if (!isok) {
                    mIsMovingPointer = false;
                    moveItem = -1;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!mIsMovingPointer) break;
                barMove(moveItem, Math.round(x));
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsMovingPointer = false;
                moveItem = -1;
                break;
        }
        return true;
    }

    private void barMove(int witch, int position) {
        int value = getValue(position);
        switch (witch) {
            case 0://a
                mColor = Color.argb(value, Color.red(mColor), Color.green(mColor), Color.blue(mColor));
                break;
            case 1://r
                mColor = Color.argb(Color.alpha(mColor), value, Color.green(mColor), Color.blue(mColor));
                break;
            case 2://g
                mColor = Color.argb(Color.alpha(mColor), Color.red(mColor), value, Color.blue(mColor));
                break;
            case 3://b
                mColor = Color.argb(Color.alpha(mColor), Color.red(mColor), Color.green(mColor), value);
                break;
        }

        setColor(mColor);
    }

    private int getValue(int position) {
        position = position - pointerHoloR;
        if (position < 0) {
            position = 0;
        }
        int value = (int) (position * 1.0f / mBarLength * 255);
        if (value > 255) value = 255;
        else if (value < 0) value = 0;
        return value;
    }

    public void setColor(int color) {
        //if (color == mColor) return;
        mColor = color;
        shaderARGB[0] = new LinearGradient(pointerHoloR, 0, mBarLength + pointerHoloR, barWidth,
                new int[]{color & 0x00ffffff, color | 0xff000000}, null, Shader.TileMode.CLAMP);
        shaderARGB[1] = new LinearGradient(pointerHoloR, 0, mBarLength + pointerHoloR, barWidth,
                new int[]{color & 0xff00ffff, color | 0x00ff0000}, null, Shader.TileMode.CLAMP);
        shaderARGB[2] = new LinearGradient(pointerHoloR, 0, mBarLength + pointerHoloR, barWidth,
                new int[]{color & 0xffff00ff, color | 0x0000ff00}, null, Shader.TileMode.CLAMP);
        shaderARGB[3] = new LinearGradient(pointerHoloR, 0, mBarLength + pointerHoloR, barWidth,
                new int[]{color & 0xffffff00, color | 0x000000ff}, null, Shader.TileMode.CLAMP);
        if (mPicker != null && mPicker.getColor() != mColor) {
            mPicker.setColor(mColor);
        }
        invalidate();

    }

    public int getColor() {
        return mColor;
    }

    public void setColorPicker(ColorPicker picker) {
        mPicker = picker;
    }
}
