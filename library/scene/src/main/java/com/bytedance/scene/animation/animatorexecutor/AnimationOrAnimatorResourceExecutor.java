package com.bytedance.scene.animation.animatorexecutor;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.AnimRes;
import android.support.annotation.AnimatorRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimationExecutor;
import com.bytedance.scene.utlity.AnimatorUtility;
import com.bytedance.scene.utlity.CancellationSignal;

import java.util.List;

/**
 * Created by JiangQi on 8/15/18.
 */
public final class AnimationOrAnimatorResourceExecutor extends NavigationAnimationExecutor {
    private AnimationOrAnimator mEnterAnimator;
    private AnimationOrAnimator mExitAnimator;

    public AnimationOrAnimatorResourceExecutor(Activity activity, @AnimatorRes @AnimRes int enterResId, @AnimatorRes @AnimRes int exitResId) {
        mEnterAnimator = loadAnimation(activity, enterResId);
        mExitAnimator = loadAnimation(activity, exitResId);
    }

    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        return true;
    }

    @Override
    public void executePushChangeCancelable(@NonNull final AnimationInfo fromInfo, @NonNull final AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        //不能放onAnimationStart，因为会有post间隔，会闪屏
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        AnimatorUtility.resetViewStatus(fromView);
        AnimatorUtility.resetViewStatus(toView);
        fromView.setVisibility(View.VISIBLE);

        final float fromViewElevation = ViewCompat.getElevation(fromView);
        if (fromViewElevation > 0) {
            ViewCompat.setElevation(fromView, 0);
        }

        //pushAndClear的情况，有可能from的Scene已经被销毁了
        if (fromInfo.mSceneState.value < State.STOPPED.value) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mAnimationViewGroup.getOverlay().add(fromView);
            } else {
                mAnimationViewGroup.addView(fromView);
            }
        }

        Runnable animationEndAction = new CountRunnable(2, new Runnable() {
            @Override
            public void run() {
                if (!toInfo.mIsTranslucent) {
                    fromView.setVisibility(View.GONE);
                }

                if (fromViewElevation > 0) {
                    ViewCompat.setElevation(fromView, fromViewElevation);
                }

                AnimatorUtility.resetViewStatus(fromView);
                AnimatorUtility.resetViewStatus(toView);

                if (fromInfo.mSceneState.value < State.STOPPED.value) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        mAnimationViewGroup.getOverlay().remove(fromView);
                    } else {
                        mAnimationViewGroup.removeView(fromView);
                    }
                }
                endAction.run();
            }
        });

        mEnterAnimator.addEndAction(animationEndAction);
        mExitAnimator.addEndAction(animationEndAction);

        mExitAnimator.start(fromView);
        mEnterAnimator.start(toView);
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                mEnterAnimator.end();
                mExitAnimator.end();
            }
        });
    }

    @Override
    public void executePopChangeCancelable(@NonNull AnimationInfo fromInfo, @NonNull AnimationInfo toInfo, @NonNull final Runnable endAction, @NonNull CancellationSignal cancellationSignal) {
        final View fromView = fromInfo.mSceneView;
        final View toView = toInfo.mSceneView;

        AnimatorUtility.resetViewStatus(fromView);
        AnimatorUtility.resetViewStatus(toView);

        fromView.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mAnimationViewGroup.getOverlay().add(fromView);
        } else {
            mAnimationViewGroup.addView(fromView);
        }

        Runnable animationEndAction = new CountRunnable(2, new Runnable() {
            @Override
            public void run() {
                //todo child是不是也得reset
                AnimatorUtility.resetViewStatus(fromView);
                AnimatorUtility.resetViewStatus(toView);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mAnimationViewGroup.getOverlay().remove(fromView);
                } else {
                    mAnimationViewGroup.removeView(fromView);
                }
                endAction.run();
            }
        });

        mEnterAnimator.reverse();
        mEnterAnimator.addEndAction(animationEndAction);
        mEnterAnimator.start(fromView);

        mExitAnimator.reverse();
        mExitAnimator.addEndAction(animationEndAction);
        mExitAnimator.start(toView);
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                mEnterAnimator.end();
                mExitAnimator.end();
            }
        });
    }

    private static class CountRunnable implements Runnable {
        int count;
        Runnable runnable;

        private CountRunnable(int count, Runnable runnable) {
            this.count = count;
            this.runnable = runnable;
        }

        @Override
        public void run() {
            count--;
            if (count == 0) {
                runnable.run();
            }
        }
    }

    private static class AnimationOrAnimator {
        public final Animation animation;
        public final Animator animator;
        private OneShotEndAction mEndAction;

        private AnimationOrAnimator(@NonNull Animation animation) {
            this.animation = animation;
            this.animator = null;
            if (animation == null) {
                throw new IllegalStateException("Animation cannot be null");
            }
        }

        private AnimationOrAnimator(@NonNull Animator animator) {
            this.animation = null;
            this.animator = animator;
            if (animator == null) {
                throw new IllegalStateException("Animator cannot be null");
            }
        }

        private static class OneShotEndAction {
            private final Runnable runnable;
            private boolean executed = false;

            private OneShotEndAction(Runnable runnable) {
                if (runnable == null) {
                    throw new IllegalStateException("runnable cannot be null");
                }
                this.runnable = runnable;
            }

            public void run() {
                if (executed) {
                    return;
                }
                this.executed = true;
                this.runnable.run();
            }
        }

        void addEndAction(@NonNull final Runnable runnable) {
            this.mEndAction = new OneShotEndAction(runnable);
            if (this.animation != null) {
                this.animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mEndAction.run();
                        animation.setAnimationListener(null);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            } else if (this.animator != null) {
                this.animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        mEndAction.run();
                        animator.removeListener(this);
                    }
                });
            }
        }

        void reverse() {
            if (this.animation != null) {
                reverse(this.animation);
            } else if (this.animator != null) {
                if (this.animator instanceof ValueAnimator) {
                    ((ValueAnimator) this.animator).reverse();
                }
            }
        }

        private void reverse(Animation animation) {
            Interpolator interpolator = animation.getInterpolator();
            if (interpolator != null) {
                if (interpolator instanceof ReverseInterpolator) {
                    animation.setInterpolator(((ReverseInterpolator) interpolator).delegate);
                } else {
                    animation.setInterpolator(new ReverseInterpolator(interpolator));
                }
            } else {
                animation.setInterpolator(new ReverseInterpolator());
            }

            if (animation instanceof AnimationSet) {
                List<Animation> animationList = ((AnimationSet) animation).getAnimations();
                for (int i = 0; i < animationList.size(); i++) {
                    reverse(animationList.get(i));
                }
            }
        }

        void start(View view) {
            if (this.animation != null) {
                view.startAnimation(this.animation);
            } else if (this.animator != null) {
                this.animator.setTarget(view);
                this.animator.start();
            }
        }

        void end() {
            if (this.animation != null) {
                this.animation.cancel();
                this.animation.reset();
                if (this.mEndAction != null) {
                    this.mEndAction.run();//animation很坑，cancel不是立刻调用到onAnimationEnd，经过了一次post，所以这里做了兼容
                }
            } else if (this.animator != null) {
                this.animator.end();
            }
        }
    }

    static class ReverseInterpolator implements Interpolator {
        final Interpolator delegate;

        ReverseInterpolator(Interpolator delegate) {
            this.delegate = delegate;
        }

        public ReverseInterpolator() {
            this(new LinearInterpolator());
        }

        @Override
        public float getInterpolation(float input) {
            return 1 - delegate.getInterpolation(input);
        }
    }

    private AnimationOrAnimator loadAnimation(Activity activity, @AnimatorRes @AnimRes int nextAnim) {
        String dir = activity.getResources().getResourceTypeName(nextAnim);
        boolean isAnim = "anim".equals(dir);
        boolean successfulLoad = false;
        if (isAnim) {
            // try AnimationUtils first
            try {
                Animation animation = AnimationUtils.loadAnimation(activity, nextAnim);
                if (animation != null) {
                    return new AnimationOrAnimator(animation);
                }
                // A null animation may be returned and that is acceptable
                successfulLoad = true; // succeeded in loading animation, but it is null
            } catch (Resources.NotFoundException e) {
                throw e; // Rethrow it -- the resource should be found if it is provided.
            } catch (RuntimeException e) {
                // Other exceptions can occur when loading an Animator from AnimationUtils.
            }
        }
        if (!successfulLoad) {
            // try Animator
            try {
                Animator animator = AnimatorInflater.loadAnimator(activity, nextAnim);
                if (animator != null) {
                    return new AnimationOrAnimator(animator);
                }
            } catch (RuntimeException e) {
                if (isAnim) {
                    // Rethrow it -- we already tried AnimationUtils and it failed.
                    throw e;
                }
                // Otherwise, it is probably an animation resource
                Animation animation = AnimationUtils.loadAnimation(activity, nextAnim);
                if (animation != null) {
                    return new AnimationOrAnimator(animation);
                }
            }
        }

        throw new IllegalArgumentException("resource is error");
    }
}
