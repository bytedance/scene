package com.bytedance.scene;

import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 9/4/18.
 */
public interface FragmentDelegate {
    NavigationScene getNavigationScene();

    /**
     * The timing of creating NavigationScene is different:
     *   1. the first time bind to Fragment
     *   2. Fragment destruction recovery
     * This method can guarantee when the NavigationScene object is created.
     * Notice: The callback doesn't have the complete life cycle,
     *         it's just a primitive Java object.
     */
    void setNavigationSceneAvailableCallback(NavigationSceneAvailableCallback callback);
}
