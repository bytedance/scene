package com.bytedance.scenedemo.lifecycle

import com.bytedance.scene.group.UserVisibleHintGroupScene
import android.widget.TextView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil

/**
 * Created by JiangQi on 8/7/18.
 */
class EmptyScene : UserVisibleHintGroupScene() {
    private var textView: TextView? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return inflater.inflate(R.layout.basic_layout, container, false) as ViewGroup
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val index = if (arguments == null) 0 else arguments!!.getInt("index")
        view.setBackgroundColor(ColorUtil.getMaterialColor(resources, index))
        textView = view.findViewById(R.id.name)
        val btn = view.findViewById<Button>(R.id.btn)
        btn.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        textView!!.text = stateHistory
    }

    companion object {
        @JvmStatic
        fun newInstance(index: Int): EmptyScene {
            val scene = EmptyScene()
            val bundle = Bundle()
            bundle.putInt("index", index)
            scene.setArguments(bundle)
            return scene
        }
    }
}