package com.bytedance.scene.animation.interaction.transition;

import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by JiangQi on 10/8/22.
 */
public class GhostViewUtilsCompatibleInvokeLayer {
    public static void addGhost(@NonNull View view, @NonNull ViewGroup viewGroup,
                                @Nullable Matrix matrix) {
        GhostViewUtils.addGhost(view, viewGroup, matrix);
    }

    public static void removeGhost(View view) {
        GhostViewUtils.removeGhost(view);
    }
}
