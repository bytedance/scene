package com.bytedance.scene.ui.template;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.scene.Scene;
import com.bytedance.scene.animation.animatorexecutor.NoAnimationExecutor;
import com.bytedance.scene.animation.interaction.InteractionNavigationPopAnimationFactory;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimation;
import com.bytedance.scene.animation.interaction.progressanimation.InteractionAnimationBuilder;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.OnBackPressedListener;
import com.bytedance.scene.utlity.Utility;
import com.bytedance.scene.view.SlidePercentFrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/29/18.
 * 手势是不支持Pop拦截的
 */
public abstract class SwipeBackGroupScene extends GroupScene {
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    private SlidePercentFrameLayout mSlidePercentFrameLayout;
    private boolean mSwipeEnabled = true;

    @NonNull
    @Override
    public final ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.mSlidePercentFrameLayout = new SlidePercentFrameLayout(requireSceneContext());
        final View backgroundView = new View(requireSceneContext());
        backgroundView.setBackgroundColor(mScrimColor);
        backgroundView.setVisibility(View.GONE);

        View contentView = onCreateSwipeContentView(inflater, container, savedInstanceState);
        if (contentView.getBackground() == null) {
            ViewCompat.setBackground(contentView, Utility.getWindowBackground(requireSceneContext()));
        }

        final FrameLayout swipeContainerView = new FrameLayout(requireSceneContext());
        swipeContainerView.addView(contentView);
        final InteractionNavigationPopAnimationFactory interactionNavigationPopAnimationFactory = new InteractionNavigationPopAnimationFactory() {

            @Override
            public boolean isSupport(Scene from, Scene to) {
                return true;
            }

            @Override
            protected List<InteractionAnimation> onPopInteraction(Scene from, Scene to) {
                List<InteractionAnimation> list = new ArrayList<>();
                list.add(InteractionAnimationBuilder.with(backgroundView).alpha(1.0f, 0.0f).build());
                list.add(InteractionAnimationBuilder.with(swipeContainerView).translationX(0.0f, from.getView().getWidth()).build());
                list.add(InteractionAnimationBuilder.with(to.getView()).translationX(-to.getView().getWidth() / 2, 0).build());
                return list;
            }

            @Override
            protected boolean canExit(float progress) {
                return progress > 0.5f;
            }

            @Override
            protected void onInteractionCancel() {
                ViewCompat.setElevation(swipeContainerView, 0);
                backgroundView.setVisibility(View.GONE);
                SwipeBackGroupScene.this.onSwipeBackCancel();
            }

            @Override
            protected void onInteractionEnd() {
                SwipeBackGroupScene.this.onSwipeBackEnd();
            }
        };

        final OnBackPressedListener onBackPressedListener = new OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                interactionNavigationPopAnimationFactory.forceCancel();
                getNavigationScene().removeOnBackPressedListener(this);
                return true;
            }
        };

        mSlidePercentFrameLayout.setCallback(new SlidePercentFrameLayout.Callback() {
            @Override
            public boolean isSupport() {
                return ((NavigationScene) getNavigationScene()).isInteractionNavigationPopSupport(interactionNavigationPopAnimationFactory);
            }

            @Override
            public void onStart() {
                if (((NavigationScene) getNavigationScene()).pop(interactionNavigationPopAnimationFactory)) {
                    Resources r = getResources();
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, r.getDisplayMetrics());
                    ViewCompat.setElevation(swipeContainerView, px);
                    backgroundView.setVisibility(View.VISIBLE);
                    getNavigationScene().addOnBackPressedListener(SwipeBackGroupScene.this, onBackPressedListener);
                }
            }

            @Override
            public void onFinish() {
                getNavigationScene().removeOnBackPressedListener(onBackPressedListener);
                interactionNavigationPopAnimationFactory.finish();
            }

            @Override
            public void onProgress(float progress) {
                interactionNavigationPopAnimationFactory.updateProgress(progress);
            }
        });
        mSlidePercentFrameLayout.addView(backgroundView);
        mSlidePercentFrameLayout.addView(swipeContainerView);
        mSlidePercentFrameLayout.setSwipeEnabled(this.mSwipeEnabled);
        return mSlidePercentFrameLayout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.mSlidePercentFrameLayout = null;
    }

    @NonNull
    protected abstract ViewGroup onCreateSwipeContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState);

    public void setSwipeEnabled(boolean enabled) {
        if (this.mSwipeEnabled == enabled) {
            return;
        }
        this.mSwipeEnabled = enabled;
        if (this.mSlidePercentFrameLayout != null) {
            this.mSlidePercentFrameLayout.setSwipeEnabled(enabled);
        }
    }

    protected void onSwipeBackEnd() {
        getNavigationScene().pop(new PopOptions.Builder().setAnimation(new NoAnimationExecutor()).build());
    }

    protected void onSwipeBackCancel() {

    }
}

