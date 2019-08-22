package com.bytedance.scene.interfaces;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/1/18.
 */
public interface ChildSceneLifecycleCallbacks {
    void onSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState);

    void onSceneStarted(@NonNull Scene scene);

    void onSceneResumed(@NonNull Scene scene);

    void onSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState);

    void onScenePaused(@NonNull Scene scene);

    void onSceneStopped(@NonNull Scene scene);

    void onSceneDestroyed(@NonNull Scene scene);
}
