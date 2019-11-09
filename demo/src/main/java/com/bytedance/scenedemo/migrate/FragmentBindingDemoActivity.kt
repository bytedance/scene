package com.bytedance.scenedemo.migrate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

import com.bytedance.scene.Scene
import com.bytedance.scene.SceneDelegate
import com.bytedance.scene.interfaces.ChildSceneLifecycleAdapterCallbacks
import com.bytedance.scene.ui.NavigationSceneCompatUtility
import com.bytedance.scene.utlity.ViewIdGenerator
import com.bytedance.scenedemo.group.viewpager.ViewPagerSample

/**
 * Created by JiangQi on 9/5/18.
 */
class FragmentBindingDemoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().add(android.R.id.content, AFragment()).commitNow()
    }

    class AFragment : Fragment() {
        private val viewId = ViewIdGenerator.generateViewId()
        private var delegate: SceneDelegate? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val frameLayout = FrameLayout(requireActivity())
            frameLayout.id = viewId
            return frameLayout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            delegate = NavigationSceneCompatUtility.setupWithFragment(this, ViewPagerSample::class.java, viewId)
                    .supportRestore(false)
                    .build()

            delegate!!.navigationScene!!.registerChildSceneLifecycleCallbacks(object : ChildSceneLifecycleAdapterCallbacks() {
                override fun onSceneResumed(scene: Scene) {
                    super.onSceneResumed(scene)

                }
            }, false)
        }
    }
}
