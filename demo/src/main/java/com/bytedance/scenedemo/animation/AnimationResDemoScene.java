package com.bytedance.scenedemo.animation;

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
import com.bytedance.scenedemo.utility.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 8/15/18.
 */
public class AnimationResDemoScene extends GroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));
        layout.setFitsSystemWindows(true);
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

        LinearLayout.LayoutParams lp;

        TextView name = new TextView(getActivity());
        name.setText(R.string.anim_xml_tip_1);
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 24;
        lp.leftMargin = 30;
        lp.rightMargin = 30;
        layout.addView(name, lp);

        final Spinner enterSpinner = new Spinner(getActivity());
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        layout.addView(enterSpinner, lp);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, enterAnimationResStr);
        enterSpinner.setAdapter(adapter);

        name = new TextView(getActivity());
        name.setText(R.string.anim_xml_tip_2);
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 30;
        lp.rightMargin = 30;
        layout.addView(name, lp);

        final Spinner exitSpinner = new Spinner(getActivity());
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120);
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        layout.addView(exitSpinner, lp);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, exitAnimationResStr);
        exitSpinner.setAdapter(adapter);

        Button button = new Button(getActivity());
        button.setText(R.string.anim_xml_btn);
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        layout.addView(button, lp);
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
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = new View(getActivity());
            view.setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 1));
            return view;
        }
    }
}
