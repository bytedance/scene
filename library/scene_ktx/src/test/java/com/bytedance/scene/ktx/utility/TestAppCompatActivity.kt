package com.bytedance.scene.ktx.utility

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class TestAppCompatActivity : AppCompatActivity() {
    lateinit var mFrameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.bytedance.scene.ktx.R.style.Theme_AppCompat)
        super.onCreate(savedInstanceState)
        mFrameLayout = FrameLayout(this)
        setContentView(mFrameLayout)
    }
}