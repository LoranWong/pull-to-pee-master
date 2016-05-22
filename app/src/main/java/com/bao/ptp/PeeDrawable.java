package com.bao.ptp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;

/**
 * Created by LoranWong/me@loranwong.com on 5/19/16.
 **/

public class PeeDrawable extends Drawable implements Animatable {
    public static final String TAG = "PeeDrawable";
    private static final int DURATION_VIBRATE = 500;
    private static final int DURATION_GROW = 3000;
    private static final int DURATION_CURVE = 1000;

    public static final int PERCENT_MIDDLE = 105;
    public static final int PERCENT_HIGH = 110;
    public static final int PERCENT_LOW = 100;

    public static final int DROP_DENSITY = 6;

    private View mParentView;
    private float mOneHundred;
    private int mParentWidth;
    private int mParentHeight;
    private int mDropWidth;
    private int mDropHeight;
    private int mCupWidth;
    private int mCupHeight;

    private Bitmap mKid;
    private Bitmap mDrop;
    private Bitmap mCupEmpty;
    private Bitmap mCupFill;

    private boolean hasInit = false;
    private Matrix mMatrix;
    private float mPercent = 0;
    private boolean isRefreshing = false;

    private int mDropCurveRepeatCount = 0;
    private float mDropBounce = 0;
    private float mDropCurve = 0;
    private float mCupGrow = 0;

    private Animation mCupGrowAnimation;
    private Animation mDropCurveAnimation;
    private Animation mKidVibrateAnimation;


    public PeeDrawable(View parent) {
        mParentView = parent;
        initAnimations();
        parent.post(new Runnable() {
            @Override
            public void run() {
                hasInit = true;
                initVariable();
                initBitmaps();
            }
        });
    }

