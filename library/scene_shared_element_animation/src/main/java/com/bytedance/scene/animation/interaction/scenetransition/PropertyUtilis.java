package com.bytedance.scene.animation.interaction.scenetransition;

import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.Property;
import android.view.View;

import com.bytedance.scene.animation.interaction.scenetransition.utils.SceneViewCompatUtils;

/**
 * Created by JiangQi on 10/19/18.
 */
public class PropertyUtilis {
    public static final Property<View, Rect> CLIP_BOUNDS =
            new Property<View, Rect>(Rect.class, "clipBounds") {

                @Override
                public Rect get(View view) {
                    return ViewCompat.getClipBounds(view);
                }

                @Override
                public void set(View view, Rect clipBounds) {
                    ViewCompat.setClipBounds(view, clipBounds);
                }

            };

    public static final Property<View, Float> TRANSITION_ALPHA =
            new Property<View, Float>(Float.class, "translationAlpha") {

                @Override
                public Float get(View view) {
                    return SceneViewCompatUtils.getTransitionAlpha(view);
                }

                @Override
                public void set(View view, Float alpha) {
                    SceneViewCompatUtils.setTransitionAlpha(view, alpha);
                }

            };
}
