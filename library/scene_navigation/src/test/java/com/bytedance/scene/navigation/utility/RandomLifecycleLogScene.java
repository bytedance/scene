package com.bytedance.scene.navigation.utility;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.bytedance.scene.Scene;

public abstract class RandomLifecycleLogScene extends Scene {
    private final StringBuilder log;

    public RandomLifecycleLogScene(StringBuilder log) {
        this.log = log;
    }

    @Override
    public final void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        onLogActivityCreated();
    }

    @Override
    public final void onStart() {
        super.onStart();
        onLogStart();
    }

    @Override
    public final void onResume() {
        super.onResume();
        onLogResume();
    }

    @Override
    public final void onPause() {
        super.onPause();
        onLogPause();
    }

    @Override
    public final void onStop() {
        super.onStop();
        onLogStop();
    }

    @Override
    public final void onDestroyView() {
        super.onDestroyView();
        onLogDestroyView();
    }

    public void onLogActivityCreated() {
        log.append(String.valueOf(System.currentTimeMillis()));
    }

    public void onLogStart() {
        log.append(String.valueOf(System.currentTimeMillis()));
    }

    public void onLogResume() {
        log.append(String.valueOf(System.currentTimeMillis()));
    }

    public void onLogPause() {
        log.append(String.valueOf(System.currentTimeMillis()));
    }

    public void onLogStop() {
        log.append(String.valueOf(System.currentTimeMillis()));
    }

    public void onLogDestroyView() {
        log.append(String.valueOf(System.currentTimeMillis()));
    }
}
