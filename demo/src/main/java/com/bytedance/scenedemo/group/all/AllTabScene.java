package com.bytedance.scenedemo.group.all;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;

import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.template.BottomNavigationViewScene;
import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 8/7/18.
 */
public class AllTabScene extends BottomNavigationViewScene {
    public static AllTabScene newInstance(int index) {
        AllTabScene scene = new AllTabScene();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        scene.setArguments(bundle);
        return scene;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        final int index = getArguments().getInt("index") * 1000;
//        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));
    }

    @Override
    protected int getMenuResId() {
        return R.menu.bottom_nav_items;
    }

    @NonNull
    @Override
    protected SparseArrayCompat<Scene> getSceneMap() {
        final int index = getArguments().getInt("index") * 1000;
        SparseArrayCompat<Scene> sparseArrayCompat = new SparseArrayCompat<>();
        sparseArrayCompat.put(R.id.menu_home, AllViewPagerScene.newInstance(index + 100));
        sparseArrayCompat.put(R.id.menu_search, EmptyScene.newInstance(index + 200));
        sparseArrayCompat.put(R.id.menu_notifications, EmptyScene.newInstance(index + 300));
        return sparseArrayCompat;
    }
}
