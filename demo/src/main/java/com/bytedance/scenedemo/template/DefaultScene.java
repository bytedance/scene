package com.bytedance.scenedemo.template;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 8/24/18.
 */
public class DefaultScene extends AppCompatScene {

    @Nullable
    @Override
    protected View onCreateContentView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return null;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setTitle("AppCompatScene");

        getToolbar().inflateMenu(R.menu.main_activity_drawer);

    }
}
