package com.bytedance.scene.animation;

import android.support.annotation.RestrictTo;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

@RestrictTo(LIBRARY_GROUP)
class ReverseInterpolator implements Interpolator {
    final Interpolator delegate;

    ReverseInterpolator(Interpolator delegate) {
        this.delegate = delegate;
    }

    public ReverseInterpolator() {
        this(new LinearInterpolator());
    }

    @Override
    public float getInterpolation(float input) {
        return 1 - delegate.getInterpolation(input);
    }
}
