package com.bytedance.scenedemo;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scene.ui.GroupSceneUtility;
import com.bytedance.scenedemo.animation.AnimationListDemoScene;

import java.util.LinkedHashMap;

/**
 * Created by JiangQi on 8/3/18.
 */
public class MainListScene extends GroupScene {
    private ViewPager mViewPager;

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                  @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.main_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mViewPager = (ViewPager) view.findViewById(R.id.vp);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNavigationScene().registerChildSceneLifecycleCallbacks(mChildSceneLifecycleCallbacks, true);

//        getActivity().getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility()
//                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        getActivity().getWindow().getDecorView().setSystemUiVisibility(getActivity().getWindow().getDecorView().getSystemUiVisibility()
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
//
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(mViewPager);

        LinkedHashMap<String, UserVisibleHintGroupScene> list = new LinkedHashMap<>();

        list.put("导航", new NavigationListDemo());
        list.put("局部", new GroupListDemo());
        list.put("动画", new AnimationListDemoScene());
        list.put("生命周期", new LifeCycleListScene());
        list.put("极端Case", new CaseListDemo());

        GroupSceneUtility.setupWithViewPager(this.mViewPager, this, list);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getNavigationScene().unregisterChildSceneLifecycleCallbacks(mChildSceneLifecycleCallbacks);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private ChildSceneLifecycleCallbacks mChildSceneLifecycleCallbacks = new ChildSceneLifecycleCallbacks() {
        @Override
        public void onSceneCreated(Scene scene, Bundle savedInstanceState) {
            log("Scene", scene.toString() + " onSceneCreated");
        }

        @Override
        public void onSceneStarted(Scene scene) {
            log("Scene", scene.toString() + " onSceneStarted");
        }

        @Override
        public void onSceneResumed(Scene scene) {
            log("Scene", scene.toString() + " onSceneResumed");
        }

        @Override
        public void onSceneSaveInstanceState(Scene scene, Bundle outState) {
            log("Scene", scene.toString() + " onSceneSaveInstanceState");
        }

        @Override
        public void onScenePaused(Scene scene) {
            log("Scene", scene.toString() + " onScenePaused");
        }

        @Override
        public void onSceneStopped(Scene scene) {
            log("Scene", scene.toString() + " onSceneStopped");
        }

        @Override
        public void onSceneDestroyed(Scene scene) {
            log("Scene", scene.toString() + " onSceneDestroyed");
        }
    };

    private static void log(String name, String tag) {
        Log.e(name, tag);
    }
}