package com.wq.freeze.wechatswipe;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.wq.freeze.wechatswipe.swipeback.DragBackLayout;
import com.wq.freeze.wechatswipe.swipeback.DragLayout;
import com.wq.freeze.wechatswipe.swipeback.SwipeBackManager;
import com.wq.freeze.wechatswipe.swipeback.SwipeBackScene;

/**
 * Created by wangqi on 2016/8/9.
 */
public class BaseFragment extends Fragment implements SwipeBackScene{

    protected DragBackLayout dragBackLayout;
    protected SwipeBackManager instance;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.v("AAA", "onCreate" + getClass().getSimpleName());
        super.onCreate(savedInstanceState);
        instance = SwipeBackManager.getInstance();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dragBackLayout = getDragBackLayout();
        dragBackLayout.setEnable(canDragBack());

        instance.onPostCreate(this, new DragLayout.FinishCallback() {
            @Override
            public void onFinish() {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onDestroyView() {
        instance.onFinishScene(getDragBackLayout());
        super.onDestroyView();
    }

    protected void addFragment(Fragment fragment, @IdRes int fragmentStack) {
        instance.onStartNewScene(((AppCompatActivity) getActivity()), fragmentStack, fragment, getDragBackLayout());
    }

    public boolean canDragBack() {
        return true;
    }

    @Override
    public DragBackLayout getDragBackLayout() {
        if (getView() != null) {
            return (DragBackLayout) getView().findViewById(R.id.drag_back);
        }
        return null;
    }
}
