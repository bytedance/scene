package com.bytedance.scene.interfaces;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.ChildSceneLifecycleCallbacks;

import java.util.List;

/**
 * Created by JiangQi on 8/1/18.
 */
public interface SceneGroup {

    @NonNull
    List<Scene> getSceneList();

    void beginTransaction();

    void commitTransaction();

    //必须有Tag，不然序列化回来完全错乱
    void add(@IdRes int viewId, @NonNull Scene childScene, @NonNull String tag);

    @Nullable
    <T extends Scene> T findSceneByTag(@NonNull String tag);

    void remove(@NonNull Scene childScene);

    void show(@NonNull Scene childScene);

    void hide(@NonNull Scene childScene);

    boolean isAdded(@NonNull Scene scene);

    boolean isShow(@NonNull Scene scene);

    void registerChildSceneLifecycleCallbacks(ChildSceneLifecycleCallbacks cb, boolean recursive);

    void unregisterChildSceneLifecycleCallbacks(ChildSceneLifecycleCallbacks cb);
}
