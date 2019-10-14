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
import android.view.Window;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scene.ui.GroupSceneUIUtility;
import com.bytedance.scenedemo.animation.AnimationListDemoScene;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

/**
 * Created by JiangQi on 8/3/18.
 */
public class MainScene extends GroupScene {
    private ViewPager mViewPager;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container,
                                  @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.main_scene, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mViewPager = view.findViewById(R.id.vp);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNavigationScene().registerChildSceneLifecycleCallbacks(mChildSceneLifecycleCallbacks, true);

        if (getActivity() != null && getActivity().getWindow() != null) {
            Window window = getActivity().getWindow();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                window.getDecorView().setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
            }
        }

        TabLayout tabLayout = findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(mViewPager);

        LinkedHashMap<String, UserVisibleHintGroupScene> list = new LinkedHashMap<>();

        list.put(getString(R.string.main_scene_title_navigation), new NavigationListDemo());
        list.put(getString(R.string.main_scene_title_part), new GroupListDemo());
        list.put(getString(R.string.main_scene_title_anim), new AnimationListDemoScene());
//        list.put(getString(R.string.main_scene_title_lifecycle), new LifeCycleListScene());
        list.put(getString(R.string.main_scene_title_extreme_case), new CaseListDemo());

        GroupSceneUIUtility.setupWithViewPager(this.mViewPager, this, list);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
            log(scene.toString() + " onSceneCreated");
        }

        @Override
        public void onSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState) {
            log(scene.toString() + " onSceneActivityCreated");
        }

        @Override
        public void onSceneStarted(Scene scene) {
            log(scene.toString() + " onSceneStarted");
        }

        @Override
        public void onSceneResumed(Scene scene) {
            log(scene.toString() + " onSceneResumed");
        }

        @Override
        public void onSceneSaveInstanceState(Scene scene, @NonNull Bundle outState) {
            log(scene.toString() + " onSceneSaveInstanceState");
        }

        @Override
        public void onScenePaused(Scene scene) {
            log(scene.toString() + " onScenePaused");
        }

        @Override
        public void onSceneStopped(Scene scene) {
            log(scene.toString() + " onSceneStopped");
        }

        @Override
        public void onSceneViewDestroyed(@NonNull Scene scene) {
            log(scene.toString() + " onSceneViewDestroyed");
        }

        @Override
        public void onSceneDestroyed(Scene scene) {
            log(scene.toString() + " onSceneDestroyed");
        }
    };

    private static void log(String tag) {
        Log.e("Scene", tag);
    }
}