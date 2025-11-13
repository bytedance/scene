package com.bytedance.scene.fragment

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bytedance.scene.NavigationSceneUtility
import com.bytedance.scene.SceneDelegate

abstract class SceneFragmentActivity : AppCompatActivity() {
    private var mDelegate: SceneDelegate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                (window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
        val arguments = getHomeFragmentArguments(intent)
        this.mDelegate = NavigationSceneUtility.setupWithActivity(
            this, FragmentScene::class.java
        ).rootSceneArguments(arguments).supportRestore(supportRestore())
            .onlyRestoreVisibleScene(true)
            .rootSceneComponentFactory { cl, className, bundle ->
                FragmentScene.newInstance(
                    homeFragmentClass, bundle
                )
            }.build()
    }

    override fun onBackPressed() {
        if (!mDelegate!!.onBackPressed()) {
            super.onBackPressed()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val navigationScene = mDelegate!!.navigationScene
        navigationScene?.onConfigurationChanged(newConfig)
    }

    protected abstract val homeFragmentClass: Class<out Fragment>

    protected abstract fun supportRestore(): Boolean

    protected fun getHomeFragmentArguments(intent: Intent?): Bundle? {
        return null
    }
}
