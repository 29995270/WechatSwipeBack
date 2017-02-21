package com.wq.freeze.wechatswipe;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wangqi on 2016/8/9.
 */
public class DemoFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_color, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.add_fragment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFragment(new DemoFragment(), R.id.fragment_stack);
            }
        });
    }
}
