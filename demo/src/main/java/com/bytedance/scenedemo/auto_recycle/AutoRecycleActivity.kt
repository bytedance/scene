package com.bytedance.scenedemo.auto_recycle

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bytedance.scene.NavigationSceneUtility
import com.bytedance.scene.SceneDelegate
import com.bytedance.scenedemo.R

class AutoRecycleActivity : AppCompatActivity() {

    private val viewModel: AutoRecycleSceneViewModel by lazy {
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ViewModelProvider(this.viewModelStore, factory).get(AutoRecycleSceneViewModel::class.java)
    }

    private lateinit var delegate: SceneDelegate

    private lateinit var traceTextView: TextView
    private lateinit var memoryTextView: TextView
    private lateinit var updateMemoryUsedBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_auto_recycle_activity)

        traceTextView = findViewById(R.id.tv_scene_trace)
        memoryTextView = findViewById(R.id.tv_memory_used)
        updateMemoryUsedBtn = findViewById(R.id.btn_update_memory_used)

        delegate = NavigationSceneUtility.setupWithActivity(this, FirstScene::class.java)
            .toView(R.id.scene_root)
            .supportRestore(true)
            .onlyRestoreVisibleScene(true)
            .autoRecycleInvisibleScenesThreshold(0.5F)
            .build()

        updateMemoryUsedBtn.setOnClickListener {
            viewModel.updateMemoryUsed()
        }

        viewModel.sceneTrace.observe(this) { list ->
            val spannableString = SpannableStringBuilder()
            var currentIndex = 0
            list.forEach {
                val endIndex = currentIndex + it.first.length + 1
                spannableString.appendLine(it.first)
                spannableString.setSpan(ForegroundColorSpan(it.second), currentIndex, endIndex, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                currentIndex = endIndex
            }
            traceTextView.text = spannableString
        }

        viewModel.memoryUpdateEvent.observe(this) {
            val runtime = Runtime.getRuntime()
            val dalvikMax = runtime.maxMemory() / 1024
            val dalvikUsed = (runtime.totalMemory() - runtime.freeMemory())  / 1024
            memoryTextView.text = getString(R.string.case_auto_recycle_memory_info, dalvikUsed, dalvikMax, dalvikUsed.toFloat() / dalvikMax)
        }
    }

    override fun onBackPressed() {
        if (!delegate.onBackPressed()) {
            super.onBackPressed()
        }
    }
}