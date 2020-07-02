package com.bytedance.scenedemo.multi_stack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.ui.template.BottomNavigationViewScene;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scenedemo.R;

import java.util.LinkedHashMap;

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
    protected LinkedHashMap<Integer, Scene> getSceneMap() {
        LinkedHashMap<Integer, Scene> linkedHashMap = new LinkedHashMap<>();

        Bundle bundle = new Bundle();
        bundle.putInt("index", 0);

        NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                new NavigationSceneOptions(MultiStackTabChildScene.class, getBundle(0)).toBundle());

        linkedHashMap.put(R.id.menu_home, navigationScene);

        navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                new NavigationSceneOptions(MultiStackTabChildScene.class, getBundle(1)).toBundle());

        linkedHashMap.put(R.id.menu_search, navigationScene);

        navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                new NavigationSceneOptions(MultiStackTabChildScene.class, getBundle(2)).toBundle());

        linkedHashMap.put(R.id.menu_notifications, navigationScene);
        return linkedHashMap;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}