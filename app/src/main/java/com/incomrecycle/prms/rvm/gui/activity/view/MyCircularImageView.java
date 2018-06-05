package com.incomrecycle.prms.rvm.gui.activity.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MyCircularImageView extends ImageView {
    private Bitmap bitmap;
    private Rect bitmapRect;
    private Bitmap mDstB;
    private Paint paint;
    private PaintFlagsDrawFilter pdf;
    private PorterDuffXfermode xfermode;

    public MyCircularImageView(Context context) {
        super(context);
        this.bitmapRect = new Rect();
        this.pdf = new PaintFlagsDrawFilter(0, 3);
        this.paint = new Paint();
        this.paint.setStyle(Style.STROKE);
        this.paint.setFlags(1);
        this.paint.setAntiAlias(true);
        this.mDstB = null;
        this.xfermode = new PorterDuffXfermode(Mode.MULTIPLY);
        init();
    }

    public MyCircularImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public MyCircularImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.bitmapRect = new Rect();
        this.pdf = new PaintFlagsDrawFilter(0, 3);
        this.paint = new Paint();
        this.paint.setStyle(Style.STROKE);
        this.paint.setFlags(1);
        this.paint.setAntiAlias(true);
        this.mDstB = null;
        this.xfermode = new PorterDuffXfermode(Mode.MULTIPLY);
        init();
    }

    private void init() {
        try {
            if (VERSION.SDK_INT >= 11) {
                setLayerType(1, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setImageResource(int resId) {
        this.bitmap = BitmapFactory.decodeResource(getResources(), resId);
    }

    private Bitmap makeDst(int w, int h) {
        Bitmap bm = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint(1);
        p.setColor(Color.parseColor("#ffffffff"));
        c.drawRoundRect(new RectF(0.0f, 0.0f, (float) w, (float) h), 26.0f, 26.0f, p);
        return bm;
    }

    protected void onDraw(Canvas canvas) {
        if (this.bitmap != null) {
            if (this.mDstB == null) {
                this.mDstB = makeDst(getWidth(), getHeight());
            }
            this.bitmapRect.set(0, 0, getWidth(), getHeight());
            canvas.save();
            canvas.setDrawFilter(this.pdf);
            canvas.drawBitmap(this.mDstB, 0.0f, 0.0f, this.paint);
            this.paint.setXfermode(this.xfermode);
            canvas.drawBitmap(this.bitmap, null, this.bitmapRect, this.paint);
            this.paint.setXfermode(null);
            canvas.restore();
        }
    }
}
