package com.bytedance.scenedemo.navigation.popto;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.bytedance.scene.Scene;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/2/18.
 */
public class PopToScene_1 extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final Bundle bundle = getArguments();
        int index = 0;
        if (bundle != null) {
            index = bundle.getInt("index");
        }
        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), index));

        Button button = new Button(getActivity());
        button.setText("现在是PopToScene1，点击跳到 PopToScene_2");
        layout.addView(button);
        final int finalIndex = index;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("index", finalIndex + 1);

                getNavigationScene().push(PopToScene_2.class, bundle);
            }
        });

        return layout;
    }
}
