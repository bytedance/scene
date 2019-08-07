package com.bytedance.scenedemo.theme;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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
    protected ViewGroup onCreateSwipeContentView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
        root.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView name = getView().findViewById(R.id.name);
        name.setVisibility(View.GONE);

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.nav_theme_btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestTheme0Scene scene = new TestTheme0Scene(R.style.AppTheme_Test1);
                getNavigationScene().push(scene);
            }
        });

        Button btn2 = getView().findViewById(R.id.btn2);
        btn2.setVisibility(View.VISIBLE);
        btn2.setText(R.string.nav_theme_btn_2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestTheme0Scene scene = new TestTheme0Scene(R.style.AppTheme_Test2);
                getNavigationScene().push(scene);
            }
        });

        Button btn3 = getView().findViewById(R.id.btn3);
        btn3.setVisibility(View.VISIBLE);
        btn3.setText(R.string.nav_theme_btn_3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TestTheme1Scene scene = new TestTheme1Scene(R.style.AppTheme_Test3);
                getNavigationScene().push(scene);
            }
        });
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
