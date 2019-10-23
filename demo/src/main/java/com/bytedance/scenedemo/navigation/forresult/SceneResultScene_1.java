package com.bytedance.scenedemo.navigation.forresult;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/3/18.
 */
public class SceneResultScene_1 extends Scene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));

        TextView name = getView().findViewById(R.id.name);
        name.setText(getNavigationScene().getStackHistory());

        final EditText editText = getView().findViewById(R.id.edit_text);
        editText.setVisibility(View.VISIBLE);
        editText.setText(String.valueOf(1234));

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(getString(R.string.nav_result_set_result_btn));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().setResult(SceneResultScene_1.this, editText.getText().toString());
            }
        });
    }
}
