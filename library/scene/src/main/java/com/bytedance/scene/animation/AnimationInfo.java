package com.bytedance.scene.animation;

import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;

public class AnimationInfo {
    public final Class<?> mSceneClass;
    public final View mSceneView;
    public final State mSceneState;
    public final boolean mIsTranslucent;

    public AnimationInfo(Scene scene, View view, State state, boolean isTranslucent) {
        this.mSceneClass = scene.getClass();
        this.mSceneView = view;
        this.mSceneState = state;
        this.mIsTranslucent = isTranslucent;
    }
}