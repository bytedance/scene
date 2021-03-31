package com.bytedance.scenedemo.navigation.push_singletop;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bytedance.scene.Scene;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 8/2/18.
 */
public class PushSingleTopScene_1 extends Scene {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.basic_layout, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getActivity() != null) {
            getView().setBackgroundColor(ColorUtil.getMaterialColor(getActivity().getResources(), 2));
        }

        TextView name = getView().findViewById(R.id.name);
        name.setText(getNavigationScene().getStackHistory());

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(getString(R.string.nav_single_top_btn_1));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PushOptions options = new PushOptions.Builder()
                        .setRemovePredicate(new PushOptions.SingleTopPredicate(PushSingleTopScene_1.class))
                        .build();
                getNavigationScene().push(PushSingleTopScene_1.class, null, options);
            }
        });
    }
}
