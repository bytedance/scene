package com.bytedance.scenedemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.animation.AnimationResDemoScene;
import com.bytedance.scenedemo.lifecycle.ChildSceneLifecycleCallbacksDemoScene;
import com.bytedance.scenedemo.lifecycle.LifeCycleDemoScene;
import com.bytedance.scenedemo.livedata.LiveDataScene;

/**
 * Created by JiangQi on 8/21/18.
 */
public class LifeCycleListScene extends UserVisibleHintGroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(getActivity());

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("ChildSceneLifecycleCallbacks");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ChildSceneLifecycleCallbacksDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("状态");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(LifeCycleDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("RxJava绑定");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(AnimationResDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("LiveData");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                SceneViewModelProviders.of(LifeCycleListScene.this).get();
                getNavigationScene().push(LiveDataScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Scope设计");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return layout;
    }
}