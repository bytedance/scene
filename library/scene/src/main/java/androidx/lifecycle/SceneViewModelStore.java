package androidx.lifecycle;


import java.util.HashMap;

public final class SceneViewModelStore {

    private final HashMap<String, ViewModel> mMap = new HashMap<>();

    public synchronized void put(String key, ViewModel viewModel) {
        ViewModel oldViewModel = mMap.put(key, viewModel);
        if (oldViewModel != null) {
            oldViewModel.onCleared();
        }
    }

    public synchronized ViewModel get(String key) {
        return mMap.get(key);
    }

    /**
     * Clears internal storage and notifies ViewModels that they are no longer used.
     */
    public synchronized void clear() {
        for (ViewModel vm : mMap.values()) {
            vm.onCleared();
        }
        mMap.clear();
    }
}