package com.bytedance.scenedemo.navigation.reuse;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytedance.scene.group.GroupScene;

/**
 * Created by JiangQi on 8/13/18.
 */
public class ReuseDemoScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getActivity());
        textView.setText("启动一个很重量级别的页面，onCreateView 耗时一秒，只有第一次会卡");
        layout.addView(textView);

        Button button = new Button(getActivity());
        button.setText("启动");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(ReuseScene1.class);
            }
        });

        return layout;
    }
}
