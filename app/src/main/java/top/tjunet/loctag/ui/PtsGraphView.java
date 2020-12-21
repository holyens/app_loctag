package top.tjunet.loctag.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class PtsGraphView extends View {
    final Object mDataLock = new Object();
    public float[] mPts;
    public int mPtsCnt = 0;
    final Paint paint = new Paint();
//    private float xMin = 0;
//    private float xMax = 0;
//    private float yMin = 0;
//    private float yMax = 0;
    private float xRange = Float.MAX_VALUE;
    private float yRange = Float.MAX_VALUE;

    public PtsGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(12);
    }
    public void setScale(float xRange, float yRange, int ptsNum) {
        this.xRange = xRange;
        this.yRange = yRange;
        synchronized (mDataLock) {
            mPts = new float[ptsNum * 2];
            mPtsCnt = 0;
        }
    }
    public void addPoint(float x, float y) {
        synchronized (mDataLock) {
            if (mPtsCnt<mPts.length) {
                mPts[mPtsCnt++] = x/xRange*getWidth();
                mPts[mPtsCnt++] = getHeight()-y/yRange*getHeight();
                // Log.i("debug-draw", String.format("%d %d %f %d", mPtsCnt, mPts.length, mPts[mPtsCnt<<1], getWidth()));
            }
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPts!=null && mPtsCnt>0) {
            canvas.drawPoints(mPts, 0, mPtsCnt, paint);
        }
    }
}
