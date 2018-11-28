package com.bytedance.scene.animation.interaction.progressanimation;

import android.annotation.SuppressLint;
import android.os.Build;
import android.support.v4.util.Pair;
import android.support.v4.util.SparseArrayCompat;
import android.util.Property;
import android.view.View;

import java.util.HashMap;
import java.util.Set;

public class ViewAnimationBuilder<T> {
    static final int NONE = 0x0000;
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

    private static SparseArrayCompat<Property<View, Float>> propertySparseArrayCompat = new SparseArrayCompat<>();

    static {
        propertySparseArrayCompat.put(TRANSLATION_X, View.TRANSLATION_X);
        propertySparseArrayCompat.put(TRANSLATION_Y, View.TRANSLATION_Y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            propertySparseArrayCompat.put(TRANSLATION_Z, View.TRANSLATION_Z);
        }
        propertySparseArrayCompat.put(SCALE_X, View.SCALE_X);
        propertySparseArrayCompat.put(SCALE_Y, View.SCALE_Y);
        propertySparseArrayCompat.put(ROTATION, View.ROTATION);
        propertySparseArrayCompat.put(ROTATION_X, View.ROTATION_X);
        propertySparseArrayCompat.put(ROTATION_Y, View.ROTATION_Y);
        propertySparseArrayCompat.put(X, View.X);
        propertySparseArrayCompat.put(Y, View.Y);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            propertySparseArrayCompat.put(Z, View.Z);
        }
        propertySparseArrayCompat.put(ALPHA, View.ALPHA);
    }

    protected View mView;
    private float mEndProgress = 1.0f;

    ViewAnimationBuilder(View view) {
        this.mView = view;
    }

    public T translationX(float fromValue, float toValue) {
        animatePropertyBy(TRANSLATION_X, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T translationXBy(float deltaValue) {
        animatePropertyBy(TRANSLATION_X, getValue(TRANSLATION_X), deltaValue);
        return (T) this;
    }

    public T translationX(float value) {
        animateProperty(TRANSLATION_X, value);
        return (T) this;
    }

    public T translationY(float fromValue, float toValue) {
        animatePropertyBy(TRANSLATION_Y, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T translationYBy(float deltaValue) {
        animatePropertyBy(TRANSLATION_Y, getValue(TRANSLATION_Y), deltaValue);
        return (T) this;
    }

    public T translationY(float value) {
        animateProperty(TRANSLATION_Y, value);
        return (T) this;
    }

    public T translationZ(float fromValue, float toValue) {
        animatePropertyBy(TRANSLATION_Z, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T translationZBy(float deltaValue) {
        animatePropertyBy(TRANSLATION_Z, getValue(TRANSLATION_Z), deltaValue);
        return (T) this;
    }

    public T translationZ(float value) {
        animateProperty(TRANSLATION_Z, value);
        return (T) this;
    }

    public T scaleX(float fromValue, float toValue) {
        animatePropertyBy(SCALE_X, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T scaleXBy(float deltaValue) {
        animatePropertyBy(SCALE_X, getValue(SCALE_X), deltaValue);
        return (T) this;
    }

    public T scaleX(float value) {
        animateProperty(SCALE_X, value);
        return (T) this;
    }

    public T scaleY(float fromValue, float toValue) {
        animatePropertyBy(SCALE_Y, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T scaleYBy(float deltaValue) {
        animatePropertyBy(SCALE_Y, getValue(SCALE_Y), deltaValue);
        return (T) this;
    }

    public T scaleY(float value) {
        animateProperty(SCALE_Y, value);
        return (T) this;
    }

    public T rotation(float fromValue, float toValue) {
        animatePropertyBy(ROTATION, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T rotationBy(float deltaValue) {
        animatePropertyBy(ROTATION, getValue(ROTATION), deltaValue);
        return (T) this;
    }

    public T rotation(float value) {
        animateProperty(ROTATION, value);
        return (T) this;
    }

    public T rotationX(float fromValue, float toValue) {
        animatePropertyBy(ROTATION_X, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T rotationXBy(float deltaValue) {
        animatePropertyBy(ROTATION_X, getValue(ROTATION_X), deltaValue);
        return (T) this;
    }

    public T rotationX(float value) {
        animateProperty(ROTATION_X, value);
        return (T) this;
    }

    public T rotationY(float fromValue, float toValue) {
        animatePropertyBy(ROTATION_Y, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T rotationYBy(float deltaValue) {
        animatePropertyBy(ROTATION_Y, getValue(ROTATION_Y), deltaValue);
        return (T) this;
    }

    public T rotationY(float value) {
        animateProperty(ROTATION_Y, value);
        return (T) this;
    }

    public T x(float fromValue, float toValue) {
        animatePropertyBy(X, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T xBy(float deltaValue) {
        animatePropertyBy(X, getValue(X), deltaValue);
        return (T) this;
    }

    public T x(float value) {
        animateProperty(X, value);
        return (T) this;
    }

    public T Y(float fromValue, float toValue) {
        animatePropertyBy(Y, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T yBy(float deltaValue) {
        animatePropertyBy(Y, getValue(Y), deltaValue);
        return (T) this;
    }

    public T y(float value) {
        animateProperty(Y, value);
        return (T) this;
    }

    public T z(float fromValue, float toValue) {
        animatePropertyBy(Z, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T zBy(float deltaValue) {
        animatePropertyBy(Z, getValue(Z), deltaValue);
        return (T) this;
    }

    public T z(float value) {
        animateProperty(Z, value);
        return (T) this;
    }

    public T alpha(float fromValue, float toValue) {
        animatePropertyBy(ALPHA, fromValue, toValue - fromValue);
        return (T) this;
    }

    public T alphaBy(float deltaValue) {
        animatePropertyBy(ALPHA, getValue(ALPHA), deltaValue);
        return (T) this;
    }

    public T alpha(float value) {
        animateProperty(ALPHA, value);
        return (T) this;
    }

    @SuppressLint("NewApi")
    private float getValue(int propertyConstant) {
        switch (propertyConstant) {
            case TRANSLATION_X:
                return mView.getTranslationX();
            case TRANSLATION_Y:
                return mView.getTranslationY();
            case TRANSLATION_Z:
                return mView.getTranslationZ();
            case ROTATION:
                return mView.getRotation();
            case ROTATION_X:
                return mView.getRotationX();
            case ROTATION_Y:
                return mView.getRotationY();
            case SCALE_X:
                return mView.getScaleX();
            case SCALE_Y:
                return mView.getScaleY();
            case X:
                return mView.getLeft() + mView.getTranslationX();
            case Y:
                return mView.getTop() + mView.getTranslationY();
            case Z:
                return mView.getElevation() + mView.getTranslationZ();
            case ALPHA:
                return mView.getAlpha();
        }
        return 0;
    }

    private void animateProperty(int constantName, float toValue) {
        float fromValue = getValue(constantName);
        float deltaValue = toValue - fromValue;
        animatePropertyBy(constantName, fromValue, deltaValue);
    }

    private HashMap<Property<View, Float>, Pair<Float, Float>> hashMap = new HashMap<>();

    private void animatePropertyBy(int constantName, float fromValue, float deltaValue) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (constantName == TRANSLATION_Z || constantName == Z) {
                return;
            }
        }
        hashMap.put(propertySparseArrayCompat.get(constantName), new Pair<>(fromValue, deltaValue));
    }

    public T endProgress(float endProgress) {
        this.mEndProgress = endProgress;
        return (T) this;
    }

    protected void onProgress(float progress) {
        Set<Property<View, Float>> set = hashMap.keySet();
        for (Property<View, Float> property : set) {
            Pair<Float, Float> value = hashMap.get(property);
            property.set(mView, value.first + (value.second * progress));
        }
    }

    public InteractionAnimation build() {
        return new InteractionAnimation(mEndProgress) {
            @Override
            public void onProgress(float progress) {
                ViewAnimationBuilder.this.onProgress(progress);
            }
        };
    }
}