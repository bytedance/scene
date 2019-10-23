package com.bytedance.scenedemo.navigation.forresult;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.interfaces.PushResultCallback;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/3/18.
 */
public class SceneResultScene_0 extends Scene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));

        TextView name = getView().findViewById(R.id.name);
        name.setText(getNavigationScene().getStackHistory());

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(getString(R.string.nav_result_scene_to_scene_btn_0));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SceneResultScene_1.class, null, new PushOptions.Builder().setPushResultCallback(new PushResultCallback() {
                    @Override
                    public void onResult(@Nullable Object result) {
                        if (result != null) {
                            Toast.makeText(getActivity(), result.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build());
            }
        });

        Button btn2 = getView().findViewById(R.id.btn2);
        btn2.setVisibility(View.VISIBLE);
        btn2.setText(getString(R.string.nav_result_scene_to_scene_btn_1));
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SceneResultScene_0.class, null, new PushOptions.Builder().clearTask().setPushResultCallback(new PushResultCallback() {
                    @Override
                    public void onResult(@Nullable Object result) {
                        if (result != null) {
                            Toast.makeText(getActivity(), result.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build());
            }
        });
    }
}
