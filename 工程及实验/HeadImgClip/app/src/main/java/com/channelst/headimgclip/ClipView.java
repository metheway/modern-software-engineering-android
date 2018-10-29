package com.channelst.headimgclip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 截图区域的自定义view,这里是画一个全面遮盖图片的幕布并在中间抠出一个圆形
 */
public class ClipView extends View{

    private Paint paint = new Paint();
    private Paint borderPaint = new Paint();

    /** 裁剪框长宽比，butel中设置正方形的裁剪框 */
    private double clipRatio = 1.0;
    /** 裁剪框宽度 */
    private int clipWidth = -1;
    /** 裁剪框高度 */
    private int clipHeight = -1;
    /** 裁剪框左边空留宽度 */
    private int clipLeftMargin = 0;
    /** 裁剪框上边空留宽度 */
    private int clipTopMargin = 0;
    /** 裁剪框边框宽度 */
    private int clipBorderWidth = 1;

    private OnDrawListenerComplete listenerComplete;

    private float circleCenterPX = 0.0f;
    private float circleCenterPY = 0.0f;
    private float radius = 0.0f;

    public ClipView(Context context) {
        super(context);
    }

    public ClipView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClipView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = this.getWidth();
        int height = this.getHeight();

        //画矩形region1
        canvas.clipRect(0, 0, width, height);

        //竖屏的时候width<height,取width的1/3作为半径
        // 横屏的时候width>height,取height的1/3作为半径
        int shortWidth = width<height?width:height;

        //画圆形region2
        Path path = new Path();
        circleCenterPX = (float) width/2.0f;
        circleCenterPY = (float) height/2.0f;

        radius = shortWidth/3.0f;
        path.addCircle(circleCenterPX, circleCenterPY, radius, Path.Direction.CCW);
        Log.i("ClipView", "onDraw()--circleCenterPX : " + circleCenterPX
                + ", circleCenterPY : " + circleCenterPY + ", radius : " + radius);
        Log.i("ClipView", "onDraw()--width : " + width + ", height : " + height);
        //path.addCircle(150,150,100, Path.Direction.CCW);
        //XOP表示补集就是全集的减去交集剩余部分,这剩余部分不用遮罩
        //也就相当于从遮罩里抠出一个圆形来
        canvas.clipPath(path, Region.Op.XOR);
        //canvas.clipRect(0,0,400,400);
        paint.setAlpha(((int)(255*0.4f)));
        canvas.drawRect(0, 0, width, height,paint);
        canvas.save();
        canvas.restore();

        // 画圆形边框
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(Color.WHITE);
        borderPaint.setStrokeWidth(clipBorderWidth);
        canvas.drawCircle(circleCenterPX, circleCenterPY, radius,borderPaint);

        clipWidth = clipHeight = (int) (radius*2);

        if (listenerComplete != null) {
            listenerComplete.onDrawComplete();
        }
    }

    public double getClipRatio() {
        return clipRatio;
    }

    public void setClipRatio(double clipRatio) {
        this.clipRatio = clipRatio;
    }

    public int getClipWidth() {
        return clipWidth ;
    }

    public float getCircleCenterPX() {
        return circleCenterPX;
    }

    public float getCircleCenterPY() {
        return circleCenterPY;
    }

    public float getRadius() {
        return radius;
    }

    public void setClipWidth(int clipWidth) {
        this.clipWidth = clipWidth;
    }

    public int getClipHeight() {
        return clipHeight;
    }

    public void setClipHeight(int clipHeight) {
        this.clipHeight = clipHeight;
    }

    public void addOnDrawCompleteListener(OnDrawListenerComplete listener) {
        this.listenerComplete = listener;
    }

    public void removeOnDrawCompleteListener() {
        this.listenerComplete = null;
    }

    /**
     * 裁剪区域画完时调用接口
     *
     * @author Cow
     *
     */
    public interface OnDrawListenerComplete {
        void onDrawComplete();
    }

}
