package com.bytedance.scenedemo.group.all;

import android.os.Bundle;
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
import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 8/7/18.
 */
public class AllGroupScene extends GroupScene {
    private DrawerLayout mDrawerLayout;

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.drawerlayout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getActivity().getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                getActivity(), mDrawerLayout, (Toolbar) findViewById(R.id.toolbar), R.string.app_name, R.string.app_name);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        SparseArrayCompat<Scene> sparseArrayCompat = new SparseArrayCompat<>();
        sparseArrayCompat.put(R.id.nav_camera, AllTabScene.newInstance(0));
        sparseArrayCompat.put(R.id.nav_gallery, EmptyScene.newInstance(1));
        sparseArrayCompat.put(R.id.nav_slideshow, EmptyScene.newInstance(2));
        sparseArrayCompat.put(R.id.nav_manage, EmptyScene.newInstance(3));

        final NavigationView navigationView = findViewById(R.id.nav_view);
        GroupSceneUtility.setupWithNavigationView(mDrawerLayout, navigationView, this, R.id.scene_container, sparseArrayCompat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("标题");

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
    }
}
