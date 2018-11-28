package com.bytedance.scenedemo.navigation.configuration;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.ConfigurationChangedListener;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/4/18.
 */
public class ConfigurationDemoScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        Button button = new Button(getActivity());
        button.setText("旋转屏幕");
        layout.addView(button);

        return layout;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getNavigationScene().addConfigurationChangedListener(this, new ConfigurationChangedListener() {
            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                Toast.makeText(getApplicationContext(), "onConfigurationChanged", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
