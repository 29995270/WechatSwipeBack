package com.wq.freeze.wechatswipe.swipeback;

import android.app.Activity;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.wq.freeze.wechatswipe.R;

import java.lang.ref.WeakReference;

/**
 * Created by wangqi on 2016/8/8.
 */
public class SwipeBackManager {
    private static SwipeBackManager instance = new SwipeBackManager();

    private DragBackLayout tempPreScene;

    private SwipeBackManager(){

    }

    public static SwipeBackManager getInstance() {
        return instance;
    }

    public void onPostCreate(@NonNull final SwipeBackScene scene, @Nullable DragLayout.FinishCallback callback) {
        if (scene.getDragBackLayout() == null) return;
        scene.getDragBackLayout().setTag(new WeakReference<>(tempPreScene));
        tempPreScene = null;
        if (callback == null) return;
        scene.getDragBackLayout().setFinishCallback(callback);
    }

    // for activity
    public void onStartNewScene(final Activity activity, DragBackLayout dragBackLayout) {
        activity.overridePendingTransition(0, 0);

        if (dragBackLayout != null) {
            tempPreScene = dragBackLayout;
        }
    }

    // for activity
    public void onFinishScene(AppCompatActivity activity, DragBackLayout dragBackLayout) {
        if (dragBackLayout == null || dragBackLayout.getTag() == null || ((WeakReference) dragBackLayout.getTag()).get() == null) {
            return;
        }
        dragBackLayout.onBackPress();
    }

    //for fragment
    public void onStartNewScene(AppCompatActivity activity, @IdRes int fragmentStack, Fragment fragment, DragBackLayout dragBackLayout) {
        activity.getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.activity_freeze, R.anim.activity_freeze, R.anim.activity_freeze, R.anim.activity_freeze)
                .add(fragmentStack, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(fragment.getClass().getSimpleName())
                .commit();

        if (dragBackLayout != null) {
            tempPreScene = dragBackLayout;
        }
    }

    //for fragment
    public void onFinishScene(DragBackLayout dragBackLayout) {
        if (dragBackLayout == null || dragBackLayout.getTag() == null || ((WeakReference) dragBackLayout.getTag()).get() == null) {
            return;
        }
        dragBackLayout.setFinishCallback(null);
        dragBackLayout.onBackPress();
    }
}
