package com.bytedance.scenedemo.navigation.push_clear_current;

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
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/17/18.
 */
public class PushClearCurrentDemoScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(getActivity());
        button.setText("跳到新页面并且关闭当前页面");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder().clearCurrent().build());
            }
        });

        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        return layout;
    }

    public static class EmptyScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(getActivity());
            textView.setText(getNavigationScene().getStackHistory());
            layout.addView(textView);

            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

            return layout;
        }
    }
}
