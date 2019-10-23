package com.bytedance.scenedemo.multi_stack;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.animation.animatorexecutor.HorizontalTransitionAnimatorExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scenedemo.MainScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/11/18.
 */
public class MultiStackDemoScene extends GroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        TextView name = getView().findViewById(R.id.name);
        name.setVisibility(View.GONE);

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.nav_multi_stack_btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(NavigationScene.class, new NavigationSceneOptions(MainScene.class, null).toBundle());
            }
        });

        Button btn2 = getView().findViewById(R.id.btn2);
        btn2.setVisibility(View.VISIBLE);
        btn2.setText(R.string.nav_multi_stack_btn_2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(NavigationScene.class, new NavigationSceneOptions(MainScene.class, null).toBundle()
                        , new PushOptions.Builder().setAnimation(new HorizontalTransitionAnimatorExecutor()).build());
            }
        });

        Button btn3 = getView().findViewById(R.id.btn3);
        btn3.setVisibility(View.VISIBLE);
        btn3.setText(R.string.nav_multi_stack_btn_3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(MultiStackTabGroupScene.class);
            }
        });
    }
}
