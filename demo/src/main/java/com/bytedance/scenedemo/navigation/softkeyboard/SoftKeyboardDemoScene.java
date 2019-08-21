package com.bytedance.scenedemo.navigation.softkeyboard;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/19/18.
 *
 * Android framework has three soft input policies: SOFT_INPUT_ADJUST_RESIZE, SOFT_INPUT_ADJUST_PAN, SOFT_INPUT_ADJUST_NOTHING
 * Scene container Activity's AndroidManifest.xml android:windowSoftInputMode must be set to <b>adjustPan</b> or <b>adjustResize</b> or <b>adjustNothing</b>,
 * adjustUnspecified flag has bug will cause <code>requireActivity().getWindow().setSoftInputMode</code> not working, then system will
 * pick the best one depending on the contents of the window
 * If you push a AppCompatScene, everything will work fine, if you push a GroupScene, you should set root view's android:fitsSystemWindows to true,
 * otherwise SOFT_INPUT_ADJUST_RESIZE will have problem
 */
public class SoftKeyboardDemoScene extends GroupScene {

    @NonNull
    @Override
    public ViewGroup onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return (ViewGroup) inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));

        TextView name = getView().findViewById(R.id.name);
        name.setText("* Android framework has three soft input policies: SOFT_INPUT_ADJUST_RESIZE, SOFT_INPUT_ADJUST_PAN, SOFT_INPUT_ADJUST_NOTHING\n" +
                " * Scene container Activity's AndroidManifest.xml android:windowSoftInputMode must be set to <b>adjustPan</b> or <b>adjustResize</b> or <b>adjustNothing</b>,\n" +
                " * adjustUnspecified flag has bug will cause <code>requireActivity().getWindow().setSoftInputMode</code> not working, then system will\n" +
                " * pick the best one depending on the contents of the window\n" +
                " * If you push a AppCompatScene, everything will work fine, if you push a GroupScene, you should set root view's android:fitsSystemWindows to true,\n" +
                " * otherwise SOFT_INPUT_ADJUST_RESIZE will have problem");

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.nav_ime_btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(SoftKeyboardResizeScene.class);
            }
        });
    }

    public static void focusAndShowInputMethod(EditText editText) {
        if (editText == null) {
            return;
        }
        editText.requestFocus();
        // In case the filter is not working, setText again.
        editText.setText(editText.getText());
        editText.setSelection(TextUtils.isEmpty(editText.getText()) ? 0 : editText.getText().length());
        ((InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(editText, 0);
    }
}
