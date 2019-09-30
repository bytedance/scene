package com.bytedance.scene.utility;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.FrameLayout;

public class TestActivity extends Activity {
    public FrameLayout mFrameLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFrameLayout = new FrameLayout(this);
        setContentView(mFrameLayout);
    }
}