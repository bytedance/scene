

package com.bytedance.scene;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

public class SceneViewModelProviders {
    private SceneViewModelProviders() {

    }

    private static Application checkApplication(Activity activity) {
        Application application = activity.getApplication();
        if (application == null) {
            throw new IllegalStateException("Your activity is not yet attached to "
                    + "Application. You can't request ViewModel before onCreate call.");
        }
        return application;
    }

    private static Activity checkActivity(Scene scene) {
        Activity activity = scene.getActivity();
        if (activity == null) {
            throw new IllegalStateException("Can't create ViewModelProvider for removed scene");
        }
        return activity;
    }

    @MainThread
    public static ViewModelProvider of(@NonNull Scene scene) {
        ViewModelProvider.AndroidViewModelFactory factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(
                        checkApplication(checkActivity(scene)));
        return new ViewModelProvider(scene.getViewModelStore(), factory);
    }

    @MainThread
    public static ViewModelProvider of(@NonNull Scene scene, @NonNull ViewModelProvider.Factory factory) {
        return new ViewModelProvider(scene.getViewModelStore(), factory);
    }
}
