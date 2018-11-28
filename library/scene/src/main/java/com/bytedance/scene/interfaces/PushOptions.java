package com.bytedance.scene.interfaces;

import android.app.Activity;
import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.animation.animatorexecutor.AnimationOrAnimatorResourceExecutor;
import com.bytedance.scene.utlity.Predicate;

/**
 * Created by JiangQi on 8/8/18.
 */
public class PushOptions {
    private final NavigationAnimationExecutor mNavigationAnimationExecutor;
    private final PushResultCallback mPushResultCallback;
    private final boolean mIsTranslucent;//todo 半透明和clearTask不能一起

    private final Predicate<Scene> mRemovePredicate;

    private PushOptions(Predicate<Scene> removePredicate, boolean isTranslucent, PushResultCallback pushResultCallback, NavigationAnimationExecutor navigationAnimationExecutor) {
        this.mRemovePredicate = removePredicate;
        this.mIsTranslucent = isTranslucent;
        this.mPushResultCallback = pushResultCallback;
        this.mNavigationAnimationExecutor = navigationAnimationExecutor;
    }

    public Predicate<Scene> getRemovePredicate() {
        return this.mRemovePredicate;
    }

    public boolean isIsTranslucent() {
        return this.mIsTranslucent;
    }

    public NavigationAnimationExecutor getNavigationAnimationFactory() {
        return this.mNavigationAnimationExecutor;
    }

    public PushResultCallback getPushResultCallback() {
        return this.mPushResultCallback;
    }

    public static class Builder {
        private boolean mIsTranslucent = false;//todo 半透明和clearTask不能一起
        private PushResultCallback mPushResultCallback;
        private NavigationAnimationExecutor mNavigationAnimationExecutor;
        private Predicate<Scene> mRemovePredicate;

        public Builder() {
        }

        @NonNull
        public Builder setPushResultCallback(PushResultCallback mPushResultCallback) {
            this.mPushResultCallback = mPushResultCallback;
            return this;
        }

        @NonNull
        public Builder setTranslucent(boolean isTranslucent) {
            this.mIsTranslucent = isTranslucent;
            return this;
        }

        @NonNull
        public Builder clearCurrent() {
            return setRemovePredicate(new ReplacePredicate());
        }

        @NonNull
        public Builder clearTask() {
            return setRemovePredicate(new ClearTaskPredicate());
        }

        @NonNull
        public Builder setAnimation(@NonNull NavigationAnimationExecutor navigationAnimationExecutor) {
            this.mNavigationAnimationExecutor = navigationAnimationExecutor;
            return this;
        }

        @NonNull
        public Builder setAnimation(@NonNull Activity activity,
                                    @AnimRes @AnimatorRes int enterAnim,
                                    @AnimRes @AnimatorRes int exitAnim) {
            this.mNavigationAnimationExecutor = new AnimationOrAnimatorResourceExecutor(activity, enterAnim, exitAnim);
            return this;
        }

        public Builder setRemovePredicate(Predicate<Scene> predicate) {
            this.mRemovePredicate = predicate;
            return this;
        }

        @NonNull
        public PushOptions build() {
            return new PushOptions(this.mRemovePredicate, this.mIsTranslucent, this.mPushResultCallback, this.mNavigationAnimationExecutor);
        }
    }

    //场景：比如微信朋友圈选图后进入写动态页面，那么原本的选图几个页面应该一并干掉
    public static class CountPredicate implements Predicate<Scene> {
        private int mCount;

        public CountPredicate(int count) {
            this.mCount = count;
        }

        @Override
        public boolean apply(Scene scene) {
            if (mCount <= 0) {
                return false;
            }
            mCount--;
            return true;
        }
    }

    //场景：Splash跳首页或者走完注册登录流程后跳首页
    public static class ClearTaskPredicate implements Predicate<Scene> {
        @Override
        public boolean apply(Scene scene) {
            return true;
        }
    }

    public static class ReplacePredicate extends CountPredicate {
        public ReplacePredicate() {
            super(1);
        }
    }

    public static class SingleTopPredicate implements Predicate<Scene> {
        private Class<? extends Scene> clazz;
        private boolean finish = false;

        public SingleTopPredicate(Class<? extends Scene> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean apply(Scene scene) {
            if (!finish && scene.getClass() == clazz) {
                finish = true;
                return true;
            } else {
                finish = true;
            }
            return false;
        }
    }

    public static class SingleTaskPredicate implements Predicate<Scene> {
        private Class<? extends Scene> clazz;

        public SingleTaskPredicate(Class<? extends Scene> clazz) {
            this.clazz = clazz;
        }

        @Override
        public boolean apply(Scene scene) {
            return scene.getClass() == clazz;
        }
    }
}
