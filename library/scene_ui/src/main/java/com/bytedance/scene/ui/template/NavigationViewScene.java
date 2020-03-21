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
package com.bytedance.scene.ui.template;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.OnBackPressedListener;
import com.bytedance.scene.ui.GroupSceneUIUtility;
import com.bytedance.scene.ui.R;
import com.google.android.material.navigation.NavigationView;

import java.util.LinkedHashMap;

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
        GroupSceneUIUtility.setupWithNavigationView(this.mDrawerLayout, this.mNavigationView, this, R.id.scene_container, getSceneMap(),
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        mToolbar.setTitle(menuItem.getTitle());
                        return false;
                    }
                });
        this.mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        requireNavigationScene().addOnBackPressedListener(this, new OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                if (mDrawerLayout.isDrawerOpen(mNavigationView)) {
                    mDrawerLayout.closeDrawer(mNavigationView);
                    return true;
                }
                return false;
            }
        });
    }

    @MenuRes
    protected abstract int getMenuResId();

    @NonNull
    protected abstract LinkedHashMap<Integer, Scene> getSceneMap();

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
