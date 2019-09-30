package com.bytedance.scene;

import android.view.View;
import android.view.animation.AlphaAnimation;
import com.bytedance.scene.utlity.AnimatorUtility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AnimatorUtilityTests {
    @Test
    public void testResetViewStatus() {
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        View view = new View(testActivity);
        view.setTranslationX(1);
        view.setTranslationY(1);
        view.setScaleX(2.0f);
        view.setScaleY(2.0f);
        view.setRotation(1.0f);
        view.setRotationX(1.0f);
        view.setRotationY(1.0f);
        view.setAlpha(0.5f);
        view.startAnimation(new AlphaAnimation(0.0f, 1.0f));

        AnimatorUtility.resetViewStatus(view);
        assertEquals(0.0f, view.getTranslationX(), 0.0f);
        assertEquals(0.0f, view.getTranslationY(), 0.0f);
        assertEquals(1.0f, view.getScaleX(), 0.0f);
        assertEquals(1.0f, view.getScaleY(), 0.0f);
        assertEquals(0.0f, view.getRotation(), 0.0f);
        assertEquals(0.0f, view.getRotationX(), 0.0f);
        assertEquals(0.0f, view.getRotationY(), 0.0f);
        assertEquals(1.0f, view.getAlpha(), 0.0f);
        assertNull(view.getAnimation());
    }

    @Test
    public void testCaptureViewStatus() {
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        View view = new View(testActivity);
        view.setTranslationX(1);
        view.setTranslationY(1);
        view.setScaleX(2.0f);
        view.setScaleY(2.0f);
        view.setRotation(1.0f);
        view.setRotationX(1.0f);
        view.setRotationY(1.0f);
        view.setAlpha(0.5f);

        AnimatorUtility.AnimatorInfo animatorInfo = AnimatorUtility.captureViewStatus(view);
        assertEquals(1.0f, animatorInfo.translationX, 0.0f);
        assertEquals(1.0f, animatorInfo.translationY, 0.0f);
        assertEquals(2.0f, animatorInfo.scaleX, 0.0f);
        assertEquals(2.0f, animatorInfo.scaleY, 0.0f);
        assertEquals(1.0f, animatorInfo.rotation, 0.0f);
        assertEquals(1.0f, animatorInfo.rotationX, 0.0f);
        assertEquals(1.0f, animatorInfo.rotationY, 0.0f);
        assertEquals(0.5f, animatorInfo.alpha, 0.0f);

        View view2 = new View(testActivity);
        AnimatorUtility.resetViewStatus(view2, animatorInfo);

        assertEquals(1.0f, view2.getTranslationX(), 0.0f);
        assertEquals(1.0f, view2.getTranslationY(), 0.0f);
        assertEquals(2.0f, view2.getScaleX(), 0.0f);
        assertEquals(2.0f, view2.getScaleY(), 0.0f);
        assertEquals(1.0f, view2.getRotation(), 0.0f);
        assertEquals(1.0f, view2.getRotationX(), 0.0f);
        assertEquals(1.0f, view2.getRotationY(), 0.0f);
        assertEquals(0.5f, view2.getAlpha(), 0.0f);
    }
}
