package com.bytedance.scenedemo.multi_stack;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/7/18.
 */
public class MultiStackTabChildScene extends GroupScene {

    private ViewGroup mRootView;
    private TextView mLifecycleView;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int index = (getArguments() == null ? 0 : getArguments().getInt("index"));
        mRootView.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));

        mLifecycleView = getView().findViewById(R.id.name);
        mLifecycleView.setText(getStateHistory());

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.nav_multi_stack_sub_btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mLifecycleView.setText("");
            }
        });

        Button btn2 = getView().findViewById(R.id.btn2);
        btn2.setVisibility(View.VISIBLE);
        btn2.setText(R.string.nav_multi_stack_sub_btn_2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene.class);
            }
        });

        Button btn3 = getView().findViewById(R.id.btn3);
        btn3.setVisibility(View.VISIBLE);
        btn3.setText(R.string.nav_multi_stack_sub_btn_3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentScene().getNavigationScene().push(EmptyScene.class);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mLifecycleView.setText(getStateHistory());
    }

    public static class EmptyScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            final View view = new View(getActivity());
            view.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 3));
            return view;
        }
    }
}