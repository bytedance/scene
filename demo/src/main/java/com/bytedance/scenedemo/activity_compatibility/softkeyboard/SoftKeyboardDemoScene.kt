package com.bytedance.scenedemo.activity_compatibility.softkeyboard

import android.content.Context
import com.bytedance.scene.group.GroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scenedemo.activity_compatibility.softkeyboard.SoftKeyboardResizeScene
import android.widget.EditText
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/19/18.
 *
 * Android framework has three soft input policies: SOFT_INPUT_ADJUST_RESIZE, SOFT_INPUT_ADJUST_PAN, SOFT_INPUT_ADJUST_NOTHING
 * Scene container Activity's AndroidManifest.xml android:windowSoftInputMode must be set to **adjustPan** or **adjustResize** or **adjustNothing**,
 * adjustUnspecified flag has bug will cause `requireActivity().getWindow().setSoftInputMode` not working, then system will
 * pick the best one depending on the contents of the window
 * If you push a AppCompatScene, everything will work fine, if you push a GroupScene, you should set root view's android:fitsSystemWindows to true,
 * otherwise SOFT_INPUT_ADJUST_RESIZE will have problem
 */
class SoftKeyboardDemoScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))
        val name = view.findViewById<TextView>(R.id.name)
        name.text =
            """* Android framework has three soft input policies: SOFT_INPUT_ADJUST_RESIZE, SOFT_INPUT_ADJUST_PAN, SOFT_INPUT_ADJUST_NOTHING
 * Scene container Activity's AndroidManifest.xml android:windowSoftInputMode must be set to <b>adjustPan</b> or <b>adjustResize</b> or <b>adjustNothing</b>,
 * adjustUnspecified flag has bug will cause <code>requireActivity().getWindow().setSoftInputMode</code> not working, then system will
 * pick the best one depending on the contents of the window
 * If you push a AppCompatScene, everything will work fine, if you push a GroupScene, you should set root view's android:fitsSystemWindows to true,
 * otherwise SOFT_INPUT_ADJUST_RESIZE will have problem"""
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.nav_ime_btn_1)
        btn.setOnClickListener { navigationScene!!.push(SoftKeyboardResizeScene::class.java) }
    }

    companion object {
        @JvmStatic
        fun focusAndShowInputMethod(editText: EditText?) {
            if (editText == null) {
                return
            }
            editText.requestFocus()
            // In case the filter is not working, setText again.
            editText.text = editText.text
            editText.setSelection(if (TextUtils.isEmpty(editText.text)) 0 else editText.text.length)
            (editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
                editText,
                0
            )
        }
    }
}