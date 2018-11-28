package com.bytedance.scenedemo.group.all;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/7/18.
 */
public class EmptyScene extends UserVisibleHintGroupScene {
    public static EmptyScene newInstance(int index) {
        EmptyScene scene = new EmptyScene();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        scene.setArguments(bundle);
        return scene;
    }

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FrameLayout layout = new FrameLayout(getActivity());
        TextView textView = new TextView(getActivity());
        textView.setText(toString());

        layout.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int index = getArguments().getInt("index");

        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));
    }
}
