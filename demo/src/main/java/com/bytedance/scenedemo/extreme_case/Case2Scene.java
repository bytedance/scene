package com.bytedance.scenedemo.extreme_case;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.interfaces.PopOptions;
import com.bytedance.scene.interfaces.PushOptions;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.lifecycle.EmptyScene;
import com.bytedance.scenedemo.utility.ColorUtil;

/**
 * Created by JiangQi on 9/5/18.
 */
public class Case2Scene extends GroupScene {

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
        name.setVisibility(View.GONE);

        Button btn = getView().findViewById(R.id.btn);
        btn.setText(R.string.case_pop_many_btn_1);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder().build());
                for (int i = 0; i < 100; i++) {
                    getNavigationScene().pop();
                }
            }
        });

        Button btn2 = getView().findViewById(R.id.btn2);
        btn2.setVisibility(View.VISIBLE);
        btn2.setText(R.string.case_pop_many_btn_2);
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder().build());
                getNavigationScene().pop(new PopOptions.Builder().setPopUtilPredicate(new PopOptions.CountUtilPredicate(100)).build());
            }
        });

        Button btn3 = getView().findViewById(R.id.btn3);
        btn3.setVisibility(View.VISIBLE);
        btn3.setText(R.string.case_pop_many_btn_3);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), R.string.case_pop_many_toast_1, Toast.LENGTH_SHORT).show();
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < 100; i++) {
                            getNavigationScene().pop();
                        }
                        getNavigationScene().push(EmptyScene.class, null, new PushOptions.Builder().build());
                        Toast.makeText(getActivity(), R.string.case_pop_many_toast_2, Toast.LENGTH_SHORT).show();
                    }
                }, 3000);
            }
        });
    }

}
