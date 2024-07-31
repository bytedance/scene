package com.bytedance.scenedemo

import com.bytedance.scene.group.UserVisibleHintGroupScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.*
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scenedemo.auto_recycle.AutoRecycleActivity
import com.bytedance.scenedemo.restore.SupportRestoreActivity
import com.bytedance.scenedemo.extreme_case.Case0Scene
import com.bytedance.scenedemo.extreme_case.Case1Scene
import com.bytedance.scenedemo.extreme_case.Case2Scene
import com.bytedance.scenedemo.extreme_case.Case3Scene
import com.bytedance.scenedemo.extreme_case.Case4Scene
import com.bytedance.scenedemo.extreme_case.Case5Scene
import com.bytedance.scenedemo.utility.addClassPathTitle

/**
 * Created by JiangQi on 8/21/18.
 */
class CaseListDemo : UserVisibleHintGroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        val scrollView = ScrollView(activity)
        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL
        scrollView.addView(layout)
        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_case))
        addButton(
            layout,
            getString(R.string.main_case_destroy_restore),
            View.OnClickListener {
                requireNavigationScene().startActivity(
                    Intent(
                        requireActivity(),
                        SupportRestoreActivity::class.java
                    )
                )
            })
        addButton(
            layout,
            getString(R.string.main_case_push_and_pop),
            View.OnClickListener { requireNavigationScene().push(Case0Scene::class.java) })
        addButton(
            layout,
            getString(R.string.main_case_push_many),
            View.OnClickListener { requireNavigationScene().push(Case1Scene::class.java) })
        addButton(
            layout,
            getString(R.string.main_case_pop_many),
            View.OnClickListener { requireNavigationScene().push(Case2Scene::class.java) })
        addButton(
            layout,
            getString(R.string.main_case_push_pop_remove),
            View.OnClickListener { requireNavigationScene().push(Case3Scene::class.java) })
        addButton(
            layout,
            getString(R.string.main_case_push_pop_in_lifecycle),
            View.OnClickListener { requireNavigationScene().push(Case4Scene::class.java) })
        addButton(
            layout,
            getString(R.string.main_case_add_remove_group_scene),
            View.OnClickListener { requireNavigationScene().push(Case5Scene::class.java) })
        addButton(layout, getString(R.string.main_case_push_pop_after_finish), View.OnClickListener {
            requireActivity().finish()
            requireNavigationScene().push(Case0Scene.EmptyScene::class.java)
        })
        addButton(
            layout,
            getString(R.string.main_case_auto_recycle_scene_when_low_memory),
            View.OnClickListener {
                requireNavigationScene().startActivity(
                    Intent(
                        requireActivity(),
                        AutoRecycleActivity::class.java
                    )
                )
            }
        )
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