package com.bytedance.scene.ui.template;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.ui.GroupSceneUtility;
import com.bytedance.scene.utlity.DispatchWindowInsetsListener;
import com.bytedance.scene.ui.R;

/**
 * Created by JiangQi on 8/24/18.
 */
public abstract class BottomNavigationViewScene extends GroupScene {
    private BottomNavigationView mBottomNavigationView;

    @NonNull
    @Override
    public final ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.scene_bottom_navigation_view_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mBottomNavigationView = findViewById(R.id.bottom_navigation);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.scene_container).setOnApplyWindowInsetsListener(new DispatchWindowInsetsListener());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mBottomNavigationView.inflateMenu(getMenuResId());
        GroupSceneUtility.setupWithBottomNavigationView(this.mBottomNavigationView, this, R.id.scene_container, getSceneMap());
    }

    @MenuRes
    protected abstract int getMenuResId();

    @NonNull
    protected abstract SparseArrayCompat<Scene> getSceneMap();

    public BottomNavigationView getBottomNavigationView() {
        return mBottomNavigationView;
    }
}
