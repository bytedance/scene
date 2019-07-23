package com.bytedance.scene.animation.interaction.scenetransition.visiblity;

import android.graphics.drawable.Drawable;

import com.bytedance.scene.animation.interaction.progressanimation.AnimationBuilder;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;

public class BackgroundFade extends SceneVisibilityTransition {
    @Override
    public InteractionAnimation getAnimation(boolean appear) {
        if (appear && mView.getBackground() != null) {
            Drawable drawable = mView.getBackground().mutate();
            mView.setBackgroundDrawable(drawable);
            return AnimationBuilder.of(mView.getBackground()).alpha(0, 255).build();
        } else if (!appear && mView.getBackground() != null) {
            Drawable drawable = mView.getBackground().mutate();
            mView.setBackgroundDrawable(drawable);
            return AnimationBuilder.of(mView.getBackground()).alpha(255, 0).build();
        }
        return InteractionAnimation.EMPTY;
    }

    @Override
    public void onFinish(boolean appear) {

    }
}