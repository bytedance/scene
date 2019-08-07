package com.bytedance.scenedemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

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
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        addSpace(layout, 12);
        addTitle(layout, getString(R.string.main_title_basic));

        addButton(layout, getString(R.string.main_lifecycle_child_callbacks), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ChildSceneLifecycleCallbacksDemoScene.class);
            }
        });

        addButton(layout, getString(R.string.main_lifecycle_status), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(LifeCycleDemoScene.class);
            }
        });

        addTitle(layout, getString(R.string.main_title_pro));

        addButton(layout, getString(R.string.main_lifecycle_rx_java), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(AnimationResDemoScene.class);
            }
        });

        addButton(layout, getString(R.string.main_lifecycle_live_data), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(LiveDataScene.class);
            }
        });

        addTitle(layout, getString(R.string.main_title_todo));

        addButton(layout, getString(R.string.main_lifecycle_scope), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        addSpace(layout, 100);

        return layout;
    }

    private void addTitle(LinearLayout parent, String text) {
        TextView textView = new TextView(getActivity());
        textView.setTextSize(14);
        textView.setText(text);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 30;
        lp.rightMargin = 30;
        lp.topMargin = 24;
        lp.bottomMargin = 24;
        parent.addView(textView, lp);
    }

    private Button addButton(LinearLayout parent, String text, View.OnClickListener onClickListener) {
        Button button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText(text);
        button.setOnClickListener(onClickListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        parent.addView(button, lp);
        return button;
    }

    private void addSpace(LinearLayout parent, int height) {
        Space space = new Space(getActivity());
        parent.addView(space, ViewGroup.LayoutParams.MATCH_PARENT, height);
    }
}