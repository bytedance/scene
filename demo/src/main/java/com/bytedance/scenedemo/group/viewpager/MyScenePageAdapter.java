package com.bytedance.scenedemo.group.viewpager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scene.ui.ScenePageAdapter;

/**
 * Created by JiangQi on 7/30/18.
 */
public class MyScenePageAdapter extends ScenePageAdapter {
    public MyScenePageAdapter(ViewPager viewPager, GroupScene scene) {
        super(scene);
    }

    @Override
    public UserVisibleHintGroupScene getItem(int position) {
        ViewPagerChildScene childScene = new ViewPagerChildScene();
        Bundle bundle = new Bundle();
        bundle.putInt("index", position);
        childScene.setArguments(bundle);
        return childScene;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return "" + position;
    }
}
