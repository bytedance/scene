package com.bytedance.scene.animation;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.transition.Transition;
import android.support.transition.TransitionListenerAdapter;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.Android8DefaultSceneAnimatorExecutor;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.Utility;

/**
 * Created by JiangQi on 8/15/18.
 *
 * TODO: Translucent is not supported now.
 */
public abstract class NavigationTransitionExecutor extends NavigationAnimationExecutor {
    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    protected abstract Transition getSharedElementTransition();

    protected abstract Transition getOthersTransition();

    private NavigationAnimationExecutor mFallbackAnimationExecutor;

    public NavigationTransitionExecutor(NavigationAnimationExecutor fallbackAnimationExecutor) {
        this.mFallbackAnimationExecutor = fallbackAnimationExecutor;
    }

    public NavigationTransitionExecutor() {
        this(new Android8DefaultSceneAnimatorExecutor());
    }

    @Override
    public final void executePushChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        if (fromInfo.mIsTranslucent || toInfo.mIsTranslucent) {
            throw new IllegalArgumentException("SharedElement animation don't support translucent scene");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.mFallbackAnimationExecutor.executePushChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            return;
        }

        executePushChangeV21(fromInfo, toInfo, endAction, cancellationSignal);
    }

    private static void sss(Transition transition, View v) {
        if (transition == null) {
            return;
        }
        if (v instanceof ViewGroup) {
            transition.excludeTarget(v, true);
            for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
                sss(transition, ((ViewGroup) v).getChildAt(i));
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void executePushChangeV21(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;
        final ViewGroup root = (ViewGroup) fromView.getParent();

        fromView.setVisibility(View.VISIBLE);
        toView.setVisibility(View.VISIBLE);

        Utility.removeFromParentView(fromView);
        Utility.removeFromParentView(toView);

        mAnimationViewGroup.addView(fromView);

        final TransitionSet transitionSet = new TransitionSet();
        transitionSet.setDuration(2500);

        Transition share = getSharedElementTransition();
        transitionSet.addTransition(share);

        Transition other = getOthersTransition();
        sss(other, fromView);
        sss(other, toView);

//        other.excludeTarget(fromView, true);
//
//
//        other.excludeTarget(toView, true);

        if (other != null) {
            transitionSet.addTransition(other);
        }
        transitionSet.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);

                Utility.removeFromParentView(fromView);
                Utility.removeFromParentView(toView);

                root.addView(fromView);
                fromView.setVisibility(View.GONE);
                root.addView(toView);
                endAction.run();
            }
        });

        TransitionManager.beginDelayedTransition(mAnimationViewGroup, transitionSet);

        mAnimationViewGroup.removeView(fromView);
        mAnimationViewGroup.addView(toView);
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                TransitionManager.endTransitions(mAnimationViewGroup);
            }
        });
    }

    @Override
    public final void executePopChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        if (fromInfo.mIsTranslucent || toInfo.mIsTranslucent) {
            throw new IllegalArgumentException("SharedElement animation don't support translucent scene");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.mFallbackAnimationExecutor.executePopChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            return;
        }

        executePopChangeV21(fromInfo, toInfo, endAction, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void executePopChangeV21(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;
        final ViewGroup root = (ViewGroup) toView.getParent();

        fromView.setVisibility(View.VISIBLE);
        toView.setVisibility(View.VISIBLE);

        Utility.removeFromParentView(fromView);
        Utility.removeFromParentView(toView);

        mAnimationViewGroup.addView(fromView);

        TransitionSet transitionSet = new TransitionSet();
        transitionSet.setDuration(2500);

        Transition share = getSharedElementTransition();
        transitionSet.addTransition(share);

        Transition other = getOthersTransition();
        sss(other, fromView);
        sss(other, toView);

        if (other != null) {
            transitionSet.addTransition(other);
        }
        transitionSet.addListener(new TransitionListenerAdapter() {
            @Override
            public void onTransitionEnd(Transition transition) {
                super.onTransitionEnd(transition);

                Utility.removeFromParentView(fromView);
                Utility.removeFromParentView(toView);

                root.addView(toView);
                endAction.run();
            }
        });

        TransitionManager.beginDelayedTransition(mAnimationViewGroup, transitionSet);

        mAnimationViewGroup.removeView(fromView);
        mAnimationViewGroup.addView(toView);

        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                TransitionManager.endTransitions(mAnimationViewGroup);
            }
        });
    }
}
