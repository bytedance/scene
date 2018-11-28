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
 * Created by JiangQi on 8/22/18.
 */
public class PopOptions {
    private final NavigationAnimationExecutor mNavigationAnimationExecutor;
    private final Predicate<Scene> mPopUtilPredicate;

    private PopOptions(NavigationAnimationExecutor navigationAnimatorExecutor, Predicate<Scene> popUtilPredicate) {
        this.mNavigationAnimationExecutor = navigationAnimatorExecutor;
        this.mPopUtilPredicate = popUtilPredicate;
    }

    public NavigationAnimationExecutor getNavigationAnimationExecutor() {
        return mNavigationAnimationExecutor;
    }

    public Predicate<Scene> getPopUtilPredicate() {
        return mPopUtilPredicate;
    }

    public static class Builder {
        private NavigationAnimationExecutor mNavigationAnimationExecutor;
        private Predicate<Scene> mPopUtilPredicate;

        public Builder() {
        }

        @NonNull
        public PopOptions.Builder setAnimation(@NonNull NavigationAnimationExecutor navigationAnimationExecutor) {
            this.mNavigationAnimationExecutor = navigationAnimationExecutor;
            return this;
        }

        @NonNull
        public PopOptions.Builder setAnimation(@NonNull Activity activity,
                                               @AnimRes @AnimatorRes int enterAnim,
                                               @AnimRes @AnimatorRes int exitAnim) {
            this.mNavigationAnimationExecutor = new AnimationOrAnimatorResourceExecutor(activity, enterAnim, exitAnim);
            return this;
        }

        public PopOptions.Builder setPopUtilPredicate(Predicate<Scene> predicate) {
            this.mPopUtilPredicate = predicate;
            return this;
        }

        @NonNull
        public PopOptions build() {
            return new PopOptions(this.mNavigationAnimationExecutor, this.mPopUtilPredicate);
        }
    }

    public static class CountUtilPredicate implements Predicate<Scene> {
        private int mCount;

        public CountUtilPredicate(int count) {
            this.mCount = count;
        }

        @Override
        public boolean apply(Scene scene) {
            if (mCount <= 0) {
                return true;
            }
            mCount--;
            return false;
        }
    }
}
