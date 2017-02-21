package me.yluo.androidble.color;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CircleView extends View {

    private int color = 0xffff0000;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CircleView(Context context) {
        super(context);
        init();
    }

    public CircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
    }

    public void setColor(int color) {
        this.color = color | 0xff000000;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(this.color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, Math.min(getHeight() / 2, getHeight() / 2), paint);
    }
}
