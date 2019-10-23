package com.bytedance.scenedemo.navigation.forresult;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bytedance.scene.interfaces.PushResultCallback;
import com.bytedance.scene.ui.SceneNavigator;
import com.bytedance.scene.ui.template.SwipeBackGroupScene;
import com.bytedance.scenedemo.R;

/**
 * Created by JiangQi on 9/18/18.
 */
public class ActivityToSceneDemoActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        Button button = new Button(this);
        button.setText("打开Scene");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SceneNavigator(ActivityToSceneDemoActivity.this, R.style.AppTheme)
                        .startScene(TestScene.class, null);
            }
        });
        linearLayout.addView(button);

        button = new Button(this);
        button.setText("立刻打开5个Scene");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 5; i++) {
                    new SceneNavigator(ActivityToSceneDemoActivity.this, R.style.AppTheme)
                            .startScene(TestScene.class, null);
                }
            }
        });
        linearLayout.addView(button);

        button = new Button(this);
        button.setText("间隔2秒慢慢打开5个Scene");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 5; i++) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            new SceneNavigator(ActivityToSceneDemoActivity.this, R.style.AppTheme)
                                    .startScene(TestScene.class, null);
                        }
                    }, i * 2000);
                }
            }
        });
        linearLayout.addView(button);

        button = new Button(this);
        button.setText("打开Scene拿结果");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SceneNavigator(ActivityToSceneDemoActivity.this, R.style.AppTheme)
                        .startSceneForResult(TestScene2.class, null, new PushResultCallback() {
                            @Override
                            public void onResult(@Nullable Object result) {
                                if (result == null) {
                                    Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), result.toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        linearLayout.addView(button);

        setContentView(linearLayout);
    }

    public static class TestScene extends SwipeBackGroupScene {
        @NonNull
        @Override
        protected ViewGroup onCreateSwipeContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            TextView tv = new TextView(requireActivity());
            tv.setText(getNavigationScene().getStackHistory());
            tv.setFitsSystemWindows(true);

            FrameLayout frameLayout = new FrameLayout(requireActivity());
            frameLayout.addView(tv);
            return frameLayout;
        }
    }

    public static class TestScene2 extends SwipeBackGroupScene {
        @NonNull
        @Override
        protected ViewGroup onCreateSwipeContentView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            Button tv = new Button(requireActivity());
            tv.setText("点击返回结果");
            tv.setFitsSystemWindows(true);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getNavigationScene().setResult(TestScene2.this, "结果是1");
                    getNavigationScene().pop();
                }
            });
            FrameLayout frameLayout = new FrameLayout(requireActivity());
            frameLayout.addView(tv);
            return frameLayout;
        }
    }
}
