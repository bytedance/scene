package com.bytedance.scenedemo.animation;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.animation.interaction.interactionanimation.AlphaInteractionAnimation;
import com.bytedance.scene.animation.interaction.interactionanimation.TranslationXInteractionAnimation;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.ui.view.StatusBarView;
import com.bytedance.scene.view.SlidePercentFrameLayout;
import com.bytedance.scenedemo.utility.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/9/18.
 */
public class SlideBackDemoScene extends Scene {
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SlidePercentFrameLayout layout = new SlidePercentFrameLayout(getActivity());

        final View backgroundView = new View(getActivity());
        backgroundView.setBackgroundColor(mScrimColor);

        final LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));
        final InteractionNavigationPopAnimationFactory interactionNavigationPopAnimationFactory = new InteractionNavigationPopAnimationFactory() {

            @Override
            public boolean isSupport(Scene from, Scene to) {
                return true;
            }

            @Override
            protected List<InteractionAnimation> onPopInteraction(Scene from, Scene to) {
                List<InteractionAnimation> list = new ArrayList<>();
                list.add(new AlphaInteractionAnimation(backgroundView, 1.0f, 0.0f, 1.0f));
                list.add(new TranslationXInteractionAnimation(linearLayout, 0.0f, from.getView().getWidth(), 1.0f));
                list.add(new TranslationXInteractionAnimation(to.getView(), -to.getView().getWidth() / 2, 0, 1.0f));
                return list;
            }

            @Override
            protected boolean canExit(float progress) {
                return progress > 0.5f;
            }

            @Override
            protected void onInteractionCancel() {
                ViewCompat.setElevation(linearLayout, 0);
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
                Resources r = getResources();
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                ViewCompat.setElevation(linearLayout, px);
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
        layout.addView(backgroundView);

        StatusBarView statusBarView = new StatusBarView(getActivity());
        linearLayout.addView(statusBarView);
        statusBarView.setStatusBarBackgroundColor(Color.RED);

        Button button = new Button(getActivity());
        button.setText("返回");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().pop();
            }
        });
        linearLayout.addView(button);

        layout.addView(linearLayout);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }
}
