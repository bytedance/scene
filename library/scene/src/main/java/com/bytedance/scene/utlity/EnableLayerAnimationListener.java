package com.bytedance.scene.utlity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.support.annotation.RestrictTo;
import android.view.View;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 8/7/18.
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class EnableLayerAnimationListener extends AnimatorListenerAdapter {
    private View mView;
    private int mInitLayerType;

    public EnableLayerAnimationListener(View view) {
        this.mView = view;
        this.mInitLayerType = this.mView.getLayerType();
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        this.mView.setLayerType(this.mInitLayerType, null);
    }

    @Override
    public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);
        this.mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }
}
