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

    public boolean forceExecuteImmediately() {
        return false;
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
        //pushAndClear的情况，有可能from的Scene已经被销毁了
        if (fromInfo.mSceneState.value < State.STOPPED.value) {
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

                if (fromInfo.mSceneState.value < State.STOPPED.value) {
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

        //万一连续Push，其实上个页面的View甚至都没layout，没有高宽，会影响后续的动画，需要保证
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


    //viewTreeObserver.isAlive()，有可能做动画之前ViewRoot还没attachToWindow，做完后attachToWindow了
    //原本的ViewTreeObserver被merge到ViewRoot的ViewTreeObserver，所以这个时候需要重新获取
    //复现步骤：Activity NavigationSceneUtility.setup后，new Handler().post 执行新的Push
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
