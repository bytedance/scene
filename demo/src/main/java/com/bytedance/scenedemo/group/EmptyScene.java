package com.bytedance.scenedemo.group;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/7/18.
 */
public class EmptyScene extends UserVisibleHintGroupScene {

    private TextView textView;

    public static EmptyScene newInstance(int index) {
        EmptyScene scene = new EmptyScene();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        scene.setArguments(bundle);
        return scene;
    }

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int index = getArguments() == null ? 0 : getArguments().getInt("index");
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));

        textView = getView().findViewById(R.id.name);
        Button btn = getView().findViewById(R.id.btn);
        btn.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        textView.setText(getStateHistory());
    }
}
