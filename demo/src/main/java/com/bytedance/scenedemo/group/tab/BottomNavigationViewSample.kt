package com.bytedance.scenedemo.group.tab

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.collection.SparseArrayCompat
import com.bytedance.scene.Scene
import com.bytedance.scene.ui.template.AppCompatScene
import com.bytedance.scene.ui.template.BottomNavigationViewScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

class BottomNavigationViewSample : BottomNavigationViewScene() {
    override fun getMenuResId(): Int {
        return R.menu.bottom_nav_items
    }

    override fun getSceneMap(): SparseArrayCompat<Scene> {
        val sparseArrayCompat = SparseArrayCompat<Scene>()
        sparseArrayCompat.put(R.id.menu_home, TabChildScene.newInstance(0))
        sparseArrayCompat.put(R.id.menu_search, TabChildScene.newInstance(1))
        sparseArrayCompat.put(R.id.menu_notifications, TabChildScene.newInstance(2))
        return sparseArrayCompat
    }
}

class TabChildScene : AppCompatScene() {
    companion object {
        fun newInstance(index: Int): TabChildScene {
            return TabChildScene().apply {
                val bundle = Bundle()
                bundle.putInt("index", index)
                setArguments(bundle)
            }
        }
    }

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return TextView(requireSceneContext()).apply {
            val index = arguments!!["index"] as Int
            setBackgroundColor(ColorUtil.getMaterialColor(resources, index))
            gravity = Gravity.CENTER
            text = "Child Scene #$index"
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        toolbar?.navigationIcon = null
    }
}