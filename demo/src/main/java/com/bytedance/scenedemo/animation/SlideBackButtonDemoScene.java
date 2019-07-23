package com.bytedance.scenedemo.animation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.animation.interaction.interactionanimation.AlphaDrawableInteractionAnimation;
import com.bytedance.scene.animation.interaction.progressanimation.DrawableAnimationBuilder;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimationBuilder;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.view.SlidePercentFrameLayout;
import com.bytedance.scenedemo.MainListScene;
import com.bytedance.scenedemo.utility.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/22/18.
 */
public class SlideBackButtonDemoScene extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SlidePercentFrameLayout layout = new SlidePercentFrameLayout(getActivity());
        final Button button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText("iOS Interaction 动画");
        layout.addView(button, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        final InteractionNavigationPopAnimationFactory interactionNavigationPopAnimationFactory = new InteractionNavigationPopAnimationFactory() {

            @Override
            public boolean isSupport(Scene from, Scene to) {
                return true;
            }

            @Override
            protected List<InteractionAnimation> onPopInteraction(Scene from, Scene to) {
                MainListScene mainListScene = (MainListScene) to;
                AnimationListDemoScene animationListDemoScene = mainListScene.findSceneByTag("android:switcher:2");

                int[] buttonLocation = new int[2];
                button.getLocationInWindow(buttonLocation);

                int[] buttonLocation2 = new int[2];
                animationListDemoScene.aaaa.getLocationInWindow(buttonLocation2);

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
                ((NavigationScene) getNavigationScene()).pop(interactionNavigationPopAnimationFactory);
                ((NavigationScene) getNavigationScene()).convertBackgroundToBlack();
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
        textView.setPadding(0, 500, 0, 0);
        textView.setText("                             右滑试试");
        layout.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return layout;
    }
}