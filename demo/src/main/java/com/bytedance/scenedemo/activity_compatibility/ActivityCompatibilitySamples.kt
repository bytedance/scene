package com.bytedance.scenedemo.activity_compatibility

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bytedance.scene.Scene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.navigation.configuration.ConfigurationDemoScene
import com.bytedance.scenedemo.navigation.forresult.SceneResultRootScene
import com.bytedance.scenedemo.navigation.softkeyboard.SoftKeyboardDemoScene
import com.bytedance.scenedemo.navigation.window.WindowDemo
import com.bytedance.scenedemo.theme.ThemeDemo


class ActivityCompatibilitySamples : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(activity)

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.main_activity_btn_for_result), View.OnClickListener {
            requireNavigationScene().push(SceneResultRootScene::class.java)
        })
        addButton(layout, getString(R.string.nav_result_permission), View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requireNavigationScene().requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 123) { grantResults ->
                    if (grantResults != null && grantResults.isNotEmpty()
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(activity, getString(R.string.nav_result_permission_tip_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, getString(R.string.nav_result_permission_tip_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
        addButton(layout, getString(R.string.main_nav_btn_configuration_change), View.OnClickListener {
            requireNavigationScene().push(ConfigurationDemoScene::class.java)
        })
        addButton(layout, getString(R.string.main_nav_btn_ime), View.OnClickListener {
            requireNavigationScene().push(SoftKeyboardDemoScene::class.java)
        })
        addButton(layout, getString(R.string.main_nav_btn_theme), View.OnClickListener {
            requireNavigationScene().push(ThemeDemo::class.java)
        })

        addButton(layout, getString(R.string.main_nav_btn_modify_activity_states), View.OnClickListener {
            requireNavigationScene().push(WindowDemo::class.java)
        })

        addSpace(layout, 100)

        return scrollView
    }

    private fun addTitle(parent: LinearLayout, text: String) {
        val textView = TextView(activity)
        textView.textSize = 14f
        textView.text = text
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.leftMargin = 30
        lp.rightMargin = 30
        lp.topMargin = 24
        lp.bottomMargin = 24
        parent.addView(textView, lp)
    }

    private fun addButton(parent: LinearLayout, text: String, onClickListener: View.OnClickListener): Button {
        val button = Button(activity)
        button.isAllCaps = false
        button.text = text
        button.setOnClickListener(onClickListener)
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 150)
        lp.leftMargin = 20
        lp.rightMargin = 20
        parent.addView(button, lp)
        return button
    }

    private fun addSpace(parent: LinearLayout, height: Int) {
        val space = Space(activity)
        parent.addView(space, ViewGroup.LayoutParams.MATCH_PARENT, height)
    }
}