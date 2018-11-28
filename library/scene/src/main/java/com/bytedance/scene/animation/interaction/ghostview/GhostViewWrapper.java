package com.bytedance.scene.animation.interaction.ghostview;

import android.graphics.Color;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GhostViewWrapper {
    private void addGhost(View view, ViewGroup viewGroup) {
        try {
            Class ghostViewClass = Class.forName("android.view.GhostView");
            Method addGhostMethod = ghostViewClass.getMethod("addGhost", View.class,
                    ViewGroup.class, Matrix.class);
            View ghostView = (View) addGhostMethod.invoke(null, view, viewGroup, null);
            ghostView.setBackgroundColor(Color.YELLOW);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static View getGhost(View view) {
        try {
            Class ghostViewClass = Class.forName("android.view.GhostView");
            Method addGhostMethod = ghostViewClass.getMethod("getGhost", View.class);
            View ghostView = (View) addGhostMethod.invoke(null, view);
            return ghostView;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
