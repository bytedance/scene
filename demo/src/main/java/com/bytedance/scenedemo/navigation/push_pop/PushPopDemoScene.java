package com.bytedance.scenedemo.navigation.push_pop;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.OnBackPressedListener;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 7/30/18.
 */
public class PushPopDemoScene extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_scene_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Bundle argument = getArguments();
        int value = 0;
        if (argument != null) {
            value = argument.getInt("1", 0);
        }

        getView().setBackgroundColor(ColorUtil.getMaterialColor(getActivity().getResources(), value));

        TextView name = (TextView) getView().findViewById(R.id.name);
        name.setText(getNavigationScene().getStackHistory());


        Button a = getView().findViewById(R.id.f);
        a.setText("" + value);
        final int finalValue = value;
        a.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putInt("1", finalValue + 1);
                getNavigationScene().push(PushPopDemoScene.class, bundle);
            }
        });

        getNavigationScene().addOnBackPressedListener(this, new OnBackPressedListener() {
            @Override
            public boolean onBackPressed() {
                return false;
            }
        });
    }
}
