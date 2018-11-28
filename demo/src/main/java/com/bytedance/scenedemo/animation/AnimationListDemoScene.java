package com.bytedance.scenedemo.animation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.animation.fullsharedelement.FullSharedElementAnimationScene;
import com.bytedance.scenedemo.animation.sharedelement.SharedElementScene0;

/**
 * Created by JiangQi on 8/9/18.
 */
public class AnimationListDemoScene extends UserVisibleHintGroupScene {
    public Button aaaa;

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("根据动画Animation/Animator res文件替换动画");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(AnimationResDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("iOS Interaction 动画，普通的右滑返回");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SlideBackDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("iOS 普通的右滑返回，集成进 SwipeBackAppCompatScene");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SwipeBackDemo.class);
            }
        });

        button = new Button(getActivity());
        aaaa = button;
        button.setAllCaps(false);
        button.setText("iOS Interaction 动画");
        layout.addView(button, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SlideBackButtonDemoScene.class);
            }
        });
        layout.setGravity(Gravity.RIGHT);


        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Android 5 Share Element 动画 1");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(TransitionDemo.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Android 5 Share Element 动画 2");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SharedElementScene0.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Android 5 Share Element 动画 3");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(TransitionDemoNew.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Android 5 Share Element 完整动画");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(FullSharedElementAnimationScene.class);
            }
        });


        return layout;
    }
}
