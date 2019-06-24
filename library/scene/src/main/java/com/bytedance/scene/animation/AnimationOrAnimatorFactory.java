package com.bytedance.scene.animation;

import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

@RestrictTo(LIBRARY_GROUP)
public interface AnimationOrAnimatorFactory {
    @Nullable
    public AnimationOrAnimator getAnimationOrAnimator();
}