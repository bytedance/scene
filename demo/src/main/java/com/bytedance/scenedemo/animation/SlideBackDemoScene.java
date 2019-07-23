package com.bytedance.scenedemo.animation;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.ui.template.SwipeBackGroupScene;
import com.bytedance.scene.ui.view.StatusBarView;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/9/18.
 */
public class SlideBackDemoScene extends SwipeBackGroupScene {

    @NonNull
    @Override
    protected ViewGroup onCreateSwipeContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        final LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

        StatusBarView statusBarView = new StatusBarView(getActivity());
        linearLayout.addView(statusBarView);
        statusBarView.setStatusBarBackgroundColor(Color.RED);

        Button button = new Button(getActivity());
        button.setText("返回");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().pop();
            }
        });
        linearLayout.addView(button);
        return linearLayout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
