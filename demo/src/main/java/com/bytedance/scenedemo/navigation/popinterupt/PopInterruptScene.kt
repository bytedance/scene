package com.bytedance.scenedemo.navigation.popinterupt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import com.bytedance.scene.Scene
import com.bytedance.scene.ktx.navigationScene
import com.bytedance.scene.ktx.requireNavigationScene
import com.bytedance.scene.navigation.OnBackPressedListener
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

import java.util.concurrent.TimeUnit

/**
 * Created by JiangQi on 8/3/18.
 */
class PopInterruptScene : Scene() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, 0))

        val name = view.findViewById<TextView>(R.id.name)
        name.text = navigationScene!!.stackHistory

        val btn = view.findViewById<Button>(R.id.btn)
        btn.visibility = View.GONE

        requireNavigationScene().addOnBackPressedListener(this, object : OnBackPressedListener {

            internal var time: Long = 0

            override fun onBackPressed(): Boolean {
                if (time == 0L || TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time) > 2) {
                    Toast.makeText(activity, getString(R.string.nav_interrupt_tip), Toast.LENGTH_SHORT).show()
                    time = System.currentTimeMillis()
                    return true
                }
                return false
            }
        })
    }
}
