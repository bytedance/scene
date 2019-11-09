package com.bytedance.scenedemo.migrate

import android.app.Activity
import android.os.Bundle
import com.bytedance.scene.GroupSceneUtility
import com.bytedance.scenedemo.group.viewpager.ViewPagerSample

class GroupSceneBindingSample : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GroupSceneUtility.setupWithActivity(this, ViewPagerSample::class.java).build()
    }
}