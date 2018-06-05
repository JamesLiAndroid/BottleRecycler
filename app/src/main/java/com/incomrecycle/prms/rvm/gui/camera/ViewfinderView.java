package com.incomrecycle.prms.rvm.gui.camera;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import com.google.zxing.ResultPoint;
import com.incomrecycle.prms.rvm.R;
import java.util.Collection;
import java.util.HashSet;

public final class ViewfinderView extends View {
    private static final long ANIMATION_DELAY = 100;
    private static final int MAX_FRAME_HEIGHT = 360;
    private static final int MAX_FRAME_WIDTH = 480;
    private static final int MIN_FRAME_HEIGHT = 240;
    private static final int MIN_FRAME_WIDTH = 240;
    private static final int OPAQUE = 255;
    private static final int[] SCANNER_ALPHA = new int[]{0, 64, 128, 192, 255, 192, 128, 64};
    private final int frameColor;
    private final int laserColor;
    private Collection<ResultPoint> lastPossibleResultPoints;
    private final int maskColor;
    private final Paint paint = new Paint();
    private Collection<ResultPoint> possibleResultPoints;
    private Bitmap resultBitmap;
    private final int resultColor;
    private final int resultPointColor;
    private int scannerAlpha;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources resources = getResources();
        this.maskColor = resources.getColor(R.color.viewfinder_mask);
        this.resultColor = resources.getColor(R.color.result_view);
        this.frameColor = resources.getColor(R.color.viewfinder_frame);
        this.laserColor = resources.getColor(R.color.viewfinder_laser);
        this.resultPointColor = resources.getColor(R.color.possible_result_points);
        this.scannerAlpha = 0;
        this.possibleResultPoints = new HashSet(5);
    }

    public void onDraw(Canvas canvas) {
        Rect frame = getFramingRect();
        if (frame != null) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            this.paint.setColor(this.resultBitmap != null ? this.resultColor : this.maskColor);
            this.paint.setColor(getResources().getColor(R.color.blue));
            this.paint.setAlpha(100);
            canvas.drawRect(0.0f, 0.0f, (float) width, (float) frame.top, this.paint);
            canvas.drawRect(0.0f, (float) frame.top, (float) frame.left, (float) (frame.bottom + 1), this.paint);
            canvas.drawRect((float) (frame.right + 1), (float) frame.top, (float) width, (float) (frame.bottom + 1), this.paint);
            canvas.drawRect(0.0f, (float) (frame.bottom + 1), (float) width, (float) height, this.paint);
            if (this.resultBitmap != null) {
                this.paint.setAlpha(255);
                canvas.drawBitmap(this.resultBitmap, (float) frame.left, (float) frame.top, this.paint);
                return;
            }
            this.paint.setColor(this.frameColor);
            Collection<ResultPoint> currentPossible = this.possibleResultPoints;
            Collection<ResultPoint> currentLast = this.lastPossibleResultPoints;
            if (currentPossible.isEmpty()) {
                this.lastPossibleResultPoints = null;
            } else {
                this.possibleResultPoints = new HashSet(5);
                this.lastPossibleResultPoints = currentPossible;
                this.paint.setAlpha(255);
                this.paint.setColor(this.resultPointColor);
                for (ResultPoint point : currentPossible) {
                    canvas.drawCircle(((float) frame.left) + point.getX(), ((float) frame.top) + point.getY(), 6.0f, this.paint);
                }
            }
            if (currentLast != null) {
                // TransportMediator.KEYCODE_MEDIA_PAUSE
                this.paint.setAlpha(127);
                this.paint.setColor(this.resultPointColor);
                for (ResultPoint point2 : currentLast) {
                    canvas.drawCircle(((float) frame.left) + point2.getX(), ((float) frame.top) + point2.getY(), 3.0f, this.paint);
                }
            }
            postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top, frame.right, frame.bottom);
        }
    }

    public void drawViewfinder() {
        this.resultBitmap = null;
        invalidate();
    }

    public void drawResultBitmap(Bitmap barcode) {
        this.resultBitmap = barcode;
        invalidate();
    }

    public void addPossibleResultPoint(ResultPoint point) {
        this.possibleResultPoints.add(point);
    }

    public Rect getFramingRect() {
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point screenResolution = new Point(display.getWidth(), display.getHeight());
        int width = (screenResolution.x * 3) / 4;
        if (width < 240) {
            width = 240;
        } else if (width > MAX_FRAME_WIDTH) {
            width = MAX_FRAME_WIDTH;
        }
        int height = (screenResolution.y * 3) / 4;
        if (height < 240) {
            height = 240;
        } else if (height > MAX_FRAME_HEIGHT) {
            height = MAX_FRAME_HEIGHT;
        }
        int leftOffset = (screenResolution.x - width) / 2;
        int topOffset = (screenResolution.y - height) / 2;
        return new Rect(leftOffset, topOffset - 120, leftOffset + width, (topOffset + height) - 5);
    }
}
