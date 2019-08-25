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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.animation.interaction.scenetransition.SceneTransition;
import com.bytedance.scene.animation.interaction.scenetransition.utils.SceneViewCompatUtils;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.BackgroundFade;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.SceneVisibilityTransition;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.NonNullPair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JiangQi on 9/2/18.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class SharedElementViewTransitionExecutor {
    private static final int ANIMATION_DURATION = 300;
    @NonNull
    private final Map<String, SceneTransition> mSharedElementTransition;
    @Nullable
    private final SceneVisibilityTransition mOtherTransition;

    /**
     * TODO: Support for excluding certain Views to do otherElementTransition
     */
    public SharedElementViewTransitionExecutor(@NonNull Map<String, SceneTransition> sharedElementTransition,
                                               @Nullable SceneVisibilityTransition otherElementTransition) {
        this.mSharedElementTransition = sharedElementTransition;
        this.mOtherTransition = otherElementTransition;
    }

    private static class Info {
        @NonNull
        View srcView;
        @NonNull
        View dstView;
        @NonNull
        SceneTransition sceneTransition;

        private Info(@NonNull View srcView, @NonNull View dstView, @NonNull SceneTransition sceneTransition) {
            this.srcView = srcView;
            this.dstView = dstView;
            this.sceneTransition = sceneTransition;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void executePushChange(@NonNull final View fromView, @NonNull final View toView,
                                  @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal,
                                  @NonNull SharedElementNotFoundPolicy sharedElementNotFoundPolicy,
                                  @NonNull Runnable fallbackAction) {
        Set<String> keySet = this.mSharedElementTransition.keySet();
        // Sort, don't let parent cover child
        ArrayMap<String, View> map = new ArrayMap<>();
        for (String transitionName : keySet) {
            View dstView = SharedElementUtils.getViewByTransitionName(toView, transitionName, true);
            if (dstView == null) {
                switch (sharedElementNotFoundPolicy) {
                    case ABORT:
                        throw new IllegalArgumentException("cant find " + transitionName + " View");
                    case FALLBACK:
                        fallbackAction.run();
                        return;
                }
            }
            map.put(transitionName, dstView);
        }

        List<NonNullPair<String, View>> list = SharedElementUtils.sortSharedElementList(map);
        final List<Info> infoList = new ArrayList<>();
        List<View> sharedElementViewList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String transitionName = list.get(i).first;

            SceneTransition sceneTransition = this.mSharedElementTransition.get(transitionName);
            View srcView = SharedElementUtils.getViewByTransitionName(fromView, transitionName, true);
            View dstView = list.get(i).second;

            if (srcView == null) {
                switch (sharedElementNotFoundPolicy) {
                    case ABORT:
                        throw new IllegalArgumentException("cant find " + transitionName + " View");
                    case FALLBACK:
                        fallbackAction.run();
                        return;
                }
            }

            infoList.add(new Info(srcView, dstView, sceneTransition));
            sharedElementViewList.add(dstView);
        }

        final List<Animator> animationList = new ArrayList<>();
        for (Info info : infoList) {
            info.sceneTransition.captureValue(info.srcView, info.dstView, info.dstView);
            SharedElementUtils.moveViewToOverlay(info.dstView, (ViewGroup) toView, null);
            animationList.add(info.sceneTransition.getAnimator(true));
            info.srcView.setVisibility(View.INVISIBLE);
        }

        List<View> transitioningViews = SharedElementUtils.captureTransitioningViews(toView, toView);
        transitioningViews = SharedElementUtils.stripOffscreenViews(transitioningViews).first;
        transitioningViews.removeAll(sharedElementViewList);

        if (mOtherTransition != null) {
            List<Animator> mOtherAnimatorList = new ArrayList<>();
            for (View view : transitioningViews) {
                // TODO: What if it is a set operation?
                SceneVisibilityTransition transition = mOtherTransition.clone();
                transition.setDuration(ANIMATION_DURATION);
                transition.captureValue(view, (ViewGroup) toView);
                mOtherAnimatorList.add(transition.getAnimator(true));
            }

            long minDelay = Long.MAX_VALUE;
            for (Animator animator : mOtherAnimatorList) {
                minDelay = Math.min(minDelay, animator.getStartDelay());
            }

            // Guarantee that there must be at least one animator that is 0 delay
            for (Animator animator : mOtherAnimatorList) {
                animator.setStartDelay(animator.getStartDelay() - minDelay);
            }
            animationList.addAll(mOtherAnimatorList);
        }

        if (toView.getBackground() != null) {
            BackgroundFade backgroundFade = new BackgroundFade();
            backgroundFade.captureValue(toView, (ViewGroup) toView);
            animationList.add(backgroundFade.getAnimator(true));
        }

        final Animator valueAnimator = TransitionUtils.mergeAnimators(animationList);
        valueAnimator.setDuration(ANIMATION_DURATION);
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                SceneViewCompatUtils.suppressLayout((ViewGroup) toView.getRootView(), true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                for (Info info : infoList) {
                    info.sceneTransition.finish(true);
                    SharedElementUtils.moveViewFromOverlay(info.dstView);
                    info.dstView.setVisibility(View.VISIBLE);
                    info.srcView.setVisibility(View.VISIBLE);
                }
                SceneViewCompatUtils.suppressLayout((ViewGroup) toView.getRootView(), false);
                endAction.run();
            }
        });
        valueAnimator.start();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                valueAnimator.end();
            }
        });
    }

    public void executePopChange(@NonNull final View fromView, @NonNull final View toView,
                                 @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal,
                                 @NonNull SharedElementNotFoundPolicy sharedElementNotFoundPolicy,
                                 @NonNull Runnable fallbackAction) {
        final List<Info> infoList = new ArrayList<>();
        List<View> sharedElementViewList = new ArrayList<>();
        Set<String> keySet = this.mSharedElementTransition.keySet();
        ArrayMap<String, View> map = new ArrayMap<>();
        for (String transitionName : keySet) {
            View srcView = SharedElementUtils.getViewByTransitionName(fromView, transitionName, true);
            if (srcView == null) {
                switch (sharedElementNotFoundPolicy) {
                    case ABORT:
                        throw new IllegalArgumentException("cant find " + transitionName + " View");
                    case FALLBACK:
                        fallbackAction.run();
                        return;

                }
            }
            map.put(transitionName, srcView);
        }

        List<NonNullPair<String, View>> list = SharedElementUtils.sortSharedElementList(map);
        for (NonNullPair<String, View> pair : list) {
            SceneTransition sceneTransition = this.mSharedElementTransition.get(pair.first);
            View srcView = pair.second;
            View dstView = SharedElementUtils.getViewByTransitionName(toView, pair.first, false);

            if (dstView == null) {
                switch (sharedElementNotFoundPolicy) {
                    case ABORT:
                        throw new IllegalArgumentException("cant find " + pair.first + " View");
                    case FALLBACK:
                        fallbackAction.run();
                        return;
                }
            }

            infoList.add(new Info(srcView, dstView, sceneTransition));
            sharedElementViewList.add(srcView);
        }

        final List<Animator> animationList = new ArrayList<>();
        for (Info info : infoList) {
            info.sceneTransition.captureValue(info.srcView, info.dstView, info.srcView);
            SharedElementUtils.moveViewToOverlay(info.srcView, (ViewGroup) fromView, null);
            animationList.add(info.sceneTransition.getAnimator(false));
            info.dstView.setVisibility(View.INVISIBLE);
        }

        List<View> transitioningViews = SharedElementUtils.captureTransitioningViews(fromView, fromView);
        transitioningViews = SharedElementUtils.stripOffscreenViews(transitioningViews).first;
        transitioningViews.removeAll(sharedElementViewList);

        if (mOtherTransition != null) {
            List<Animator> mOtherAnimatorList = new ArrayList<>();
            for (View view : transitioningViews) {
                SceneVisibilityTransition transition = mOtherTransition.clone();
                transition.setDuration(ANIMATION_DURATION);
                transition.captureValue(view, (ViewGroup) fromView);
                mOtherAnimatorList.add(transition.getAnimator(false));
            }

            long minDelay = Long.MAX_VALUE;
            for (Animator animator : mOtherAnimatorList) {
                minDelay = Math.min(minDelay, animator.getStartDelay());
            }

            // Guarantee that there must be at least one animator that is 0 delay
            for (Animator animator : mOtherAnimatorList) {
                animator.setStartDelay(animator.getStartDelay() - minDelay);
            }
            animationList.addAll(mOtherAnimatorList);
        }

        if (fromView.getBackground() != null) {
            BackgroundFade backgroundFade = new BackgroundFade();
            backgroundFade.captureValue(fromView, (ViewGroup) fromView);
            animationList.add(backgroundFade.getAnimator(false));
        }

        final Animator valueAnimator = TransitionUtils.mergeAnimators(animationList);
        valueAnimator.setDuration(ANIMATION_DURATION);

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                SceneViewCompatUtils.suppressLayout((ViewGroup) toView.getRootView(), true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                for (Info info : infoList) {
                    info.sceneTransition.finish(false);
                    SharedElementUtils.moveViewFromOverlay(info.srcView);
                    info.dstView.setVisibility(View.VISIBLE);
                    info.srcView.setVisibility(View.VISIBLE);
                }
                SceneViewCompatUtils.suppressLayout((ViewGroup) toView.getRootView(), false);
                endAction.run();
            }
        });

        valueAnimator.start();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                valueAnimator.end();
            }
        });
    }
}
