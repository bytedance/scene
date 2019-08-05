package com.bytedance.scene.animation;

import android.os.Build;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.CancellationSignalList;
import com.bytedance.scene.utlity.Utility;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by JiangQi on 7/30/18.
 */
public abstract class NavigationAnimationExecutor {
    protected ViewGroup mAnimationViewGroup;

    public void setAnimationViewGroup(@NonNull ViewGroup viewGroup) {
        this.mAnimationViewGroup = viewGroup;
    }

    public abstract boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to);

    public final void executePushChange(@NonNull final NavigationScene navigationScene,
                                        @NonNull final View rootView,
                                        @NonNull final AnimationInfo fromInfo,
                                        @NonNull final AnimationInfo toInfo,
                                        @NonNull final CancellationSignalList cancellationSignal,
                                        @NonNull final Runnable endAction) {
        navigationScene.requestDisableTouchEvent(true);
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;
        // In the case of pushAndClear, it is possible that the Scene come from has been destroyed.
        if (fromInfo.mSceneState.value < State.VIEW_CREATED.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mAnimationViewGroup.getOverlay().add(fromView);
            } else {
                mAnimationViewGroup.addView(fromView);
            }
        }

        final Runnable pushEndAction = new Runnable() {
            @Override
            public void run() {
                navigationScene.requestDisableTouchEvent(false);

                if (fromInfo.mSceneState.value < State.VIEW_CREATED.value) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        mAnimationViewGroup.getOverlay().remove(fromView);
                    } else {
                        mAnimationViewGroup.removeView(fromView);
                    }
                }
                endAction.run();
            }
        };

        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                pushEndAction.run();
            }
        });

        /*
         * In case of continuous Push, the view of the previous page haven't been layout and has no height and width.
         * Need to guarantee it here, otherwise it will affect the subsequent animation.
         */
        final boolean isFromViewReady = !(fromView.getWidth() == 0 || fromView.getHeight() == 0);
        boolean isToViewReady = !(toView.getWidth() == 0 || toView.getHeight() == 0);
        if (!isFromViewReady || !isToViewReady) {
            final CancellationSignal layoutCancellationSignal = cancellationSignal.getChildCancellationSignal();

            skipDrawUntilViewMeasureReady(rootView, layoutCancellationSignal, new Runnable() {
                @Override
                public void run() {
                    if (!isFromViewReady) {
                        fromView.setVisibility(View.GONE);
                    }

                    if (!layoutCancellationSignal.isCanceled()) {
                        executePushChangeCancelable(fromInfo, toInfo, pushEndAction, cancellationSignal.getChildCancellationSignal());
                    }
                }
            });

            if (!isFromViewReady) {
                fromView.setVisibility(View.VISIBLE);
                fromView.requestLayout();
            }

            if (!isToViewReady) {
                toView.requestLayout();
            }
        } else {
            executePushChangeCancelable(fromInfo, toInfo, pushEndAction, cancellationSignal.getChildCancellationSignal());
        }
    }

    public final void executePopChange(@NonNull final NavigationScene navigationScene,
                                       @NonNull final View rootView,
                                       @NonNull final AnimationInfo fromInfo,
                                       @NonNull final AnimationInfo toInfo,
                                       @NonNull final CancellationSignalList cancellationSignal,
                                       @NonNull final Runnable endAction) {
        navigationScene.requestDisableTouchEvent(true);
        final Runnable popEndAction = new Runnable() {
            @Override
            public void run() {
                navigationScene.requestDisableTouchEvent(false);
                endAction.run();
            }
        };

        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                popEndAction.run();
            }
        });

        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        final boolean isFromViewReady = !(fromView.getWidth() == 0 || fromView.getHeight() == 0);
        boolean isToViewReady = !(toView.getWidth() == 0 || toView.getHeight() == 0);

        if (!isFromViewReady || !isToViewReady) {
            final CancellationSignal layoutCancellationSignal = cancellationSignal.getChildCancellationSignal();

            skipDrawUntilViewMeasureReady(rootView, layoutCancellationSignal, new Runnable() {
                @Override
                public void run() {
                    if (!isFromViewReady) {
                        Utility.removeFromParentView(fromView);
                        fromView.setVisibility(View.GONE);
                    }
                    if (!layoutCancellationSignal.isCanceled()) {
                        executePopChangeCancelable(fromInfo, toInfo, popEndAction, cancellationSignal.getChildCancellationSignal());
                    }
                }
            });

            if (!isFromViewReady) {
                mAnimationViewGroup.addView(fromView);
                fromView.setVisibility(View.VISIBLE);
                fromView.requestLayout();
            }

            if (!isToViewReady) {
                toView.requestLayout();
            }
        } else {
            executePopChangeCancelable(fromInfo, toInfo, popEndAction, cancellationSignal.getChildCancellationSignal());
        }
    }

    public abstract void executePushChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal);

    public abstract void executePopChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal);

    /**
     * When viewTreeObserver.isAlive(), it is possible that ViewRoot has not attachedToWindow before doing animation
     * The original ViewTreeObserver is merged into the ViewTreeObserver of ViewRoot, so this time we need to re-acquire
     * Recurring steps: After Activity NavigationSceneUtility.setup(), do a new Push with new Handler().post().
     */
    private static void skipDrawUntilViewMeasureReady(@NonNull final View rootView,
                                                      @NonNull CancellationSignal cancellationSignal,
                                                      @NonNull final Runnable endAction) {
        if (rootView != rootView.getRootView()) {
            throw new IllegalArgumentException("Need View.getRootView()");
        }
        final ViewTreeObserver viewTreeObserver = rootView.getViewTreeObserver();
        final AtomicBoolean skipDraw = new AtomicBoolean(true);
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                boolean value = skipDraw.get();
                if (!value) {
                    if (viewTreeObserver.isAlive()) {
                        viewTreeObserver.removeOnPreDrawListener(this);
                    } else {
                        rootView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });

        final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.removeGlobalOnLayoutListener(this);
                } else {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                skipDraw.set(false);
                endAction.run();
            }
        };

        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.removeGlobalOnLayoutListener(onGlobalLayoutListener);
                } else {
                    rootView.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
                }
                skipDraw.set(false);
                endAction.run();
            }
        });
        viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener);
    }
}
