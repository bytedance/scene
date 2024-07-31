package com.bytedance.scenedemo.auto_recycle

import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.random.Random


class AutoRecycleSceneViewModel : ViewModel() {

    private val colorMap = mutableMapOf<String, Int>()

    private val _sceneTrace: MutableLiveData<List<Pair<String, Int>>> = MutableLiveData()
    val sceneTrace: LiveData<List<Pair<String, Int>>> = _sceneTrace

    private val _memoryUpdateEvent = MutableLiveData<Unit>()
    val memoryUpdateEvent: LiveData<Unit> = _memoryUpdateEvent

    fun updateMemoryUsed() {
        _memoryUpdateEvent.value = Unit
    }

    fun sceneOnCreate(name: String, savedState: Bundle?) {
        setState {
            it + ("$name onCreate savedState=$savedState" to getColor(name))
        }
    }

    fun sceneOnStart(name: String) {
        setState {
            it + ("$name onStart" to getColor(name))
        }
    }

    fun sceneOnResume(name: String) {
        setState {
            it + ("$name onResume" to getColor(name))
        }
    }

    fun sceneOnPause(name: String) {
        setState {
            it + ("$name onPause" to getColor(name))
        }
    }

    fun sceneOnStop(name: String) {
        setState {
            it + ("$name onStop" to getColor(name))
        }
    }

    fun sceneOnSaveStated(name: String, savedState: Bundle) {
        setState {
            it + ("$name onSaveInstanceState savedState=$savedState" to getColor(name))
        }
    }

    fun sceneOnDestroy(name: String) {
        setState {
            it + ("$name onDestroy" to getColor(name))
        }
    }

    private fun getColor(key: String): Int = colorMap.getOrPut(key) {
        var color = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
        while (colorMap.containsValue(color)) {
            color = Color.argb(255, Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
        }
        color
    }

    private fun setState(block: (List<Pair<String, Int>>) -> List<Pair<String, Int>>) {
        _sceneTrace.value = block(_sceneTrace.value ?: emptyList())
    }

}