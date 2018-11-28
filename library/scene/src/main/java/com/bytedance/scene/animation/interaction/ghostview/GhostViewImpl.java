package com.bytedance.scene.animation.interaction.ghostview;

import android.graphics.Matrix;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.view.ViewGroup;

@RequiresApi(14)
interface GhostViewImpl {

    interface Creator {

        GhostViewImpl addGhost(View view, ViewGroup viewGroup, Matrix matrix);

        void removeGhost(View view);

    }

    void setVisibility(int visibility);

    /**
     * Reserves a call to {@link ViewGroup#endViewTransition(View)} at the time when the GhostView
     * starts drawing its real view.
     */
    void reserveEndViewTransition(ViewGroup viewGroup, View view);

}
