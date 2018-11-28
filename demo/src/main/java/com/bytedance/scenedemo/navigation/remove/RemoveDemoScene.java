package com.bytedance.scenedemo.navigation.remove;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/4/18.
 */
public class RemoveDemoScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        Button button = new Button(getActivity());
        button.setText("现在是RemoveDemoScene ，点击跳到 RemoveDemoScene1");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(RemoveDemoScene1.class);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getNavigationScene().remove(RemoveDemoScene.this);
                    }
                }, 3000);
            }
        });

        return layout;
    }

    public static class RemoveDemoScene1 extends GroupScene {
        @NonNull
        @Override
        public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

            Button button = new Button(getActivity());
            button.setText("等3秒，返回，会发现没有RemoveDemoScene了");
            layout.addView(button);


            return layout;
        }
    }
}
