package com.bytedance.scenedemo.restore;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.bytedance.scene.Scene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

public class SupportRestoreRootScene extends Scene {
    private CheckBox mCheckBox;
    private TextView mTextView;
    private int mClickCount;

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_support_restore_root_scene, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCheckBox = view.findViewById(R.id.checkbox);
        mTextView = view.findViewById(R.id.textview);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 0));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("mClickCount", mClickCount);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        String text = null;
        if (savedInstanceState != null) {
            text = getString(R.string.case_restore_toast_2);
        } else {
            text = getString(R.string.case_restore_toast_1);
        }
        Toast.makeText(requireActivity(), text, Toast.LENGTH_SHORT).show();

        if (savedInstanceState != null) {
            mClickCount = savedInstanceState.getInt("mClickCount");
        }

        mCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickCount++;
                showClickCount();
            }
        });
        showClickCount();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Toast.makeText(requireActivity(), R.string.case_restore_toast_3, Toast.LENGTH_SHORT).show();
    }

    private void showClickCount() {
        mTextView.setText(getString(R.string.case_restore_click, mClickCount));
    }
}
