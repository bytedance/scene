package com.bytedance.scenedemo.group.activity

import android.app.Activity
import android.os.Bundle
import com.bytedance.scene.GroupSceneUtility
import com.bytedance.scenedemo.group.viewpager.ViewPagerGroupScene

class GroupSceneBindingSample : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GroupSceneUtility.setupWithActivity(this, ViewPagerGroupScene::class.java).build()
    }
}