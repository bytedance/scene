package com.bytedance.scenedemo;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;

import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.bytedance.scenedemo.extreme_case.Case0Scene;
import com.bytedance.scenedemo.extreme_case.Case1Scene;
import com.bytedance.scenedemo.extreme_case.Case2Scene;
import com.bytedance.scenedemo.extreme_case.Case3Scene;
import com.bytedance.scenedemo.extreme_case.Case4Scene;
import com.bytedance.scenedemo.extreme_case.Case5Scene;
import com.bytedance.scenedemo.restore.SupportRestoreActivity;

/**
 * Created by JiangQi on 8/21/18.
 */
public class CaseListDemo extends UserVisibleHintGroupScene {
    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView scrollView = new ScrollView(getActivity());

        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(layout);

        addSpace(layout, 12);
        addTitle(layout, getString(R.string.main_title_case));

        addButton(layout, getString(R.string.main_case_destroy_restore), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireNavigationScene().startActivity(new Intent(requireActivity(), SupportRestoreActivity.class));
            }
        });

        addButton(layout, getString(R.string.main_case_push_and_pop), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireNavigationScene().push(Case0Scene.class);
            }
        });

        addButton(layout, getString(R.string.main_case_push_many), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireNavigationScene().push(Case1Scene.class);
            }
        });

        addButton(layout, getString(R.string.main_case_pop_many), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireNavigationScene().push(Case2Scene.class);
            }
        });

        addButton(layout, getString(R.string.main_case_push_pop_remove), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireNavigationScene().push(Case3Scene.class);
            }
        });

        addButton(layout, getString(R.string.main_case_push_pop_in_lifecycle), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireNavigationScene().push(Case4Scene.class);
            }
        });

        addButton(layout, getString(R.string.main_case_add_remove_group_scene), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireNavigationScene().push(Case5Scene.class);
            }
        });

        addButton(layout, getString(R.string.main_case_push_pop_after_finish), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
                requireNavigationScene().push(Case0Scene.EmptyScene.class);
            }
        });

        addSpace(layout, 100);

        return scrollView;
    }

    private void addTitle(LinearLayout parent, String text) {
        TextView textView = new TextView(getActivity());
        textView.setTextSize(14);
        textView.setText(text);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = 30;
        lp.rightMargin = 30;
        lp.topMargin = 24;
        lp.bottomMargin = 24;
        parent.addView(textView, lp);
    }

    private Button addButton(LinearLayout parent, String text, View.OnClickListener onClickListener) {
        Button button = new Button(getActivity());
        button.setAllCaps(false);
        button.setText(text);
        button.setOnClickListener(onClickListener);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150);
        lp.leftMargin = 20;
        lp.rightMargin = 20;
        parent.addView(button, lp);
        return button;
    }

    private void addSpace(LinearLayout parent, int height) {
        Space space = new Space(getActivity());
        parent.addView(space, ViewGroup.LayoutParams.MATCH_PARENT, height);
    }
}
