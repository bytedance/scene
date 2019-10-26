package com.bytedance.scenedemo.group.drawer

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bytedance.scene.Scene
import com.bytedance.scene.ui.template.NavigationViewScene
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil
import java.util.*

/**
 * Created by JiangQi on 8/3/18.
 */
class NavigationViewSample : NavigationViewScene() {
    override fun getMenuResId(): Int {
        return R.menu.drawer_view
    }

    override fun getSceneMap(): LinkedHashMap<Int, Scene> {
        val sparseArrayCompat = LinkedHashMap<Int, Scene>()
        sparseArrayCompat[R.id.nav_camera] = getScene(0)
        sparseArrayCompat[R.id.nav_gallery] = getScene(1)
        sparseArrayCompat[R.id.nav_slideshow] = getScene(2)
        sparseArrayCompat[R.id.nav_manage] = getScene(3)
        return sparseArrayCompat
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        navigationView.inflateHeaderView(R.layout.nav_header)
        if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity!!.window.statusBarColor = Color.TRANSPARENT
        }
    }

    private fun getScene(index: Int): Scene {
        val scene = DrawerChildScene()
        val bundle = Bundle()
        bundle.putInt("index", index)
        scene.setArguments(bundle)
        return scene
    }
}

class DrawerChildScene : Scene() {
    companion object {
        fun newInstance(index: Int): DrawerChildScene {
            return DrawerChildScene().apply {
                val bundle = Bundle()
                bundle.putInt("index", index)
                setArguments(bundle)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        return TextView(requireSceneContext()).apply {
            val index = arguments!!["index"] as Int
            setBackgroundColor(ColorUtil.getMaterialColor(resources, index))
            gravity = Gravity.CENTER
            text = "Child Scene #$index"
        }
    }
}
