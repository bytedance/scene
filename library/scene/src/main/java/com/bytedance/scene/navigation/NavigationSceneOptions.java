package com.bytedance.scene.navigation;

import android.os.Bundle;
import android.support.annotation.*;
import android.text.TextUtils;

import com.bytedance.scene.Scene;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 9/5/18.
 */
public class NavigationSceneOptions {
    private static final String EXTRA_ROOT_SCENE = "extra_rootScene";
    private static final String EXTRA_ROOT_SCENE_ARGUMENTS = "extra_rootScene_arguments";
    private static final String EXTRA_DRAW_WINDOW_BACKGROUND = "extra_drawWindowBackground";
    private static final String EXTRA_FIX_SCENE_BACKGROUND_ENABLED = "extra_fixSceneBackground_enabled";
    private static final String EXTRA_SCENE_BACKGROUND = "extra_sceneBackground";
    @NonNull
    private final String mRootSceneClassName;
    @Nullable
    private final Bundle mRootSceneArguments;
    private boolean mDrawWindowBackground = true;
    private boolean mFixSceneBackgroundEnabled = true;
    @DrawableRes
    private int mSceneBackgroundResId = 0;

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
        return bundle;
    }
}
