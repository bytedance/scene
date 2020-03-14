package com.bytedance.scene.animation.animatorexecutor;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimatorExecutor;

/**
 * BottomDialog 进入动画
 * @author gtf35
 * 2020/3/14
 */
public class BottomDialogSceneAnimatorExecutor extends NavigationAnimatorExecutor {
    // 动画时长
    private final static long ANIMATION_DURATION = 300;

    /**
     * 在目标 Scene push 时的回调
     *
     * @param fromInfo 要进入目标 Scene 动画信息
     * @param toInfo 要返回目标 Scene 动画信息
     * @return 要做的动画，父类会调用他来实现动画效果
     */
    @NonNull
    @Override
    protected Animator onPushAnimator(AnimationInfo fromInfo, AnimationInfo toInfo) {
        // 当前要返回的 Scene 的根视图
        final View fromView = fromInfo.mSceneView;
        // 当记录下他的高度，底部对话框最开始就要偏移这个高度，来顶出屏幕
        final float fromViewHeight = fromView.getHeight();
        // 当前要进入的 Scene 的根视图
        final View toView = toInfo.mSceneView;
        // 记录下他的 TranslationY ，动画从就运行到这个高度，也就是开发者原来定义的
        float toViewTranslationY = toView.getTranslationY();
        // 构建浮点的 ValueAnimator，一会要返回
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromViewHeight, toViewTranslationY);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                toView.setTranslationY((float)animation.getAnimatedValue());
            }
        });
        // 设置时长
        valueAnimator.setDuration(ANIMATION_DURATION);
        // 设置动画插值器
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        // 返回要做的动画，父类会调用他来实现动画效果
        return valueAnimator;
    }

    /**
     * 在目标 Scene pop 时的回调
     *
     * @param fromInfo 要退出目标 Scene 动画信息
     * @param toInfo 要返回目标 Scene 动画信息
     * @return 要做的动画，父类会调用他来实现动画效果
     */
    @NonNull
    @Override
    protected Animator onPopAnimator(AnimationInfo fromInfo, AnimationInfo toInfo) {
        // 当前要退出的 Scene 的根视图
        final View fromView = fromInfo.mSceneView;
        // 记录下他的 TranslationY ，这是开发者定义的高度，一会增加他，来逐渐顶出屏幕
        final float fromViewTranslationY = fromView.getTranslationY();
        // 当前要返回的 Scene 的根视图
        final View toView = toInfo.mSceneView;
        // 当记录下他的高度，动画运行到这个高度就会被顶出屏幕
        final float toViewHeight = toView.getHeight();
        // 构建浮点的 ValueAnimator，一会要返回
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(fromViewTranslationY, toViewHeight);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                fromView.setTranslationY((float)animation.getAnimatedValue());
            }
        });
        // 设置动画时长
        valueAnimator.setDuration(ANIMATION_DURATION);
        // 设置动画插值器
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        // 返回要做的动画，父类会调用他来实现动画效果
        return valueAnimator;
    }

    /**
     * 判断目标 Scene 或当前 Scene 是否支持该动画
     */
    @Override
    public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
        // 没有不支持的情况....hope
        return true;
    }

    /**
     * 是否禁用父类控制时长
     */
    @Override
    protected boolean disableConfigAnimationDuration() {
        // 自己设置时长，这个慢点比较好看
        return true;
    }
}
