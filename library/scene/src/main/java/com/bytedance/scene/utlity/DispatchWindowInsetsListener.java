package com.bytedance.scene.utlity;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

/**
 * Created by JiangQi on 8/24/18.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DispatchWindowInsetsListener implements View.OnApplyWindowInsetsListener {
    @Override
    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        WindowInsets copy = new WindowInsets(insets);
        ViewGroup viewGroup = (ViewGroup) v;
        final int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            viewGroup.getChildAt(i).dispatchApplyWindowInsets(copy);
        }
        return insets.consumeSystemWindowInsets();
    }
}
