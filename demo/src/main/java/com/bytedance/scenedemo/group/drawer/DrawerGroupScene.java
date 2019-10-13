package com.bytedance.scenedemo.group.drawer;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.template.NavigationViewScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.group.EmptyScene;

import java.util.LinkedHashMap;

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
    protected LinkedHashMap<Integer, Scene> getSceneMap() {
        LinkedHashMap<Integer, Scene> sparseArrayCompat = new LinkedHashMap<>();
        sparseArrayCompat.put(R.id.nav_camera, getScene(0));
        sparseArrayCompat.put(R.id.nav_gallery, getScene(1));
        sparseArrayCompat.put(R.id.nav_slideshow, getScene(2));
        sparseArrayCompat.put(R.id.nav_manage, getScene(3));
        return sparseArrayCompat;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getNavigationView().inflateHeaderView(R.layout.nav_header);
        if (getActivity() != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private Scene getScene(int index) {
        DrawerEmptyScene scene = new DrawerEmptyScene();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        scene.setArguments(bundle);
        return scene;
    }

    public static class DrawerEmptyScene extends EmptyScene {
        @Override
        public void onResume() {
            super.onResume();
            int index = getArguments() == null ? 0 : getArguments().getInt("index");
            ((DrawerGroupScene) getParentScene()).getToolbar().setTitle("Scene" + index);
        }
    }
}
