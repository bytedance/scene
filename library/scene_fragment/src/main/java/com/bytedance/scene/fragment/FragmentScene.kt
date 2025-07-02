package com.bytedance.scene.fragment

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentController
import androidx.fragment.app.FragmentHostCallback
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.dispatchViewCreated
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.bytedance.scene.ActivityCompatibilityUtility
import com.bytedance.scene.Scene
import com.bytedance.scene.interfaces.ActivityCompatibleBehavior
import com.bytedance.scene.ktx.navigationScene

internal class FragmentScene : Scene(), ActivityCompatibleBehavior {

    companion object {
        private const val FRAGMENTS_TAG = "scene:support:fragments"
        private const val ROOT_FRAGMENT_TAG = "root_fragment"
        private const val ROOT_FRAGMENT_CLASS_NAME_TAG = "root_fragment_class_name"
        private const val ROOT_FRAGMENT_ARGUMENT_NAME_TAG = "root_fragment_arguments"

        fun newInstance(
            fragmentClass: Class<out Fragment>, fragmentArguments: Bundle? = null
        ): FragmentScene {
            val scene = FragmentScene()
            val sceneArgument = Bundle()
            sceneArgument.putString(ROOT_FRAGMENT_CLASS_NAME_TAG, fragmentClass.name)
            sceneArgument.putBundle(ROOT_FRAGMENT_ARGUMENT_NAME_TAG, fragmentArguments)
            scene.setArguments(sceneArgument)
            return scene
        }
    }

    private val fragmentClass: Class<in Fragment>
        get() {
            return Class.forName(requireArguments().getString(ROOT_FRAGMENT_CLASS_NAME_TAG)) as Class<in Fragment>
        }

    internal var rootFragment: Fragment? = null

    private val fragmentLifecycleRegistry = LifecycleRegistry(this)

    private val fragmentController: FragmentController by lazy {
        FragmentController.createController(SceneHostCallBacks(requireActivity(), Handler(), 0))
    }

    private val fragmentManager: FragmentManager by lazy {
        fragmentController.supportFragmentManager
    }

    private val dispatcher = OnBackPressedDispatcher {
        navigationScene?.let {
            it.onBackPressed()
        } ?: onBackPressed()
    }

