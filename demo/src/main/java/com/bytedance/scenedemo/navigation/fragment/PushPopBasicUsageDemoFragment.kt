package com.bytedance.scenedemo.navigation.fragment

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.bytedance.scene.fragment.push
import com.bytedance.scene.fragment.requireNavigationScene
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scenedemo.R
import com.bytedance.scenedemo.utility.ColorUtil
import com.bytedance.scenedemo.utility.addButton
import com.bytedance.scenedemo.utility.addClassPathTitle
import com.bytedance.scenedemo.utility.addSpace
import com.bytedance.scenedemo.utility.addTitle

class PushPopBasicUsageDemoFragment : Fragment(), ActivityCompatibleBehavior {

    companion object {
        const val TAG = "Fragment"

        fun newInstance(bundle: Bundle): PushPopBasicUsageDemoFragment {
            return PushPopBasicUsageDemoFragment().apply {
                arguments = bundle
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val scrollView = ScrollView(requireActivity())
        scrollView.fitsSystemWindows = true

        val layout = LinearLayout(requireActivity())
        layout.orientation = LinearLayout.VERTICAL

        scrollView.addView(layout)

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0
        scrollView.setBackgroundColor(
            ColorUtil.getMaterialColor(
                requireActivity().resources, value
            )
        )

        addClassPathTitle(layout)
        addSpace(layout, 12)
        addTitle(layout, getString(R.string.main_title_basic))

        addButton(layout, value.toString()) {
            val bundle = Bundle()
            bundle.putInt("1", value + 1)
            requireNavigationScene().push(PushPopBasicUsageDemoFragment.newInstance(bundle))
        }

        addSpace(layout, 100)

        return scrollView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle", this.toString() + " $value onActivityCreated")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val argument = arguments
        val value = argument?.getInt("1", 0) ?: 0

        Log.i("Lifecycle", this.toString() + " $value onDestroyView")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "Not yet implemented")
    }

    override fun onNewIntent(arguments: Bundle?) {
        Log.i(TAG, "Not yet implemented")
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.i(TAG, "Not yet implemented")
    }
}
