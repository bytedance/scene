package com.bytedance.scene.interfaces;

import android.os.Bundle;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/1/18.
 */
public interface ChildSceneLifecycleCallbacks {
    void onSceneCreated(Scene scene, Bundle savedInstanceState);

    void onSceneStarted(Scene scene);

    void onSceneResumed(Scene scene);

    void onSceneSaveInstanceState(Scene scene, Bundle outState);

    void onScenePaused(Scene scene);

    void onSceneStopped(Scene scene);

    void onSceneDestroyed(Scene scene);
}