    private inner class SceneHostCallBacks(
        context: Context, handler: Handler, windowAnimations: Int
    ) : FragmentHostCallback<FragmentScene>(context, handler, windowAnimations),
        ViewModelStoreOwner, OnBackPressedDispatcherOwner {

        private fun requireFragmentActivity() = (requireActivity() as FragmentActivity)

        override fun onGetHost(): FragmentScene {
            return this@FragmentScene
        }

        override fun onFindViewById(id: Int): View? {
            return view?.findViewById(id)
        }

        override fun onShouldSaveFragmentState(fragment: Fragment): Boolean {
            return !this@FragmentScene.isViewDestroyed
        }

        override fun onGetLayoutInflater(): LayoutInflater {
            return requireFragmentActivity().layoutInflater.cloneInContext(requireFragmentActivity())
        }

        override fun onStartActivityFromFragment(
            fragment: Fragment, intent: Intent?, requestCode: Int, options: Bundle?
        ) {
            intent ?: return
            ActivityCompatibilityUtility.startActivityForResult(
                requireFragmentActivity(), this, intent, requestCode, options
            ) { resultCode, data ->
                fragmentController.noteStateNotSaved()
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }

        override fun onRequestPermissionsFromFragment(
            fragment: Fragment, permissions: Array<String?>, requestCode: Int
        ) {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompatibilityUtility.requestPermissions(
                    requireFragmentActivity(), this, permissions, requestCode
                ) { grantResults ->
                    fragmentController.noteStateNotSaved()
                    fragment.onRequestPermissionsResult(
                        requestCode, permissions, grantResults ?: IntArray(0)
                    )
                }
            } else {
                val handler = Handler(Looper.getMainLooper())
                val activity = requireFragmentActivity()
                handler.post {
                    val grantResults = IntArray(permissions.size)
                    val packageManager = activity.packageManager
                    val packageName = activity.packageName
                    val permissionCount = permissions.size
                    for (i in 0 until permissionCount) {
                        grantResults[i] = packageManager.checkPermission(
                            permissions[i] ?: "", packageName,
                        )
                    }
                    fragmentController.noteStateNotSaved()
                    fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
                }
            }
        }

        override fun onShouldShowRequestPermissionRationale(permission: String): Boolean {
            return ActivityCompat.shouldShowRequestPermissionRationale(
                requireFragmentActivity(), permission,
            )
        }

        override fun onHasWindowAnimations(): Boolean {
            return activity?.window != null
        }

        override fun onGetWindowAnimations(): Int {
            return activity?.window?.attributes?.windowAnimations ?: 0
        }

        override fun onHasView(): Boolean {
            val w: Window? = activity?.window
            return w?.peekDecorView() != null
        }

        override fun getViewModelStore(): ViewModelStore {
            return this@FragmentScene.viewModelStore
        }

        override fun getLifecycle(): Lifecycle {
            return fragmentLifecycleRegistry
        }

        override fun getOnBackPressedDispatcher(): OnBackPressedDispatcher {
            return this@FragmentScene.dispatcher
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?
    ): ViewGroup {
        fragmentController.dispatchViewCreated()
        return requireFragment().requireView() as ViewGroup
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        fragmentController.attachHost(null /*parent*/)

        if (savedInstanceState != null) {
            val p = savedInstanceState.getParcelable<Parcelable>(FRAGMENTS_TAG)
            fragmentController.restoreSaveState(p)
        }

        if (rootFragment == null) {
            fragmentManager.findFragmentByTag(ROOT_FRAGMENT_TAG)?.let { restoreFragment ->
                rootFragment = restoreFragment
            }
        }

        if (rootFragment == null) {
            rootFragment = fragmentClass.newInstance() as Fragment
            rootFragment?.arguments =
                this.requireArguments().getBundle(ROOT_FRAGMENT_ARGUMENT_NAME_TAG)
        }

        val fragment = rootFragment ?: throw IllegalStateException("rootFragment not found")

        super.onCreate(savedInstanceState)
        fragmentLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fragmentController.dispatchCreate()
        if (fragmentManager.findFragmentByTag(ROOT_FRAGMENT_TAG) == null) {
            this.fragmentManager.beginTransaction().add(fragment, ROOT_FRAGMENT_TAG)
                .commitNowAllowingStateLoss()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        fragmentController.dispatchActivityCreated()
        super.onActivityCreated(savedInstanceState)
        navigationScene?.addOnBackPressedListener(this) {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        fragmentController.noteStateNotSaved()
        fragmentController.execPendingActions()
        fragmentLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        fragmentController.dispatchStart()
    }

    override fun onResume() {
        super.onResume()
        fragmentController.noteStateNotSaved()
        fragmentController.execPendingActions()
        fragmentLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fragmentController.dispatchResume()
    }

    override fun onPause() {
        super.onPause()
        fragmentController.dispatchPause()
        fragmentLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStop() {
        super.onStop()
        markFragmentsCreated()
        fragmentController.dispatchStop()
        fragmentLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        markFragmentsCreated()
        fragmentLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity?.isChangingConfigurations == false) {
            scope?.destroy()
        }
        fragmentController.dispatchDestroy()
        fragmentLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        requireFragment()?.let { f ->
            fragmentManager.beginTransaction().remove(f).commitNowAllowingStateLoss()
        }
    }

    private fun markFragmentsCreated() {
        var reiterate: Boolean
        do {
            reiterate = markState(fragmentManager, Lifecycle.State.CREATED)
        } while (reiterate)
    }

    private fun markState(manager: FragmentManager, state: Lifecycle.State): Boolean {
        var hadNotMarked = false
        val fragments: Collection<Fragment> = manager.fragments
        for (fragment in fragments) {
            if (fragment == null) {
                continue
            }
            if (fragment.host != null) {
                val childFragmentManager = fragment.childFragmentManager
                hadNotMarked = hadNotMarked or markState(childFragmentManager, state)
            }
            if (fragment.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                (fragment.lifecycle as LifecycleRegistry).currentState = state
                hadNotMarked = true
            }
        }
        return hadNotMarked
    }

    private fun requireFragment(): Fragment {
        return requireNotNull(rootFragment)
    }

    override fun onNewIntent(bundle: Bundle?) {
        fragmentController.noteStateNotSaved()
        if (rootFragment is ActivityCompatibleBehavior) {
            (rootFragment as ActivityCompatibleBehavior).onNewIntent(bundle)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (rootFragment is ActivityCompatibleBehavior) {
            (rootFragment as ActivityCompatibleBehavior).onWindowFocusChanged(hasFocus)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        fragmentController.noteStateNotSaved()
        fragmentController.dispatchConfigurationChanged(newConfig)
    }

    fun onBackPressed(): Boolean {
        return false
    }

    override fun toString(): String {
        return super.toString() + " fragment: " + fragmentClass.name
    }
}