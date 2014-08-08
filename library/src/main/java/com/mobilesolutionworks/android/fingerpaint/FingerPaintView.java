package com.mobilesolutionworks.android.fingerpaint;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by yunarta on 28/5/14.
 */
public class FingerPaintView extends View
{

    protected Path mPath;
    protected Path mStroke;
    protected Path mDraw;
    protected int mDrawHistory = Integer.MAX_VALUE;

    protected float mStrokeWidth;
    protected float mShadowRadius;

    protected float lastX;
    protected float lastY;

    protected Bitmap mBitmap;
    protected Canvas mCanvas;
    private Paint mLinePaint;
    private Paint mQuickPaint;

    public FingerPaintView(Context context)
    {
        this(context, null);
    }

    public FingerPaintView(Context context, AttributeSet attrs)
    {
        this(context, attrs, 0);
    }

    public FingerPaintView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);

        mPath = new Path();
        mDraw = new Path();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mStrokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, metrics);
        mShadowRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, metrics);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int unit = MeasureSpec.getSize(widthMeasureSpec);
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(unit, MeasureSpec.EXACTLY));
    }

    public void reset()
    {
        mPath = new Path();
        invalidate();
    }

    public Bitmap saveToBitmap(int width, int height)
    {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        float scale = Math.max((float) width / mBitmap.getWidth(), (float) height / mBitmap.getHeight());
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        Paint mFinalPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFinalPaint.setStyle(Paint.Style.STROKE);
        mFinalPaint.setStrokeWidth(scale * mStrokeWidth);
        mFinalPaint.setShadowLayer(scale * mShadowRadius, 0, 0, Color.BLACK);

        Path path = new Path(mPath);

        RectF bounds = new RectF();
        path.computeBounds(bounds, true);

        Log.d("bound", "bound = " + bounds);

        path.transform(matrix);

        path.computeBounds(bounds, true);

        Log.d("bound", "bound = " + bounds);

        Canvas canvas = new Canvas(bitmap);
        mFinalPaint.setColor(Color.WHITE);
        canvas.drawPaint(mFinalPaint);
        mFinalPaint.setColor(Color.BLACK);
        canvas.drawPath(path, mFinalPaint);

        return bitmap;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        mBitmap = Bitmap.createBitmap(right - left, bottom - top, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        mQuickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mQuickPaint.setColor(Color.BLACK);
        mQuickPaint.setStyle(Paint.Style.STROKE);
        mQuickPaint.setStrokeWidth(mStrokeWidth);

        mLinePaint = new Paint(mQuickPaint);
        mLinePaint.setShadowLayer(mShadowRadius, 0, 0, Color.BLACK);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = event.getAction();

        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
            {
                lastX = event.getX();
                lastY = event.getY();

                mStroke = new Path();
                mStroke.moveTo(lastX, lastY);

                break;
            }

            case MotionEvent.ACTION_UP:
            {
                mPath.addPath(mStroke);
                mStroke = null;


                mBitmap.eraseColor(Color.TRANSPARENT);
                mDrawHistory = Integer.MAX_VALUE;
                invalidate();
                break;
            }

            case MotionEvent.ACTION_MOVE:
            {

//                int size = event.getHistorySize();
//                if (size > 1)
                {
                    float toX = event.getX();
                    float toY = event.getY();

                    float fromX = lastX;
                    float fromY = lastY;

                    final float x = (toX + fromX) / 2;
                    final float y = (toY + fromY) / 2;

                    mStroke.cubicTo(fromX, fromY, toX, toY, x, y);

                    if (mDrawHistory++ > 30)
                    {
                        mDraw.reset();
                        mDraw.moveTo(fromX, fromY);
                        mDrawHistory = 0;

                        mCanvas.drawLine(fromX, fromY, toX, toY, mQuickPaint);
                    }

                    mDraw.cubicTo(fromX, fromY, toX, toY, x, y);

                    mCanvas.drawPath(mDraw, mQuickPaint);
//                    mCanvas.drawLine(fromX, fromY, toX, toY, mLinePaint);

                    invalidate();
                }

                lastX = event.getX();
                lastY = event.getY();
                break;
            }
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        canvas.drawPath(mPath, mLinePaint);
        canvas.drawBitmap(mBitmap, 0, 0, mLinePaint);
    }
}
