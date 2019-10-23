package com.bytedance.scenedemo.navigation.configuration;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.navigation.ConfigurationChangedListener;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/4/18.
 */
public class ConfigurationDemoScene extends GroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        TextView name = getView().findViewById(R.id.name);
        name.setText(getString(R.string.nav_configuration_tip));

        Button btn = getView().findViewById(R.id.btn);
        btn.setVisibility(View.GONE);

        getNavigationScene().addConfigurationChangedListener(this, new ConfigurationChangedListener() {
            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                Toast.makeText(getApplicationContext(), "onConfigurationChanged", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
