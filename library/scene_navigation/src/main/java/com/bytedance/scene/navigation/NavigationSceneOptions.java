/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.navigation;

import android.os.Bundle;
import androidx.annotation.*;
import android.text.TextUtils;

import com.bytedance.scene.Scene;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 9/5/18.
 */
public class NavigationSceneOptions {
    private static final String EXTRA_ROOT_SCENE = "extra_rootScene";
    private static final String EXTRA_ROOT_SCENE_ARGUMENTS = "extra_rootScene_arguments";
    private static final String EXTRA_DRAW_WINDOW_BACKGROUND = "extra_drawWindowBackground";
    private static final String EXTRA_FIX_SCENE_BACKGROUND_ENABLED = "extra_fixSceneBackground_enabled";
    private static final String EXTRA_SCENE_BACKGROUND = "extra_sceneBackground";
    private static final String EXTRA_ONLY_RESTORE_VISIBLE_SCENE = "extra_onlyRestoreVisibleScene";
    private static final String EXTRA_USE_POST_IN_LIFECYCLE = "extra_usePostInLifecycle";
    private static final String EXTRA_AUTO_RECYCLE_INVISIBLE_SCENES_THRESHOLD = "extra_autoRecycleInvisibleScenesThreshold";
    private static final String EXTRA_USE_ACTIVITY_CONTEXT_AND_LAYOUT_INFLATER = "extra_useActivityContextAndLayoutInflater";
    private static final String EXTRA_LAZY_LOAD_NAVIGATION_SCENE_UNNECESSARY_VIEW = "extra_lazyLoadNavigationSceneUnnecessaryView";
    private static final String EXTRA_MERGE_NAVIGATION_SCENE_VIEW = "extra_mergeNavigationSceneView";
    private static final String EXTRA_USE_WINDOW_FOCUS_CHANGED_DISPATCH = "extra_useWindowFocusChangedDispatch";

    @NonNull
    private final String mRootSceneClassName;
    @Nullable
    private final Bundle mRootSceneArguments;
    private boolean mDrawWindowBackground = true;
    private boolean mFixSceneBackgroundEnabled = true;
    @DrawableRes
    private int mSceneBackgroundResId = 0;
    private boolean mOnlyRestoreVisibleScene = false;
    private boolean mUsePostInLifecycle = false;
    private float mAutoRecycleInvisibleScenesThreshold = 0F;
    private boolean mUseActivityContextAndLayoutInflater = false;
    private boolean mLazyLoadNavigationSceneUnnecessaryView = false;
    private boolean mMergeNavigationSceneView = false;
    private boolean mUseWindowFocusChangedDispatch = false;

    public NavigationSceneOptions(@NonNull Class<? extends Scene> rootSceneClazz, @Nullable Bundle rootSceneArguments) {
        if (rootSceneClazz.isAssignableFrom(NavigationScene.class)) {
            throw new IllegalArgumentException("cant use NavigationScene as root scene");
        }
        this.mRootSceneClassName = rootSceneClazz.getName();
        this.mRootSceneArguments = rootSceneArguments;
    }

    public NavigationSceneOptions(@NonNull Class<? extends Scene> rootSceneClazz) {
        this(rootSceneClazz, null);
    }

    private NavigationSceneOptions(@NonNull String rootSceneClassName, @Nullable Bundle rootSceneArguments) {
        this.mRootSceneClassName = rootSceneClassName;
        this.mRootSceneArguments = rootSceneArguments;
    }

    @NonNull
    public NavigationSceneOptions setDrawWindowBackground(boolean drawWindowBackground) {
        this.mDrawWindowBackground = drawWindowBackground;
        return this;
    }

    @NonNull
    public NavigationSceneOptions setFixSceneWindowBackgroundEnabled(boolean fixSceneBackground) {
        this.mFixSceneBackgroundEnabled = fixSceneBackground;
        return this;
    }

    @NonNull
    public NavigationSceneOptions setSceneBackground(@DrawableRes int resId) {
        this.mSceneBackgroundResId = resId;
        return this;
    }

    @NonNull
    public NavigationSceneOptions setOnlyRestoreVisibleScene(boolean onlyRestoreVisibleScene) {
        this.mOnlyRestoreVisibleScene = onlyRestoreVisibleScene;
        return this;
    }

    @NonNull
    public NavigationSceneOptions setUsePostInLifecycle(boolean usePostInLifecycle) {
        this.mUsePostInLifecycle = usePostInLifecycle;
        return this;
    }

    @NonNull
    public NavigationSceneOptions setAutoRecycleInvisibleScenesThreshold(@FloatRange(from = 0.0, to = 1.0) float threshold) {
        this.mAutoRecycleInvisibleScenesThreshold = threshold;
        return this;
    }

    /**
     * this is conflict with setDrawWindowBackground, if you use setUseActivityContextAndLayoutInflater(true),
     * you should use setDrawWindowBackground(false) together
     *
     * @param useActivityContextAndLayoutInflater
     * @return
     */
    @NonNull
    public NavigationSceneOptions setUseActivityContextAndLayoutInflater(boolean useActivityContextAndLayoutInflater) {
        this.mUseActivityContextAndLayoutInflater = useActivityContextAndLayoutInflater;
        return this;
    }

    @NonNull
    public NavigationSceneOptions setLazyLoadNavigationSceneUnnecessaryView(boolean lazyLoadNavigationSceneUnnecessaryView) {
        this.mLazyLoadNavigationSceneUnnecessaryView = lazyLoadNavigationSceneUnnecessaryView;
        return this;
    }

    @NonNull
    public NavigationSceneOptions setMergeNavigationSceneView(boolean mergeNavigationSceneView) {
        this.mMergeNavigationSceneView = mergeNavigationSceneView;
        return this;
    }

