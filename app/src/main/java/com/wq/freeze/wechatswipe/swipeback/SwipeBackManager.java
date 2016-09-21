package com.wq.freeze.wechatswipe.swipeback;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.wq.freeze.wechatswipe.R;

import java.lang.ref.WeakReference;

/**
 * Created by wangqi on 2016/8/8.
 */
public class SwipeBackManager {
    private static SwipeBackManager instance = new SwipeBackManager();
    private boolean finishByDrag;

    private DragBackLayout tempPreScene;

    private SwipeBackManager(){

    }

    public static SwipeBackManager getInstance() {
        return instance;
    }

    public void onPostCreate(@NonNull final SwipeBackScene scene, DragLayout.DragCallback callback) {
        scene.getDragBackLayout().setTag(new WeakReference<>(tempPreScene));
        tempPreScene = null;
        scene.getDragBackLayout().addDragCallback(callback);
        scene.getDragBackLayout().addDragCallback(new DragLayout.DragCallback() {
            @Override
            public void call() {
                instance.onDragEnd();
            }

            @Override
            public void onProcessing(float percent) {
                instance.onDragProcess(percent);
            }

            @Override
            public void onStart() {
                instance.onDragStart(scene);
            }

            @Override
            public void onReturn() {
                instance.onDragReturn();
            }

            @Override
            public void onRelease(boolean notEnough) {
                instance.onRelease(notEnough);
            }
        });
    }

    public void onStartNewScene(final Activity activity, DragBackLayout dragBackLayout) {
        activity.overridePendingTransition(R.anim.activity_enter, 0);

        if (dragBackLayout != null) {
            tempPreScene = dragBackLayout;
            dragBackLayout.startNewSceneAnimation();
        }
    }

    public void onFinishScene(AppCompatActivity activity, DragBackLayout dragBackLayout) {
        if (dragBackLayout == null || dragBackLayout.getTag() == null || ((WeakReference) dragBackLayout.getTag()).get() == null) {
            return;
        }
        activity.overridePendingTransition(0, R.anim.activity_exit);

        DragBackLayout preScene = (DragBackLayout) ((WeakReference) dragBackLayout.getTag()).get();

        if (preScene != null && !finishByDrag) {
            preScene.startFinishSceneAnimation();
        }
        finishByDrag = false;
    }

    //for fragment
    public void onStartNewScene(AppCompatActivity activity, @IdRes int fragmentStack, Fragment fragment, DragBackLayout dragBackLayout) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.activity_enter, 0, 0, R.anim.activity_exit)
                .add(fragmentStack, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();

        if (dragBackLayout != null) {
            tempPreScene = dragBackLayout;
            dragBackLayout.startNewSceneAnimation();
        }
    }

    //for fragment
    public void onFinishScene(DragBackLayout dragBackLayout) {
        if (dragBackLayout == null || dragBackLayout.getTag() == null || ((WeakReference) dragBackLayout.getTag()).get() == null) {
            return;
        }
        DragBackLayout preScene = (DragBackLayout) ((WeakReference) dragBackLayout.getTag()).get();
        if (preScene != null && !finishByDrag) {
            preScene.startFinishSceneAnimation();
        }
        finishByDrag = false;
    }

    private DragBackLayout currentBackgroundDragBackLayout;

    private void onDragStart(SwipeBackScene scene) {
        DragBackLayout dragBackLayout = scene.getDragBackLayout();
        if (dragBackLayout == null || dragBackLayout.getTag() == null || ((WeakReference) dragBackLayout.getTag()).get() == null) {
            return;
        }

        currentBackgroundDragBackLayout = (DragBackLayout) ((WeakReference) dragBackLayout.getTag()).get();

        //todo find the previous and DragBackLayout
    }

    private void onDragProcess(float percent) {
        if (currentBackgroundDragBackLayout != null) {
            currentBackgroundDragBackLayout.updateBackgroundProcess(percent);
        }
    }

    private void onDragEnd() {
        if (currentBackgroundDragBackLayout != null) {
            currentBackgroundDragBackLayout.updateBackgroundProcess(1);
            currentBackgroundDragBackLayout = null;
        }
    }

    private void onDragReturn() {
        if (currentBackgroundDragBackLayout != null) {
            currentBackgroundDragBackLayout.resetForegroundColor();
            currentBackgroundDragBackLayout = null;
        }
    }

    private void onRelease(boolean notEnough) {
        if (!notEnough) {
            finishByDrag = true;
        }
    }
}
