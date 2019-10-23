package com.bytedance.scenedemo.multi_stack;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.ui.template.BottomNavigationViewScene;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scenedemo.R;

public class MultiStackTabGroupScene extends BottomNavigationViewScene {
    @Override
    protected int getMenuResId() {
        return R.menu.bottom_nav_items;
    }

    private Bundle getBundle(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        return bundle;
    }

    @NonNull
    @Override
    protected SparseArrayCompat<Scene> getSceneMap() {
        SparseArrayCompat<Scene> sparseArrayCompat = new SparseArrayCompat<>();

        Bundle bundle = new Bundle();
        bundle.putInt("index", 0);

        NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                new NavigationSceneOptions(MultiStackTabChildScene.class, getBundle(0)).toBundle());

        sparseArrayCompat.put(R.id.menu_home, navigationScene);

        navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                new NavigationSceneOptions(MultiStackTabChildScene.class, getBundle(1)).toBundle());

        sparseArrayCompat.put(R.id.menu_search, navigationScene);

        navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                new NavigationSceneOptions(MultiStackTabChildScene.class, getBundle(2)).toBundle());

        sparseArrayCompat.put(R.id.menu_notifications, navigationScene);
        return sparseArrayCompat;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}