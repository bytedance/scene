package com.bytedance.scenedemo.migrate

import android.app.Activity
import com.bytedance.scenedemo.migrate.TestSceneDelegateToViewView
import android.os.Bundle
import com.bytedance.scene.navigation.NavigationScene

/**
 * Created by JiangQi on 11/6/18.
 *
 * Demonstrate how to manually manage the life cycle of Scene
 * and host the entire Scene with normal View
 */
class TestSceneToViewActivity : Activity() {
    private var viewView: TestSceneDelegateToViewView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewView = TestSceneDelegateToViewView(this)
        setContentView(viewView)
        viewView?.onActivityCreated(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        viewView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        viewView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        viewView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewView?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewView?.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewView?.onSaveInstanceState(outState)
    }

    override fun onBackPressed() {
        val navigationScene = viewView?.navigationScene
        if (navigationScene != null && navigationScene.onBackPressed()) {
            //empty
        } else {
            super.onBackPressed()
        }
    }
}