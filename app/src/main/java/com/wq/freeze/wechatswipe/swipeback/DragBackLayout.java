package com.wq.freeze.wechatswipe.swipeback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangqi on 2016/8/3.
 */
public class DragBackLayout extends FrameLayout implements DragLayout{
    private int scaledTouchSlop;
    private float initTouchX;
    private float initTouchY;
    private List<DragCallback> callbacks;
    private float mPercent;
    private boolean mEnable;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private MyVelocityTracker mVelocityTracker;

    private static final int MIN_FLING_VELOCITY = 400; // dips
    private int mEdgeSlop;

    public DragBackLayout(Context context) {
        super(context);
        init(context);
    }

    public DragBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.scaledTouchSlop = viewConfiguration.getScaledWindowTouchSlop();
        mEdgeSlop = viewConfiguration.getScaledEdgeSlop() * 2;
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        final float density = context.getResources().getDisplayMetrics().density;

        mMinimumVelocity = (int) (MIN_FLING_VELOCITY * density);
        callbacks = new ArrayList<>();

        /* only work between activity
        addDragCallback(new DragCallback() {
            @Override
            public void call() {
                if (getParent() != null && ((ViewGroup) getParent()).getId() == android.R.id.content) {
                    ((ViewGroup) getParent()).setBackgroundColor(Color.TRANSPARENT);
                }
            }

            @Override
            public void onReturn() {
                if (getParent() != null && ((ViewGroup) getParent()).getId() == android.R.id.content) {
                    ((ViewGroup) getParent()).setBackgroundColor(Color.TRANSPARENT);
                }
            }

            @Override
            public void onProcessing(float percent) {
                DragBackLayout.this.mPercent = percent;
                if (getParent() != null && ((ViewGroup) getParent()).getId() == android.R.id.content) {
                    ((ViewGroup) getParent()).setBackgroundColor(Color.argb((int) (0x99 * (1 - percent)), 0, 0, 0));
                }
                invalidate();
            }
        });
        */

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (!mEnable) return false;

        if (mVelocityTracker == null) {
            mVelocityTracker = MyVelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(motionEvent);

        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (animating) return false;
                float x = motionEvent.getRawX();
                if (x > mEdgeSlop) return false;
                initTouchX = motionEvent.getRawX();
                initTouchY = motionEvent.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (initTouchX == 0 && initTouchY == 0) return false;
                if (motionEvent.getRawX() - initTouchX < 0) return false;
                if (motionEvent.getRawX() - initTouchX > scaledTouchSlop && Math.abs(motionEvent.getRawY() - initTouchY) < scaledTouchSlop) {
                    for (DragCallback callback : callbacks) {
                        callback.onStart();
                    }
                    return true;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                return false;
        }

        return super.onInterceptTouchEvent(motionEvent);
    }

    private float moveFirstX = -1;
    private boolean animating = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (!mEnable) return super.onTouchEvent(event);

        if (mVelocityTracker == null) {
            mVelocityTracker = MyVelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (moveFirstX == -1) {
                    moveFirstX = event.getRawX();
                } else {
                    float dx = event.getRawX() - moveFirstX;
                    if (getTranslationX() + dx < 0 ) {
                        setTranslationX(0);
                    } else {
                        setTranslationX(getTranslationX() + dx);
                        float percent = getTranslationX()/getMeasuredWidth();
                        for (DragCallback callback : callbacks) {
                            callback.onProcessing(percent);
                        }
                    }
                    moveFirstX = event.getRawX();
                }
                break;
            case MotionEvent.ACTION_UP:
                
//                mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) mVelocityTracker.getXVelocity();
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }

//                Log.v("AAA", initialVelocity + "_________" + mMinimumVelocity + "|||" + getTranslationX() + "____" + scaledTouchSlop);
                boolean notEnough = getTranslationX() < getMeasuredWidth() / 2;
                boolean notEnoughButFast = getTranslationX() < getMeasuredWidth() / 2 && getTranslationX() > scaledTouchSlop && initialVelocity > mMinimumVelocity;
                for (DragCallback callback : callbacks) {
                    callback.onRelease(notEnough);
                }
                if (notEnough) {
                    if (notEnoughButFast) {
                        animateRelease(getTranslationX(), getMeasuredWidth(), true);
                    } else {
                        animateRelease(getTranslationX(), 0, false);
                    }

                } else {
                    animateRelease(getTranslationX(), getMeasuredWidth(), true);
                }
                moveFirstX = -1;
                break;
            case MotionEvent.ACTION_DOWN:
                moveFirstX = -1;
                float x = event.getRawX();
                if (x <= mEdgeSlop) {
                    for (DragCallback callback : callbacks) {
                        callback.onStart();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void animateRelease(float start, float end, final boolean dragOut) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "translationX", start, end);
        animator.setDuration(300);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = getTranslationX()/getMeasuredWidth();
                for (DragCallback callback : callbacks) {
                    callback.onProcessing(percent);
                }
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animating = false;
                for (DragCallback callback : callbacks) {
                    callback.onProcessing(dragOut? 1: 0);
                    if (dragOut) {
                        callback.call();
                    } else {
                        callback.onReturn();
                    }
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                animating = true;
            }
        });
        animator.start();
    }

    public void setEnable(boolean enable) {
        this.mEnable = enable;
    }


    @Override
    public void onDetachedFromWindow() {
        clearAnimation();
        callbacks.clear();
        super.onDetachedFromWindow();
    }

    @Override
    public void addDragCallback(DragCallback callback) {
        this.callbacks.add(callback);
    }

    public void startNewSceneAnimation() {
        animate().translationX(-getMeasuredWidth()/2).setStartDelay(150).start();
    }

    public void startFinishSceneAnimation() {
        animate().translationX(0).start();
    }

    private ColorDrawable foregroundColor;
    public void updateBackgroundProcess(float percent) {
        setTranslationX((float) (-0.5 * getMeasuredWidth() * (1 - percent)));
        if (foregroundColor == null) {
            foregroundColor = new ColorDrawable();
        }
        ((ColorDrawable) foregroundColor.mutate()).setColor(Color.argb((int) (0x99 * (1 - percent)), 0, 0, 0));
        setForeground(foregroundColor);
    }

    public void resetForegroundColor() {
        ((ColorDrawable) foregroundColor.mutate()).setColor(Color.TRANSPARENT);
        setForeground(foregroundColor);
    }

    private static class MyVelocityTracker {
        
        public static MyVelocityTracker obtain() {
            return new MyVelocityTracker();
        }

        private Pair<Long, Float> firstPair;
        private Pair<Long, Float> lastPair;
        private MyVelocityTracker() {
        }

        public void addMovement(MotionEvent event) {
            if (firstPair == null) {
                firstPair = new Pair<>(System.currentTimeMillis(), event.getRawX());
            } else {
                lastPair = new Pair<>(System.currentTimeMillis(), event.getRawX());
            }
        }

        public float getXVelocity() {
            if (firstPair == null || lastPair == null) return 0f;
            return ((lastPair.second - firstPair.second)/(lastPair.first - firstPair.first)) * 1000;
        }

        public void recycle(){

        }
    }

}
