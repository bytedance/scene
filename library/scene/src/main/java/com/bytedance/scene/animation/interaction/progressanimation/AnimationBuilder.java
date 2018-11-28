package com.bytedance.scene.animation.interaction.progressanimation;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class AnimationBuilder {
    public static DDD of(View view) {
        return new DDD(view);
    }

    public static class DDD extends ViewOtherAnimationBuilder<DDD> {
        DDD(View view) {
            super(view);
        }
    }

    public static ImageViewAnimationBuilder of(ImageView view) {
        return new ImageViewAnimationBuilder(view);
    }

    public static TextViewAnimationBuilder of(TextView view) {
        return new TextViewAnimationBuilder(view);
    }

    public static DrawableAnimationBuilder of(Drawable drawable) {
        return new DrawableAnimationBuilder(drawable);
    }
}
