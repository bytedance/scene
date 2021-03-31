package com.bytedance.scenedemo.activity_compatibility.softkeyboard

import com.bytedance.scenedemo.activity_compatibility.softkeyboard.SoftKeyboardDemoScene.Companion.focusAndShowInputMethod
import com.bytedance.scene.ui.template.AppCompatScene
import android.widget.EditText
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import com.bytedance.scenedemo.R
import android.view.WindowManager
import android.widget.Button
import com.bytedance.scenedemo.activity_compatibility.softkeyboard.SoftKeyboardDemoScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by dss886 on 2019-08-06.
 */
class SoftKeyboardResizeScene : AppCompatScene() {
    private var mEditText: EditText? = null
    override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.nav_ime_problems_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 2))
        mEditText = view.findViewById(R.id.edit_text)
        requireViewById<View>(R.id.resize).setOnClickListener { requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) }
        requireViewById<View>(R.id.pan).setOnClickListener { requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) }
        requireViewById<View>(R.id.nothing).setOnClickListener { requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING) }
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.nav_ime_btn_top)
        btn.setOnClickListener { navigationScene!!.pop() }
        setTitle("SOFT_INPUT_ADJUST")
    }

    override fun onResume() {
        super.onResume()
        focusAndShowInputMethod(mEditText)
    }
}