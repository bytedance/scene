package com.bytedance.scenedemo.benchmark.performance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import com.bytedance.scenedemo.benchmark.performance.PerformanceDemo
import android.widget.Toast
import com.bytedance.scenedemo.R

/**
 * Created by JiangQi on 8/21/18.
 */
class EmptyAppCompatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = View(this)
        view.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.viewTreeObserver.removeOnPreDrawListener(this)
                val a = System.currentTimeMillis() - PerformanceDemo.startTimestamp
                Toast.makeText(this@EmptyAppCompatActivity, getString(R.string.nav_compare_tip, a), Toast.LENGTH_SHORT)
                    .show()
                return true
            }
        })
        setContentView(view)
    }
}