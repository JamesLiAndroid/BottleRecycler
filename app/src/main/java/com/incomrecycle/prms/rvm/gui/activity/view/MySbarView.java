package com.incomrecycle.prms.rvm.gui.activity.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import com.incomrecycle.common.SysGlobal;

public class MySbarView extends View {
    private static final String EMPTY = "";
    private String color = null;
    private float currentTextWidth;
    private int fontSize = 30;
    private boolean isChanged = true;
    private boolean isEnable = true;
    private MyDrawThread myDrawThread = null;
    private OnScrollEvent onScrollEvent = null;
    private int step = 1;
    private String text = EMPTY;
    private int xPos = 0;
    private int yPos = 0;

    private class MyDrawThread implements Runnable {
        private MyDrawThread() {
        }

        public void run() {
            while (MySbarView.this.isEnable) {
                try {
                    Thread.sleep(50);
                    MySbarView.this.postInvalidate();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            synchronized (MySbarView.this) {
                MySbarView.this.myDrawThread = null;
            }
        }
    }

    public interface OnScrollEvent {
        void onChange(View view);
    }

    public MySbarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MySbarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySbarView(Context context) {
        super(context);
        init();
    }

    private void init() {
        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View arg0) {
                MySbarView.this.isEnable = true;
                synchronized (MySbarView.this) {
                    if (MySbarView.this.myDrawThread == null) {
                        MySbarView.this.isEnable = true;
                        MySbarView.this.myDrawThread = new MyDrawThread();
                        SysGlobal.execute(MySbarView.this.myDrawThread);
                    }
                }
            }

            public void onViewDetachedFromWindow(View arg0) {
                MySbarView.this.isEnable = false;
            }
        });
    }

    public void setText(String text, int fontSize, String color, int step) {
        String prevText = this.text;
        if (prevText == null) {
            prevText = EMPTY;
        }
        if (text == null) {
            text = EMPTY;
        }
        if (!text.equals(prevText)) {
            synchronized (this) {
                this.text = text;
                this.isChanged = true;
            }
            this.color = color;
            this.fontSize = fontSize;
            this.xPos = getWidth();
            this.step = step;
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.color != null) {
            String text;
            boolean isChanged;
            synchronized (this) {
                text = this.text;
                isChanged = this.isChanged;
                this.isChanged = false;
            }
            if (text == null) {
                text = EMPTY;
            }
            if (isChanged || text.length() != 0) {
                Paint paint = new Paint();
                paint.setTextSize((float) this.fontSize);
                paint.setAntiAlias(true);
                paint.setColor(Color.parseColor(this.color));
                if (isChanged) {
                    this.currentTextWidth = paint.measureText(this.text);
                    this.xPos = getWidth();
                }
                this.yPos = (getHeight() - this.fontSize) / 2;
                canvas.drawText(text, (float) this.xPos, (float) this.yPos, paint);
                this.xPos -= this.step;
                if (((float) this.xPos) < (-this.currentTextWidth)) {
                    this.xPos = getWidth();
                    if (this.onScrollEvent != null) {
                        SysGlobal.execute(new Thread() {
                            public void run() {
                                OnScrollEvent onScrollEvent = MySbarView.this.onScrollEvent;
                                if (onScrollEvent != null) {
                                    onScrollEvent.onChange(MySbarView.this);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    public void setOnScrollEvent(OnScrollEvent onScrollEvent) {
        synchronized (this) {
            this.onScrollEvent = onScrollEvent;
        }
    }
}
