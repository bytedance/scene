package com.bytedance.scene.navigation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 9/5/18.
 */
public class NavigationSceneOptions {
    private static final String EXTRA_ROOT_SCENE = "extra_rootScene";
    private static final String EXTRA_ROOT_SCENE_ARGUMENTS = "extra_rootScene_arguments";
    private static final String EXTRA_DRAW_WINDOW_BACKGROUND = "extra_drawWindowBackground";
    private static final String EXTRA_FIX_SCENE_BACKGROUND_ENABLED = "extra_fixSceneBackground_enabled";
    private static final String EXTRA_SCENE_BACKGROUND = "extra_sceneBackground";
    private String mRootSceneClassName;
    private Bundle mRootSceneArguments;
    private boolean mDrawWindowBackground = true;
    private boolean mFixSceneBackgroundEnabled = true;
    private int mSceneBackgroundResId;

    public NavigationSceneOptions() {

    }

    public NavigationSceneOptions setRootScene(@NonNull Class<? extends Scene> clazz, @Nullable Bundle bundle) {
        if (clazz.isAssignableFrom(NavigationScene.class)) {
            throw new IllegalArgumentException("cant use NavigationScene as root scene");
        }
        this.mRootSceneClassName = clazz.getName();
        this.mRootSceneArguments = bundle;
        return this;
    }

    public NavigationSceneOptions setDrawWindowBackground(boolean drawWindowBackground) {
        this.mDrawWindowBackground = drawWindowBackground;
        return this;
    }

    public NavigationSceneOptions setFixSceneWindowBackgroundEnabled(boolean fixSceneBackground) {
        this.mFixSceneBackgroundEnabled = fixSceneBackground;
        return this;
    }

    public NavigationSceneOptions setSceneBackground(int resId) {
        this.mSceneBackgroundResId = resId;
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

    public static NavigationSceneOptions fromBundle(Bundle bundle) {
        NavigationSceneOptions navigationSceneOptions = new NavigationSceneOptions();
        navigationSceneOptions.mRootSceneClassName = bundle.getString(EXTRA_ROOT_SCENE);
        navigationSceneOptions.mRootSceneArguments = bundle.getBundle(EXTRA_ROOT_SCENE_ARGUMENTS);
        navigationSceneOptions.mDrawWindowBackground = bundle.getBoolean(EXTRA_DRAW_WINDOW_BACKGROUND);
        navigationSceneOptions.mFixSceneBackgroundEnabled = bundle.getBoolean(EXTRA_FIX_SCENE_BACKGROUND_ENABLED);
        navigationSceneOptions.mSceneBackgroundResId = bundle.getInt(EXTRA_SCENE_BACKGROUND);
        return navigationSceneOptions;
    }

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
        return bundle;
    }
}
