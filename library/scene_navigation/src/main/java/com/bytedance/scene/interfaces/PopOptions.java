/*
 * Copyright (C) 2019 ByteDance Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.scene.interfaces;

import android.app.Activity;

import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;

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
    private final boolean mUsePost;
    private final boolean mUsePostWhenPause;
    private final boolean mUseIdleWhenStop;
    private final boolean mStopAfterAnimation;

    private PopOptions(NavigationAnimationExecutor navigationAnimatorExecutor, Predicate<Scene> popUtilPredicate, boolean usePost, boolean usePostWhenPause, boolean useIdleWhenStop, boolean mStopAfterAnimation) {
        this.mNavigationAnimationExecutor = navigationAnimatorExecutor;
        this.mPopUtilPredicate = popUtilPredicate;
        this.mUsePost = usePost;
        this.mUsePostWhenPause = usePostWhenPause;
        this.mUseIdleWhenStop = useIdleWhenStop;
        this.mStopAfterAnimation = mStopAfterAnimation;
    }

    public NavigationAnimationExecutor getNavigationAnimationExecutor() {
        return mNavigationAnimationExecutor;
    }

    public Predicate<Scene> getPopUtilPredicate() {
        return mPopUtilPredicate;
    }

    public boolean isUsePost() {
        return this.mUsePost;
    }

    public boolean isUsePostWhenPause() {
        return this.mUsePostWhenPause;
    }

    public boolean isUseIdleWhenStop() {
        return this.mUseIdleWhenStop;
    }

    public boolean isStopAfterAnimation() { return this.mStopAfterAnimation; }

    public static class Builder {
        private NavigationAnimationExecutor mNavigationAnimationExecutor;
        private Predicate<Scene> mPopUtilPredicate;
        private boolean mUsePost;
        private boolean mUsePostWhenPause = false;
        private boolean mUseIdleWhenStop = false;
        private boolean mStopAfterAnimation = false;

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
        public PopOptions.Builder setUsePost(boolean usePost) {
            this.mUsePost = usePost;
            return this;
        }

        @NonNull
        public PopOptions.Builder setUsePostWhenPause(boolean usePostWhenPause) {
            this.mUsePostWhenPause = usePostWhenPause;
            return this;
        }

        @NonNull
        public Builder setUseIdleWhenStop(boolean useIdleWhenStop) {
            this.mUseIdleWhenStop = useIdleWhenStop;
            return this;
        }

        @NonNull
        public Builder setStopAfterAnimation(boolean stopAfterAnimation) {
            this.mStopAfterAnimation = stopAfterAnimation;
            return this;
        }

        @NonNull
        public PopOptions build() {
            return new PopOptions(this.mNavigationAnimationExecutor, this.mPopUtilPredicate, this.mUsePost, this.mUsePostWhenPause, this.mUseIdleWhenStop, this.mStopAfterAnimation);
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
