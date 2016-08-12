package com.wq.freeze.wechatswipe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;

import com.wq.freeze.wechatswipe.swipeback.DragBackLayout;
import com.wq.freeze.wechatswipe.swipeback.DragLayout;
import com.wq.freeze.wechatswipe.swipeback.SwipeBackManager;
import com.wq.freeze.wechatswipe.swipeback.SwipeBackScene;

/**
 * Created by wangqi on 2016/8/8.
 */
public class BaseActivity extends AppCompatActivity implements SwipeBackScene {

    protected SwipeBackManager instance;
    protected DragBackLayout dragBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v("AAA", "onCreate" + getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        instance = SwipeBackManager.getInstance();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        dragBack = getDragBackLayout();
        instance.onPostCreate(this, new DragLayout.DragCallback() {
            @Override
            public void call() {
                finish();
            }
        });

        if (dragBack == null) return;

        dragBack.setEnable(canDragBack());
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        instance.onStartNewScene(this);
    }

    @Override
    public void finish() {
        super.finish();
        instance.onFinishScene(this);
    }

    protected void addFragment(Fragment fragment, @IdRes int fragmentStack) {
        instance.onStartNewScene(this, fragmentStack, fragment);
    }

    @Override
    public boolean canDragBack() {
        return true;
    }

    @Override
    public DragBackLayout getDragBackLayout() {
        return (DragBackLayout) findViewById(R.id.drag_back);
    }
}
