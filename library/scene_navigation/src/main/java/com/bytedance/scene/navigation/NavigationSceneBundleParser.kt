package com.bytedance.scene.navigation

import android.os.Bundle
import com.bytedance.scene.SceneStateSaveStrategy
import com.bytedance.scene.parcel.ParcelConstants

data class SceneBundleRecord(
    val sceneClassName: String,
    val sceneArguments: Bundle?,
    val sceneSavedInstanceState: Bundle?,
    private val deleteFunction: () -> Unit
) {
    fun delete() {
        this.deleteFunction()
    }
}

/**
 * This utility can be used to read and modify onSavedInstanceState status in container(Activity or Fragment) onCreate or onActivityCreated
 * for example, remove specific Scene
 */
object NavigationSceneBundleParser {
    fun parse(
        savedInstanceState: Bundle, sceneStateSaveStrategy: SceneStateSaveStrategy? = null
    ): List<SceneBundleRecord> {
        val sceneBundle = if (sceneStateSaveStrategy != null) {
            sceneStateSaveStrategy.onRestoreInstanceState(savedInstanceState)
        } else {
            savedInstanceState
        }
        if (sceneBundle == null) {
            return emptyList()
        }
        val recordList =
            sceneBundle.getParcelableArrayList<Record>(ParcelConstants.KEY_NAVIGATION_RECORD_LIST)
        if (recordList == null || recordList.size == 0) {
            return emptyList()
        }

        val bundleList =
            sceneBundle.getParcelableArrayList<Bundle>(ParcelConstants.KEY_NAVIGATION_SCENE_MANAGER_TAG)
        val result = ArrayList<SceneBundleRecord>()

        val tmp = ArrayList(recordList)
        for (i in 0 until tmp.size) {
            val record: Record = tmp[i]
            val recordBundle = bundleList?.get(i)
            result.add(
                SceneBundleRecord(
                    record.mSceneClassName,
                    recordBundle?.getBundle(ParcelConstants.KEY_SCENE_ARGUMENT),
                    recordBundle
                ) {
                    recordList.remove(record)
                    bundleList?.remove(recordBundle)
                })
        }
        return result.toList()
    }
}
