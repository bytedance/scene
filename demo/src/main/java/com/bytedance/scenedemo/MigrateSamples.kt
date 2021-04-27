package com.bytedance.scenedemo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.migrate.NavigationSceneBindToFragmentSample
import com.bytedance.scenedemo.migrate.GroupSceneBindToFragmentSample
import com.bytedance.scenedemo.migrate.GroupSceneBindToActivitySample
import com.bytedance.scenedemo.migrate.TestSceneToViewActivity
import com.bytedance.scenedemo.migrate.migrate_from_classic_activity_fragment.MigrateFromClassicAndroidActivitySamplesActivity
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle


class MigrateSamples : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(activity)

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.main_part_btn_bind_navigationscene_to_fragment), View.OnClickListener {
            requireNavigationScene().startActivity(Intent(activity, NavigationSceneBindToFragmentSample::class.java))
        })

        addButton(layout, getString(R.string.main_part_btn_bind_groupscene_to_activity), View.OnClickListener {
            requireNavigationScene().startActivity(Intent(activity, GroupSceneBindToActivitySample::class.java))
        })

        addButton(layout, getString(R.string.main_part_btn_bind_groupscene_to_fragment), View.OnClickListener {
            requireNavigationScene().startActivity(Intent(activity, GroupSceneBindToFragmentSample::class.java))
        })

        addButton(layout, getString(R.string.main_part_btn_bind_to_view), View.OnClickListener {
            requireNavigationScene().startActivity(Intent(activity, TestSceneToViewActivity::class.java))
        })

        addButton(layout, getString(R.string.main_part_btn_migrate_from_classic_android_app), View.OnClickListener {
            requireNavigationScene().startActivity(Intent(activity, MigrateFromClassicAndroidActivitySamplesActivity::class.java))
        })

        addSpace(layout, 100)

        return scrollView
    }
}