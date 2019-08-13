package com.bytedance.scene.ui.template;

import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.ui.GroupSceneUtility;
import com.bytedance.scene.ui.R;

/**
 * Created by JiangQi on 8/24/18.
 */
public abstract class NavigationViewScene extends GroupScene {
    private Toolbar mToolbar;
    private NavigationView mNavigationView;
    private DrawerLayout mDrawerLayout;

    @NonNull
    @Override
    public final ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.scene_navigation_view_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mToolbar = findViewById(R.id.toolbar);
        this.mNavigationView = findViewById(R.id.nav_view);
        this.mDrawerLayout = findViewById(R.id.drawer_layout);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), mDrawerLayout, mToolbar, 0, 0);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        this.mNavigationView.inflateMenu(getMenuResId());
        GroupSceneUtility.setupWithNavigationView(this.mDrawerLayout, this.mNavigationView, this, R.id.scene_container, getSceneMap());
        this.mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }

    @MenuRes
    protected abstract int getMenuResId();

    @NonNull
    protected abstract SparseArrayCompat<Scene> getSceneMap();

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public NavigationView getNavigationView() {
        return mNavigationView;
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public void setTitle(CharSequence title) {
        this.mToolbar.setTitle(title);
    }

    public void setTitle(int titleId) {
        setTitle(getText(titleId));
    }
}
