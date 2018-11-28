//package com.bytedance.scenedemo.navigation.popinterupt;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//
//import com.bytedance.scene.NavigationSceneUtility;
//import com.bytedance.scene.navigation.NavigationScene;
//
///**
// * Created by JiangQi on 8/3/18.
// */
//public class PopInteruptActivity extends Activity{
//    NavigationScene scene;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        scene = NavigationSceneUtility.setupWithActivity(this, savedInstanceState, PopInteruptRootScene.class,true);
//    }
//
//    @Override
//    public void onBackPressed() {
//        scene.pop();
//    }
//}
