//package com.bytedance.scenedemo.navigation.popto;
//
//import android.app.Activity;
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//
//import com.bytedance.scene.NavigationSceneUtility;
//import com.bytedance.scene.navigation.NavigationScene;
//
///**
// * Created by JiangQi on 8/2/18.
// */
//public class PopToActivity extends Activity {
//    private NavigationScene scene;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        scene = NavigationSceneUtility.setupWithActivity(this, savedInstanceState, PopToScene.class);
//    }
//
//    @Override
//    public void onBackPressed() {
//        scene.pop();
//    }
//}
