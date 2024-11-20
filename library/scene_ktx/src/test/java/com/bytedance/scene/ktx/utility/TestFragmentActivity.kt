package com.bytedance.scene.ktx.utility

import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity

class TestFragmentActivity : FragmentActivity() {
    lateinit var mFrameLayout: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mFrameLayout = FrameLayout(this)
        setContentView(mFrameLayout)
    }
}