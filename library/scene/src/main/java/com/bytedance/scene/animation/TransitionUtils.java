package com.bytedance.scene.animation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.View;

import java.util.Arrays;
import java.util.List;

public class TransitionUtils {
    public static Animator mergeAnimators(Animator... animator) {
        return mergeAnimators(Arrays.asList(animator));
    }

    public static Animator mergeAnimators(List<Animator> animator) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        return animatorSet;
    }

    public static Bitmap viewToBitmap(View view) {
        if (view.getWidth() == 0 || view.getHeight() == 0) {
            return null;
        }
        final int width = view.getWidth();
        final int height = view.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
}
