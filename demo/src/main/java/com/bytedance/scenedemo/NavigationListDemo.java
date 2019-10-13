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
import android.widget.Space;
import android.widget.TextView;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.navigation.forresult.SceneResultScene_0;
import com.bytedance.scenedemo.navigation.performance.PerformanceDemo;
import com.bytedance.scenedemo.navigation.popinterupt.PopInterruptScene;
import com.bytedance.scenedemo.navigation.popto.PopToScene;
import com.bytedance.scenedemo.navigation.push_clear_current.PushClearCurrentDemoScene;
import com.bytedance.scenedemo.navigation.push_pop.PushPopDemoScene;
import com.bytedance.scenedemo.navigation.push_singletop.PushSingleTopRootScene;
import com.bytedance.scenedemo.navigation.pushandclear.PushClearTaskScene;
import com.bytedance.scenedemo.navigation.remove.RemoveDemoScene;
import com.bytedance.scenedemo.navigation.reuse.ReuseDemoScene;
import com.bytedance.scenedemo.navigation.singletask.SingleTaskDemoScene;
import com.bytedance.scenedemo.template.DefaultScene;

import org.jetbrains.annotations.NotNull;

/**
 * Created by JiangQi on 8/21/18.
 */
public class NavigationListDemo extends UserVisibleHintGroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(layout);

        addSpace(layout, 12);
        addTitle(layout, getString(R.string.main_title_basic));

        addButton(layout, getString(R.string.main_nav_btn_push_pop), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushPopDemoScene.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_single_top), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushSingleTopRootScene.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_single_task), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SingleTaskDemoScene.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_clear_task), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushClearTaskScene.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_clear_current), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushClearCurrentDemoScene.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_remove), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(RemoveDemoScene.class);
            }
        });

        addTitle(layout, getString(R.string.main_title_pro));

        addButton(layout, getString(R.string.main_nav_btn_pop_to), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PopToScene.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_interrupt_pop), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PopInterruptScene.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_for_result), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SceneResultScene_0.class);
            }
        });

        addTitle(layout, getString(R.string.main_title_other));

        addButton(layout, getString(R.string.main_nav_btn_reuse), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ReuseDemoScene.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_compare_activity), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PerformanceDemo.class);
            }
        });

        addButton(layout, getString(R.string.main_nav_btn_app_compat), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DefaultScene.class);
            }
        });

        addTitle(layout, getString(R.string.main_title_todo));

        addButton(layout, getString(R.string.main_nav_btn_deep_link), new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        addSpace(layout, 100);

        return scrollView;
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
