package com.bytedance.scene.interfaces;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.group.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.navigation.ConfigurationChangedListener;
import com.bytedance.scene.navigation.PopListener;

/**
 * Created by JiangQi on 8/1/18.
 * 要求是Class，不支持复用Scene
 */
public interface SceneNavigation {

    void push(@NonNull Class<? extends Scene> clazz);

    void push(@NonNull Class<? extends Scene> clazz, @Nullable Bundle argument);

    void push(@NonNull Class<? extends Scene> clazz, @Nullable Bundle argument, @Nullable PushOptions pushOptions);

    void push(@NonNull Scene scene);

    void push(@NonNull Scene scene, @Nullable PushOptions pushOptions);

    void setResult(@NonNull Scene scene, @Nullable Object result);

    boolean pop();

    void pop(PopOptions popOptions);

    void popTo(@NonNull Class<? extends Scene> clazz);

    void popTo(@NonNull Class<? extends Scene> clazz, @Nullable NavigationAnimationExecutor animationFactory);

    //todo 还是有问题的，并没有到Root
    void popToRoot();

    void popToRoot(@Nullable NavigationAnimationExecutor animationFactory);

    void remove(Scene scene);

    void addPopListener(@NonNull Scene scene, @NonNull PopListener popListener);

    void removePopListener(@NonNull PopListener popListener);

    void addConfigurationChangedListener(@NonNull Scene scene, @NonNull ConfigurationChangedListener configurationChangedListener);

    @NonNull
    String getStackHistory();

    Scene getCurrentScene();

    void startActivity(@NonNull Intent intent);

    void startActivityForResult(@NonNull Intent intent, int requestCode, ActivityResultCallback resultCallback);

    void requestPermissions(@NonNull String[] permissions, int requestCode, PermissionResultCallback resultCallback);

    void onConfigurationChanged(Configuration newConfig);

    void registerChildSceneLifecycleCallbacks(ChildSceneLifecycleCallbacks cb, boolean recursive);

    void unregisterChildSceneLifecycleCallbacks(ChildSceneLifecycleCallbacks cb);
}
