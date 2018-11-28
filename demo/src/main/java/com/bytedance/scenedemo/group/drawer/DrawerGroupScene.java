package com.bytedance.scenedemo.group.drawer;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.template.NavigationViewScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/3/18.
 */
public class DrawerGroupScene extends NavigationViewScene {
    @Override
    protected int getMenuResId() {
        return R.menu.drawer_view;
    }

    @NonNull
    @Override
    protected SparseArrayCompat<Scene> getSceneMap() {
        SparseArrayCompat<Scene> sparseArrayCompat = new SparseArrayCompat<>();
        sparseArrayCompat.put(R.id.nav_camera, new Scene0());
        sparseArrayCompat.put(R.id.nav_gallery, new Scene1());
        sparseArrayCompat.put(R.id.nav_slideshow, new Scene2());
        sparseArrayCompat.put(R.id.nav_manage, new Scene3());
        return sparseArrayCompat;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getToolbar().setTitle("标题");
        getNavigationView().inflateHeaderView(R.layout.nav_header);
        getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    public static class Scene0 extends Scene {
        TextView textView;

        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

            textView = new TextView(getActivity());
            textView.setAllCaps(false);
            textView.setText("Scene0");
            layout.addView(textView);

            return layout;
        }

        @Override
        public void onResume() {
            super.onResume();
            textView.setText(getStateHistory());
            ((DrawerGroupScene) getParentScene()).getToolbar().setTitle("Scene0");
        }
    }

    public static class Scene1 extends Scene {
        TextView textView;

        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

            textView = new TextView(getActivity());
            textView.setAllCaps(false);
            textView.setText("Scene0");
            layout.addView(textView);

            return layout;
        }

        @Override
        public void onResume() {
            super.onResume();
            textView.setText(getStateHistory());
            ((DrawerGroupScene) getParentScene()).getToolbar().setTitle("Scene1");
        }
    }

    public static class Scene2 extends Scene {
        TextView textView;

        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));

            textView = new TextView(getActivity());
            textView.setAllCaps(false);
            textView.setText("Scene0");
            layout.addView(textView);

            return layout;
        }

        @Override
        public void onResume() {
            super.onResume();
            textView.setText(getStateHistory());
            ((DrawerGroupScene) getParentScene()).getToolbar().setTitle("Scene2");
        }
    }


    public static class Scene3 extends Scene {
        TextView textView;

        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            LinearLayout layout = new LinearLayout(getActivity());
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 3));

            textView = new TextView(getActivity());
            textView.setAllCaps(false);
            textView.setText("Scene0");
            layout.addView(textView);

            return layout;
        }

        @Override
        public void onResume() {
            super.onResume();
            textView.setText(getStateHistory());
            ((DrawerGroupScene) getParentScene()).getToolbar().setTitle("Scene3");
        }
    }
}
