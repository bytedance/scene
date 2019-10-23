package com.bytedance.scenedemo.navigation.popinterupt;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.OnBackPressedListener;
import com.bytedance.scenedemo.R;
import com.bytedance.scenedemo.utility.ColorUtil;

import java.util.concurrent.TimeUnit;

/**
 * Created by JiangQi on 8/3/18.
 */
public class PopInterruptScene extends Scene {

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
        name.setText(getNavigationScene().getStackHistory());

        Button btn = getView().findViewById(R.id.btn);
        btn.setVisibility(View.GONE);

        getNavigationScene().addOnBackPressedListener(this, new OnBackPressedListener() {

            long time = 0;

            @Override
            public boolean onBackPressed() {
                if (time == 0 || TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time) > 2) {
                    Toast.makeText(getActivity(), getString(R.string.nav_interrupt_tip), Toast.LENGTH_SHORT).show();
                    time = System.currentTimeMillis();
                    return true;
                }
                return false;
            }
        });
    }
}
