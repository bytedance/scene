package com.bytedance.scene.ui.template;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.bytedance.scene.ui.view.NavigationBarView;
import com.bytedance.scene.ui.view.StatusBarView;
import com.bytedance.scene.ui.R;

/**
 * Created by JiangQi on 8/29/18.
 */
abstract class SwipeBackAppCompatScene extends SwipeBackGroupScene {
    private Toolbar mToolbar;
    private StatusBarView mStatusBarView;
    private NavigationBarView mNavigationBarView;
    private FrameLayout mContentLayout;

    @NonNull
    @Override
    protected final ViewGroup onCreateSwipeContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.scene_appcompat_layout, container, false);
        this.mToolbar = view.findViewById(R.id.scene_toolbar);
        this.mContentLayout = view.findViewById(R.id.scene_content);
        this.mStatusBarView = view.findViewById(R.id.scene_status_bar);
        this.mNavigationBarView = view.findViewById(R.id.scene_navigation_bar);

        View contentView = onCreateContentView(inflater, this.mContentLayout, savedInstanceState);
        if (contentView != null) {
            this.mContentLayout.addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        Resources r = getResources();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, r.getDisplayMetrics());
        ViewCompat.setElevation(this.mToolbar, px);
        return view;
    }

    @Nullable
    protected abstract View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState);

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().pop();
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Activity activity = this.requireActivity();
            Window window = activity.getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
    }

    public void setTitle(@Nullable CharSequence title) {
        this.mToolbar.setTitle(title);
    }

    public void setTitle(@StringRes int titleId) {
        setTitle(getText(titleId));
    }

    @Nullable
    public Toolbar getToolbar() {
        return this.mToolbar;
    }

    @Nullable
    public StatusBarView getStatusBarView() {
        return this.mStatusBarView;
    }

    @Nullable
    public NavigationBarView getNavigationBarView() {
        return this.mNavigationBarView;
    }

    public void setToolbarVisible(boolean visible) {
        this.mToolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setStatusBarVisible(boolean visible) {
        this.mStatusBarView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setNavigationBarVisible(boolean visible) {
        this.mNavigationBarView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}

