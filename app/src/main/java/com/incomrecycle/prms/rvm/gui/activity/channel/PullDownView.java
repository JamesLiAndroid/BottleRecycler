package com.incomrecycle.prms.rvm.gui.activity.channel;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import com.incomrecycle.prms.rvm.R;
import com.incomrecycle.prms.rvm.gui.activity.channel.ScrollOverListView.OnScrollOverListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PullDownView extends LinearLayout implements OnScrollOverListener {
    private static final int AUTO_INCREMENTAL = 10;
    private static final int DEFAULT_HEADER_VIEW_HEIGHT = 105;
    private static final int HEADER_VIEW_STATE_IDLE = 0;
    private static final int HEADER_VIEW_STATE_NOT_OVER_HEIGHT = 1;
    private static final int HEADER_VIEW_STATE_OVER_HEIGHT = 2;
    private static final int START_PULL_DEVIATION = 50;
    private static final String TAG = "PullDownView";
    private static final int WHAT_DID_LOAD_DATA = 1;
    private static final int WHAT_DID_MORE = 5;
    private static final int WHAT_DID_REFRESH = 3;
    private static final int WHAT_ON_REFRESH = 2;
    private static final int WHAT_SET_HEADER_HEIGHT = 4;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm");
    private boolean mEnableAutoFetchMore;
    private View mFooterLoadingView;
    private TextView mFooterTextView;
    private View mFooterView;
    private ImageView mHeaderArrowView;
    private int mHeaderIncremental;
    private View mHeaderLoadingView;
    private TextView mHeaderTextView;
    private View mHeaderView;
    private TextView mHeaderViewDateView;
    private LayoutParams mHeaderViewParams;
    private int mHeaderViewState = 0;
    private boolean mIsDown;
    private boolean mIsFetchMoreing;
    private boolean mIsPullUpDone;
    private boolean mIsRefreshing;
    private ScrollOverListView mListView;
    private float mMotionDownLastY;
    private OnPullDownListener mOnPullDownListener;
    private RotateAnimation mRotate180To0Animation;
    private RotateAnimation mRotateOTo180Animation;
    private Handler mUIHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PullDownView.this.mHeaderViewParams.height = 0;
                    PullDownView.this.mHeaderLoadingView.setVisibility(8);
                    PullDownView.this.mHeaderTextView.setText(R.string.loding_on_down);
                    PullDownView.this.mHeaderViewDateView = (TextView) PullDownView.this.mHeaderView.findViewById(R.id.pulldown_header_date);
                    PullDownView.this.mHeaderViewDateView.setVisibility(0);
                    PullDownView.this.mHeaderViewDateView.setText(R.string.loding_time + PullDownView.dateFormat.format(new Date(System.currentTimeMillis())));
                    PullDownView.this.mHeaderArrowView.setVisibility(0);
                    PullDownView.this.showFooterView();
                    return;
                case 2:
                    PullDownView.this.mHeaderArrowView.clearAnimation();
                    PullDownView.this.mHeaderArrowView.setVisibility(4);
                    PullDownView.this.mHeaderLoadingView.setVisibility(0);
                    PullDownView.this.mOnPullDownListener.onRefresh();
                    return;
                case 3:
                    PullDownView.this.mIsRefreshing = false;
                    PullDownView.this.mHeaderViewState = 0;
                    PullDownView.this.mHeaderArrowView.setVisibility(0);
                    PullDownView.this.mHeaderLoadingView.setVisibility(8);
                    PullDownView.this.mHeaderViewDateView.setText(R.string.loding_time + PullDownView.dateFormat.format(new Date(System.currentTimeMillis())));
                    PullDownView.this.setHeaderHeight(0);
                    PullDownView.this.showFooterView();
                    return;
                case 4:
                    PullDownView.this.setHeaderHeight(PullDownView.this.mHeaderIncremental);
                    return;
                case 5:
                    PullDownView.this.mIsFetchMoreing = false;
                    PullDownView.this.mFooterTextView.setText(R.string.loding_more);
                    PullDownView.this.mFooterLoadingView.setVisibility(8);
                    return;
                default:
                    return;
            }
        }
    };

    class HideHeaderViewTask extends TimerTask {
        HideHeaderViewTask() {
        }

        public void run() {
            if (PullDownView.this.mIsDown) {
                cancel();
                return;
            }
            PullDownView.this.mHeaderIncremental = PullDownView.this.mHeaderIncremental - 10;
            if (PullDownView.this.mHeaderIncremental > 0) {
                PullDownView.this.mUIHandler.sendEmptyMessage(4);
                return;
            }
            PullDownView.this.mHeaderIncremental = 0;
            PullDownView.this.mUIHandler.sendEmptyMessage(4);
            cancel();
        }
    }

    public interface OnPullDownListener {
        void onMore();

        void onRefresh();
    }

    class ShowHeaderViewTask extends TimerTask {
        ShowHeaderViewTask() {
        }

        public void run() {
            if (PullDownView.this.mIsDown) {
                cancel();
                return;
            }
            PullDownView.this.mHeaderIncremental = PullDownView.this.mHeaderIncremental - 10;
            if (PullDownView.this.mHeaderIncremental > PullDownView.DEFAULT_HEADER_VIEW_HEIGHT) {
                PullDownView.this.mUIHandler.sendEmptyMessage(4);
                return;
            }
            PullDownView.this.mHeaderIncremental = PullDownView.DEFAULT_HEADER_VIEW_HEIGHT;
            PullDownView.this.mUIHandler.sendEmptyMessage(4);
            if (!PullDownView.this.mIsRefreshing) {
                PullDownView.this.mIsRefreshing = true;
                PullDownView.this.mUIHandler.sendEmptyMessage(2);
            }
            cancel();
        }
    }

    public PullDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initHeaderViewAndFooterViewAndListView(context);
    }

    public PullDownView(Context context) {
        super(context);
        initHeaderViewAndFooterViewAndListView(context);
    }

    public void notifyDidLoad() {
        this.mUIHandler.sendEmptyMessage(1);
    }

    public void notifyDidRefresh() {
        this.mUIHandler.sendEmptyMessage(3);
    }

    public void notifyDidMore() {
        this.mUIHandler.sendEmptyMessage(5);
    }

    public void setOnPullDownListener(OnPullDownListener listener) {
        this.mOnPullDownListener = listener;
    }

    public ListView getListView() {
        return this.mListView;
    }

    public void enableAutoFetchMore(boolean enable, int index) {
        if (enable) {
            this.mListView.setBottomPosition(index);
            this.mFooterLoadingView.setVisibility(VISIBLE);
        } else {
            this.mFooterTextView.setText(R.string.loding_more);
            this.mFooterLoadingView.setVisibility(GONE);
        }
        this.mEnableAutoFetchMore = enable;
    }

    private void initHeaderViewAndFooterViewAndListView(Context context) {
        setOrientation(1);
        this.mHeaderView = LayoutInflater.from(context).inflate(R.layout.pulldown_header, null);
        this.mHeaderViewParams = new LayoutParams(-1, -2);
        addView(this.mHeaderView, 0, this.mHeaderViewParams);
        this.mHeaderTextView = (TextView) this.mHeaderView.findViewById(R.id.pulldown_header_text);
        this.mHeaderArrowView = (ImageView) this.mHeaderView.findViewById(R.id.pulldown_header_arrow);
        this.mHeaderLoadingView = this.mHeaderView.findViewById(R.id.pulldown_header_loading);
        this.mRotateOTo180Animation = new RotateAnimation(0.0f, 180.0f, 1, 0.5f, 1, 0.5f);
        this.mRotateOTo180Animation.setDuration(250);
        this.mRotateOTo180Animation.setFillAfter(true);
        this.mRotate180To0Animation = new RotateAnimation(180.0f, 0.0f, 1, 0.5f, 1, 0.5f);
        this.mRotate180To0Animation.setDuration(250);
        this.mRotate180To0Animation.setFillAfter(true);
        this.mFooterView = LayoutInflater.from(context).inflate(R.layout.pulldown_footer, null);
        this.mFooterTextView = (TextView) this.mFooterView.findViewById(R.id.pulldown_footer_text);
        this.mFooterLoadingView = this.mFooterView.findViewById(R.id.pulldown_footer_loading);
        this.mFooterView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!PullDownView.this.mIsFetchMoreing) {
                    PullDownView.this.mIsFetchMoreing = true;
                    PullDownView.this.mFooterLoadingView.setVisibility(VISIBLE);
                    PullDownView.this.mOnPullDownListener.onMore();
                }
            }
        });
        this.mListView = new ScrollOverListView(context);
        this.mListView.setOnScrollOverListener(this);
        this.mListView.setCacheColorHint(0);
        addView(this.mListView, -1, -1);
        this.mOnPullDownListener = new OnPullDownListener() {
            public void onRefresh() {
            }

            public void onMore() {
            }
        };
    }

    private void checkHeaderViewState() {
        if (this.mHeaderViewParams.height >= DEFAULT_HEADER_VIEW_HEIGHT) {
            if (this.mHeaderViewState != 2) {
                this.mHeaderViewState = 2;
                this.mHeaderTextView.setText(R.string.loding_on);
                this.mHeaderArrowView.startAnimation(this.mRotateOTo180Animation);
            }
        } else if (this.mHeaderViewState != 1 && this.mHeaderViewState != 0) {
            this.mHeaderViewState = 1;
            this.mHeaderTextView.setText(R.string.loding_on_down);
            this.mHeaderArrowView.startAnimation(this.mRotate180To0Animation);
        }
    }

    private void setHeaderHeight(int height) {
        this.mHeaderIncremental = height;
        this.mHeaderViewParams.height = height;
        this.mHeaderView.setLayoutParams(this.mHeaderViewParams);
    }

    private void showFooterView() {
        if (this.mListView.getFooterViewsCount() == 0 && isFillScreenItem()) {
            this.mListView.addFooterView(this.mFooterView);
            this.mListView.setAdapter(this.mListView.getAdapter());
        }
    }

    private boolean isFillScreenItem() {
        if (((this.mListView.getLastVisiblePosition() - this.mListView.getFooterViewsCount()) - this.mListView.getFirstVisiblePosition()) + 1 < this.mListView.getCount() - this.mListView.getFooterViewsCount()) {
            return true;
        }
        return false;
    }

    public boolean onListViewTopAndPullDown(int delta) {
        if (this.mIsRefreshing || this.mListView.getCount() - this.mListView.getFooterViewsCount() == 0) {
            return false;
        }
        this.mHeaderIncremental += (int) Math.ceil(((double) Math.abs(delta)) / 2.0d);
        if (this.mHeaderIncremental >= 0) {
            setHeaderHeight(this.mHeaderIncremental);
            checkHeaderViewState();
        }
        return true;
    }

    public boolean onListViewBottomAndPullUp(int delta) {
        if (!this.mEnableAutoFetchMore || this.mIsFetchMoreing) {
            return false;
        }
        if (!isFillScreenItem()) {
            return false;
        }
        this.mIsFetchMoreing = true;
        this.mFooterTextView.setText(R.string.loding);
        this.mFooterLoadingView.setVisibility(VISIBLE);
        this.mOnPullDownListener.onMore();
        return true;
    }

    public boolean onMotionDown(MotionEvent ev) {
        this.mIsDown = true;
        this.mIsPullUpDone = false;
        this.mMotionDownLastY = ev.getRawY();
        return false;
    }

    public boolean onMotionMove(MotionEvent ev, int delta) {
        if (this.mIsPullUpDone || ((int) Math.abs(ev.getRawY() - this.mMotionDownLastY)) < START_PULL_DEVIATION) {
            return true;
        }
        int i = (int) Math.ceil(((double) Math.abs(delta)) / 2.0d);
        if (this.mHeaderViewParams.height <= 0 || delta >= 0) {
            return false;
        }
        this.mHeaderIncremental -= i;
        if (this.mHeaderIncremental > 0) {
            setHeaderHeight(this.mHeaderIncremental);
            checkHeaderViewState();
            return true;
        }
        this.mHeaderViewState = 0;
        this.mHeaderIncremental = 0;
        setHeaderHeight(this.mHeaderIncremental);
        this.mIsPullUpDone = true;
        return true;
    }

    public boolean onMotionUp(MotionEvent ev) {
        this.mIsDown = false;
        if (this.mHeaderViewParams.height <= 0) {
            return false;
        }
        int x = this.mHeaderIncremental - 105;
        Timer timer = new Timer(true);
        if (x < 0) {
            timer.scheduleAtFixedRate(new HideHeaderViewTask(), 0, 10);
        } else {
            timer.scheduleAtFixedRate(new ShowHeaderViewTask(), 0, 10);
        }
        return true;
    }
}
