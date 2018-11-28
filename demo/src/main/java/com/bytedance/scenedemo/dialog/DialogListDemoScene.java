package com.bytedance.scenedemo.dialog;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytedance.scene.animation.animatorexecutor.DialogSceneAnimatorExecutor;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scene.navigation.NavigationScene;

/**
 * Created by JiangQi on 8/8/18.
 */
public class DialogListDemoScene extends GroupScene {
    TextView textView;

    @NonNull
    @Override
    public ViewGroup onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        textView = new TextView(getActivity());
        layout.addView(textView);

        Button button = new Button(getActivity());
        button.setText("对话框");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DemoDialogScene.class, null, new PushOptions.Builder()
                        .setTranslucent(true).setAnimation(new DialogSceneAnimatorExecutor()).build());
            }
        });

        button = new Button(getActivity());
        button.setText("对话框，背景暗色");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(DemoDialogWithDimScene.class, null, new PushOptions.Builder()
                        .setTranslucent(true).setAnimation(new DialogSceneAnimatorExecutor()).build());
            }
        });

        button = new Button(getActivity());
        button.setText("BottomSheet 对话框");
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                new TestBottomSheetDialog().show((NavigationScene) getNavigationScene());
            }
        });

        return layout;
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
