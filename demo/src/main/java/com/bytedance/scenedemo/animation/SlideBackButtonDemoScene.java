package com.bytedance.scenedemo.animation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.animation.interaction.progressanimation.DrawableAnimationBuilder;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimationBuilder;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.view.SlidePercentFrameLayout;
import com.bytedance.scenedemo.AnimationListDemoScene;
import com.bytedance.scenedemo.MainScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/22/18.
 */
public class SlideBackButtonDemoScene extends Scene {
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SlidePercentFrameLayout layout = new SlidePercentFrameLayout(getActivity());
        layout.setFitsSystemWindows(true);
        final Button button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText(R.string.main_anim_btn_ios_anim);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        lp.topMargin = 20;
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        layout.addView(button, lp);

        final InteractionNavigationPopAnimationFactory interactionNavigationPopAnimationFactory = new InteractionNavigationPopAnimationFactory() {

            @Override
            public boolean isSupport(Scene from, Scene to) {
                return true;
            }

            @Override
            protected List<InteractionAnimation> onPopInteraction(Scene from, Scene to) {
                MainScene mainScene = (MainScene) to;
                AnimationListDemoScene animationListDemoScene = mainScene.findSceneByTag("android:switcher:2");

                int[] buttonLocation = new int[2];
                button.getLocationInWindow(buttonLocation);

                int[] buttonLocation2 = new int[2];
                animationListDemoScene.mInteractionButton.getLocationInWindow(buttonLocation2);

                List<InteractionAnimation> a = new ArrayList<>();

                a.add(InteractionAnimationBuilder.with(button).translationXBy(buttonLocation2[0] - buttonLocation[0]).endProgress(0.5f).build());
                a.add(InteractionAnimationBuilder.with(button).translationYBy(buttonLocation2[1] - buttonLocation[1]).endProgress(0.5f).build());
                a.add(DrawableAnimationBuilder.with(getView().getBackground()).alpha(255, 0).endProgress(0.5f).build());
                return a;
            }

            @Override
            protected boolean canExit(float progress) {
                return progress > 0.3f;
            }

            @Override
            protected void onInteractionCancel() {

            }

            @Override
            protected void onInteractionEnd() {
                getNavigationScene().pop(new PopOptions.Builder().setAnimation(new NoAnimationExecutor()).build());
            }
        };
        layout.setCallback(new SlidePercentFrameLayout.Callback() {
            @Override
            public boolean isSupport() {
                return true;
            }

            @Override
            public void onStart() {
                getNavigationScene().pop(interactionNavigationPopAnimationFactory);
                getNavigationScene().convertBackgroundToBlack();
            }

            @Override
            public void onFinish() {
                interactionNavigationPopAnimationFactory.finish();
            }

            @Override
            public void onProgress(float progress) {
                interactionNavigationPopAnimationFactory.updateProgress(progress);
            }
        });
        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

        TextView textView = new TextView(getActivity());
        textView.setPadding(0, 400, 0, 0);
        textView.setText(R.string.anim_ios_interaction_tip);
        textView.setGravity(Gravity.CENTER);
        layout.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return layout;
    }
}