package com.bytedance.scenedemo.navigation.forresult;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytedance.scene.Scene;

/**
 * Created by JiangQi on 8/3/18.
 */
public class SceneResultScene_1 extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getActivity());
        textView.setText(getNavigationScene().getStackHistory());
        layout.addView(textView);

        final EditText editText = new EditText(getActivity());
        editText.setText("1234");
        layout.addView(editText);

        Button button = new Button(getActivity());
        button.setText("SceneResultScene_1，点击设置输入框的数据");
        button.setAllCaps(false);
        layout.addView(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().setResult(SceneResultScene_1.this, editText.getText().toString());
            }
        });

        return layout;
    }
}