    private void initAnimations() {
        mKidVibrateAnimation = getVibrate(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mDropBounce = interpolatedTime;
                invalidateSelf();
            }
        });


        mDropCurveAnimation = getCurve(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mDropCurve = interpolatedTime;
                invalidateSelf();
            }
        });


        mCupGrowAnimation = getGrow(new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                mCupGrow = interpolatedTime;
                invalidateSelf();
            }
        });
    }

    private void initVariable() {
        mMatrix = new Matrix();
        mParentWidth = mParentView.getWidth();
        mParentHeight = mParentView.getHeight();
        mOneHundred = mParentWidth / 100;

        mDropWidth = mParentWidth / 30;
        mDropHeight = mParentWidth / 35;

        mCupWidth = mParentWidth / 5;
        mCupHeight = (int) (mParentWidth / 7.5);

    }

    private void initBitmaps() {
        mKid = ViewUtils.getBitmapFromImage(getContext(), mParentWidth, mParentHeight, R.mipmap.image_kid);
        mDrop = ViewUtils.getBitmapFromImage(getContext(), mDropWidth, mDropHeight, R.mipmap.image_drop);
        mCupEmpty = ViewUtils.getBitmapFromImage(getContext(), mCupWidth, mCupHeight, R.mipmap.image_cup);
        mCupFill = ViewUtils.getBitmapFromImage(getContext(), mCupWidth, mCupHeight, R.mipmap.image_cup_fill);
    }


    @Override
    public void draw(Canvas canvas) {
        if (!hasInit) return;

        final int restoreCount = canvas.save();

        drawKid(canvas);
        drawCup(canvas);
        drawDrop(canvas);

        canvas.restoreToCount(restoreCount);
    }

    private void drawKid(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAlpha((int) (mPercent * 2.55));

        mMatrix.reset();
        mMatrix.postTranslate(0, 0);
        if(isRefreshing){
            mMatrix.postScale(1,0.97f+((1-mDropBounce)*0.03f),mParentWidth/2,mParentHeight);
        }

        canvas.drawBitmap(mKid, mMatrix, paint);
    }


    private void drawCup(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAlpha((int) (mPercent * 2.55));

        mMatrix.reset();
        mMatrix.postTranslate(mOneHundred * 78, mOneHundred * 85);
        float cupPercent = mPercent < 65 ? 0 : ((mPercent - 65) / 35 ) * 100;


        mMatrix.postScale(cupPercent/100,cupPercent/100,mOneHundred * 78 + mCupWidth/2 , mOneHundred * 85 + mCupHeight / 2);
        canvas.drawBitmap(mCupEmpty, mMatrix, paint);

        if (isRefreshing) {
            int srcTop = (int) (mCupHeight - (mCupGrow * mCupHeight));
            canvas.drawBitmap(mCupFill, new Rect(0, srcTop, mCupWidth, mCupHeight), new RectF(mOneHundred * 78, mOneHundred * 85 + srcTop, mOneHundred * 78 + mCupWidth, mOneHundred * 85 + mCupHeight), paint);
        }
    }


    private void drawDrop(Canvas canvas) {


        Paint paint = new Paint();
        paint.setAlpha((int) (mPercent * 2.55));

//        float dropPercent = mPercent < 65 ? 0 : ((mPercent - 65) / 35 ) * 100;
        float dropPercent = mPercent;

        for (int i = 0; i <= dropPercent; i = i + DROP_DENSITY) {
            if (dropPercent == 0 || isRefreshing) break;

            mMatrix.reset();
            mMatrix.postRotate(315 + (i * 0.9f));
            mMatrix.postTranslate(getXByPercent(i), getYByPercent(i));
            canvas.drawBitmap(mDrop, mMatrix, paint);
        }

        if (isRefreshing) {

            for (int i = 0; i <= PERCENT_MIDDLE; i = i + DROP_DENSITY) {
                float pos = i + mDropCurve * PERCENT_MIDDLE;
                if (pos > PERCENT_MIDDLE) pos -= PERCENT_MIDDLE;
                mMatrix.reset();
                mMatrix.postRotate(315 + (pos * 0.9f));
                mMatrix.postTranslate(getXByPercent(pos), getYByPercent(pos));
                canvas.drawBitmap(mDrop, mMatrix, paint);
            }

            if (mDropCurveRepeatCount == 1) {
                for (int i = 0; i <= mDropCurve * PERCENT_HIGH; i = i + DROP_DENSITY) {
                    if(mDropCurve == 1) break;
                    float pos = i + mDropCurve * PERCENT_HIGH;
                    if (pos > mDropCurve * PERCENT_HIGH) pos -= mDropCurve * PERCENT_HIGH;
                    mMatrix.reset();
                    mMatrix.postRotate(315 + (pos * 0.9f));
                    mMatrix.postTranslate(getXByPercent(pos), getYByPercent(pos) - mOneHundred * 5 * pos / PERCENT_HIGH);
                    canvas.drawBitmap(mDrop, mMatrix, paint);
                }
            } else if (mDropCurveRepeatCount > 1) {
                for (int i = 0; i <= PERCENT_HIGH; i = i + DROP_DENSITY) {
                    float pos = i + mDropCurve * PERCENT_HIGH;
                    if (pos > PERCENT_HIGH) pos -= PERCENT_HIGH;
                    mMatrix.reset();
                    mMatrix.postRotate(315 + (pos * 0.9f));
                    mMatrix.postTranslate(getXByPercent(pos), getYByPercent(pos) - mOneHundred * 5 * pos / PERCENT_HIGH);
                    canvas.drawBitmap(mDrop, mMatrix, paint);
                }
            }

            if (mDropCurveRepeatCount == 3) {
                for (int i = 0; i <= mDropCurve * PERCENT_LOW; i = i + DROP_DENSITY) {
                    if(mDropCurve == 1) break;
                    float pos = i + mDropCurve * PERCENT_LOW;
                    if (pos > mDropCurve * PERCENT_LOW) pos -= mDropCurve * PERCENT_LOW;
                    mMatrix.reset();
                    mMatrix.postRotate(315 + (pos * 0.9f));
                    mMatrix.postTranslate(getXByPercent(pos), getYByPercent(pos) + mOneHundred * 5 * pos / PERCENT_LOW);
                    canvas.drawBitmap(mDrop, mMatrix, paint);
                }
            } else if (mDropCurveRepeatCount > 3) {
                for (int i = 0; i <= PERCENT_LOW; i = i + DROP_DENSITY) {
                    float pos = i + mDropCurve * PERCENT_LOW;
                    if (pos > PERCENT_LOW) pos -= PERCENT_LOW;
                    mMatrix.reset();
                    mMatrix.postRotate(315 + (pos * 0.9f));
                    mMatrix.postTranslate(getXByPercent(pos), getYByPercent(pos) + mOneHundred * 5 * pos / PERCENT_LOW);
                    canvas.drawBitmap(mDrop, mMatrix, paint);
                }
            }
        }
    }

    public void setPercent(float percent) {
        if (percent > 100) percent = 100;
        mPercent = percent;
        invalidateSelf();
    }


    private float getXByPercent(float percent) {
        // the x float from 30 to 80
        return (30 + percent * 0.55f) * mOneHundred;
    }

    private float getRawXByPercent(float percent) {
        // the x float from 30 to 80
        return (30 + percent * 0.55f);
    }

    private float getYByPercent(float percent) {
        // the x float from 75 up to 40 then down to 85
        return (float) ((0.03376f * Math.pow(getRawXByPercent(percent), 2) - 3.7f * getRawXByPercent(percent) + 155.6) * mOneHundred);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(ColorFilter cf) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    @Override
    public boolean isRunning() {
        return isRefreshing;
    }

    @Override
    public void start() {
        mKidVibrateAnimation.reset();
        mDropCurveAnimation.reset();
        mCupGrowAnimation.reset();


        isRefreshing = true;

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(mCupGrowAnimation);
        animationSet.addAnimation(mDropCurveAnimation);
        animationSet.addAnimation(mKidVibrateAnimation);
        mParentView.startAnimation(animationSet);

        mDropCurveAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isRefreshing = false;
                mDropCurveRepeatCount = 0;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                mDropCurveRepeatCount ++;
                Log.i(TAG, "onAnimationRepeat -> " + mDropCurveRepeatCount);

            }
        });

    }

    @Override
    public void stop() {
        mParentView.clearAnimation();
        mKidVibrateAnimation.cancel();
        mDropCurveAnimation.cancel();
        mCupGrowAnimation.cancel();

        mKidVibrateAnimation.reset();
        mDropCurveAnimation.reset();
        mCupGrowAnimation.reset();
        isRefreshing = false;
        mDropCurveRepeatCount = 0;
    }

    private Context getContext() {
        return mParentView.getContext();
    }


    private Animation getVibrate(Animation animation) {
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(DURATION_VIBRATE);
        animation.setStartOffset(0);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(100);
        return animation;
    }


    private Animation getCurve(Animation animation) {
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(DURATION_CURVE);
        animation.setStartOffset(0);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(100);
        return animation;
    }


    private Animation getGrow(Animation animation) {
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(DURATION_GROW);
        animation.setStartOffset(0);
        animation.setRepeatMode(Animation.RESTART);
        animation.setRepeatCount(100);
        return animation;
    }

}
