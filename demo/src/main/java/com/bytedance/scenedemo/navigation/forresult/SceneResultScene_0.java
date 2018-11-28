package com.bytedance.scenedemo.navigation.forresult;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.interfaces.PushResultCallback;

/**
 * Created by JiangQi on 8/3/18.
 */
public class SceneResultScene_0 extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getActivity());
        textView.setText(getNavigationScene().getStackHistory());
        layout.addView(textView);

        Button button = new Button(getActivity());
        button.setText("现在是SceneResultScene_0，点击跳到 SceneResultScene_1 拿结果");
        button.setAllCaps(false);

        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
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

        button = new Button(getActivity());
        button.setText("现在是SceneResultScene_0，点击跳到 SceneResultScene_0 拿结果（PushSingleTop）");
        button.setAllCaps(false);

        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SceneResultScene_1.class, null, new PushOptions.Builder().clearTask().setPushResultCallback(new PushResultCallback() {
                    @Override
                    public void onResult(@Nullable Object result) {
                        if (result != null) {
                            Toast.makeText(getActivity(), result.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).build());
            }
        });

        return layout;
    }
}
