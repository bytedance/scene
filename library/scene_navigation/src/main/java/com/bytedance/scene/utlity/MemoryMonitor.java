package com.bytedance.scene.utlity;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.FloatRange;
import androidx.annotation.RestrictTo;

@RestrictTo(LIBRARY_GROUP)
public class MemoryMonitor {

    private final float threshold;
    private final Runnable onReachThreshold;

    private final Runnable watcher = new Runnable() {
        @Override
        public void run() {
            Runtime runtime = Runtime.getRuntime();
            long dalvikMax = runtime.maxMemory();
            long dalvikUsed = runtime.totalMemory() - runtime.freeMemory();
            if (dalvikUsed > dalvikMax * threshold) {
                onReachThreshold.run();
            }
        }
    };

    public MemoryMonitor(@FloatRange(from = 0.0, to = 1F) float threshold, Runnable onReachThreshold) {
        this.threshold = threshold;
        this.onReachThreshold = onReachThreshold;
    }

    public void start() {
        GcWatchers.addGcWatcher(watcher);
    }

    public void stop() {
        GcWatchers.removeGcWatcher(watcher);
    }
}
