package com.bytedance.scenedemo.navigation.pushandclear;

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
import com.bytedance.scene.animation.animatorexecutor.HorizontalTransitionAnimatorExecutor;
import com.bytedance.scene.interfaces.PushOptions;

/**
 * Created by JiangQi on 8/2/18.
 */
public class PushClearScene_2 extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getActivity());
        textView.setText(getNavigationScene().getStackHistory());
        layout.addView(textView);

        Button button = new Button(getActivity());
        button.setText("现在是PushClearScene_2，点击跳到 PushClearScene_3 并清空之前的Stack");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushOptions options = new PushOptions.Builder()
                        .clearTask()
                        .setAnimation(new HorizontalTransitionAnimatorExecutor())
                        .build();
                getNavigationScene().push(PushClearScene_3.class, null, options);
            }
        });

        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
