package com.bytedance.scenedemo.dialog;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.animation.animatorexecutor.DialogSceneAnimatorExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/8/18.
 */
public class DialogListDemoScene extends GroupScene {

    private TextView textView;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        textView = getView().findViewById(R.id.name);

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.part_dialog_btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DemoDialogScene.class, null, new PushOptions.Builder()
                        .setTranslucent(true).setAnimation(new DialogSceneAnimatorExecutor()).build());
            }
        });

        Button btn2 = getView().findViewById(R.id.btn2);
        btn2.setVisibility(View.VISIBLE);
        btn2.setText(R.string.part_dialog_btn_2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DemoDialogWithDimScene.class, null, new PushOptions.Builder()
                        .setTranslucent(true).setAnimation(new DialogSceneAnimatorExecutor()).build());
            }
        });

        Button btn3 = getView().findViewById(R.id.btn3);
        btn3.setVisibility(View.VISIBLE);
        btn3.setText(R.string.part_dialog_btn_3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        textView.setText(getStateHistory());
    }

    @Override
    public void onResume() {
        super.onResume();
        textView.setText(getStateHistory());
    }

    @Override
    public void onPause() {
        super.onPause();
        textView.setText(getStateHistory());
    }

    @Override
    public void onStop() {
        super.onStop();
        textView.setText(getStateHistory());
    }
}
