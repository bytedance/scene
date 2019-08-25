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

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.Android8DefaultSceneAnimatorExecutor;
import com.bytedance.scene.animation.interaction.scenetransition.SceneTransition;
import com.bytedance.scene.animation.interaction.scenetransition.visiblity.SceneVisibilityTransition;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.CancellationSignalList;
import com.bytedance.scene.utlity.Utility;

import java.util.Map;

/**
 * Created by JiangQi on 9/2/18.
 */
public class SharedElementSceneTransitionExecutor extends NavigationAnimationExecutor {
    @NonNull
    private final Map<String, SceneTransition> mSharedElementTransition;
    @Nullable
    private final SceneVisibilityTransition mOtherTransition;
    @NonNull
    private SharedElementNotFoundPolicy mSharedElementNotFoundPolicy;
    @NonNull
    private final NavigationAnimationExecutor mFallbackAnimationExecutor;
    private boolean mDelayEnterTransitionExecute = false;
    private Runnable mEnterTransitionRunnable = null;

    public SharedElementSceneTransitionExecutor(@NonNull Map<String, SceneTransition> sharedElementTransition,
                                                @Nullable SceneVisibilityTransition otherTransition,
                                                @NonNull NavigationAnimationExecutor fallbackAnimationExecutor,
                                                @NonNull SharedElementNotFoundPolicy sharedElementNotFoundPolicy) {
        this.mSharedElementTransition = sharedElementTransition;
        this.mOtherTransition = otherTransition;
        this.mSharedElementNotFoundPolicy = sharedElementNotFoundPolicy;
        this.mFallbackAnimationExecutor = fallbackAnimationExecutor;
    }

    public SharedElementSceneTransitionExecutor(@NonNull Map<String, SceneTransition> sharedElementTransition,
                                                @Nullable SceneVisibilityTransition otherTransition,
                                                @NonNull NavigationAnimationExecutor fallbackAnimationExecutor) {
        this(sharedElementTransition, otherTransition, fallbackAnimationExecutor, SharedElementNotFoundPolicy.FALLBACK);
    }

    public SharedElementSceneTransitionExecutor(Map<String, SceneTransition> sharedElementTransition, SceneVisibilityTransition otherTransition) {
        this(sharedElementTransition, otherTransition, new Android8DefaultSceneAnimatorExecutor());
    }

    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    public void setSharedElementNotFoundPolicy(@NonNull SharedElementNotFoundPolicy sharedElementNotFoundPolicy) {
        this.mSharedElementNotFoundPolicy = sharedElementNotFoundPolicy;
    }

    public void postponeEnterTransition() {
        this.mDelayEnterTransitionExecute = true;
    }

    public void startPostponedEnterTransition() {
        if (this.mEnterTransitionRunnable != null) {
            this.mEnterTransitionRunnable.run();
            this.mEnterTransitionRunnable = null;
        }
    }

    @Override
    public final void executePushChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull final CancellationSignal cancellationSignal) {
        if (fromInfo.mIsTranslucent || toInfo.mIsTranslucent) {
            throw new IllegalArgumentException("SharedElement animation don't support translucent scene");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.mFallbackAnimationExecutor.setAnimationViewGroup(mAnimationViewGroup);
            this.mFallbackAnimationExecutor.executePushChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            return;
        }

        executePushChangeV21(fromInfo, toInfo, endAction, cancellationSignal);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void executePushChangeV21(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo
            toInfo, @NonNull final Runnable endAction, @NonNull final CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        fromView.setVisibility(View.VISIBLE);
        final View toView = toInfo.mSceneView;

        final SharedElementViewTransitionExecutor sharedElementViewTransitionExecutor = new SharedElementViewTransitionExecutor(mSharedElementTransition, mOtherTransition);
        final Runnable fallbackAction = new Runnable() {
            @Override
            public void run() {
                mFallbackAnimationExecutor.setAnimationViewGroup(mAnimationViewGroup);
                mFallbackAnimationExecutor.executePushChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            }
        };

        if (this.mDelayEnterTransitionExecute) {
            toView.setVisibility(View.INVISIBLE);
            final CancellationSignalList cancellationSignalListAdapter = new CancellationSignalList();
            this.mEnterTransitionRunnable = new Runnable() {
                @Override
                public void run() {
                    toView.setVisibility(View.VISIBLE);
                    sharedElementViewTransitionExecutor.executePushChange(fromView, toView, new Runnable() {
                        @Override
                        public void run() {
                            fromView.setVisibility(View.GONE);
                            endAction.run();
                        }
                    }, cancellationSignalListAdapter.getChildCancellationSignal(), mSharedElementNotFoundPolicy, fallbackAction);
                }
            };
            cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                @Override
                public void onCancel() {
                    mEnterTransitionRunnable = null;
                    toView.setVisibility(View.VISIBLE);
                    fromView.setVisibility(View.GONE);
                    cancellationSignalListAdapter.cancel();
                }
            });
        } else {
            sharedElementViewTransitionExecutor.executePushChange(fromView, toView, new Runnable() {
                @Override
                public void run() {
                    fromView.setVisibility(View.GONE);
                    endAction.run();
                }
            }, cancellationSignal, mSharedElementNotFoundPolicy, fallbackAction);
        }
    }

    @Override
    public final void executePopChangeCancelable(@NonNull AnimationInfo
                                                         fromInfo, @NonNull AnimationInfo toInfo, @NonNull final Runnable endAction,
                                                 @NonNull CancellationSignal cancellationSignal) {
        if (fromInfo.mIsTranslucent || toInfo.mIsTranslucent) {
            throw new IllegalArgumentException("SharedElement animation don't support translucent scene");
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            this.mFallbackAnimationExecutor.setAnimationViewGroup(mAnimationViewGroup);
            this.mFallbackAnimationExecutor.executePopChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            return;
        }

        executePopChangeV21(fromInfo, toInfo, endAction, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void executePopChangeV21(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo
            toInfo, @NonNull final Runnable endAction, @NonNull final CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        mAnimationViewGroup.addView(fromView);
        fromView.setVisibility(View.VISIBLE);

        final Runnable fallbackAction = new Runnable() {
            @Override
            public void run() {
                mFallbackAnimationExecutor.setAnimationViewGroup(mAnimationViewGroup);
                mFallbackAnimationExecutor.executePopChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            }
        };

        new SharedElementViewTransitionExecutor(mSharedElementTransition, mOtherTransition).executePopChange(fromView, toView, new Runnable() {
            @Override
            public void run() {
                fromView.setVisibility(View.GONE);
                Utility.removeFromParentView(fromView);
                endAction.run();
            }
        }, cancellationSignal, mSharedElementNotFoundPolicy, fallbackAction);
    }
}
