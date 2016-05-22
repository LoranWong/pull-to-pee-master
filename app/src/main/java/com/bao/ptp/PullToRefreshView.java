package com.bao.ptp;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.ImageView;

/**
 * Created by LoranWong/me@loranwong.com on 5/20/16.
 **/
public class PullToRefreshView extends ViewGroup {

    private static final int DRAG_MAX_DISTANCE = 110;
    private static final float DRAG_RATE = .5f;
    private static final int MAX_OFFSET_ANIMATION_DURATION = 1000;
    private static final int PEE_VIEW_WIDTH = 100;
    private static final int PEE_VIEW_HEIGHT = 100;
    private static final int PEE_VIEW_MARGIN = 5;

    private final int mTotalDragDistance;
    private final PeeDrawable mPeeDrawable;
    private boolean mRefreshing;
    private OnRefreshListener mListener;

    private final ImageView mRefreshView;

    private View mTarget;
    private boolean mIsBeingDragged;
    private float mInitialMotionY;
    private int mCurrentOffsetTop;
    private float mCurrentDragPercent;
    private boolean mNotify = true;
    private int mFrom;
    private float mFromDragPercent;

    private int mTargetPaddingTop;
    private int mTargetPaddingBottom;
    private int mTargetPaddingRight;
    private int mTargetPaddingLeft;

    public PullToRefreshView(Context context) {
        this(context, null);
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);

//        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);
//        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mTotalDragDistance = ViewUtils.dip2px(context, DRAG_MAX_DISTANCE);

