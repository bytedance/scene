package com.bytedance.scenedemo.group.async_inflate

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.RequiresApi

/**
 * Created by JiangQi on 9/19/18.
 */
class TestAsyncView : View {
    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val layoutInflater = LayoutInflater.from(context)
        Log.e("ddd", "sss")
    }
}