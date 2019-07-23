package com.bytedance.scene.animation.interaction.scenetransition;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;

import com.bytedance.scene.animation.interaction.evaluator.RectEvaluator;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

/**
 * Created by JiangQi on 10/19/18.
 */
public class ChangeClipBounds extends SceneTransition {
    private static Rect captureValues(View view) {
        if (view.getVisibility() == View.GONE) {
            return null;
        }

        return ViewCompat.getClipBounds(view);
    }

    private Rect mFromRect;
    private Rect mToRect;
    private static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());

    @Override
    public void captureValue(@NonNull View fromView, @NonNull View toView, @NonNull View animationView) {
        super.captureValue(fromView, toView, animationView);
        this.mFromRect = captureValues(fromView);
        this.mToRect = captureValues(toView);
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        if (this.mFromRect == null || this.mToRect == null || this.mFromRect.equals(this.mToRect)) {
            return InteractionAnimation.EMPTY;
        }
        return new InteractionAnimation() {
            @Override
            public void onProgress(float progress) {
                PropertyUtilis.CLIP_BOUNDS.set(mAnimationView, RECT_EVALUATOR.evaluate(progress, mFromRect, mToRect));
            }
        };
    }

    @Override
    public void finish(boolean push) {
        ViewCompat.setClipBounds(mAnimationView, null);
    }
}
