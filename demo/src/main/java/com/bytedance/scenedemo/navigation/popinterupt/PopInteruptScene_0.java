package com.bytedance.scenedemo.navigation.popinterupt;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.Scene;
import com.bytedance.scene.navigation.OnBackPressedListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by JiangQi on 8/3/18.
 */
public class PopInteruptScene_0 extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(getActivity());
        textView.setText(getNavigationScene().getStackHistory());
        layout.addView(textView);

        textView = new TextView(getActivity());
        textView.setText("拦截返回弹Toast");
        layout.addView(textView);

        getNavigationScene().addOnBackPressedListener(this, new OnBackPressedListener() {

            long time = 0;

            @Override
            public boolean onBackPressed() {
                if (time == 0 || TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time) > 2) {
                    Toast.makeText(getActivity(), "再按一次返回", Toast.LENGTH_SHORT).show();
                    time = System.currentTimeMillis();
                    return true;
                }
                return false;
            }
        });

        return layout;
    }
}
