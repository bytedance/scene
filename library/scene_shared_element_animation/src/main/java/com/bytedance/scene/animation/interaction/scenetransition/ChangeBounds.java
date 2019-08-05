package com.bytedance.scene.animation.interaction.scenetransition;

import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Property;
import android.view.View;

import com.bytedance.scene.animation.interaction.progressanimation.AnimatorFactory;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.utils.SceneViewCompatUtils;

/**
 * Created by JiangQi on 9/2/18.
 * The purpose of this class is to change the size only.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ChangeBounds extends SceneTransition {
    private static final Property<View, Rect> CENTER_BOUNDS = new Property<View, Rect>(Rect.class, "center_bounds") {
        @Override
        public void set(View object, final Rect value) {
            SceneViewCompatUtils.setLeftTopRightBottom(object, value.left, value.top, value.right, value.bottom);
        }

        @Override
        public Rect get(View object) {
            return new Rect(object.getLeft(), object.getTop(), object.getRight(), object.getBottom());
        }
    };

    private Rect mFromRect;
    private Rect mToRect;

    @Override
    public void captureValue(@NonNull View fromView, @NonNull final View toView, @NonNull final View animationView) {
        super.captureValue(fromView, toView, animationView);

        int fromViewWidth = fromView.getWidth();
        int fromViewHeight = fromView.getHeight();

        int toViewWidth = toView.getWidth();
        int toViewHeight = toView.getHeight();

        //返回
        if (fromView == animationView) {
            this.mFromRect = new Rect(fromView.getLeft(), fromView.getTop(), fromView.getLeft() + fromViewWidth, fromView.getTop() + fromViewHeight);
            this.mToRect = new Rect(fromView.getLeft(), fromView.getTop(), fromView.getLeft() + toViewWidth, fromView.getTop() + toViewHeight);
        } else {
            this.mFromRect = new Rect(toView.getLeft(), toView.getTop(), toView.getLeft() + fromViewWidth, toView.getTop() + fromViewHeight);
            this.mToRect = new Rect(toView.getLeft(), toView.getTop(), toView.getLeft() + toViewWidth, toView.getTop() + toViewHeight);
        }
    }

    @Override
    public InteractionAnimation getAnimation(boolean push) {
        if (this.mFromRect.equals(this.mToRect)) {
            return InteractionAnimation.EMPTY;
        }
        AnimatorFactory<View, Rect> animatorFactory = new AnimatorFactory<>(mAnimationView, CENTER_BOUNDS, new com.bytedance.scene.animation.interaction.evaluator.RectEvaluator(), this.mFromRect, this.mToRect, null);
        return animatorFactory.toInteractionAnimation();
    }

    @Override
    public void finish(boolean push) {

    }
}
