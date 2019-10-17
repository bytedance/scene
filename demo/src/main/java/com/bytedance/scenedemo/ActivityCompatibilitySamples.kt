package com.bytedance.scenedemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.PermissionResultCallback
import com.bytedance.scene.ktx.requestPermissions
import com.bytedance.scenedemo.activity_compatibility.activity_result.SceneGetActivityResultSample
import com.bytedance.scenedemo.activity_compatibility.configuration.ConfigurationDemoScene
import com.bytedance.scenedemo.activity_compatibility.softkeyboard.SoftKeyboardDemoScene
import com.bytedance.scenedemo.activity_compatibility.theme.ThemeDemo
import com.bytedance.scenedemo.activity_compatibility.window.WindowDemo
import com.bytedance.scenedemo.activity_compatibility.scene_result.ActivityGetSceneResultSampleActivity
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle


class ActivityCompatibilitySamples : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(requireSceneContext())

        val layout = LinearLayout(requireSceneContext())
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.main_activity_btn_scene_get_activity_result), View.OnClickListener {
            requireNavigationScene().push(SceneGetActivityResultSample::class.java)
        })
        addButton(layout, getString(R.string.main_activity_btn_activity_get_scene_result), View.OnClickListener {
            requireNavigationScene().startActivity(Intent(requireActivity(), ActivityGetSceneResultSampleActivity::class.java))
        })
        addButton(layout, getString(R.string.nav_result_permission), View.OnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 123, PermissionResultCallback {
                    if (it != null && it.isNotEmpty()
                            && it[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(activity, getString(R.string.nav_result_permission_tip_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(activity, getString(R.string.nav_result_permission_tip_failed), Toast.LENGTH_SHORT).show()
                    }
                })
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
}