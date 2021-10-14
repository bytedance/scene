package com.bytedance.scene.navigation.utility;

import android.app.Activity;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public class TestActivity extends Activity {
    public FrameLayout mFrameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFrameLayout = new FrameLayout(this);
        setContentView(mFrameLayout);
    }
}