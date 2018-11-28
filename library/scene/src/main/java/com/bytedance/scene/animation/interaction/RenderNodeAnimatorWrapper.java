package com.bytedance.scene.animation.interaction;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RenderNodeAnimatorWrapper {
    private final View mView;
    private final ArrayList<NameValuesHolder> mPendingAnimations = new ArrayList<NameValuesHolder>();
    private Runnable mPendingSetupAction;
    private Runnable mPendingCleanupAction;

    private RenderNodeAnimatorWrapper(View view) {
        this.mView = view;
    }

    public static RenderNodeAnimatorWrapper of(@NonNull View view) {
        return new RenderNodeAnimatorWrapper(view);
    }

    private static class ViewPropertyAnimatorConstant {
        private static final int NONE = 0x0000;
        private static final int TRANSLATION_X = 0x0001;
        private static final int TRANSLATION_Y = 0x0002;
        private static final int TRANSLATION_Z = 0x0004;
        private static final int SCALE_X = 0x0008;
        private static final int SCALE_Y = 0x0010;
        private static final int ROTATION = 0x0020;
        private static final int ROTATION_X = 0x0040;
        private static final int ROTATION_Y = 0x0080;
        private static final int X = 0x0100;
        private static final int Y = 0x0200;
        private static final int Z = 0x0400;
        private static final int ALPHA = 0x0800;
    }

    private static class RenderNodeAnimatorConstant {
        public static final int TRANSLATION_X = 0;
        public static final int TRANSLATION_Y = 1;
        public static final int TRANSLATION_Z = 2;
        public static final int SCALE_X = 3;
        public static final int SCALE_Y = 4;
        public static final int ROTATION = 5;
        public static final int ROTATION_X = 6;
        public static final int ROTATION_Y = 7;
        public static final int X = 8;
        public static final int Y = 9;
        public static final int Z = 10;
        public static final int ALPHA = 11;
    }

    private static final SparseIntArray sViewPropertyAnimatorMap = new SparseIntArray(15) {{
        put(ViewPropertyAnimatorConstant.TRANSLATION_X, RenderNodeAnimatorConstant.TRANSLATION_X);
        put(ViewPropertyAnimatorConstant.TRANSLATION_Y, RenderNodeAnimatorConstant.TRANSLATION_Y);
        put(ViewPropertyAnimatorConstant.TRANSLATION_Z, RenderNodeAnimatorConstant.TRANSLATION_Z);
        put(ViewPropertyAnimatorConstant.SCALE_X, RenderNodeAnimatorConstant.SCALE_X);
        put(ViewPropertyAnimatorConstant.SCALE_Y, RenderNodeAnimatorConstant.SCALE_Y);
        put(ViewPropertyAnimatorConstant.ROTATION, RenderNodeAnimatorConstant.ROTATION);
        put(ViewPropertyAnimatorConstant.ROTATION_X, RenderNodeAnimatorConstant.ROTATION_X);
        put(ViewPropertyAnimatorConstant.ROTATION_Y, RenderNodeAnimatorConstant.ROTATION_Y);
        put(ViewPropertyAnimatorConstant.X, RenderNodeAnimatorConstant.X);
        put(ViewPropertyAnimatorConstant.Y, RenderNodeAnimatorConstant.Y);
        put(ViewPropertyAnimatorConstant.Z, RenderNodeAnimatorConstant.Z);
        put(ViewPropertyAnimatorConstant.ALPHA, RenderNodeAnimatorConstant.ALPHA);
    }};

    private static int mapViewPropertyToRenderProperty(int viewProperty) {
        return sViewPropertyAnimatorMap.get(viewProperty);
    }

    public List<Animator> build() {
        Constructor constructor = null;
        Method method = null;
        try {
            Class clazz = Class.forName("android.view.RenderNodeAnimator");
            constructor = clazz.getConstructor(Integer.TYPE, Float.TYPE);
            constructor.setAccessible(true);
            method = clazz.getMethod("setTarget", View.class);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        if (constructor == null || method == null) {
            return new ArrayList<>();
        }

        List<Animator> animatorList = new ArrayList<>();
        AnimatorEventListener animatorEventListener = new AnimatorEventListener();
        for (int i = 0; i < mPendingAnimations.size(); i++) {
            NameValuesHolder holder = mPendingAnimations.get(i);
            int property = mapViewPropertyToRenderProperty(holder.mNameConstant);

            final float finalValue = holder.mFromValue + holder.mDeltaValue;
            try {
                Animator animator = (Animator) constructor.newInstance(property, finalValue);
                method.invoke(animator, mView);
                if (mPendingSetupAction != null || mPendingCleanupAction != null) {
                    animator.addListener(animatorEventListener);
                }
                animatorList.add(animator);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return animatorList;
    }

    private class AnimatorEventListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationStart(Animator animation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mView.setHasTransientState(true);
            }
            if (mPendingSetupAction != null) {
                mPendingSetupAction.run();
                mPendingSetupAction = null;
            }
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mView.setHasTransientState(false);
            }
            if (mPendingCleanupAction != null) {
                mPendingCleanupAction.run();
                mPendingCleanupAction = null;
            }
        }
    }

    public RenderNodeAnimatorWrapper x(float value) {
        animateProperty(ViewPropertyAnimatorConstant.X, value);
        return this;
    }

    public RenderNodeAnimatorWrapper xBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.X, value);
        return this;
    }

    public RenderNodeAnimatorWrapper y(float value) {
        animateProperty(ViewPropertyAnimatorConstant.Y, value);
        return this;
    }

    public RenderNodeAnimatorWrapper yBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.Y, value);
        return this;
    }

    public RenderNodeAnimatorWrapper z(float value) {
        animateProperty(ViewPropertyAnimatorConstant.Z, value);
        return this;
    }

    public RenderNodeAnimatorWrapper zBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.Z, value);
        return this;
    }

    public RenderNodeAnimatorWrapper rotation(float value) {
        animateProperty(ViewPropertyAnimatorConstant.ROTATION, value);
        return this;
    }

    public RenderNodeAnimatorWrapper rotationBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.ROTATION, value);
        return this;
    }

    public RenderNodeAnimatorWrapper rotationX(float value) {
        animateProperty(ViewPropertyAnimatorConstant.ROTATION_X, value);
        return this;
    }

    public RenderNodeAnimatorWrapper rotationXBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.ROTATION_X, value);
        return this;
    }

    public RenderNodeAnimatorWrapper rotationY(float value) {
        animateProperty(ViewPropertyAnimatorConstant.ROTATION_Y, value);
        return this;
    }


    public RenderNodeAnimatorWrapper rotationYBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.ROTATION_Y, value);
        return this;
    }


    public RenderNodeAnimatorWrapper translationX(float value) {
        animateProperty(ViewPropertyAnimatorConstant.TRANSLATION_X, value);
        return this;
    }


    public RenderNodeAnimatorWrapper translationXBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.TRANSLATION_X, value);
        return this;
    }

    public RenderNodeAnimatorWrapper translationY(float value) {
        animateProperty(ViewPropertyAnimatorConstant.TRANSLATION_Y, value);
        return this;
    }

    public RenderNodeAnimatorWrapper translationYBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.TRANSLATION_Y, value);
        return this;
    }

    public RenderNodeAnimatorWrapper translationZ(float value) {
        animateProperty(ViewPropertyAnimatorConstant.TRANSLATION_Z, value);
        return this;
    }

    public RenderNodeAnimatorWrapper translationZBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.TRANSLATION_Z, value);
        return this;
    }

    public RenderNodeAnimatorWrapper scaleX(float value) {
        animateProperty(ViewPropertyAnimatorConstant.SCALE_X, value);
        return this;
    }

    public RenderNodeAnimatorWrapper scaleXBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.SCALE_X, value);
        return this;
    }

    public RenderNodeAnimatorWrapper scaleY(float value) {
        animateProperty(ViewPropertyAnimatorConstant.SCALE_Y, value);
        return this;
    }


    public RenderNodeAnimatorWrapper scaleYBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.SCALE_Y, value);
        return this;
    }


    public RenderNodeAnimatorWrapper alpha(float value) {
        animateProperty(ViewPropertyAnimatorConstant.ALPHA, value);
        return this;
    }


    public RenderNodeAnimatorWrapper alphaBy(float value) {
        animatePropertyBy(ViewPropertyAnimatorConstant.ALPHA, value);
        return this;
    }

    private void animatePropertyBy(int constantName, float byValue) {
        float fromValue = getValue(constantName);
        animatePropertyBy(constantName, fromValue, byValue);
    }

    private void animateProperty(int constantName, float toValue) {
        float fromValue = getValue(constantName);
        float deltaValue = toValue - fromValue;
        animatePropertyBy(constantName, fromValue, deltaValue);
    }

    private void animatePropertyBy(int constantName, float startValue, float byValue) {
        NameValuesHolder nameValuePair = new NameValuesHolder(constantName, startValue, byValue);
        mPendingAnimations.add(nameValuePair);
    }

    static class NameValuesHolder {
        int mNameConstant;
        float mFromValue;
        float mDeltaValue;

        NameValuesHolder(int nameConstant, float fromValue, float deltaValue) {
            mNameConstant = nameConstant;
            mFromValue = fromValue;
            mDeltaValue = deltaValue;
        }
    }

    private float getValue(int propertyConstant) {
        final View node = mView;
        switch (propertyConstant) {
            case ViewPropertyAnimatorConstant.TRANSLATION_X:
                return node.getTranslationX();
            case ViewPropertyAnimatorConstant.TRANSLATION_Y:
                return node.getTranslationY();
            case ViewPropertyAnimatorConstant.TRANSLATION_Z:
                return node.getTranslationZ();
            case ViewPropertyAnimatorConstant.ROTATION:
                return node.getRotation();
            case ViewPropertyAnimatorConstant.ROTATION_X:
                return node.getRotationX();
            case ViewPropertyAnimatorConstant.ROTATION_Y:
                return node.getRotationY();
            case ViewPropertyAnimatorConstant.SCALE_X:
                return node.getScaleX();
            case ViewPropertyAnimatorConstant.SCALE_Y:
                return node.getScaleY();
            case ViewPropertyAnimatorConstant.X:
                return mView.getLeft() + node.getTranslationX();
            case ViewPropertyAnimatorConstant.Y:
                return mView.getTop() + node.getTranslationY();
            case ViewPropertyAnimatorConstant.Z:
                return node.getElevation() + node.getTranslationZ();
            case ViewPropertyAnimatorConstant.ALPHA:
                return mView.getAlpha();
        }
        return 0;
    }

//    public RenderNodeAnimatorWrapper withLayer() {
//        mPendingSetupAction = new Runnable() {
//            @Override
//            public void run() {
//                //这个不能放在Animator的start回调里面，算了，不加了
//                mView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
//                if (mView.isAttachedToWindow()) {
//                    mView.buildLayer();
//                }
//            }
//        };
//        final int currentLayerType = mView.getLayerType();
//        mPendingCleanupAction = new Runnable() {
//            @Override
//            public void run() {
//                mView.setLayerType(currentLayerType, null);
//            }
//        };
//        return this;
//    }
}
