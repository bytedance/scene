package com.bytedance.scenedemo.restore;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.bytedance.scene.Scene;
import com.bytedance.scenedemo.R;

/**
 * 打开开发者选项的不保留活动，然后进入页面，不停点击累加点击数，切到其他App，切回来
 * 看CheckBox和TextView显示是否正确，再点击，看是否累加数正确
 */
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
            text = "销毁恢复启动";
        } else {
            text = "正常启动";
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
        Toast.makeText(requireActivity(), "销毁恢复，View状态恢复完成", Toast.LENGTH_SHORT).show();
    }

    private void showClickCount() {
        mTextView.setText("总共点击次数: " + mClickCount);
    }
}