    @NonNull
    public NavigationSceneOptions setUseWindowFocusChangedDispatch(boolean useWindowFocusChangedDispatch) {
        this.mUseWindowFocusChangedDispatch = useWindowFocusChangedDispatch;
        return this;
    }

    @NonNull
    public String getRootSceneClassName() {
        return this.mRootSceneClassName;
    }

    @Nullable
    public Bundle getRootSceneArguments() {
        return this.mRootSceneArguments;
    }

    public boolean drawWindowBackground() {
        return this.mDrawWindowBackground;
    }

    public boolean fixSceneBackground() {
        return this.mFixSceneBackgroundEnabled;
    }

    public int getSceneBackgroundResId() {
        return this.mSceneBackgroundResId;
    }

    public boolean onlyRestoreVisibleScene() {
        return this.mOnlyRestoreVisibleScene;
    }

    public boolean usePostInLifecycle() {
        return this.mUsePostInLifecycle;
    }

    public float getAutoRecycleInvisibleSceneThreshold() {
        return this.mAutoRecycleInvisibleScenesThreshold;
    }

    public boolean getUseActivityContextAndLayoutInflater(){
        return this.mUseActivityContextAndLayoutInflater;
    }

    public boolean getLazyLoadNavigationSceneUnnecessaryView(){
        return this.mLazyLoadNavigationSceneUnnecessaryView;
    }

    public boolean getMergeNavigationSceneView(){
        return this.mMergeNavigationSceneView;
    }

    public boolean getUseWindowFocusChangedDispatch() {
        return mUseWindowFocusChangedDispatch;
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @NonNull
    public static NavigationSceneOptions fromBundle(@NonNull Bundle bundle) {
        String rootSceneClassName = bundle.getString(EXTRA_ROOT_SCENE);
        if (rootSceneClassName == null) {
            throw new IllegalStateException("root scene class name cant be null");
        }
        Bundle rootSceneArguments = bundle.getBundle(EXTRA_ROOT_SCENE_ARGUMENTS);
        NavigationSceneOptions navigationSceneOptions = new NavigationSceneOptions(rootSceneClassName, rootSceneArguments);
        navigationSceneOptions.mDrawWindowBackground = bundle.getBoolean(EXTRA_DRAW_WINDOW_BACKGROUND);
        navigationSceneOptions.mFixSceneBackgroundEnabled = bundle.getBoolean(EXTRA_FIX_SCENE_BACKGROUND_ENABLED);
        navigationSceneOptions.mSceneBackgroundResId = bundle.getInt(EXTRA_SCENE_BACKGROUND);
        navigationSceneOptions.mOnlyRestoreVisibleScene = bundle.getBoolean(EXTRA_ONLY_RESTORE_VISIBLE_SCENE);
        navigationSceneOptions.mUsePostInLifecycle = bundle.getBoolean(EXTRA_USE_POST_IN_LIFECYCLE);
        navigationSceneOptions.mAutoRecycleInvisibleScenesThreshold = bundle.getFloat(EXTRA_AUTO_RECYCLE_INVISIBLE_SCENES_THRESHOLD);
        navigationSceneOptions.mUseActivityContextAndLayoutInflater = bundle.getBoolean(EXTRA_USE_ACTIVITY_CONTEXT_AND_LAYOUT_INFLATER, false);
        navigationSceneOptions.mLazyLoadNavigationSceneUnnecessaryView = bundle.getBoolean(EXTRA_LAZY_LOAD_NAVIGATION_SCENE_UNNECESSARY_VIEW, false);
        navigationSceneOptions.mMergeNavigationSceneView = bundle.getBoolean(EXTRA_MERGE_NAVIGATION_SCENE_VIEW, false);
        navigationSceneOptions.mUseWindowFocusChangedDispatch = bundle.getBoolean(EXTRA_USE_WINDOW_FOCUS_CHANGED_DISPATCH, false);
        return navigationSceneOptions;
    }

    @NonNull
    public Bundle toBundle() {
        if (TextUtils.isEmpty(mRootSceneClassName)) {
            throw new IllegalArgumentException("call setRootScene first");
        }
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_ROOT_SCENE, mRootSceneClassName);
        bundle.putBundle(EXTRA_ROOT_SCENE_ARGUMENTS, mRootSceneArguments);
        bundle.putBoolean(EXTRA_DRAW_WINDOW_BACKGROUND, mDrawWindowBackground);
        bundle.putBoolean(EXTRA_FIX_SCENE_BACKGROUND_ENABLED, mFixSceneBackgroundEnabled);
        bundle.putInt(EXTRA_SCENE_BACKGROUND, mSceneBackgroundResId);
        bundle.putBoolean(EXTRA_ONLY_RESTORE_VISIBLE_SCENE, mOnlyRestoreVisibleScene);
        bundle.putBoolean(EXTRA_USE_POST_IN_LIFECYCLE, mUsePostInLifecycle);
        bundle.putFloat(EXTRA_AUTO_RECYCLE_INVISIBLE_SCENES_THRESHOLD, mAutoRecycleInvisibleScenesThreshold);
        bundle.putBoolean(EXTRA_USE_ACTIVITY_CONTEXT_AND_LAYOUT_INFLATER, mUseActivityContextAndLayoutInflater);
        bundle.putBoolean(EXTRA_LAZY_LOAD_NAVIGATION_SCENE_UNNECESSARY_VIEW, mLazyLoadNavigationSceneUnnecessaryView);
        bundle.putBoolean(EXTRA_MERGE_NAVIGATION_SCENE_VIEW, mMergeNavigationSceneView);
        bundle.putBoolean(EXTRA_USE_WINDOW_FOCUS_CHANGED_DISPATCH, mUseWindowFocusChangedDispatch);
        return bundle;
    }
}
