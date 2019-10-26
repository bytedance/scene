package com.bytedance.scenedemo.benchmark.performance;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 8/21/18.
 */
public class EmptyAppCompatActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View view = new View(this);
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                long a = System.currentTimeMillis() - PerformanceDemo.startTimestamp;
                Toast.makeText(EmptyAppCompatActivity.this, getString(R.string.nav_compare_tip, a), Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        setContentView(view);
    }
}
