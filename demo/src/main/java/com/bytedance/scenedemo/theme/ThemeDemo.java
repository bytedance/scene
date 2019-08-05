package com.bytedance.scenedemo.theme;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.template.SwipeBackGroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 11/9/18.
 */
public class ThemeDemo extends SwipeBackGroupScene {
    @NonNull
    @Override
    protected ViewGroup onCreateSwipeContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setFitsSystemWindows(true);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        Button button = new Button(getActivity());

        button = new Button(getActivity());
        layout.addView(button);
        button.setAllCaps(false);
        button.setText("AppTheme_Test0");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestTheme0Scene scene = new TestTheme0Scene(R.style.AppTheme_Test0);
                getNavigationScene().push(scene);
            }
        });

        button = new Button(getActivity());
        layout.addView(button);
        button.setAllCaps(false);
        button.setText("AppTheme_Test1");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestTheme0Scene scene = new TestTheme0Scene(R.style.AppTheme_Test1);
                getNavigationScene().push(scene);
            }
        });

        button = new Button(getActivity());
        layout.addView(button);
        button.setAllCaps(false);
        button.setText("AppTheme_Test2");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestTheme1Scene scene = new TestTheme1Scene(R.style.AppTheme_Test2);
                getNavigationScene().push(scene);
            }
        });

        return layout;
    }

    private static class TestTheme0Scene extends Scene {

        private TestTheme0Scene(@StyleRes int themeId) {
            setTheme(themeId);
        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_theme_demo, container, false);
        }
    }

    private static class TestTheme1Scene extends Scene {
        private int mThemeId;

        private TestTheme1Scene(@StyleRes int themeId) {
            this.mThemeId = themeId;
        }

        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            setTheme(this.mThemeId);
            return inflater.inflate(R.layout.layout_theme_demo1, container, false);
        }
    }
}
