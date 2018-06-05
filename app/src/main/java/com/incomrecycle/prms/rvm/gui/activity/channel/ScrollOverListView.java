package com.incomrecycle.prms.rvm.gui.activity.channel;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ScrollOverListView extends ListView {
    private int mBottomPosition;
    private int mLastY;
    private OnScrollOverListener mOnScrollOverListener = new OnScrollOverListener() {
        public boolean onListViewTopAndPullDown(int delta) {
            return false;
        }

        public boolean onListViewBottomAndPullUp(int delta) {
            return false;
        }

        public boolean onMotionDown(MotionEvent ev) {
            return false;
        }

        public boolean onMotionMove(MotionEvent ev, int delta) {
            return false;
        }

        public boolean onMotionUp(MotionEvent ev) {
            return false;
        }
    };
    private int mTopPosition;

    public interface OnScrollOverListener {
        boolean onListViewBottomAndPullUp(int i);

        boolean onListViewTopAndPullDown(int i);

        boolean onMotionDown(MotionEvent motionEvent);

        boolean onMotionMove(MotionEvent motionEvent, int i);

        boolean onMotionUp(MotionEvent motionEvent);
    }

    public ScrollOverListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ScrollOverListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScrollOverListView(Context context) {
        super(context);
        init();
    }

    private void init() {
        this.mTopPosition = 0;
        this.mBottomPosition = 0;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        int y = (int) ev.getRawY();
        switch (ev.getAction()) {
            case 0:
                this.mLastY = y;
                boolean isHandled = this.mOnScrollOverListener.onMotionDown(ev);
                if (isHandled) {
                    this.mLastY = y;
                    return isHandled;
                }
                break;
            case 1:
                if (this.mOnScrollOverListener.onMotionUp(ev)) {
                    this.mLastY = y;
                    return true;
                }
                break;
            case 2:
                int childCount = getChildCount();
                if (childCount == 0) {
                    return super.onTouchEvent(ev);
                }
                int itemCount = getAdapter().getCount() - this.mBottomPosition;
                int deltaY = y - this.mLastY;
                int firstTop = getChildAt(0).getTop();
                int listPadding = getListPaddingTop();
                int lastBottom = getChildAt(childCount - 1).getBottom();
                int end = getHeight() - getPaddingBottom();
                int firstVisiblePosition = getFirstVisiblePosition();
                if (this.mOnScrollOverListener.onMotionMove(ev, deltaY)) {
                    this.mLastY = y;
                    return true;
                } else if (firstVisiblePosition <= this.mTopPosition && firstTop >= listPadding && deltaY > 0 && this.mOnScrollOverListener.onListViewTopAndPullDown(deltaY)) {
                    this.mLastY = y;
                    return true;
                } else if (firstVisiblePosition + childCount >= itemCount && lastBottom <= end && deltaY < 0 && this.mOnScrollOverListener.onListViewBottomAndPullUp(deltaY)) {
                    this.mLastY = y;
                    return true;
                }
                break;
        }
        this.mLastY = y;
        return super.onTouchEvent(ev);
    }

    public void setTopPosition(int index) {
        if (getAdapter() == null) {
            throw new NullPointerException("You must set adapter before setTopPosition!");
        } else if (index < 0) {
            throw new IllegalArgumentException("Top position must > 0");
        } else {
            this.mTopPosition = index;
        }
    }

    public void setBottomPosition(int index) {
        if (getAdapter() == null) {
            throw new NullPointerException("You must set adapter before setBottonPosition!");
        } else if (index < 0) {
            throw new IllegalArgumentException("Bottom position must > 0");
        } else {
            this.mBottomPosition = index;
        }
    }

    public void setOnScrollOverListener(OnScrollOverListener onScrollOverListener) {
        this.mOnScrollOverListener = onScrollOverListener;
    }
}
