package com.bytedance.scenedemo.extreme_case;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.AnimationInfo;
import com.bytedance.scene.animation.NavigationAnimatorExecutor;
import com.bytedance.scene.animation.TransitionUtils;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.group.EmptyScene;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/5/18.
 */
public class Case3Scene extends GroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        TextView name = getView().findViewById(R.id.name);
        name.setVisibility(View.GONE);

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.case_remove_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder().setAnimation(new AAA()).build());
                getNavigationScene().remove(Case3Scene.this);
            }
        });
    }

    private static class AAA extends NavigationAnimatorExecutor {
        @Override
        public boolean isSupport(@NonNull Class<? extends Scene> from, @NonNull Class<? extends Scene> to) {
            return true;
        }

        @Override
        protected boolean disableConfigAnimationDuration() {
            return true;
        }

        @NonNull
        @Override
        protected Animator onPushAnimator(AnimationInfo from, final AnimationInfo to) {
            final View fromView = from.mSceneView;
            final View toView = to.mSceneView;

            ValueAnimator fromAlphaAnimator = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1.0f, 1.0f);//之前是0.7，但是动画后面会露出NavigationScene的背景色白色很怪异
            fromAlphaAnimator.setInterpolator(new FastOutSlowInInterpolator());
            fromAlphaAnimator.setDuration(120 * 20);

            ValueAnimator toAlphaAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0.0f, 1.0f);
            toAlphaAnimator.setInterpolator(new DecelerateInterpolator(2));
            toAlphaAnimator.setDuration(120 * 20);

            ValueAnimator toTranslateAnimator = ObjectAnimator.ofFloat(toView, View.TRANSLATION_Y, 0.08f * toView.getHeight(), 0);
            toTranslateAnimator.setInterpolator(new DecelerateInterpolator(2.5f));
            toTranslateAnimator.setDuration(200 * 20);
            return TransitionUtils.mergeAnimators(fromAlphaAnimator, toAlphaAnimator, toTranslateAnimator);
        }

        @NonNull
        @Override
        protected Animator onPopAnimator(final AnimationInfo fromInfo, final AnimationInfo toInfo) {
            final View toView = toInfo.mSceneView;
            final View fromView = fromInfo.mSceneView;

            ValueAnimator fromAlphaAnimator = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1.0f, 0.0f);
            fromAlphaAnimator.setInterpolator(new LinearInterpolator());
            fromAlphaAnimator.setDuration(150 * 20);
            fromAlphaAnimator.setStartDelay(50 * 20);

            ValueAnimator fromTranslateAnimator = ObjectAnimator.ofFloat(fromView, View.TRANSLATION_Y, 0, 0.08f * toView.getHeight());
            fromTranslateAnimator.setInterpolator(new AccelerateInterpolator(2));
            fromTranslateAnimator.setDuration(200 * 20);

            ValueAnimator toAlphaAnimator = ObjectAnimator.ofFloat(toView, View.ALPHA, 0.7f, 1.0f);
            toAlphaAnimator.setInterpolator(new LinearOutSlowInInterpolator());
            toAlphaAnimator.setDuration(20 * 20);
            return TransitionUtils.mergeAnimators(fromAlphaAnimator, fromTranslateAnimator, toAlphaAnimator);
        }
    }

}
