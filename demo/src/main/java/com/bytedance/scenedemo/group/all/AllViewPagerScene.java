package com.bytedance.scenedemo.group.all;

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
import com.bytedance.scene.ui.GroupSceneUtility;
import com.bytedance.scenedemo.R;

import java.util.LinkedHashMap;

/**
 * Created by JiangQi on 8/7/18.
 */
public class AllViewPagerScene extends GroupScene {
    public static AllViewPagerScene newInstance(int index) {
        AllViewPagerScene scene = new AllViewPagerScene();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        scene.setArguments(bundle);
        return scene;
    }

    private ViewPager mViewPager;
    private int index;

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                  @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.parent_scene_viewpager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mViewPager = (ViewPager) view.findViewById(R.id.vp);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(mViewPager);

        index = getArguments().getInt("index");

        LinkedHashMap<String, UserVisibleHintGroupScene> list = new LinkedHashMap<>();

        for (int i = 0; i < 6; i++) {
            UserVisibleHintGroupScene scene;
            if (i < 1) {
                scene = AllViewPagerChildScene.newInstance(index + i * 10);
            } else {
                scene = EmptyScene.newInstance(index + i * 10);
            }

            list.put("" + (index + i * 10), scene);
        }

        GroupSceneUtility.setupWithViewPager(this.mViewPager, this, list);
    }
}


