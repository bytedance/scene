package com.bytedance.scenedemo.navigation.forresult;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by JiangQi on 9/14/18.
 */
public class TestActivityResultActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        finish();
    }
}
