package com.bytedance.scene.ui;

import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 9/20/18.
 */
public interface SceneNavigationContainer {
    NavigationScene getNavigationScene();

    boolean isVisible();

    int getThemeId();
}
