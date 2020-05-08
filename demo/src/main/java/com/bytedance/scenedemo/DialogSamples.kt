package com.bytedance.scenedemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import com.bytedance.scene.Scene
import com.bytedance.scene.animation.animatorexecutor.DialogSceneAnimatorExecutor
import com.bytedance.scene.interfaces.PushOptions
import com.bytedance.scenedemo.dialog.BottomSheetDialogSample
import com.bytedance.scenedemo.dialog.DemoDialogScene
import com.bytedance.scenedemo.dialog.DemoDialogWithDimScene
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle


class DialogSamples : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val scrollView = ScrollView(activity)

        val layout = LinearLayout(activity)
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, getString(R.string.part_dialog_btn_1), View.OnClickListener {
            requireNavigationScene().push(DemoDialogScene::class.java, null, PushOptions.Builder()
                    .setTranslucent(true).setAnimation(DialogSceneAnimatorExecutor()).build())
        })
        addButton(layout, getString(R.string.part_dialog_btn_2), View.OnClickListener {
            requireNavigationScene().push(DemoDialogWithDimScene::class.java, null, PushOptions.Builder()
                    .setTranslucent(true).setAnimation(DialogSceneAnimatorExecutor()).build())
        })
        addButton(layout, getString(R.string.part_dialog_btn_3), View.OnClickListener {
            BottomSheetDialogSample().show(this)
        })
        addButton(layout, getString(R.string.main_part_btn_floating_window), View.OnClickListener {
            Toast.makeText(requireApplicationContext(), "TODO", Toast.LENGTH_SHORT).show()
        })

        addSpace(layout, 100)

        return scrollView
    }
}