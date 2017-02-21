package com.wq.freeze.wechatswipe;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wq.freeze.wechatswipe.swipeback.DragBackLayout;
import com.wq.freeze.wechatswipe.swipeback.DragLayout;
import com.wq.freeze.wechatswipe.swipeback.SwipeBackManager;
import com.wq.freeze.wechatswipe.swipeback.SwipeBackScene;
import com.wq.freeze.wechatswipe.swipeback.Utils;

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
        instance.onPostCreate(this, new DragLayout.FinishCallback() {
            @Override
            public void onFinish() {
                BaseActivity.this.finish();
                overridePendingTransition(0, 0);
            }
        });

        if (dragBack == null) return;

        dragBack.setEnable(canDragBack());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canDragBack()) {
            Utils.convertActivityToTranslucent(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (canDragBack()) {
            App.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Utils.convertActivityFromTranslucent(BaseActivity.this);
                }
            }, 450);
        }
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        instance.onStartNewScene(this, getDragBackLayout());
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            super.onBackPressed();
        } else {
            if (canDragBack()) {
                instance.onFinishScene(this, getDragBackLayout());
            } else {
                super.onBackPressed();
            }
        }
    }

    protected void addFragment(Fragment fragment, @IdRes int fragmentStack) {
        instance.onStartNewScene(this, fragmentStack, fragment, getDragBackLayout());
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
