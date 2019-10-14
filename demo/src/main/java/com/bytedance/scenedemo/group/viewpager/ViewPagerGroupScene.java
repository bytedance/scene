package com.bytedance.scenedemo.group.viewpager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scene.ui.GroupSceneUIUtility;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.group.EmptyScene;

import java.util.LinkedHashMap;

/**
 * Created by JiangQi on 7/30/18.
 */
public class ViewPagerGroupScene extends GroupScene {

    private ViewPager mViewPager;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                  @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.parent_scene_viewpager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mViewPager = view.findViewById(R.id.vp);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TabLayout tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(mViewPager);

        LinkedHashMap<String, UserVisibleHintGroupScene> list = new LinkedHashMap<>();

        for (int i = 0; i < 4; i++) {
            list.put(String.valueOf(i), EmptyScene.newInstance(i));
        }

        GroupSceneUIUtility.setupWithViewPager(this.mViewPager, this, list);
        this.mViewPager.setCurrentItem(1);
    }
}
