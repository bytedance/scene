package com.bytedance.scenedemo.navigation.softkeyboard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.bytedance.scene.ui.template.AppCompatScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by dss886 on 2019-08-06.
 */
public class SoftKeyboardResizeScene extends AppCompatScene {

    private EditText mEditText;

    @NonNull
    @Override
    protected View onCreateContentView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.nav_ime_problems_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));

        mEditText = getView().findViewById(R.id.edit_text);

        findViewById(R.id.resize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        });

        findViewById(R.id.pan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            }
        });

        findViewById(R.id.nothing).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            }
        });

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.nav_ime_btn_top);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().pop();
            }
        });
        setTitle("SOFT_INPUT_ADJUST");
    }

    @Override
    public void onResume() {
        super.onResume();
        SoftKeyboardDemoScene.focusAndShowInputMethod(mEditText);
    }
}
