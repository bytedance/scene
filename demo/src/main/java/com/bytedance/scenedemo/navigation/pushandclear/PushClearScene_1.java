package com.bytedance.scenedemo.navigation.pushandclear;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

import org.jetbrains.annotations.NotNull;

/**
 * Created by JiangQi on 8/2/18.
 */
public class PushClearScene_1 extends Scene {

    @NonNull
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getView().setBackgroundColor(ColorUtil.getMaterialColor(getResources(), 2));

        TextView name = getView().findViewById(R.id.name);
        name.setText(getNavigationScene().getStackHistory());

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(getString(R.string.nav_clear_task_btn_1));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(PushClearScene_2.class);
            }
        });
    }

}
