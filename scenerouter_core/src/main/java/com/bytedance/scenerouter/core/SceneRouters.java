package com.bytedance.scenerouter.core;

import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.NavigationScene;

public final class SceneRouters {
    @NonNull
    public static SceneRouter of(@NonNull Scene scene) {
        return of(scene.requireNavigationScene());
    }

    @NonNull
    public static SceneRouter of(@NonNull NavigationScene navigationScene) {
        return new SceneRouter(navigationScene);
    }

    public static void bind(@NonNull Scene scene) {
        RouterValueBinder.bind(scene);
    }
}
