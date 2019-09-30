package com.bytedance.scene.ktx.utility

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.FrameLayout

class TestAppCompatActivity : AppCompatActivity() {
    lateinit var mFrameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(com.bytedance.scene.R.style.Theme_AppCompat)
        super.onCreate(savedInstanceState)
        mFrameLayout = FrameLayout(this)
        setContentView(mFrameLayout)
    }
}