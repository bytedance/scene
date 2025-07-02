package com.bytedance.scenedemo

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import com.bytedance.scene.Scene
import com.bytedance.scene.ui.template.NavigationViewScene
import com.bytedance.scenedemo.benchmark.BenchmarkSamples
import com.bytedance.scenedemo.other_library.OtherLibrarySamples
import com.bytedance.scenedemo.router.RouterSample


class MainScene : NavigationViewScene() {
    override fun getMenuResId(): Int {
        return R.menu.navigation_view
    }

    override fun getSceneMap(): LinkedHashMap<Int, Scene> {
        val map = LinkedHashMap<Int, Scene>()
        map[R.id.navigation] = NavigationListDemo()
        map[R.id.navigation_multi_stack] = MultiStackDemoScene()
        map[R.id.navigation_router] = RouterSample()
        map[R.id.group] = GroupSceneUsageSamples()
        map[R.id.dialog] = DialogSamples()
        map[R.id.animation] = AnimationListDemoScene()
        map[R.id.architecture_patterns] = PatternsSamples()
        map[R.id.activity_compatibility] = ActivityCompatibilitySamples()
        map[R.id.other_library] = OtherLibrarySamples()
        map[R.id.migrate] = MigrateSamples()
        map[R.id.compose] = ComposeSamples()
        map[R.id.benchmark] = BenchmarkSamples()
        map[R.id.extreme_case] = CaseListDemo()
        return map
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val window = requireActivity().window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            window.decorView.systemUiVisibility = window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = Color.TRANSPARENT
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp: WindowManager.LayoutParams = window.attributes
            lp.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.attributes = lp
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

}