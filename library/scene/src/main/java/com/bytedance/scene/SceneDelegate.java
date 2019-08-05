package com.bytedance.scene;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.bytedance.scene.navigation.NavigationScene;

public interface SceneDelegate {
    boolean onBackPressed();

    @Nullable
    NavigationScene getNavigationScene();

    /**
     * The timing of creating NavigationScene is different:
     *   1. the first time bind to Fragment
     *   2. Fragment destruction recovery
     * This method can guarantee when the NavigationScene object is created.
     * Notice: The callback doesn't have the complete life cycle,
     *         it's just a primitive Java object.
     */
    void setNavigationSceneAvailableCallback(@NonNull NavigationSceneAvailableCallback callback);

    //clear all lifecycle fragment, scope fragment, NavigationScene, GroupScene relation to this SceneDelegate
    void abandon();
}