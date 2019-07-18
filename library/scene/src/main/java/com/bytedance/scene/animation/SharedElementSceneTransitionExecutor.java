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
    private NavigationAnimationExecutor mFallbackAnimationExecutor;
    private boolean mDelayEnterTransitionExecute = false;
    private Runnable mEnterTransitionRunnable = null;

    public SharedElementSceneTransitionExecutor(@NonNull Map<String, SceneTransition> sharedElementTransition,
                                                @Nullable SceneVisibilityTransition otherTransition,
                                                @NonNull NavigationAnimationExecutor fallbackAnimationExecutor) {
        this.mSharedElementTransition = sharedElementTransition;
        this.mOtherTransition = otherTransition;
        this.mFallbackAnimationExecutor = fallbackAnimationExecutor;
    }

    public SharedElementSceneTransitionExecutor(Map<String, SceneTransition> sharedElementTransition, SceneVisibilityTransition otherTransition) {
        this(sharedElementTransition, otherTransition, new Android8DefaultSceneAnimatorExecutor());
    }

    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    public void delayEnterTransitionExecute() {
        this.mDelayEnterTransitionExecute = true;
    }

    public void postponeEnterTransition() {
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
            this.mFallbackAnimationExecutor.executePushChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            return;
        }

        executePushChangeV21(fromInfo, toInfo, endAction, cancellationSignal);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void executePushChangeV21(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo
            toInfo, @NonNull final Runnable endAction, @NonNull final CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        fromView.setVisibility(View.VISIBLE);
        final View toView = toInfo.mSceneView;

        final SharedElementViewTransitionExecutor sharedElementViewTransitionExecutor = new SharedElementViewTransitionExecutor(mSharedElementTransition, mOtherTransition);
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
                    }, cancellationSignalListAdapter.getChildCancellationSignal());
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
            }, cancellationSignal);
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
            this.mFallbackAnimationExecutor.executePopChangeCancelable(fromInfo, toInfo, endAction, cancellationSignal);
            return;
        }

        executePopChangeV21(fromInfo, toInfo, endAction, cancellationSignal);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void executePopChangeV21(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo
            toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        mAnimationViewGroup.addView(fromView);
        fromView.setVisibility(View.VISIBLE);

        new SharedElementViewTransitionExecutor(mSharedElementTransition, mOtherTransition).executePopChange(fromView, toView, new Runnable() {
            @Override
            public void run() {
                fromView.setVisibility(View.GONE);
                Utility.removeFromParentView(fromView);
                endAction.run();
            }
        }, cancellationSignal);
    }
}
