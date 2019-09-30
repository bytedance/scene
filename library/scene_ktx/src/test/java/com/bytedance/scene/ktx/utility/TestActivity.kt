package com.bytedance.scene.ktx.utility

import android.app.Activity
import android.os.Bundle
import android.widget.FrameLayout

class TestActivity : Activity() {
    lateinit var mFrameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFrameLayout = FrameLayout(this)
        setContentView(mFrameLayout)
    }
}