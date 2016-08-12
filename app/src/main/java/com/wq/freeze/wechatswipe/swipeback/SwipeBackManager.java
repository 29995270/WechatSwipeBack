package com.wq.freeze.wechatswipe.swipeback;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wq.freeze.wechatswipe.BaseActivity;
import com.wq.freeze.wechatswipe.BaseFragment;
import com.wq.freeze.wechatswipe.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Created by wangqi on 2016/8/8.
 */
public class SwipeBackManager {
    private static SwipeBackManager instance = new SwipeBackManager();

    private SwipeBackManager(){

    }

    public static SwipeBackManager getInstance() {
        return instance;
    }

    private LinkedList<WeakReference<DragBackLayout>> dragBackLayoutRefs = new LinkedList<>();

    public void onPostCreate(@NonNull final SwipeBackScene scene, DragLayout.DragCallback callback) {
        dragBackLayoutRefs.add(new WeakReference<>(scene.getDragBackLayout()));
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
        });
    }

    public void onStartNewScene(Activity activity) {
        activity.overridePendingTransition(R.anim.activity_enter, 0);

        DragBackLayout dragBackLayout = dragBackLayoutRefs.getLast().get();
        if (dragBackLayout != null) {
            dragBackLayout.startNewSceneAnimation();
        }
    }

    public void onFinishScene(AppCompatActivity activity) {
        if (dragBackLayoutRefs.size() - 2 < 0) {
            dragBackLayoutRefs.removeLast();
            return;
        }
        activity.overridePendingTransition(0, R.anim.activity_exit);

        WeakReference<DragBackLayout> preReference = dragBackLayoutRefs.get(dragBackLayoutRefs.size() - 2);
        if (preReference != null && preReference.get() != null) {
            DragBackLayout dragBackLayout = preReference.get();
            if (dragBackLayout != null) {
                dragBackLayout.startFinishSceneAnimation();
            }
        }
        dragBackLayoutRefs.removeLast();
    }

    public void onStartNewScene(AppCompatActivity activity, @IdRes int fragmentStack, Fragment fragment) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.activity_enter, 0, 0, R.anim.activity_exit)
                .add(fragmentStack, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();
        DragBackLayout dragBackLayout = dragBackLayoutRefs.getLast().get();
        if (dragBackLayout != null) {
            dragBackLayout.startNewSceneAnimation();
        }
    }

    public void onFinishScene() {
        if (dragBackLayoutRefs.size() - 2 < 0) {
            dragBackLayoutRefs.removeLast();
            return;
        }

        WeakReference<DragBackLayout> preReference = dragBackLayoutRefs.get(dragBackLayoutRefs.size() - 2);
        if (preReference != null && preReference.get() != null) {
            DragBackLayout dragBackLayout = preReference.get();
            if (dragBackLayout != null) {
                dragBackLayout.startFinishSceneAnimation();
            }
        }
        dragBackLayoutRefs.removeLast();
    }

    private DragBackLayout currentBackgroundDragBackLayout;

    public void onDragStart(SwipeBackScene scene) {

        if (dragBackLayoutRefs.size() - 2 < 0) {
            return;
        }

        WeakReference<DragBackLayout> preReference = dragBackLayoutRefs.get(dragBackLayoutRefs.size() - 2);
        if (preReference != null && preReference.get() != null) {
            currentBackgroundDragBackLayout = preReference.get();
        }

        //todo find the previous and DragBackLayout
    }

    public void onDragStart() {
        if (dragBackLayoutRefs.size() - 2 < 0) {
            return;
        }
        WeakReference<DragBackLayout> preReference = dragBackLayoutRefs.get(dragBackLayoutRefs.size() - 2);
        if (preReference != null && preReference.get() != null) {
            currentBackgroundDragBackLayout = preReference.get();
        }
    }

    public void onDragProcess(float percent) {
        if (currentBackgroundDragBackLayout != null) {
            currentBackgroundDragBackLayout.updateBackgroundProcess(percent);
        }
    }

    public void onDragEnd() {
        if (currentBackgroundDragBackLayout != null) {
            currentBackgroundDragBackLayout.updateBackgroundProcess(1);
            currentBackgroundDragBackLayout = null;
        }
    }

    public void onDragReturn() {
        if (currentBackgroundDragBackLayout != null) {
            currentBackgroundDragBackLayout.resetForegroundColor();
            currentBackgroundDragBackLayout = null;
        }
    }

    private class MyWeakReference<T> extends WeakReference<T> {

        public MyWeakReference(T referent) {
            super(referent);
        }

        @Override
        public boolean equals(Object obj) {
            if (get() == null) return obj == null;
            if (obj == null) return false;
            return obj instanceof MyWeakReference && get().equals(((MyWeakReference) obj).get());
        }
    }
}
