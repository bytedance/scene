package com.bytedance.scenedemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.case0.Case0Scene;
import com.bytedance.scenedemo.case0.Case1Scene;
import com.bytedance.scenedemo.case0.Case2Scene;
import com.bytedance.scenedemo.case0.Case3Scene;
import com.bytedance.scenedemo.case0.Case4Scene;
import com.bytedance.scenedemo.case0.Case5Scene;

/**
 * Created by JiangQi on 8/21/18.
 */
public class CaseListDemo extends UserVisibleHintGroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(layout);

        Button button = new Button(getActivity());
        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Push后立刻Pop，循环100次，动画要正确");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(Case0Scene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("同时Push 100个");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(Case1Scene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("同时多个Pop超过当前的Scene数量");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(Case2Scene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Push和Pop动画过程中，Remove");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(Case3Scene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Scene 生命周期内又执行Push/Pop");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(Case4Scene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("GroupScene 没添加到Scene之前就add/remove");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(Case5Scene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Activity finish后立刻 Push/Pop");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                getNavigationScene().push(Case0Scene.EmptyScene.class);
            }
        });

        return scrollView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisible() && isVisibleToUser) {

        }
    }
}