        mRefreshView = new ImageView(context);
        mPeeDrawable = new PeeDrawable(mRefreshView);
        mRefreshView.setImageDrawable(mPeeDrawable);
        addView(mRefreshView);
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);


        ensureTarget();
        if (mTarget == null)
            return;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);

        int refreshWidthMeasureSpec = MeasureSpec.makeMeasureSpec(ViewUtils.dip2px(getContext(),PEE_VIEW_WIDTH) - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        int refreshHeightMeasureSpec = MeasureSpec.makeMeasureSpec(ViewUtils.dip2px(getContext(),PEE_VIEW_HEIGHT) - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);

        mTarget.measure(widthMeasureSpec, heightMeasureSpec);
        mRefreshView.measure(refreshWidthMeasureSpec, refreshHeightMeasureSpec);

        Log.i(PeeDrawable.TAG,"onMeasure ->  getMeasuredWidth -> " + getMeasuredWidth());
        Log.i(PeeDrawable.TAG,"onMeasure ->  getMeasuredHeight -> " + getMeasuredHeight());
        Log.i(PeeDrawable.TAG,"onMeasure ->  getPaddingLeft -> " + getPaddingLeft());
        Log.i(PeeDrawable.TAG,"onMeasure ->  getPaddingTop -> " + getPaddingTop());
        Log.i(PeeDrawable.TAG,"onMeasure ->  getPaddingRight -> " + getPaddingRight());
        Log.i(PeeDrawable.TAG,"onMeasure ->  getPaddingBottom -> " + getPaddingBottom());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ensureTarget();
        if (mTarget == null)
            return;

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();

        int peeWidth = ViewUtils.dip2px(getContext(),PEE_VIEW_WIDTH);
        int peeHeight = ViewUtils.dip2px(getContext(),PEE_VIEW_HEIGHT);

        int margin = ViewUtils.dip2px(getContext(), PEE_VIEW_MARGIN);

        Log.i(PeeDrawable.TAG,"onLayout ->  getMeasuredWidth -> " + width);
        Log.i(PeeDrawable.TAG,"onLayout ->  getMeasuredHeight -> " + height);
        Log.i(PeeDrawable.TAG,"onLayout ->  getPaddingLeft -> " + left);
        Log.i(PeeDrawable.TAG,"onLayout ->  getPaddingTop -> " + top);
        Log.i(PeeDrawable.TAG,"onLayout ->  getPaddingRight -> " + right);
        Log.i(PeeDrawable.TAG,"onLayout ->  getPaddingBottom -> " + bottom);

        Log.i(PeeDrawable.TAG,"onLayout ->  peeWidth -> " + peeWidth);
        Log.i(PeeDrawable.TAG,"onLayout ->  peeHeight -> " + peeHeight);


        mTarget.layout(left, top + mCurrentOffsetTop, left + width - right, top + height - bottom + mCurrentOffsetTop);
        mRefreshView.layout(left + (width-peeWidth)/2 , top + margin, left + width - right -  (width-peeWidth)/2, top + peeHeight + margin - bottom);
    }
    private void ensureTarget() {
        if (mTarget != null)
            return;
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != mRefreshView) {
                    mTarget = child;
                    mTargetPaddingBottom = mTarget.getPaddingBottom();
                    mTargetPaddingLeft = mTarget.getPaddingLeft();
                    mTargetPaddingRight = mTarget.getPaddingRight();
                    mTargetPaddingTop = mTarget.getPaddingTop();
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isEnabled() || canChildScrollUp() || mRefreshing) {
            return false;
        }

        final int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mIsBeingDragged = false;
                final float initialMotionY = ev.getY();
                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;
                break;
            case MotionEvent.ACTION_MOVE:
                final float yDiff = ev.getY() - mInitialMotionY;
                if (yDiff > 0 && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                break;
        }

        return mIsBeingDragged;

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsBeingDragged) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();

        switch (action){
            case MotionEvent.ACTION_MOVE:
                float yDiff = event.getY() - mInitialMotionY;
                float scrollTop = yDiff * DRAG_RATE;
                mCurrentDragPercent = scrollTop / mTotalDragDistance;
                if (mCurrentDragPercent < 0) {
                    return false;
                }

                Log.i(PeeDrawable.TAG,"mCurrentDragPercent -> " + mCurrentDragPercent);

                mPeeDrawable.setPercent(mCurrentDragPercent * 100);
                setTargetOffsetTop((int)scrollTop - mCurrentOffsetTop);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                yDiff = (event.getY() - mInitialMotionY) ;
                scrollTop = yDiff * DRAG_RATE;

                mIsBeingDragged = false;
                if (scrollTop > mTotalDragDistance) {
                    setRefreshing(true, true);
                } else {
                    mRefreshing = false;
                    animateOffsetToStartPosition();
                }
                return false;
        }

        return true;
    }

    public void setRefreshing(boolean refreshing) {
        if (mRefreshing != refreshing) {
            setRefreshing(refreshing,false);
        }
    }

    private void setRefreshing(boolean refreshing, boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                mPeeDrawable.setPercent(100);
                animateOffsetToCorrectPosition();
            } else {
                animateOffsetToStartPosition();
            }
        }
    }


    private void animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;
        long animationDuration = Math.abs((long) (MAX_OFFSET_ANIMATION_DURATION * mFromDragPercent));

        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(animationDuration);
        mAnimateToStartPosition.setInterpolator(new DecelerateInterpolator());
        mAnimateToStartPosition.setAnimationListener(mToStartListener);
        clearAnimation();
        startAnimation(mAnimateToStartPosition);
    }

    private void animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;

        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(MAX_OFFSET_ANIMATION_DURATION);
        mAnimateToCorrectPosition.setInterpolator(new DecelerateInterpolator());
        clearAnimation();
        startAnimation(mAnimateToCorrectPosition);

        if (mRefreshing) {
            mPeeDrawable.start();
            if (mNotify) {
                if (mListener != null) {
                    mListener.onRefresh();
                }
            }
        } else {
            mPeeDrawable.stop();
            animateOffsetToStartPosition();
        }
        mCurrentOffsetTop = mTarget.getTop();
        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTotalDragDistance);
    }


    private void moveToStart(float interpolatedTime) {
        int targetTop = mFrom - (int) (mFrom * interpolatedTime);
        float targetPercent = mFromDragPercent * (1.0f - interpolatedTime);
        int offset = targetTop - mTarget.getTop();

        mCurrentDragPercent = targetPercent;
        mPeeDrawable.setPercent(mCurrentDragPercent * 100);
        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTargetPaddingBottom + targetTop);
        setTargetOffsetTop(offset);
    }


    /**
     * 滑动View的位置
     * @param offset
     */
    private void setTargetOffsetTop(int offset) {
        mTarget.offsetTopAndBottom(offset);
//        mRefreshView.offsetTopAndBottom(offset);
        mCurrentOffsetTop = mTarget.getTop();
        invalidate();
//        mRefreshView.invalidate();
    }


    /**
     * 滑动View是否还能向上滚动
     * @return
     */
    private boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }




    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private final Animation.AnimationListener mToStartListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            mPeeDrawable.stop();
            mCurrentOffsetTop = mTarget.getTop();
        }
    };

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop;
            int endTarget = mTotalDragDistance;
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mTarget.getTop();

            mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime;
            mPeeDrawable.setPercent(mCurrentDragPercent * 100);

            setTargetOffsetTop(offset);
        }
    };

    public void setOnRefreshListener(OnRefreshListener mListener) {
        this.mListener = mListener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }
}
