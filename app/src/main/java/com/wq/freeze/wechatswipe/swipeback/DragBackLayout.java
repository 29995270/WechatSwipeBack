package com.wq.freeze.wechatswipe.swipeback;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.wq.freeze.wechatswipe.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangqi on 2016/8/3.
 */
public class DragBackLayout extends FrameLayout implements DragLayout {
    private int scaledTouchSlop;
    private List<DragCallback> callbacks;
    private boolean enable;
    private int minimumFlingVelocity;
    private VelocityTracker velocityTracker;

    private boolean isBeingDragged;
    private boolean animating = false;

    private int edgeSlop;
    
    private int lastMotionX = -1;
    private int activePointerId;
    private float offsetX;
    private Drawable shadow;
    private int shadowWidth;
    private FinishCallback finishCallback;

    private boolean hasFinished;

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
        edgeSlop = viewConfiguration.getScaledEdgeSlop() * 2;
        minimumFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        callbacks = new ArrayList<>();

        setWillNotDraw(false);

        shadow = ContextCompat.getDrawable(context, R.mipmap.drag_back_shadow_left);
        shadowWidth = Utils.dp2px(context, 16);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
            getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    getViewTreeObserver().removeOnPreDrawListener(this);
                    if (enable) {
                        shadow.setBounds(0, 0, shadowWidth, getHeight());
                        animateRelease(getWidth(), 0, false, false);
                    }
                    return false;
                }
            });
    }

    @Override
    public void setTag(Object tag) {
        super.setTag(tag);
        if (tag != null &&
                (tag instanceof WeakReference) &&
                ((WeakReference) getTag()).get() != null &&
                (((WeakReference) getTag()).get() instanceof DragBackLayout)) {

            final DragBackLayout layout = (DragBackLayout) ((WeakReference) tag).get();
            addDragCallback(new DragCallback() {
                @Override
                public void onArrived() {
                    layout.updateBackgroundProcess(1);
                }

                @Override
                public void onStart() {
                }

                @Override
                public void onProcessing(float percent) {
                    layout.updateBackgroundProcess(percent);
                }

                @Override
                public void onRelease(boolean notEnough) {
                }

                @Override
                public void onReturned() {
                    layout.resetForegroundColor();
                }
            });
        }

    }

    private void ensureVelocityTracker() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private boolean isPointInEdge(int x, int y) {
        return x <= edgeSlop;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!enable || animating) return false;
        final int action = ev.getAction();

        // Shortcut since we're being dragged
        if (action == MotionEvent.ACTION_MOVE && isBeingDragged) {
            return true;
        }

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                 isBeingDragged = false;
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();
                if (isPointInEdge(x, y)) {
                    lastMotionX = x;
                    activePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerId = this.activePointerId;
                if (activePointerId == MotionEvent.INVALID_POINTER_ID) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }
                final int pointerIndex = ev.findPointerIndex(activePointerId);
                if (pointerIndex == -1) {
                    break;
                }

                final int x = (int) ev.getX(pointerIndex);
                final int xDiff = Math.abs(x - lastMotionX);
                if (xDiff > scaledTouchSlop && lastMotionX != -1) {
                    isBeingDragged = true;
                    lastMotionX = x;

                    for (DragCallback callback : callbacks) {
                        callback.onStart();
                    }
                }
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                isBeingDragged = false;
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                lastMotionX = -1;
                break;
            }
        }

        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }

        return isBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        if (!enable || animating) return super.onTouchEvent(ev);

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN: {
                final int x = (int) ev.getX();
                final int y = (int) ev.getY();

                if (isPointInEdge(x, y)) {
                    lastMotionX = x;
                    activePointerId = ev.getPointerId(0);
                    ensureVelocityTracker();
                } else {
                    return false;
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (activePointerIndex == -1) {
                    return false;
                }

                final int x = (int) ev.getX(activePointerIndex);
                int dx = lastMotionX - x;

                if (!isBeingDragged && Math.abs(dx) > scaledTouchSlop) {
                    isBeingDragged = true;
                    if (dx > 0) {
                        dx -= scaledTouchSlop;
                    } else {
                        dx += scaledTouchSlop;
                    }
                    for (DragCallback callback : callbacks) {
                        callback.onStart();
                    }
                }

                if (isBeingDragged) {
                    lastMotionX = x;
                    // We're being dragged so scroll
                    if (getOffsetX() + (-dx) < 0) {
                        setOffsetX(0);
                    } else {
                        setOffsetX(getOffsetX() + (-dx));
                        float percent = getOffsetX() / getMeasuredWidth();
                        for (DragCallback callback : callbacks) {
                            callback.onProcessing(percent);
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_UP:
                if (velocityTracker != null) {
                    velocityTracker.addMovement(ev);
                    velocityTracker.computeCurrentVelocity(1000);
                    float xvel = VelocityTrackerCompat.getXVelocity(velocityTracker,
                            activePointerId);

                    boolean notEnough = getOffsetX() < getMeasuredWidth() / 2;

                    boolean notEnoughButFast =
                            getOffsetX() < getMeasuredWidth() / 2 &&
                            getOffsetX() > scaledTouchSlop &&
                                    xvel > minimumFlingVelocity;

                    for (DragCallback callback : callbacks) {
                        callback.onRelease(notEnough);
                    }
                    if (notEnough) {
                        if (notEnoughButFast) {
                            animateRelease(getOffsetX(), getMeasuredWidth(), true, true);
                        } else {
                            animateRelease(getOffsetX(), 0, false, true);
                        }

                    } else {
                        animateRelease(getOffsetX(), getMeasuredWidth(), true, true);
                    }
                }
                lastMotionX = -1;

            case MotionEvent.ACTION_CANCEL: {
                isBeingDragged = false;
                activePointerId = MotionEvent.INVALID_POINTER_ID;
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                lastMotionX = -1;
                break;
            }
        }

        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }

        return true;
    }

    private void setOffsetX(float v) {
        this.offsetX = v;
        invalidate();
    }

    private float getOffsetX() {
        return this.offsetX;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.translate(offsetX, 0);
        super.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.translate(offsetX - shadowWidth, 0);
        float v = (1 - (offsetX / getWidth())) * 255;
        shadow.setAlpha(offsetX <= 0 ? 255 : (int) v);
        shadow.draw(canvas);
        canvas.restore();
    }

    private void animateRelease(final float start, float end, final boolean outOrIn, final boolean byDragAction) {
        if (animating || hasFinished) return;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "offsetX", start, end);
        animator.setDuration((long) (400 * Math.abs(end - start)/getWidth()));
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = ((Float) animation.getAnimatedValue()) / getMeasuredWidth();
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
                    callback.onProcessing(outOrIn ? 1 : 0);
                    if (outOrIn) {
                        callback.onArrived();
                    } else {
                        callback.onReturned();
                    }
                }
                if (finishCallback != null && outOrIn) {
                    hasFinished = true;
                    finishCallback.onFinish();
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
        this.enable = enable;
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

    @Override
    public void setFinishCallback(FinishCallback callback) {
        this.finishCallback = callback;
    }

    public void onBackPress() {
        animateRelease(0, getWidth(), true, false);
    }

    void updateBackgroundProcess(float percent) {
        Log.v("AAA", percent + "___" + (float) (-0.5 * getMeasuredWidth() * (1 - percent)));
        setOffsetX((float) (-0.5 * getMeasuredWidth() * (1 - percent)));
    }

    void resetForegroundColor() {
        setOffsetX(0);
    }
}
