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
