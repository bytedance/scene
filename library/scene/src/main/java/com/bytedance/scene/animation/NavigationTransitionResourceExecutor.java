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
