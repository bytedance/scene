package com.bytedance.scene.animation.interaction.scenetransition.visiblity;

import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.transitionpropagation.SidePropagation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@TargetApi(21)
public class Slide extends SceneVisibilityTransition {
    private static final String TAG = "Slide";
    private static final TimeInterpolator sDecelerate = new DecelerateInterpolator();
    private static final TimeInterpolator sAccelerate = new AccelerateInterpolator();
    private CalculateSlide mSlideCalculator = sCalculateBottom;
    private @GravityFlag
    int mSlideEdge = Gravity.BOTTOM;
    private float mSlideFraction = 1;

    /**
     * @hide
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Gravity.LEFT, Gravity.TOP, Gravity.RIGHT, Gravity.BOTTOM, Gravity.START, Gravity.END})
    public @interface GravityFlag {
    }

    private interface CalculateSlide {

        /**
         * Returns the translation value for view when it goes out of the scene
         */
        float getGoneX(ViewGroup sceneRoot, View view, float fraction);

        /**
         * Returns the translation value for view when it goes out of the scene
         */
        float getGoneY(ViewGroup sceneRoot, View view, float fraction);
    }

    private static abstract class CalculateSlideHorizontal implements CalculateSlide {

        @Override
        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY();
        }
    }

    private static abstract class CalculateSlideVertical implements CalculateSlide {

        @Override
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX();
        }
    }

    private static final CalculateSlide sCalculateLeft = new CalculateSlideHorizontal() {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX() - sceneRoot.getWidth() * fraction;
        }
    };

    private static final CalculateSlide sCalculateStart = new CalculateSlideHorizontal() {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            final boolean isRtl = sceneRoot.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
            final float x;
            if (isRtl) {
                x = view.getTranslationX() + sceneRoot.getWidth() * fraction;
            } else {
                x = view.getTranslationX() - sceneRoot.getWidth() * fraction;
            }
            return x;
        }
    };

    private static final CalculateSlide sCalculateTop = new CalculateSlideVertical() {
        @Override
        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY() - sceneRoot.getHeight() * fraction;
        }
    };

    private static final CalculateSlide sCalculateRight = new CalculateSlideHorizontal() {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationX() + sceneRoot.getWidth() * fraction;
        }
    };

    private static final CalculateSlide sCalculateEnd = new CalculateSlideHorizontal() {
        @Override
        public float getGoneX(ViewGroup sceneRoot, View view, float fraction) {
            final boolean isRtl = sceneRoot.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
            final float x;
            if (isRtl) {
                x = view.getTranslationX() - sceneRoot.getWidth() * fraction;
            } else {
                x = view.getTranslationX() + sceneRoot.getWidth() * fraction;
            }
            return x;
        }
    };

    private static final CalculateSlide sCalculateBottom = new CalculateSlideVertical() {
        @Override
        public float getGoneY(ViewGroup sceneRoot, View view, float fraction) {
            return view.getTranslationY() + sceneRoot.getHeight() * fraction;
        }
    };

    private int[] mPosition;

    public Slide() {
        this(Gravity.BOTTOM);
    }

    public Slide(int slideEdge) {
        setSlideEdge(slideEdge);
    }

    public void setSlideEdge(@GravityFlag int slideEdge) {
        switch (slideEdge) {
            case Gravity.LEFT:
                mSlideCalculator = sCalculateLeft;
                break;
            case Gravity.TOP:
                mSlideCalculator = sCalculateTop;
                break;
            case Gravity.RIGHT:
                mSlideCalculator = sCalculateRight;
                break;
            case Gravity.BOTTOM:
                mSlideCalculator = sCalculateBottom;
                break;
            case Gravity.START:
                mSlideCalculator = sCalculateStart;
                break;
            case Gravity.END:
                mSlideCalculator = sCalculateEnd;
                break;
            default:
                throw new IllegalArgumentException("Invalid slide direction");
        }
        mSlideEdge = slideEdge;
        SidePropagation propagation = new SidePropagation();
        propagation.setSide(slideEdge);
        setPropagation(propagation);
    }

    @GravityFlag
    public int getSlideEdge() {
        return mSlideEdge;
    }

    @Override
    public void captureValue(@NonNull View view, @NonNull ViewGroup rootView) {
        super.captureValue(view, rootView);
        mPosition = new int[2];
        view.getLocationOnScreen(mPosition);
    }

    @Override
    public InteractionAnimation getAnimation(boolean appear) {
        int[] position = this.mPosition;
        if (appear) {
            float endX = mView.getTranslationX();
            float endY = mView.getTranslationY();
            float startX = mSlideCalculator.getGoneX(mRootView, mView, mSlideFraction);
            float startY = mSlideCalculator.getGoneY(mRootView, mView, mSlideFraction);
            return TranslationAnimationCreator
                    .createAnimation(mView, position[0], position[1],
                            startX, startY, endX, endY, sDecelerate);
        } else {
            float startX = mView.getTranslationX();
            float startY = mView.getTranslationY();
            float endX = mSlideCalculator.getGoneX(mRootView, mView, mSlideFraction);
            float endY = mSlideCalculator.getGoneY(mRootView, mView, mSlideFraction);
            return TranslationAnimationCreator
                    .createAnimation(mView, position[0], position[1],
                            startX, startY, endX, endY, sAccelerate);
        }
    }

    @Override
    public void onFinish(boolean appear) {

    }

    public void setSlideFraction(float slideFraction) {
        mSlideFraction = slideFraction;
    }
}
