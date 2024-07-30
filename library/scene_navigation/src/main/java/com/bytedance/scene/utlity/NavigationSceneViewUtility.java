package com.bytedance.scene.utlity;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.RestrictTo;

import com.bytedance.scene.Scene;
import com.bytedance.scene.logger.LoggerManager;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.navigation.NavigationSceneOptions;

import java.util.List;

/**
 * Created by jiangqi on 2023/11/12
 *
 * @author jiangqi@bytedance.com
 */

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationSceneViewUtility {
    //make sure scene view index is correct
    public static int targetViewIndexOfScene(NavigationScene navigationScene, NavigationSceneOptions navigationSceneOptions, Scene scene) {
        if (!navigationSceneOptions.onlyRestoreVisibleScene()) {
            LoggerManager.getInstance().i("NavigationSceneViewUtility#targetViewIndexOfScene", "onlyRestoreVisibleScene false, targetViewIndex: -1");
            return -1;
        }

        //this scene is the top scene, so add to the last position
        if (navigationScene.getCurrentScene() == scene) {
            LoggerManager.getInstance().i("NavigationSceneViewUtility#targetViewIndexOfScene", "this scene is top scene, targetViewIndex: -1");
            return -1;
        }

        List<Scene> sceneList = navigationScene.getSceneList();
        int sceneIndex = sceneList.indexOf(scene);
        if (sceneIndex == -1) {
            throw new SceneInternalException("Can't find target Scene " + scene.toString());
        }
        if (sceneIndex == sceneList.size() - 1) {
            throw new SceneInternalException("Target Scene " + scene.toString() + " is the top Scene, impossible!");
        }

        ViewGroup containerView = navigationScene.getSceneContainer();
        if (containerView == null) {
            throw new SceneInternalException("Why NavigationScene SceneContainer not found, impossible!");
        }

        int aboveSceneIndex = sceneIndex + 1;
        Scene aboveScene = sceneList.get(aboveSceneIndex);
        View aboveSceneView = aboveScene.getView();

        if (aboveSceneView != null) {
            if (aboveSceneView.getParent() != containerView) {
                throw new SceneInternalException("Above Scene " + scene.toString() + " is not in parent scene container, impossible!");
            }

            //use this index to add before the above scene to make sure the above scene view overlays this scene view
            int targetViewIndex = containerView.indexOfChild(aboveSceneView);
            LoggerManager.getInstance().i("NavigationSceneViewUtility#targetViewIndexOfScene", "find above scene, targetViewIndex: " + targetViewIndex);
            return targetViewIndex;
        }

        //above scene is not created and this scene is the first scene
        if (sceneIndex == 0) {
            LoggerManager.getInstance().i("NavigationSceneViewUtility#targetViewIndexOfScene", "above scene is not created, targetViewIndex: -1");
            return -1;
        }

        int belowSceneIndex = sceneIndex - 1;
        Scene belowScene = sceneList.get(belowSceneIndex);
        View belowSceneView = belowScene.getView();

        if (belowSceneView != null) {
            if (belowSceneView.getParent() != containerView) {
                throw new SceneInternalException("Below Scene " + scene.toString() + " is not in parent scene container, impossible!");
            }

            //use this index to add after the below scene to make sure the this scene view overlays the below scene view
            int targetViewIndex = containerView.indexOfChild(belowSceneView) + 1;
            LoggerManager.getInstance().i("NavigationSceneViewUtility#targetViewIndexOfScene", "find below scene, targetViewIndex: " + targetViewIndex);
            return targetViewIndex;
        }

        //so both the above scene and the below scene are not created
        LoggerManager.getInstance().i("NavigationSceneViewUtility#targetViewIndexOfScene", "above scene and below scene all not created, targetViewIndex: -1");
        return -1;
    }
}
