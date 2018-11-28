package com.bytedance.scene.animation.interaction.ghostview;

import android.graphics.Matrix;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

public class GhostViewUtils {
    private static final GhostViewImpl.Creator CREATOR;

    static {
        if (Build.VERSION.SDK_INT >= 21) {
            CREATOR = new GhostViewApi21.Creator();
        } else {
            CREATOR = new GhostViewApi14.Creator();
        }
    }

    public static GhostViewImpl addGhost(View view, ViewGroup viewGroup, Matrix matrix) {
        return CREATOR.addGhost(view, viewGroup, matrix);
    }

    public static void removeGhost(View view) {
        CREATOR.removeGhost(view);
    }
}
