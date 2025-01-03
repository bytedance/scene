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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.res.Resources;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import com.bytedance.scene.utlity.AnimationUtilityKt;
import com.bytedance.scene.utlity.DurationScaleType;

import java.util.List;

@RestrictTo(LIBRARY_GROUP)
public class AnimationOrAnimator {
    public final Animation animation;
    public final Animator animator;
    private OneShotEndAction mEndAction;

    public static AnimationOrAnimator loadAnimation(Activity activity, @AnimatorRes @AnimRes int nextAnim) {
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

    public AnimationOrAnimator(@NonNull Animation animation) {
        this.animation = animation;
        this.animator = null;
        if (animation == null) {
            throw new IllegalStateException("Animation cannot be null");
        }
    }

    public AnimationOrAnimator(@NonNull Animator animator) {
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

    public void addEndAction(@NonNull final Runnable runnable) {
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

    public void reverse() {
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

    /**
     * Scene follow [Settings.Global.ANIMATOR_DURATION_SCALE] as default but Activity follow [Settings.Global.TRANSITION_ANIMATION_SCALE]
     *
     * @param view
     */
    public void applySystemDurationScale(View view) {
        this.applySystemDurationScale(view, Settings.Global.ANIMATOR_DURATION_SCALE);
    }

    public void applySystemDurationScale(View view, @DurationScaleType String durationScaleType) {
        if (this.animation != null) {
            float durationScale = AnimationUtilityKt.getDurationScale(view, durationScaleType);
            this.animation.setDuration((long) (this.animation.getDuration() * durationScale));
        }
    }

    public void start(View view) {
        if (this.animation != null) {
            view.startAnimation(this.animation);
        } else if (this.animator != null) {
            this.animator.setTarget(view);
            this.animator.start();
        }
    }

    public void end() {
        if (this.animation != null) {
            this.animation.cancel();
            this.animation.reset();
            if (this.mEndAction != null) {
                /*
                 * The animation of cancel is not immediately called to onAnimationEnd,
                 * which is be called after a post, so we do some compatible work here.
                 */
                this.mEndAction.run();
            }
        } else if (this.animator != null) {
            this.animator.end();
        }
    }
}
