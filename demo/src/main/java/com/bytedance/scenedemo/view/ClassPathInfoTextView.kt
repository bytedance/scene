package com.bytedance.scenedemo.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.bytedance.scene.ktx.getScene


class ClassPathInfoTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        this.text = getScene().javaClass.name
    }
}