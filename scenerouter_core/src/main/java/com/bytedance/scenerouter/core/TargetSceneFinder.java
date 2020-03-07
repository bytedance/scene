package com.bytedance.scenerouter.core;

import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;

interface TargetSceneFinder {
    Class<? extends Scene> findSceneClassByUrl(@NonNull String url);
}
