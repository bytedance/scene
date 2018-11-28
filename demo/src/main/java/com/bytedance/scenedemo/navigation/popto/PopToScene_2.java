package com.bytedance.scenedemo.navigation.popto;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/2/18.
 */
public class PopToScene_2 extends Scene {
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

        TextView textView = new TextView(getActivity());
        textView.setText(getNavigationScene().getStackHistory());
        layout.addView(textView);

        Button button = new Button(getActivity());
        button.setText("现在是PopToScene2，点击返回到 PopToScene_0");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().popTo(PopToScene_0.class);
            }
        });

        Button button2 = new Button(getActivity());
        button2.setText("现在是PopToScene2，点击返回 popToRoot");
        layout.addView(button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().popToRoot();
            }
        });

        return layout;
    }
}
