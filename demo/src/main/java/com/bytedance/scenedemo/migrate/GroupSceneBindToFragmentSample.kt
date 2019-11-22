package com.bytedance.scenedemo.migrate

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bytedance.scene.GroupSceneDelegate
import com.bytedance.scene.ui.GroupSceneCompatUtility
import com.bytedance.scene.utlity.ViewIdGenerator
import com.bytedance.scenedemo.group.viewpager.ViewPagerSample

/**
 * Created by JiangQi on 9/5/18.
 */
class GroupSceneBindToFragmentSample : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportFragmentManager.beginTransaction().add(android.R.id.content, AFragment()).commitNow()
    }

    class AFragment : Fragment() {
        private val viewId = ViewIdGenerator.generateViewId()
        private var delegate: GroupSceneDelegate? = null

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val frameLayout = FrameLayout(activity!!)
            frameLayout.id = viewId
            return frameLayout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            delegate = GroupSceneCompatUtility.setupWithFragment(this, ViewPagerSample::class.java, viewId).build()
        }
    }
}
