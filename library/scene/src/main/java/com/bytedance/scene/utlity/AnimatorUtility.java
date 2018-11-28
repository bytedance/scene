package com.bytedance.scene.utlity;

import android.view.View;
import android.view.ViewGroup;

/**
 * Created by JiangQi on 8/2/18.
 */
public class AnimatorUtility {
    public static void resetViewStatus(View view) {
        view.setTranslationX(0);
        view.setTranslationY(0);
        view.setScaleX(1.0f);
        view.setScaleY(1.0f);
        view.setRotation(0.0f);
        view.setRotationX(0.0f);
        view.setRotationY(0.0f);
        view.setAlpha(1.0f);
        view.clearAnimation();
    }

    public static void bringToFrontIfNeeded(View view) {
        ViewGroup viewGroup = (ViewGroup) view.getParent();
        int childCount = viewGroup.getChildCount();
        int childIndex = viewGroup.indexOfChild(view);
        if (childIndex >= 0 && childIndex != childCount - 1) {
            view.bringToFront();
        }
    }
}
