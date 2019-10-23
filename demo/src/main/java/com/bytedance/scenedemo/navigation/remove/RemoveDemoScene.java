package com.bytedance.scenedemo.navigation.remove;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/4/18.
 */
public class RemoveDemoScene extends GroupScene {

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
        name.setText(getNavigationScene().getStackHistory());

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(getString(R.string.nav_remove_scene_in_task_0));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(RemoveDemoScene1.class);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getNavigationScene().remove(RemoveDemoScene.this);
                    }
                }, 3000);
            }
        });
    }

}
