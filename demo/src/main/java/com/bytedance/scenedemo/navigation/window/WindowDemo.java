package com.bytedance.scenedemo.navigation.window;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/21/18.
 */
public class WindowDemo extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(layout);

        Button button = new Button(getActivity());

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("状态栏导航栏背景色+图标颜色");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(WindowColorDemo.class);
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("状态栏导航栏布局");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(WindowLayoutDemo.class);
            }
        });


        View emptyView = new View(getActivity());
        layout.addView(emptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));

        return scrollView;
    }
}
