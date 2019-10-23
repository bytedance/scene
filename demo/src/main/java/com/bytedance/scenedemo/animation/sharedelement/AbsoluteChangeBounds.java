package com.bytedance.scenedemo.animation.sharedelement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;

import androidx.transition.Transition;
import androidx.transition.TransitionValues;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class AbsoluteChangeBounds extends Transition {

    private static final String PROPNAME_WINDOW_X = "android:changeBounds:windowX";
    private static final String PROPNAME_WINDOW_Y = "android:changeBounds:windowY";

    private int[] tempLocation = new int[2];

    private static final Property<Drawable, PointF> DRAWABLE_ORIGIN_PROPERTY =
            new Property<Drawable, PointF>(PointF.class, "boundsOrigin") {
                private Rect mBounds = new Rect();

                @Override
                public void set(Drawable object, PointF value) {
                    object.copyBounds(mBounds);
                    mBounds.offsetTo(Math.round(value.x), Math.round(value.y));
                    object.setBounds(mBounds);
                }

                @Override
                public PointF get(Drawable object) {
                    object.copyBounds(mBounds);
                    return new PointF(mBounds.left, mBounds.top);
                }
            };

    @Override
    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    @Override
    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private void captureValues(TransitionValues values) {
        View view = values.view;
        if (view.isLaidOut() || view.getWidth() != 0 || view.getHeight() != 0) {
            values.view.getLocationInWindow(tempLocation);
            values.values.put(PROPNAME_WINDOW_X, tempLocation[0]);
            values.values.put(PROPNAME_WINDOW_Y, tempLocation[1]);
        }
    }

    @Override
    public Animator createAnimator(final ViewGroup sceneRoot, TransitionValues startValues, TransitionValues endValues) {
        if (startValues == null || endValues == null) {
            return null;
        }

        final View view = endValues.view;
        sceneRoot.getLocationInWindow(tempLocation);
        int startX = (Integer) startValues.values.get(PROPNAME_WINDOW_X) - tempLocation[0];
        int startY = (Integer) startValues.values.get(PROPNAME_WINDOW_Y) - tempLocation[1];
        int endX = (Integer) endValues.values.get(PROPNAME_WINDOW_X) - tempLocation[0];
        int endY = (Integer) endValues.values.get(PROPNAME_WINDOW_Y) - tempLocation[1];
        // TODO: also handle size changes: check bounds and animate size changes
        if (startX != endX || startY != endY) {
            final int width = view.getWidth();
            final int height = view.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            final BitmapDrawable drawable = new BitmapDrawable(bitmap);
            view.setAlpha(0);
            drawable.setBounds(startX, startY, startX + width, startY + height);
            sceneRoot.getOverlay().add(drawable);
            Path topLeftPath = getPathMotion().getPath(startX, startY, endX, endY);
            PropertyValuesHolder origin = PropertyValuesHolder.ofObject(
                    DRAWABLE_ORIGIN_PROPERTY, null, topLeftPath);
            ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(drawable, origin);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    sceneRoot.getOverlay().remove(drawable);
                    view.setAlpha(1);
                }
            });
            return anim;
        }
        return null;
    }

}