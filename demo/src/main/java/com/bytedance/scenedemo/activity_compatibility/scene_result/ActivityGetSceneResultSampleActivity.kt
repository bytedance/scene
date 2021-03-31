package com.bytedance.scenedemo.activity_compatibility.scene_result

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import com.bytedance.scenedemo.R
import com.bytedance.scene.ui.SceneNavigator
import com.bytedance.scene.interfaces.PushResultCallback
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.bytedance.scene.Scene

/**
 * Created by JiangQi on 9/18/18.
 */
class ActivityGetSceneResultSampleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        var button = Button(this)
        //        button.setText("打开Scene");
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new SceneNavigator(ActivityGetSceneResultSampleActivity.this, R.style.AppTheme)
//                        .startScene(TestScene.class, null);
//            }
//        });
//        linearLayout.addView(button);
//
//        button = new Button(this);
//        button.setText("立刻打开5个Scene");
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (int i = 0; i < 5; i++) {
//                    new SceneNavigator(ActivityGetSceneResultSampleActivity.this, R.style.AppTheme)
//                            .startScene(TestScene.class, null);
//                }
//            }
//        });
//        linearLayout.addView(button);
//
//        button = new Button(this);
//        button.setText("间隔2秒慢慢打开5个Scene");
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                for (int i = 0; i < 5; i++) {
//                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            new SceneNavigator(ActivityGetSceneResultSampleActivity.this, R.style.AppTheme)
//                                    .startScene(TestScene.class, null);
//                        }
//                    }, i * 2000);
//                }
//            }
//        });
//        linearLayout.addView(button);
        button = Button(this)
        button.setText(R.string.main_activity_btn_activity_get_scene_result)
        button.setOnClickListener {
            SceneNavigator(this@ActivityGetSceneResultSampleActivity, R.style.AppTheme)
                .startSceneForResult(TestScene2::class.java, null) { result ->
                    if (result == null) {
                        Toast.makeText(applicationContext, "null", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(applicationContext, result.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
        }
        linearLayout.addView(button)
        setContentView(linearLayout)
    }

    class TestScene2 : Scene() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup,
            savedInstanceState: Bundle?
        ): ViewGroup {
            val tv = Button(requireActivity())
            tv.text = "Click to set result and finish"
            tv.fitsSystemWindows = true
            tv.isAllCaps = false
            tv.setOnClickListener {
                requireNavigationScene().setResult(this@TestScene2, "Result is one")
                requireNavigationScene().pop()
            }
            val frameLayout = FrameLayout(requireActivity())
            frameLayout.addView(tv)
            return frameLayout
        }
    }
}