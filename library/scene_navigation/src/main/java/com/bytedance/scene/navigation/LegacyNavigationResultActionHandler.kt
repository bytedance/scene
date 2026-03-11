package com.bytedance.scene.navigation

import com.bytedance.scene.interfaces.PushOptions

/**
 * A -> B
 * A：caller
 * B: callee
 */

interface INavigationResultActionHandler {
    fun saveCallback(calleeRecord: Record, pushOptions: PushOptions)
    fun saveResult(calleeRecord: Record, result: Any?)
    fun deliverResultLegacy(calleeRecord: Record)
}

class LegacyNavigationResultActionHandler : INavigationResultActionHandler {
    override fun saveCallback(calleeRecord: Record, pushOptions: PushOptions) {
        calleeRecord.mPushResultCallback = pushOptions.pushResultCallback
    }

    override fun saveResult(calleeRecord: Record, result: Any?) {
        calleeRecord.mPushResult = result
    }

    override fun deliverResultLegacy(calleeRecord: Record) {
        // Ensure that the requesting Scene is correct
        calleeRecord.mPushResultCallback?.onResult(calleeRecord.mPushResult)
    }
}