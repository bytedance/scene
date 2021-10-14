package com.bytedance.scene.navigation;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.view.View;
import com.bytedance.scene.animation.TransitionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TransitionUtilsTests {
    @Test
    public void testMergeAnimators() {
        ValueAnimator animator1 = ValueAnimator.ofFloat(0.0f, 1.0f);
        ValueAnimator animator2 = ValueAnimator.ofInt(1, 100, 1000);
        Animator mergedAnimator = TransitionUtils.mergeAnimators(animator1, animator2);
        assertTrue(mergedAnimator instanceof AnimatorSet);
    }

    @Test
    public void testViewToBitmap() {
        ActivityController<NavigationSourceUtility.TestActivity> controller = Robolectric.buildActivity(NavigationSourceUtility.TestActivity.class).create().start().resume();
        NavigationSourceUtility.TestActivity testActivity = controller.get();
        View view = new View(testActivity);
        view.measure(View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(500, View.MeasureSpec.EXACTLY));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = TransitionUtils.viewToBitmap(view);
        assertNotNull(bitmap);
    }
}
