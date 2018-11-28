package com.bytedance.scenedemo.animation;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/15/18.
 */
public class AnimationResDemoScene extends GroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        final int[] enterAnimationRes = new int[]{
                R.anim.slide_in_from_right,
                R.anim.abc_fade_in,
                R.anim.nav_default_enter_anim,
                android.R.anim.fade_in,
                android.R.anim.slide_in_left
        };

        final List<String> enterAnimationResStr = new ArrayList<>();
        for (int resId : enterAnimationRes) {
            enterAnimationResStr.add(getActivity().getResources().getResourceEntryName(resId));
        }

        final int[] exitAnimationRes = new int[]{
                R.anim.slide_out_to_left,
                R.anim.abc_fade_out,
                R.anim.nav_default_exit_anim,
                android.R.anim.fade_out,
                android.R.anim.slide_out_right
        };

        final List<String> exitAnimationResStr = new ArrayList<>();
        for (int resId : exitAnimationRes) {
            exitAnimationResStr.add(getActivity().getResources().getResourceEntryName(resId));
        }

        TextView name = new TextView(getActivity());
        name.setText("进入动画");
        layout.addView(name);
        final Spinner enterSpinner = new Spinner(getActivity());
        layout.addView(enterSpinner, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, enterAnimationResStr);
        enterSpinner.setAdapter(adapter);

        name = new TextView(getActivity());
        name.setText("退出动画");
        layout.addView(name);

        final Spinner exitSpinner = new Spinner(getActivity());
        layout.addView(exitSpinner, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, exitAnimationResStr);
        exitSpinner.setAdapter(adapter);

        Button button = new Button(getActivity());
        button.setText("打开");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int enter = enterAnimationRes[enterAnimationResStr.indexOf(enterSpinner.getSelectedItem())];
                int exit = exitAnimationRes[exitAnimationResStr.indexOf(exitSpinner.getSelectedItem())];
                getNavigationScene().push(EmptyScene.class, null,
                        new PushOptions.Builder().setAnimation(getActivity(), enter, exit).build());
            }
        });
        return layout;
    }

    public static class EmptyScene extends Scene {

        @NonNull
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = new View(getActivity());
            view.setBackgroundColor(Color.RED);
            return view;
        }
    }
}
