package com.bytedance.scene.ui.utility;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import com.bytedance.scene.ui.R;
import com.bytedance.scene.utlity.ViewIdGenerator;

public class TestAppCompatActivity extends AppCompatActivity {
    public FrameLayout mFrameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat);
        super.onCreate(savedInstanceState);
        mFrameLayout = new FrameLayout(this);
        mFrameLayout.setId(ViewIdGenerator.generateViewId());
        setContentView(mFrameLayout);
    }
}