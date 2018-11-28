package com.bytedance.scenedemo;

import android.content.Intent;
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
import com.bytedance.scenedemo.async_inflate.AsyncInflateSceneDemo;
import com.bytedance.scenedemo.dialog.DialogListDemoScene;
import com.bytedance.scenedemo.group.all.AllGroupScene;
import com.bytedance.scenedemo.group.drawer.DrawerGroupScene;
import com.bytedance.scenedemo.group.fragment.FragmentBindingDemoActivity;
import com.bytedance.scenedemo.group.fragment.TestSceneToViewActivity;
import com.bytedance.scenedemo.group.inherited.InheritedDemo;
import com.bytedance.scenedemo.group.pageblock.PageBlockGroupScene;
import com.bytedance.scenedemo.group.tab.TabGroupScene;
import com.bytedance.scenedemo.group.viewpager.ViewPagerGroupScene;

/**
 * Created by JiangQi on 8/21/18.
 */
public class GroupListDemo extends UserVisibleHintGroupScene {
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
        button.setText("顶Tab+ViewPager 用法");
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
        button.setText("底Tab 用法");
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
        button.setText("Drawer+底Tab+顶Tab+ViewPager+单个页面划分多个Scene 混合的用法");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(AllGroupScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("绑定到Fragment");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().startActivity(new Intent(getActivity(), FragmentBindingDemoActivity.class));
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("绑定到View");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().startActivity(new Intent(getActivity(), TestSceneToViewActivity.class));
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("InheritedScene");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(InheritedDemo.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("各种对话框");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DialogListDemoScene.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("异步LayoutInflate");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(AsyncInflateSceneDemo.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("悬浮窗（未完成）");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        View emptyView = new View(getActivity());
        layout.addView(emptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));

        return scrollView;
    }
}