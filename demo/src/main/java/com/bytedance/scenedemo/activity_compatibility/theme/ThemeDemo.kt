package com.bytedance.scenedemo.activity_compatibility.theme

import com.bytedance.scene.ui.template.AppCompatScene
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.bytedance.scenedemo.R
import android.widget.TextView
import com.bytedance.scenedemo.activity_compatibility.theme.ThemeDemo.TestTheme0Scene
import com.bytedance.scenedemo.activity_compatibility.theme.ThemeDemo.TestTheme1Scene
import androidx.annotation.StyleRes
import com.bytedance.scene.Scene

/**
 * Created by JiangQi on 11/9/18.
 */
class ThemeDemo : AppCompatScene() {
    protected override fun onCreateContentView(
        inflater: LayoutInflater,
        container: ViewGroup,
        savedInstanceState: Bundle?
    ): ViewGroup {
        return inflater.inflate(R.layout.layout_theme, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle(R.string.main_nav_btn_theme)
        val name = view.findViewById<TextView>(R.id.name)
        name.visibility = View.GONE
        val btn = view.findViewById<Button>(R.id.btn)
        btn.setText(R.string.nav_theme_btn_1)
        btn.setOnClickListener {
            val scene = TestTheme0Scene.newInstance(R.style.AppTheme_Test1)
            requireNavigationScene().push(scene)
        }
        val btn2 = view.findViewById<Button>(R.id.btn2)
        btn2.visibility = View.VISIBLE
        btn2.setText(R.string.nav_theme_btn_2)
        btn2.setOnClickListener {
            val scene = TestTheme0Scene.newInstance(R.style.AppTheme_Test2)
            requireNavigationScene().push(scene)
        }
        val btn3 = view.findViewById<Button>(R.id.btn3)
        btn3.visibility = View.VISIBLE
        btn3.setText(R.string.nav_theme_btn_3)
        btn3.setOnClickListener {
            val scene = TestTheme1Scene.newInstance(R.style.AppTheme_Test3)
            requireNavigationScene().push(scene)
        }
    }

    class TestTheme0Scene : Scene() {
        private val mThemeId = 0
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            return inflater.inflate(R.layout.layout_theme_demo, container, false)
        }

        companion object {
            fun newInstance(@StyleRes themeId: Int): TestTheme0Scene {
                val scene = TestTheme0Scene()
                val bundle = Bundle()
                bundle.putInt("themeId", themeId)
                scene.setArguments(bundle)
                scene.theme = themeId
                return scene
            }
        }
    }

    class TestTheme1Scene : Scene() {
        private var mThemeId = 0
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            mThemeId = arguments!!.getInt("themeId")
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
            theme = mThemeId
            return inflater.inflate(R.layout.layout_theme_demo1, container, false)
        }

        companion object {
            fun newInstance(@StyleRes themeId: Int): TestTheme1Scene {
                val scene = TestTheme1Scene()
                val bundle = Bundle()
                bundle.putInt("themeId", themeId)
                scene.setArguments(bundle)
                return scene
            }
        }
    }
}