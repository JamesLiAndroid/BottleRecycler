package com.incomrecycle.prms.rvm.gui.activity.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import com.incomrecycle.prms.rvm.gui.entity.TextAdEntity;
import java.util.ArrayList;
import java.util.List;

public class VerticalMarqueeTextview extends TextView {
    public static int STATE_FROZEN = 3;
    public static int STATE_MARQUEE_RUNNING = 1;
    public static int STATE_MARQUEE_STOPED = 2;
    public static int STATE_NO_ENOUGH_LENGTH = 4;
    private final int FPS = 66;
    private final int SPEED = 1;
    private boolean hasAttachedToWindow;
    private boolean isEnoughToMarquee = false;
    private boolean isFrozen = true;
    private boolean isFrozenFromVisible = Boolean.FALSE.booleanValue();
    private boolean isFrozenFromWindowFocusChanged = Boolean.FALSE.booleanValue();
    private boolean isMarquee = false;
    final Runnable makeMaxScrollAndCheckFrezon = new Runnable() {
        public void run() {
            VerticalMarqueeTextview.this.maxScrollY = VerticalMarqueeTextview.this.getLineCount() * VerticalMarqueeTextview.this.getLineHeight();
            VerticalMarqueeTextview.this.checkEnoughToMarquee();
            VerticalMarqueeTextview.this.checkFrozen();
        }
    };
    private MarqueeModel marqueeModel = MarqueeModel.AUTO_ON_VISIBLE;
    private int maxScrollY;
    private List<TextAdEntity> mylist = new ArrayList();
    private int postion = 1;
    final Runnable scrollTextRunnable = new Runnable() {
        public void run() {
            if (VerticalMarqueeTextview.this.isMarquee && !VerticalMarqueeTextview.this.isFrozen) {
                VerticalMarqueeTextview.this.scrollBy(0, 1);
                if (VerticalMarqueeTextview.this.getScrollY() > VerticalMarqueeTextview.this.maxScrollY - 15) {
                    if (VerticalMarqueeTextview.this.mylist.size() > 1) {
                        if (VerticalMarqueeTextview.this.postion > VerticalMarqueeTextview.this.mylist.size() - 1) {
                            VerticalMarqueeTextview.this.postion = 0;
                        }
                        VerticalMarqueeTextview.this.setText(((TextAdEntity) VerticalMarqueeTextview.this.mylist.get(VerticalMarqueeTextview.this.postion)).getSbarTxt());
                        VerticalMarqueeTextview.this.postion = VerticalMarqueeTextview.this.postion + 1;
                    }
                    VerticalMarqueeTextview.this.scrollTo(0, (-VerticalMarqueeTextview.this.getHeight()) + 10);
                }
                VerticalMarqueeTextview.this.checkScroll();
            }
        }
    };

    public enum MarqueeModel {
        AUTO_ON_VISIBLE,
        MANUAL
    }

    public VerticalMarqueeTextview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalMarqueeTextview(Context context) {
        super(context);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initMaxScrollAndCheckFrezon();
    }

    private void checkEnoughToMarquee() {
        boolean z = this.maxScrollY < getHeight() || this.maxScrollY > getHeight() || this.maxScrollY == getHeight();
        this.isEnoughToMarquee = z;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            this.isFrozen = true;
            this.isFrozenFromWindowFocusChanged = true;
        } else if (this.isFrozenFromWindowFocusChanged) {
            this.isFrozenFromWindowFocusChanged = false;
            checkFrozen();
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != 0) {
            this.isFrozen = true;
            this.isFrozenFromVisible = true;
        } else if (this.isFrozenFromVisible) {
            this.isFrozenFromVisible = false;
            checkFrozen();
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.hasAttachedToWindow = true;
        checkFrozen();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.hasAttachedToWindow = false;
        checkFrozen();
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        initMaxScrollAndCheckFrezon();
    }

    private void checkFrozen() {
        boolean oldIsFronze = this.isFrozen;
        boolean z = !this.isEnoughToMarquee || !this.hasAttachedToWindow || this.isFrozenFromVisible || this.isFrozenFromWindowFocusChanged;
        this.isFrozen = z;
        if (!oldIsFronze) {
            return;
        }
        if (this.marqueeModel == MarqueeModel.AUTO_ON_VISIBLE) {
            if (this.isMarquee) {
                checkScroll();
            } else {
                startMarquee();
            }
        } else if (this.isMarquee) {
            checkScroll();
        }
    }

    private void checkScroll() {
        if (this.isMarquee && !this.isFrozen) {
            postDelayed(this.scrollTextRunnable, 66);
        }
    }

    private void initMaxScrollAndCheckFrezon() {
        post(this.makeMaxScrollAndCheckFrezon);
    }

    public void startMarquee() {
        if (!this.isMarquee) {
            this.isMarquee = true;
            checkScroll();
        }
    }

    public void resetMarqueeLocation() {
        scrollTo(0, 0);
    }

    public void stopMarquee() {
        this.isMarquee = false;
        removeCallbacks(this.scrollTextRunnable);
    }

    public int getMarqueeState() {
        if (this.isFrozen) {
            return STATE_FROZEN;
        }
        if (!this.isEnoughToMarquee) {
            return STATE_NO_ENOUGH_LENGTH;
        }
        if (this.isMarquee) {
            return STATE_MARQUEE_RUNNING;
        }
        return STATE_MARQUEE_STOPED;
    }

    public void setMarqueeModel(MarqueeModel model) {
        this.marqueeModel = model;
        if (this.marqueeModel == null) {
            this.marqueeModel = MarqueeModel.AUTO_ON_VISIBLE;
        }
        switch (this.marqueeModel) {
            case AUTO_ON_VISIBLE:
                if (getVisibility() == 0) {
                    startMarquee();
                    return;
                } else {
                    stopMarquee();
                    return;
                }
            default:
                return;
        }
    }

    public List<TextAdEntity> getMylist() {
        return this.mylist;
    }

    public void setMylist(List<TextAdEntity> mylist) {
        this.mylist = mylist;
    }
}
