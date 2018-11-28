package com.bytedance.scene.ui;

import com.bytedance.scene.interfaces.SceneNavigation;

/**
 * Created by JiangQi on 9/20/18.
 */
public interface SceneNavigationContainer {
    SceneNavigation getNavigationScene();

    boolean isVisible();

    int getThemeId();
}
