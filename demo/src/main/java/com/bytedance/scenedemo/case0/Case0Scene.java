package com.bytedance.scenedemo.case0;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.HorizontalTransitionAnimatorExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/5/18.
 */
public class Case0Scene extends GroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        TextView name = getView().findViewById(R.id.name);
        name.setVisibility(View.GONE);

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.case_push_pop_btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 100; i++) {
                    getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder()
                            .setAnimation(new HorizontalTransitionAnimatorExecutor()).build());
                    getNavigationScene().pop();
                }
            }
        });

        Button btn2 = getView().findViewById(R.id.btn2);
        btn2.setVisibility(View.VISIBLE);
        btn2.setText(R.string.case_push_pop_btn_2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 100; i++) {
                    getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder()
                            .setAnimation(new HorizontalTransitionAnimatorExecutor()).build());
                }

                for (int i = 0; i < 100; i++) {
                    getNavigationScene().pop();
                }
            }
        });

        Button btn3 = getView().findViewById(R.id.btn3);
        btn3.setVisibility(View.VISIBLE);
        btn3.setText(R.string.case_push_pop_btn_3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene0.class, null, new PushOptions.Builder()
                        .setAnimation(new HorizontalTransitionAnimatorExecutor()).build());

                for (int i = 0; i < 99; i++) {
                    getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder()
                            .setAnimation(new HorizontalTransitionAnimatorExecutor()).build());
                }

                for (int i = 0; i < 99; i++) {
                    getNavigationScene().pop();
                }
            }
        });
    }

    public static class EmptyScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(getActivity());
            textView.setText(getNavigationScene().getStackHistory());
            layout.addView(textView);

            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

            layout.setFitsSystemWindows(true);

            return layout;
        }
    }

    public static class EmptyScene0 extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);

            TextView textView = new TextView(getActivity());
            textView.setText(getNavigationScene().getStackHistory());
            layout.addView(textView);

            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));

            layout.setFitsSystemWindows(true);

            return layout;
        }
    }
}
