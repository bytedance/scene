package com.bytedance.scene;

import android.support.annotation.NonNull;
import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 10/25/18.
 */
public interface NavigationSceneAvailableCallback {
    public void onNavigationSceneAvailable(@NonNull NavigationScene navigationScene);
}
