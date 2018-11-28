package com.bytedance.scenedemo.animation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bytedance.scene.ui.template.SwipeBackAppCompatScene;

/**
 * Created by JiangQi on 8/29/18.
 */
public class SwipeBackDemo extends SwipeBackAppCompatScene {

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(getActivity());
    }

    private boolean mSwipeBack = false;

    @Override
    protected void onSwipeBackEnd() {
        mSwipeBack = true;
        super.onSwipeBackEnd();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mSwipeBack) {
            Toast.makeText(getActivity(), "侧滑返回", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "非侧滑返回", Toast.LENGTH_SHORT).show();
        }
    }
}
