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

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by dss886 on 2019-08-06.
 */
public class EmptyResizeScene extends GroupScene {

    private EditText mEditText;

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.nav_ime_problems_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity() != null) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));

        mEditText = getView().findViewById(R.id.edit_text);

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.nav_ime_btn_top);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().pop();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        SoftKeyboardDemoScene.focusAndShowInputMethod(mEditText);
    }
}
