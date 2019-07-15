package com.bytedance.scenedemo.multi_stack;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bytedance.scene.animation.animatorexecutor.HorizontalTransitionAnimatorExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.ConfigurationChangedListener;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scenedemo.MainListScene;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/11/18.
 */
public class MultiStackDemoScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setFitsSystemWindows(true);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        Button button = new Button(getActivity());

        button = new Button(getActivity());
        layout.addView(button);
        button.setAllCaps(false);
        button.setText("Push 新的 NavigationScene，默认动画");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(NavigationScene.class, new NavigationSceneOptions(MainListScene.class, null).toBundle());
            }
        });

        button = new Button(getActivity());
        layout.addView(button);
        button.setAllCaps(false);
        button.setText("Push 新的 NavigationScene，修改动画");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(NavigationScene.class, new NavigationSceneOptions(MainListScene.class, null).toBundle()
                        , new PushOptions.Builder().setAnimation(new HorizontalTransitionAnimatorExecutor()).build());
            }
        });

        button = new Button(getActivity());
        layout.addView(button);
        button.setAllCaps(false);
        button.setText("Material Design 2.0 规范，每个 Tab 单独有个 BackStack");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(MultiStackTabGroupScene.class);
            }
        });

        return layout;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNavigationScene().addConfigurationChangedListener(this, new ConfigurationChangedListener() {
            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                Toast.makeText(getApplicationContext(), "onConfigurationChanged", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
