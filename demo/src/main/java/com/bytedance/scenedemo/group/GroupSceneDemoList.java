package com.bytedance.scenedemo.group;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scenedemo.group.all.AllGroupScene;
import com.bytedance.scenedemo.group.drawer.DrawerGroupScene;
import com.bytedance.scenedemo.group.pageblock.PageBlockGroupScene;
import com.bytedance.scenedemo.group.tab.TabGroupScene;
import com.bytedance.scenedemo.group.viewpager.ViewPagerGroupScene;

/**
 * Created by JiangQi on 7/30/18.
 */
public class GroupSceneDemoList extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getActivity());
        textView.setText(getNavigationScene().getStackHistory());
        layout.addView(textView);

        Button button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("ViewPager 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ViewPagerGroupScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Drawer 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DrawerGroupScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Tab 用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(TabGroupScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("页面划分多个Scene用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PageBlockGroupScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("Drawer+Tab+ViewPager+单个页面划分多个Scene 混合的用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(AllGroupScene.class);
            }
        });

        return layout;
    }
}
