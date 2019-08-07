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
import android.widget.Space;
import android.widget.TextView;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.async_inflate.AsyncInflateSceneDemo;
import com.bytedance.scenedemo.dialog.DialogListDemoScene;
import com.bytedance.scenedemo.group.drawer.DrawerGroupScene;
import com.bytedance.scenedemo.group.fragment.FragmentBindingDemoActivity;
import com.bytedance.scenedemo.group.fragment.TestSceneToViewActivity;
import com.bytedance.scenedemo.group.inherited.InheritedDemo;
import com.bytedance.scenedemo.group.pageblock.PageBlockGroupScene;
import com.bytedance.scenedemo.group.viewpager.ViewPagerGroupScene;
import com.bytedance.scenedemo.multi_stack.MultiStackTabGroupScene;

/**
 * Created by JiangQi on 8/21/18.
 */
public class GroupListDemo extends UserVisibleHintGroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(layout);

        addSpace(layout, 12);
        addTitle(layout, getString(R.string.main_title_basic));

        addButton(layout, getString(R.string.main_part_btn_tab_view_pager), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ViewPagerGroupScene.class);
            }
        });

        addButton(layout, getString(R.string.main_part_btn_drawer), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DrawerGroupScene.class);
            }
        });

        addButton(layout, getString(R.string.main_part_btn_bottom_tab_layout), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(MultiStackTabGroupScene.class);
            }
        });

        addButton(layout, getString(R.string.main_part_btn_dialog), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DialogListDemoScene.class);
            }
        });

        addButton(layout, getString(R.string.main_part_btn_child_scene), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PageBlockGroupScene.class);
            }
        });

        addTitle(layout, getString(R.string.main_title_pro));

        addButton(layout, getString(R.string.main_part_btn_bind_to_fragment), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().startActivity(new Intent(getActivity(), FragmentBindingDemoActivity.class));
            }
        });

        addButton(layout, getString(R.string.main_part_btn_bind_to_view), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().startActivity(new Intent(getActivity(), TestSceneToViewActivity.class));
            }
        });

        addButton(layout, getString(R.string.main_part_btn_async_inflate), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(AsyncInflateSceneDemo.class);
            }
        });

        addButton(layout, getString(R.string.main_part_btn_inherited_scene), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(InheritedDemo.class);
            }
        });

        addTitle(layout, getString(R.string.main_title_todo));

        addButton(layout, getString(R.string.main_part_btn_floating_window), new View.OnClickListener() {
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