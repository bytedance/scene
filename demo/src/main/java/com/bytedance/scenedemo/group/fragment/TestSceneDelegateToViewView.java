package com.bytedance.scenedemo.group.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.LifeCycleFrameLayout;
import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;
import com.bytedance.scene.utlity.SceneInstanceUtility;

/**
 * Created by JiangQi on 11/6/18.
 */
public class TestSceneDelegateToViewView extends LifeCycleFrameLayout {
    public TestSceneDelegateToViewView(@NonNull Context context) {
        super(context);
        init();
    }

    public TestSceneDelegateToViewView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TestSceneDelegateToViewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TestSceneDelegateToViewView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        NavigationSceneOptions options = new NavigationSceneOptions();
        options.setDrawWindowBackground(false);
        options.setRootScene(TestScene.class, null);
        NavigationScene navigationScene = (NavigationScene) SceneInstanceUtility.getInstanceFromClass(NavigationScene.class,
                options.toBundle());
        setNavigationScene(navigationScene);
    }

    @Override
    public void startActivityForResult(@NonNull Intent intent, int requestCode) {

    }

    @Override
    public void requestPermissions(@NonNull String[] permissions, int requestCode) {

    }

    public static class TestScene extends Scene {
        @NonNull
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = new View(getActivity());
            view.setBackgroundColor(Color.RED);
            return view;
        }
    }
}
