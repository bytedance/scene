package com.bytedance.scenedemo.group.tab;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;

import com.bytedance.scene.Scene;
import com.bytedance.scene.ui.template.BottomNavigationViewScene;
import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 8/3/18.
 */
public class TabGroupScene extends BottomNavigationViewScene {
    @Override
    protected int getMenuResId() {
        return R.menu.bottom_nav_items;
    }

    @NonNull
    @Override
    protected SparseArrayCompat<Scene> getSceneMap() {
        SparseArrayCompat<Scene> sparseArrayCompat = new SparseArrayCompat<>();
        sparseArrayCompat.put(R.id.menu_home, TabChildScene.newInstance(0));
        sparseArrayCompat.put(R.id.menu_search, TabChildScene.newInstance(1));
        sparseArrayCompat.put(R.id.menu_notifications, TabChildScene.newInstance(2));
        return sparseArrayCompat;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}
