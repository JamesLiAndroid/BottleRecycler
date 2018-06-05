package com.incomrecycle.prms.rvm.gui.activity.view;

import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.System;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.DigitalClock;
import java.util.Calendar;

public class MyDigitalClock extends DigitalClock {
    private static final String mFormat = "yyyy-MM-dd k:mm:ss";
    Calendar mCalendar;
    private FormatChangeObserver mFormatChangeObserver;
    private Handler mHandler;
    private Runnable mTicker;
    private boolean mTickerStopped = false;

    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange) {
        }
    }

    public MyDigitalClock(Context context) {
        super(context);
        initClock(context);
    }

    public MyDigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initClock(context);
    }

    private void initClock(Context context) {
        Resources r = context.getResources();
        if (this.mCalendar == null) {
            this.mCalendar = Calendar.getInstance();
        }
        this.mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(System.CONTENT_URI, true, this.mFormatChangeObserver);
    }

    protected void onAttachedToWindow() {
        this.mTickerStopped = false;
        super.onAttachedToWindow();
        this.mHandler = new Handler();
        this.mTicker = new Runnable() {
            public void run() {
                if (!MyDigitalClock.this.mTickerStopped) {
                    MyDigitalClock.this.mCalendar.setTimeInMillis(java.lang.System.currentTimeMillis());
                    MyDigitalClock.this.setText(DateFormat.format(MyDigitalClock.mFormat, MyDigitalClock.this.mCalendar));
                    MyDigitalClock.this.invalidate();
                    long now = SystemClock.uptimeMillis();
                    MyDigitalClock.this.mHandler.postAtTime(MyDigitalClock.this.mTicker, now + (1000 - (now % 1000)));
                }
            }
        };
        this.mTicker.run();
    }
}
