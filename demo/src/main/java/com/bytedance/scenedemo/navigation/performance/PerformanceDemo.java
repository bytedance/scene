package com.bytedance.scenedemo.navigation.performance;

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
import android.widget.TextView;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/21/18.
 */
public class PerformanceDemo extends Scene {
    public static long startTimestamp = 0;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(layout);

        TextView textView = new TextView(getActivity());
        layout.addView(textView);
        textView.setText("启动全空Scene的耗时通常只有启动全空Activity的1/3，启动普通Activity至少节省60ms的耗时");

        Button button = new Button(getActivity());

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("启动空的Activity耗时");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimestamp = System.currentTimeMillis();
                getNavigationScene().startActivity(new Intent(getActivity(), EmptyActivity.class));
            }
        });

        button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("启动空的Scene耗时");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimestamp = System.currentTimeMillis();
                getNavigationScene().push(EmptyScene.class);
            }
        });


        View emptyView = new View(getActivity());
        layout.addView(emptyView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));

        return scrollView;
    }
}
