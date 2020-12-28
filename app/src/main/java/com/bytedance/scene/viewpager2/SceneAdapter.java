/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bytedance.scene.viewpager2;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.collection.LongSparseArray;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;

import java.util.Set;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;

//TODO support StatefulAdapter
public abstract class SceneAdapter extends RecyclerView.Adapter<SceneViewHolder> {
    private final GroupScene mGroupScene;

    private final LongSparseArray<UserVisibleHintGroupScene> mScenes = new LongSparseArray<>();
    private final LongSparseArray<Integer> mItemIdToViewHolder = new LongSparseArray<>();

    private SceneMaxLifecycleEnforcer mSceneMaxLifecycleEnforcer;

    private boolean mIsInGracePeriod = false;
    private boolean mHasStaleScenes = false;

    public SceneAdapter(@NonNull GroupScene groupScene) {
        mGroupScene = groupScene;
        super.setHasStableIds(true);
    }

    @CallSuper
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        mSceneMaxLifecycleEnforcer = new SceneMaxLifecycleEnforcer();
        mSceneMaxLifecycleEnforcer.register(recyclerView);
    }

    @CallSuper
    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        mSceneMaxLifecycleEnforcer.unregister(recyclerView);
        mSceneMaxLifecycleEnforcer = null;
    }

    @NonNull
    public abstract UserVisibleHintGroupScene createScene(int position);

    @NonNull
    @Override
    public final SceneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return SceneViewHolder.create(parent);
    }

    @Override
    public final void onBindViewHolder(final @NonNull SceneViewHolder holder, int position) {
        final long itemId = holder.getItemId();
        final int viewHolderId = holder.getContainer().getId();
        final Long boundItemId = itemForViewHolder(viewHolderId); // item currently bound to the VH
        if (boundItemId != null && boundItemId != itemId) {
            removeScene(boundItemId);
            mItemIdToViewHolder.remove(boundItemId);
        }

        mItemIdToViewHolder.put(itemId, viewHolderId); // this might overwrite an existing entry
        ensureScene(position);

        /** Special case when {@link RecyclerView} decides to keep the {@link container}
         * attached to the window, but not to the view hierarchy (i.e. parent is null) */
        final FrameLayout container = holder.getContainer();
        if (ViewCompat.isAttachedToWindow(container)) {
            if (container.getParent() != null) {
                throw new IllegalStateException("Design assumption violated.");
            }
            container.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    if (container.getParent() != null) {
                        container.removeOnLayoutChangeListener(this);
                        placeSceneInViewHolder(holder);
                    }
                }
            });
        }

        gcScenes();
    }

    private void gcScenes() {
        if (!mHasStaleScenes) {
            return;
        }

        // Remove Scenes for items that are no longer part of the data-set
        Set<Long> toRemove = new ArraySet<>();
        for (int ix = 0; ix < mScenes.size(); ix++) {
            long itemId = mScenes.keyAt(ix);
            if (!containsItem(itemId)) {
                toRemove.add(itemId);
                mItemIdToViewHolder.remove(itemId); // in case they're still bound
            }
        }

        // Remove Scenes that are not bound anywhere -- pending a grace period
        if (!mIsInGracePeriod) {
            mHasStaleScenes = false; // we've executed all GC checks

            for (int ix = 0; ix < mScenes.size(); ix++) {
                long itemId = mScenes.keyAt(ix);
                if (!isSceneViewBound(itemId)) {
                    toRemove.add(itemId);
                }
            }
        }

        for (Long itemId : toRemove) {
            removeScene(itemId);
        }
    }

    private boolean isSceneViewBound(long itemId) {
        if (mItemIdToViewHolder.containsKey(itemId)) {
            return true;
        }

        Scene scene = mScenes.get(itemId);
        if (scene == null) {
            return false;
        }

        View view = scene.getView();
        if (view == null) {
            return false;
        }

        return view.getParent() != null;
    }

    private Long itemForViewHolder(int viewHolderId) {
        Long boundItemId = null;
        for (int ix = 0; ix < mItemIdToViewHolder.size(); ix++) {
            if (mItemIdToViewHolder.valueAt(ix) == viewHolderId) {
                if (boundItemId != null) {
                    throw new IllegalStateException("Design assumption violated: "
                            + "a ViewHolder can only be bound to one item at a time.");
                }
                boundItemId = mItemIdToViewHolder.keyAt(ix);
            }
        }
        return boundItemId;
    }

    private void ensureScene(int position) {
        long itemId = getItemId(position);
        if (!mScenes.containsKey(itemId)) {
            UserVisibleHintGroupScene newScene = createScene(position);
            newScene.disableSupportRestore();//TODO support StatefulAdapter
            mScenes.put(itemId, newScene);
        }
    }

    @Override
    public final void onViewAttachedToWindow(@NonNull final SceneViewHolder holder) {
        placeSceneInViewHolder(holder);
        gcScenes();
    }

    /**
     * @param holder that has been bound to a Scene in the {@link #onBindViewHolder} stage.
     */
    @SuppressWarnings("WeakerAccess")
    // to avoid creation of a synthetic accessor
    void placeSceneInViewHolder(@NonNull final SceneViewHolder holder) {
        UserVisibleHintGroupScene scene = mScenes.get(holder.getItemId());
        if (scene == null) {
            throw new IllegalStateException("Design assumption violated.");
        }
        FrameLayout container = holder.getContainer();

        mGroupScene.add(container.getId(), scene, "f" + holder.getItemId());
        scene.setUserVisibleHint(false);
        mSceneMaxLifecycleEnforcer.updateSceneMaxLifecycle(false);
    }

    @Override
    public final void onViewRecycled(@NonNull SceneViewHolder holder) {
        final int viewHolderId = holder.getContainer().getId();
        final Long boundItemId = itemForViewHolder(viewHolderId); // item currently bound to the VH
        if (boundItemId != null) {
            removeScene(boundItemId);
            mItemIdToViewHolder.remove(boundItemId);
        }
    }

    @Override
    public final boolean onFailedToRecycleView(@NonNull SceneViewHolder holder) {
        return true;
    }

    private void removeScene(long itemId) {
        Scene scene = mScenes.get(itemId);
        if (scene == null) {
            return;
        }

        if (mGroupScene.isAdded(scene)) {
            mGroupScene.remove(scene);
        }
        mScenes.remove(itemId);
    }

    /**
     * Default implementation works for collections that don't add, move, remove items.
     * <p>
     * TODO(b/122670460): add lint rule
     * When overriding, also override {@link #containsItem(long)}.
     * <p>
     * If the item is not a part of the collection, return {@link RecyclerView#NO_ID}.
     *
     * @param position Adapter position
     * @return stable item id {@link RecyclerView.Adapter#hasStableIds()}
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Default implementation works for collections that don't add, move, remove items.
     * <p>
     * TODO(b/122670460): add lint rule
     * When overriding, also override {@link #getItemId(int)}
     */
    public boolean containsItem(long itemId) {
        return itemId >= 0 && itemId < getItemCount();
    }

    @Override
    public final void setHasStableIds(boolean hasStableIds) {
        throw new UnsupportedOperationException(
                "Stable Ids are required for the adapter to function properly, and the adapter "
                        + "takes care of setting the flag.");
    }

    /**
     * Pauses (STARTED) all Scenes that are attached and not a primary item.
     * Keeps primary item Scene RESUMED.
     */
    class SceneMaxLifecycleEnforcer {
        private ViewPager2.OnPageChangeCallback mPageChangeCallback;
        private RecyclerView.AdapterDataObserver mDataObserver;
        private ViewPager2 mViewPager;

        private long mPrimaryItemId = NO_ID;

        void register(@NonNull RecyclerView recyclerView) {
            mViewPager = inferViewPager(recyclerView);

            // signal 1 of 3: current item has changed
            mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageScrollStateChanged(int state) {
                    updateSceneMaxLifecycle(false);
                }

                @Override
                public void onPageSelected(int position) {
                    updateSceneMaxLifecycle(false);
                }
            };
            mViewPager.registerOnPageChangeCallback(mPageChangeCallback);

            // signal 2 of 3: underlying data-set has been updated
            mDataObserver = new DataSetChangeObserver() {
                @Override
                public void onChanged() {
                    updateSceneMaxLifecycle(true);
                }
            };
            registerAdapterDataObserver(mDataObserver);
        }

        void unregister(@NonNull RecyclerView recyclerView) {
            ViewPager2 viewPager = inferViewPager(recyclerView);
            viewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
            unregisterAdapterDataObserver(mDataObserver);
            mViewPager = null;
        }

        void updateSceneMaxLifecycle(boolean dataSetChanged) {
            if (mViewPager.getScrollState() != ViewPager2.SCROLL_STATE_IDLE) {
                return; // do not update while not idle to avoid jitter
            }

            if (mScenes.isEmpty() || getItemCount() == 0) {
                return; // nothing to do
            }

            final int currentItem = mViewPager.getCurrentItem();
            if (currentItem >= getItemCount()) {
                /** current item is yet to be updated; it is guaranteed to change, so we will be
                 * notified via {@link ViewPager2.OnPageChangeCallback#onPageSelected(int)}  */
                return;
            }

            long currentItemId = getItemId(currentItem);
            if (currentItemId == mPrimaryItemId && !dataSetChanged) {
                return; // nothing to do
            }

            Scene currentItemScene = mScenes.get(currentItemId);
            if (currentItemScene == null) {
                return;
            }

            mPrimaryItemId = currentItemId;

            UserVisibleHintGroupScene toResume = null;
            for (int ix = 0; ix < mScenes.size(); ix++) {
                long itemId = mScenes.keyAt(ix);
                UserVisibleHintGroupScene scene = mScenes.valueAt(ix);

                if (itemId != mPrimaryItemId) {
                    scene.setUserVisibleHint(false);
                } else {
                    toResume = scene; // itemId map key, so only one can match the predicate
                }
            }
            if (toResume != null) { // in case the Scene wasn't added yet
                if (!mGroupScene.isAdded(toResume)) {
                    mGroupScene.add(mItemIdToViewHolder.get(mPrimaryItemId), toResume, "f" + mPrimaryItemId);
                }
                toResume.setUserVisibleHint(true);
            }
        }

        @NonNull
        private ViewPager2 inferViewPager(@NonNull RecyclerView recyclerView) {
            ViewParent parent = recyclerView.getParent();
            if (parent instanceof ViewPager2) {
                return (ViewPager2) parent;
            }
            throw new IllegalStateException("Expected ViewPager2 instance. Got: " + parent);
        }
    }

    /**
     * Simplified {@link RecyclerView.AdapterDataObserver} for clients interested in any data-set
     * changes regardless of their nature.
     */
    private abstract static class DataSetChangeObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public abstract void onChanged();

        @Override
        public final void onItemRangeChanged(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeChanged(int positionStart, int itemCount,
                                             @Nullable Object payload) {
            onChanged();
        }

        @Override
        public final void onItemRangeInserted(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeRemoved(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public final void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            onChanged();
        }
    }
}
