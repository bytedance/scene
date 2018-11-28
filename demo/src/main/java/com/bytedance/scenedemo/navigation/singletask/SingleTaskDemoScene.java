package com.bytedance.scenedemo.navigation.singletask;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/4/18.
 */
public class SingleTaskDemoScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        Button button = new Button(getActivity());
        button.setText("现在是SingleTaskDemoScene ，点击跳到 SingleTaskDemoScene1");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SingleTaskDemoScene1.class);
            }
        });

        return layout;
    }

    public static class SingleTaskDemoScene1 extends GroupScene {
        @NonNull
        @Override
        public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

            Button button = new Button(getActivity());
            button.setText("现在是SingleTaskDemoScene1 ，点击跳到 SingleTaskDemoScene");
            layout.addView(button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNavigationScene().push(SingleTaskDemoScene.class, null,
                            new PushOptions.Builder().setRemovePredicate(new PushOptions.SingleTaskPredicate(SingleTaskDemoScene.class)).build());
                }
            });

            return layout;
        }
    }
}
