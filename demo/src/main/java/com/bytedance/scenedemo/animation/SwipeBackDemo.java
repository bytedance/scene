package com.bytedance.scenedemo.animation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/29/18.
 */
public class SwipeBackDemo extends AppCompatScene {

    @Nullable
    @Override
    protected View onCreateContentView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = new View(getActivity());
        view.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSwipeEnabled(true);
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
            Toast.makeText(getActivity(), R.string.anim_swipe_back_tip_1, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), R.string.anim_swipe_back_tip_2, Toast.LENGTH_SHORT).show();
        }
    }
}
