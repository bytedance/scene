package com.bytedance.scenedemo.navigation.pushandclear;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/2/18.
 */
public class PushClearRootScene extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        Button button = new Button(getActivity());
        button.setText("现在是PushClearRootScene，点击跳到 PushClearScene_0");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushClearScene_0.class);
            }
        });

        return layout;
    }
}

