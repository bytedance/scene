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
package com.bytedance.scene.animation;

import android.app.Activity;
import android.support.annotation.TransitionRes;
import android.support.transition.Transition;
import android.support.transition.TransitionInflater;

/**
 * Created by JiangQi on 8/15/18.
 */
public class NavigationTransitionResourceExecutor extends NavigationTransitionExecutor {
    private Transition mSharedTransition;
    private Transition mOtherTransition;

    public NavigationTransitionResourceExecutor(Activity activity,
                                                @TransitionRes int shareResId,
                                                @TransitionRes int otherResId) {
        mSharedTransition = TransitionInflater.from(activity).inflateTransition(shareResId);
        mOtherTransition = TransitionInflater.from(activity).inflateTransition(otherResId);
    }

    @Override
    protected Transition getSharedElementTransition() {
        return mSharedTransition;
    }

    @Override
    protected Transition getOthersTransition() {
        return mOtherTransition;
    }
}
