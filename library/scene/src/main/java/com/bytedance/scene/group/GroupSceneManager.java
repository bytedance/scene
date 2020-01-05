/*
 * Copyright (C) 2019 ByteDance Inc
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
package com.bytedance.scene.group;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.SceneTrace;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationOrAnimator;
import com.bytedance.scene.animation.AnimationOrAnimatorFactory;
import com.bytedance.scene.navigation.NavigationScene;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.Utility;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.*;

/**
 * Created by JiangQi on 7/30/18.
 *
 * Require GroupScene to be inResume state, if not, cache it.
 *
 * All operations are performed immediately,
 * batch operations are performed after commit,
 * and lifecycle callbacks are performed on the spot.
 */
class GroupRecord implements Parcelable {
    @IdRes
    int viewId = View.NO_ID;
    Scene scene;
    String tag;
    boolean isHidden = false;
    boolean isCurrentFocus = false;
    String sceneClassName;
    @Nullable
    Bundle bundle;

    protected GroupRecord(@NonNull Parcel in) {
        viewId = in.readInt();
        tag = Utility.requireNonNull(in.readString(), "tag not found in Parcel");
        isHidden = in.readByte() != 0;
        isCurrentFocus = in.readByte() != 0;
        sceneClassName = Utility.requireNonNull(in.readString(), "class name not found in Parcel");
    }

    public static final Creator<GroupRecord> CREATOR = new Creator<GroupRecord>() {
        @Override
        public GroupRecord createFromParcel(Parcel in) {
            return new GroupRecord(in);
        }

        @Override
        public GroupRecord[] newArray(int size) {
            return new GroupRecord[size];
        }
    };

    public GroupRecord() {

    }

    static GroupRecord newInstance(@IdRes int viewId, @NonNull Scene scene, @NonNull String tag) {
        GroupRecord record = new GroupRecord();
        record.viewId = viewId;
        record.scene = Utility.requireNonNull(scene, "scene can't be null");
        record.tag = Utility.requireNonNull(tag, "tag can't be null");
        record.sceneClassName = Utility.requireNonNull(scene.getClass().getName(), "Scene class name is null");
        return record;
    }

    public void setHidden(boolean hidden) {
        this.isHidden = hidden;
    }

    public boolean isHidden() {
        return this.isHidden;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(viewId);
        dest.writeString(tag);
        dest.writeByte((byte) (isHidden ? 1 : 0));
        dest.writeByte((byte) (isCurrentFocus ? 1 : 0));
        dest.writeString(sceneClassName);
    }
}

class GroupRecordList {
    private static final String KEY_TAG = ParcelConstants.KEY_GROUP_RECORD_LIST;

    private List<GroupRecord> mSceneList = new ArrayList<>();

    public void add(GroupRecord record) {
        mSceneList.add(record);
    }

    public void remove(GroupRecord record) {
        mSceneList.remove(record);
    }

    public GroupRecord findByScene(Scene scene) {
        GroupRecord groupRecord = null;
        for (GroupRecord record : mSceneList) {
            if (record.scene == scene) {
                groupRecord = record;
                break;
            }
        }
        return groupRecord;
    }

    public GroupRecord findByTag(String tag) {
        GroupRecord groupRecord = null;
        for (GroupRecord record : mSceneList) {
            if (tag.equals(record.tag)) {
                groupRecord = record;
                break;
            }
        }
        return groupRecord;
    }

    public GroupRecord findByView(View view) {
        GroupRecord groupRecord = null;
        for (GroupRecord record : mSceneList) {
            if (view.equals(record.scene.getView())) {
                groupRecord = record;
                break;
            }
        }
        return groupRecord;
    }

    public List<Scene> getChildSceneList() {
        List<Scene> sceneList = new ArrayList<>();
        for (GroupRecord record : mSceneList) {
            sceneList.add(record.scene);
        }
        return Collections.unmodifiableList(sceneList);
    }

    public List<GroupRecord> getChildSceneRecordList() {
        return Collections.unmodifiableList(mSceneList);
    }

    public void saveToBundle(@NonNull Bundle bundle) {
        bundle.putParcelableArrayList(KEY_TAG, new ArrayList<Parcelable>(mSceneList));
    }

    public void restoreFromBundle(@NonNull Context context, @NonNull Bundle bundle) {
        if (this.mSceneList != null && this.mSceneList.size() > 0) {
            throw new IllegalStateException("mSceneList size is not zero, Scene is added before restore");
        }
        this.mSceneList = new ArrayList<>(bundle.<GroupRecord>getParcelableArrayList(KEY_TAG));
        for (GroupRecord record : this.mSceneList) {
            record.scene = SceneInstanceUtility.getInstanceFromClassName(context, record.sceneClassName, null);
        }
    }

    public void clear() {
        this.mSceneList.clear();
    }
}

class GroupSceneManager {
    private static final String TRACE_EXECUTE_OPERATION_TAG = "GroupSceneManager#executeOperation";

    @NonNull
    private final GroupScene mGroupScene;
    @Nullable
    private ViewGroup mView;
    @NonNull
    private final GroupRecordList mSceneList = new GroupRecordList();
    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @NonNull
    private static final HashMap<Scene, CancellationSignal> SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP = new HashMap<>();
    @NonNull
    private final Set<Pair<Scene, String>> mCurrentTrackMoveStateSceneSet = new HashSet<>();

    GroupSceneManager(@NonNull GroupScene groupScene) {
        this.mGroupScene = groupScene;
    }

    public void setView(ViewGroup view) {
        this.mView = view;
    }

    private static final Runnable EMPTY_RUNNABLE = new Runnable() {
        @Override
        public void run() {

        }
    };

    private void executeOperation(final Operation operation) {
        SceneTrace.beginSection(TRACE_EXECUTE_OPERATION_TAG);
        operation.execute(EMPTY_RUNNABLE);
        SceneTrace.endSection();
    }

    private boolean mIsInTransaction = false;
    private List<Operation> mOperationTransactionList = new ArrayList<>();

    public void beginTransaction() {
        if (mIsInTransaction) {
            throw new IllegalStateException("you must call commitTransaction before another beginTransaction");
        }
        mIsInTransaction = true;
    }

    /**
     * TODO: What if there are more than one Scene Tag repeated?
     */
    void commitTransaction() {
        if (!mIsInTransaction) {
            throw new IllegalStateException("you must call beginTransaction before commitTransaction");
        }
        if (mOperationTransactionList.size() > 0) {
            Iterator<Operation> iterator = mOperationTransactionList.iterator();

            LinkedHashMap<Scene, List<Operation>> map = new LinkedHashMap<>();
            while (iterator.hasNext()) {
                Operation operation = iterator.next();
                List<Operation> list = map.get(operation.scene);
                if (list == null) {
                    list = new ArrayList<>();
                    map.put(operation.scene, list);
                }
                list.add(operation);
            }

            Set<Scene> set = map.keySet();
            for (Scene scene : set) {
                List<Operation> list = map.get(scene);

                State initState = scene.getState();
                State dstState = list.get(list.size() - 1).state;

                boolean forceShow = list.get(list.size() - 1).forceShow;
                boolean forceHide = list.get(list.size() - 1).forceHide;
                boolean forceRemove = list.get(list.size() - 1).forceRemove;

                if (initState == dstState && !forceShow && !forceHide && !forceRemove) {
                    //nothing changed
                    continue;
                }

                if (initState == State.NONE) {
                    AddOperation addOperation = getAddOperation(list);
                    if (addOperation == null) {
                        throw new IllegalStateException("you must add Scene first");
                    }

                    if (findByTag(addOperation.tag) != null) {
                        throw new IllegalStateException("already have a Scene with tag " + addOperation.tag);
                    }

                    executeOperation(new TransactionOperation(scene, addOperation.viewId, addOperation.tag, dstState, forceShow, forceHide, forceRemove));
                } else {
                    executeOperation(new TransactionOperation(scene, View.NO_ID, null, dstState, forceShow, forceHide, forceRemove));
                }
            }
            mOperationTransactionList.clear();
        }
        mIsInTransaction = false;
    }

    private static AddOperation getAddOperation(List<Operation> list) {
        for (int i = list.size() - 1; i >= 0; i--) {
            Operation operation = list.get(i);
            if (operation instanceof AddOperation) {
                return (AddOperation) operation;
            }
        }
        return null;
    }

    /**
     * GroupScene don't allow child Scene modify its state in its lifecycle method，for example
     * 1 child Scene invoke parent's remove method to remove itself in its onActivityCreated lifecycle method
     * 2 child Scene invoke parent's remove method to hide itself in its onResume lifecycle method
     * but child Scene can invoke parent's add/remove/show/hide to operate other child Scene
     */
    private void checkStateChange(@NonNull Scene scene) {
        for (Pair<Scene, String> pair : this.mCurrentTrackMoveStateSceneSet) {
            if (pair.first == scene) {
                throw new IllegalStateException("Cant add/remove/show/hide " + scene.getClass().getSimpleName() + " before it finish previous add/remove/show/hide operation or in its lifecycle method");
            }
        }
    }

    private void beginTrackSceneStateChange(@NonNull Scene scene) {
        for (Pair<Scene, String> pair : this.mCurrentTrackMoveStateSceneSet) {
            if (pair.first == scene) {
                throw new SceneInternalException("Target scene is already tracked");
            }
        }
        //forbid NavigationScene execute navigation stack operation immediately, otherwise GroupScene may sync lifecycle to child,
        //then throw SceneInternalException("Target scene is already tracked")
        NavigationScene navigationScene = mGroupScene.getNavigationScene();
        String suppressTag = null;
        if (navigationScene != null) {
            suppressTag = navigationScene.beginSuppressStackOperation(scene.toString());
        } else {
            //execute GroupScene operations before GroupScene attached or after detached
            suppressTag = null;
        }
        this.mCurrentTrackMoveStateSceneSet.add(Pair.create(scene, suppressTag));
    }

    private void endTrackSceneStateChange(@NonNull Scene scene) {
        Pair<Scene, String> target = null;
        for (Pair<Scene, String> pair : this.mCurrentTrackMoveStateSceneSet) {
            if (pair.first == scene) {
                target = pair;
                break;
            }
        }
        if (target == null) {
            throw new SceneInternalException("Target scene is not tracked");
        }
        String suppressTag = target.second;
        if (suppressTag != null) {
            mGroupScene.getNavigationScene().endSuppressStackOperation(target.second);
        }
        this.mCurrentTrackMoveStateSceneSet.remove(target);
    }

    public void add(int viewId, Scene scene, String tag, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
        checkStateChange(scene);
        final Operation operation = new AddOperation(viewId, scene, tag, animationOrAnimatorFactory);
        if (mIsInTransaction) {
            mOperationTransactionList.add(operation);
        } else {
            executeOperation(operation);
        }
    }

    public void remove(Scene scene, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
        checkStateChange(scene);
        if (!mIsInTransaction && mSceneList.findByScene(scene) == null) {
            throw new IllegalStateException("Target scene is not find");
        }
        final Operation operation = new RemoveOperation(scene, animationOrAnimatorFactory);
        if (mIsInTransaction) {
            mOperationTransactionList.add(operation);
        } else {
            executeOperation(operation);
        }
    }

    public void clear() {
        mSceneList.clear();
    }

    public void hide(Scene scene, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
        checkStateChange(scene);
        if (!mIsInTransaction && mSceneList.findByScene(scene) == null) {
            throw new IllegalStateException("Target scene is not find");
        }
        final Operation operation = new HideOperation(scene, animationOrAnimatorFactory);
        if (mIsInTransaction) {
            mOperationTransactionList.add(operation);
        } else {
            executeOperation(operation);
        }
    }

    public void show(Scene scene, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
        checkStateChange(scene);
        if (!mIsInTransaction && mSceneList.findByScene(scene) == null) {
            throw new IllegalStateException("Target scene is not find");
        }
        final Operation operation = new ShowOperation(scene, animationOrAnimatorFactory);
        if (mIsInTransaction) {
            mOperationTransactionList.add(operation);
        } else {
            executeOperation(operation);
        }
    }

    void dispatchChildrenState(State state) {
        List<Scene> childSceneList = this.getChildSceneList();
        for (int i = 0; i <= childSceneList.size() - 1; i++) {
            final Scene scene = childSceneList.get(i);
            //may be removed by other child Scene
            if (containsScene(scene)) {
                beginTrackSceneStateChange(scene);
                GroupSceneManager.moveState(mGroupScene, scene, state, false, new Runnable() {
                    @Override
                    public void run() {
                        endTrackSceneStateChange(scene);
                    }
                });
            }
        }
    }

    void dispatchVisibleChildrenState(State state) {
        List<GroupRecord> list = this.getChildSceneRecordList();
        for (int i = 0; i <= list.size() - 1; i++) {
            GroupRecord record = list.get(i);
            if (!record.isHidden) {
                final Scene scene = record.scene;
                //may be removed by other child Scene
                if (containsScene(scene)) {
                    beginTrackSceneStateChange(scene);
                    GroupSceneManager.moveState(mGroupScene, record.scene, state, false, new Runnable() {
                        @Override
                        public void run() {
                            endTrackSceneStateChange(scene);
                        }
                    });
                }
            }
        }
    }

    @Nullable
    GroupRecord findByScene(@NonNull Scene scene) {
        return mSceneList.findByScene(scene);
    }

    @Nullable
    GroupRecord findByTag(@NonNull String tag) {
        return mSceneList.findByTag(tag);
    }

    @Nullable
    GroupRecord findByView(@NonNull View view) {
        return mSceneList.findByView(view);
    }

    int findSceneViewId(@NonNull Scene scene) {
        return mSceneList.findByScene(scene).viewId;
    }

    @NonNull
    String findSceneTag(@NonNull Scene scene) {
        return mSceneList.findByScene(scene).tag;
    }

    @NonNull
    List<Scene> getChildSceneList() {
        return mSceneList.getChildSceneList();
    }

    private List<GroupRecord> getChildSceneRecordList() {
        return mSceneList.getChildSceneRecordList();
    }

    private static final String KEY_TAG = ParcelConstants.KEY_GROUP_SCENE_MANAGER_TAG;

    void saveToBundle(@NonNull Bundle bundle) {
        this.mSceneList.saveToBundle(bundle);

        ArrayList<Bundle> bundleList = new ArrayList<>();
        List<Scene> childSceneList = this.getChildSceneList();
        for (int i = 0; i <= childSceneList.size() - 1; i++) {
            Scene scene = childSceneList.get(i);

            Bundle sceneBundle = new Bundle();
            scene.dispatchSaveInstanceState(sceneBundle);
            bundleList.add(sceneBundle);
        }

        bundle.putParcelableArrayList(KEY_TAG, bundleList);
    }

    void restoreFromBundle(@NonNull Context context, @NonNull Bundle bundle) {
        this.mSceneList.restoreFromBundle(context, bundle);
        List<GroupRecord> childSceneList = this.mSceneList.getChildSceneRecordList();
        if (childSceneList.size() == 0) {
            return;
        }

        ArrayList<Bundle> bundleList = bundle.getParcelableArrayList(KEY_TAG);
        for (int i = 0; i <= childSceneList.size() - 1; i++) {
            GroupRecord record = childSceneList.get(i);
            final Scene scene = record.scene;
            record.bundle = bundleList.get(i);

            //may be removed by other child Scene, but because restoreFromBundle is invoked at GroupScene onCreate,
            //so this should not happen
            if (!containsScene(scene)) {
                throw new SceneInternalException("Scene is not found");
            }
            beginTrackSceneStateChange(scene);
            moveState(this.mGroupScene, scene, mGroupScene.getState(), false, new Runnable() {
                @Override
                public void run() {
                    endTrackSceneStateChange(scene);
                }
            });
        }
    }

    private abstract class Operation {
        @NonNull
        final Scene scene;
        @NonNull
        final State state;
        final boolean forceShow;
        /**
         * Forced display and hiding must be distinguished from normal related to the life cycle.
         * Can't just rely on DstState, otherwise it's very easy to make mistakes, mix together mess
         */
        final boolean forceHide;
        final boolean forceRemove;

        Operation(@NonNull Scene scene, @NonNull State state, boolean forceShow, boolean forceHide, boolean forceRemove) {
            this.scene = scene;
            this.state = state;
            this.forceShow = forceShow;
            this.forceHide = forceHide;
            this.forceRemove = forceRemove;
        }

        abstract void execute(@NonNull Runnable operationEndAction);
    }

    private boolean containsScene(@NonNull Scene scene) {
        List<GroupRecord> recordList = getChildSceneRecordList();
        for (int i = 0; i < recordList.size(); i++) {
            if (recordList.get(i).scene == scene) {
                return true;
            }
        }
        return false;
    }

    private abstract class MoveStateOperation extends Operation {
        @IdRes
        final int viewId;
        @Nullable
        final String tag;
        @NonNull
        final State dstState;

        MoveStateOperation(@NonNull Scene scene, @IdRes int viewId, @Nullable String tag, @NonNull State dstState, boolean forceShow, boolean forceHide, boolean forceRemove) {
            super(scene, dstState, forceShow, forceHide, forceRemove);
            if (forceShow && forceHide) {
                throw new IllegalArgumentException("cant forceShow with forceHide");
            }

            this.viewId = viewId;
            this.tag = tag;
            this.dstState = dstState;
        }

        @Override
        final void execute(@NonNull Runnable operationEndAction) {
            CancellationSignal cancellationSignal = SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.get(scene);
            if (cancellationSignal != null) {
                cancellationSignal.cancel();
                if (SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.get(scene) != null) {
                    throw new SceneInternalException("CancellationSignal cancel callback should remove target Scene from CancellationSignal map");
                }
            }

            if (!containsScene(scene)) {
                if (scene.getState() == State.NONE) {
                    Utility.requireNonNull(tag, "tag can't be null");
                    mSceneList.add(GroupRecord.newInstance(viewId, scene, tag));
                } else {
                    throw new SceneInternalException("Scene state is " + scene.getState().name + " but it is not added to record list");
                }
            }

            if (forceShow) {
                mSceneList.findByScene(scene).isHidden = false;
            }
            if (forceHide) {
                mSceneList.findByScene(scene).isHidden = true;
            }

            boolean executeStateChange = scene.getState() != dstState;
            executeOnStart(executeStateChange);

            beginTrackSceneStateChange(scene);
            moveState(mGroupScene, scene, dstState, forceRemove, new Runnable() {
                @Override
                public void run() {
                    endTrackSceneStateChange(scene);
                }
            });

            if (forceRemove) {
                mSceneList.remove(mSceneList.findByScene(scene));
            }

            executeOnFinish(executeStateChange);
            operationEndAction.run();
        }

        protected void executeOnStart(boolean stateChanged) {

        }

        protected void executeOnFinish(boolean stateChanged) {

        }
    }

    private static State getMinState(State groupState, State state) {
        if (groupState.value < state.value) {
            return groupState;
        } else {
            return state;
        }
    }

    private final class TransactionOperation extends MoveStateOperation {
        TransactionOperation(@NonNull Scene scene, int viewId, @Nullable String tag, @NonNull State dstState, boolean forceShow, boolean forceHide, boolean forceRemove) {
            super(scene, viewId, tag, dstState, forceShow, forceHide, forceRemove);
        }

        @Override
        protected void executeOnStart(boolean stateChanged) {
            super.executeOnStart(stateChanged);
            View view = this.scene.getView();
            if (view != null && forceShow) {
                setSceneViewVisibility(scene, View.VISIBLE);
            }
        }

        @Override
        protected void executeOnFinish(boolean stateChanged) {
            super.executeOnFinish(stateChanged);
            View view = this.scene.getView();
            if (view != null && forceHide) {
                setSceneViewVisibility(scene, View.GONE);
            }
        }
    }

    private final class AddOperation extends MoveStateOperation {
        final int viewId;
        final String tag;
        final AnimationOrAnimatorFactory animationOrAnimatorFactory;

        private AddOperation(int viewId, Scene scene, String tag, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
            super(scene, viewId, tag, getMinState(State.RESUMED, mGroupScene.getState()), true, false, false);
            this.viewId = viewId;
            this.tag = tag;
            this.animationOrAnimatorFactory = animationOrAnimatorFactory;
        }

        @Override
        protected void executeOnFinish(boolean stateChanged) {
            super.executeOnFinish(stateChanged);
            if (!stateChanged) {
                return;
            }
            final AnimationOrAnimator animationOrAnimator = animationOrAnimatorFactory.getAnimationOrAnimator();
            if (animationOrAnimator == null) {
                return;
            }
            View view = this.scene.getView();
            if (view == null) {
                return;
            }
            animationOrAnimator.addEndAction(new Runnable() {
                @Override
                public void run() {
                    SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.remove(scene);
                }
            });
            SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.put(this.scene, new CancellationSignal() {
                @Override
                public void cancel() {
                    super.cancel();
                    animationOrAnimator.end();
                }
            });
            animationOrAnimator.start(view);
        }
    }

    private final class RemoveOperation extends MoveStateOperation {
        private final AnimationOrAnimatorFactory animationOrAnimatorFactory;
        private final boolean canAnimation;
        private final View sceneView;
        private final ViewGroup parentViewGroup;
        private boolean isAnimating = false;
        private int dstVisibility = View.VISIBLE;

        private RemoveOperation(Scene scene, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
            super(scene, View.NO_ID, null, State.NONE, false, false, true);
            this.animationOrAnimatorFactory = animationOrAnimatorFactory;
            this.canAnimation = scene.getView() != null && scene.getView().getParent() != null;
            if (this.canAnimation) {
                this.sceneView = scene.getView();
                this.parentViewGroup = (ViewGroup) this.sceneView.getParent();
            } else {
                this.sceneView = null;
                this.parentViewGroup = null;
            }
        }

        @Override
        protected void executeOnStart(boolean stateChanged) {
            super.executeOnStart(stateChanged);
            if (!stateChanged) {
                return;
            }
            if (!canAnimation) {
                return;
            }

            final AnimationOrAnimator animationOrAnimator = this.animationOrAnimatorFactory.getAnimationOrAnimator();
            if (animationOrAnimator == null) {
                return;
            }

            /*
             * View try execute remove() without call the measure() or layout() first,
             * will result in a 0 of height and width, which can not be animated.
             */
            if (this.parentViewGroup != null && (this.sceneView.getWidth() == 0 || this.sceneView.getHeight() == 0)) {
                Log.w("GroupScene", "Scene view width or height is zero, skip animation");
                return;
            }

            animationOrAnimator.addEndAction(new Runnable() {
                @Override
                public void run() {
                    SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.remove(scene);
                    parentViewGroup.endViewTransition(sceneView);
                    sceneView.setVisibility(dstVisibility);
                }
            });

            SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.put(scene, new CancellationSignal() {
                @Override
                public void cancel() {
                    super.cancel();
                    animationOrAnimator.end();
                }
            });
            this.parentViewGroup.startViewTransition(this.sceneView);
            animationOrAnimator.start(this.sceneView);
            this.isAnimating = true;
        }

        @Override
        protected void executeOnFinish(boolean stateChanged) {
            super.executeOnFinish(stateChanged);
            if (!stateChanged) {
                return;
            }
            if (!this.isAnimating) {
                return;
            }
            this.dstVisibility = this.sceneView.getVisibility();
            this.sceneView.setVisibility(View.VISIBLE);
        }
    }

    private final class HideOperation extends MoveStateOperation {
        private final AnimationOrAnimatorFactory animationOrAnimatorFactory;

        private HideOperation(Scene scene, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
            super(scene, View.NO_ID, null, getMinState(State.ACTIVITY_CREATED, mGroupScene.getState()), false, true, false);
            this.animationOrAnimatorFactory = animationOrAnimatorFactory;
        }

        @Override
        protected void executeOnFinish(boolean stateChanged) {
            super.executeOnFinish(stateChanged);
            final View sceneView = this.scene.getView();
            if (sceneView == null) {
                return;
            }
            setSceneViewVisibility(scene, View.GONE);

            if (!stateChanged) {
                return;
            }

            final AnimationOrAnimator animationOrAnimator = this.animationOrAnimatorFactory.getAnimationOrAnimator();
            if (animationOrAnimator == null) {
                return;
            }

            final int dstVisibility = sceneView.getVisibility();
            sceneView.setVisibility(View.VISIBLE);
            animationOrAnimator.addEndAction(new Runnable() {
                @Override
                public void run() {
                    SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.remove(scene);
                    sceneView.setVisibility(dstVisibility);
                }
            });

            SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.put(scene, new CancellationSignal() {
                @Override
                public void cancel() {
                    super.cancel();
                    animationOrAnimator.end();
                }
            });
            animationOrAnimator.start(this.scene.getView());
        }
    }

    private final class ShowOperation extends MoveStateOperation {
        private final AnimationOrAnimatorFactory animationOrAnimatorFactory;

        private ShowOperation(Scene scene, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
            super(scene, View.NO_ID, null, getMinState(State.RESUMED, mGroupScene.getState()), true, false, false);
            this.animationOrAnimatorFactory = animationOrAnimatorFactory;
        }

        @Override
        protected void executeOnStart(boolean stateChanged) {
            super.executeOnStart(stateChanged);
            final View sceneView = this.scene.getView();
            if (sceneView == null) {
                return;
            }
            setSceneViewVisibility(scene, View.VISIBLE);
        }

        @Override
        protected void executeOnFinish(boolean stateChanged) {
            super.executeOnFinish(stateChanged);
            if (!stateChanged) {
                return;
            }
            final View sceneView = this.scene.getView();
            if (sceneView == null) {
                return;
            }

            final AnimationOrAnimator animationOrAnimator = this.animationOrAnimatorFactory.getAnimationOrAnimator();
            if (animationOrAnimator == null) {
                return;
            }
            animationOrAnimator.addEndAction(new Runnable() {
                @Override
                public void run() {
                    SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.remove(scene);
                }
            });

            SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP.put(scene, new CancellationSignal() {
                @Override
                public void cancel() {
                    super.cancel();
                    animationOrAnimator.end();
                }
            });
            animationOrAnimator.start(sceneView);
        }
    }

    /**
     * @hide
     */
    @IntDef({View.VISIBLE, View.INVISIBLE, View.GONE})
    @Retention(RetentionPolicy.SOURCE)
    @interface Visibility {
    }

    private static void setSceneViewVisibility(@NonNull Scene scene, @Visibility int visibility) {
        View view = scene.getView();
        if (view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    /**
     * forceRemove value is true when be invoked by GroupScene.remove()
     */
    private static void moveState(@NonNull GroupScene groupScene,
                                  @NonNull Scene scene, @NonNull State to,
                                  boolean forceRemove,
                                  @Nullable Runnable endAction) {
        State currentState = scene.getState();
        if (currentState == to) {
            if (endAction != null) {
                endAction.run();
            }
            return;
        }

        GroupRecord record = null;
        Bundle sceneBundle = null;
        if (currentState.value < to.value) {
            switch (currentState) {
                case NONE:
                    scene.dispatchAttachActivity(groupScene.requireActivity());
                    scene.dispatchAttachScene(groupScene);
                    record = groupScene.getGroupSceneManager().findByScene(scene);
                    sceneBundle = record.bundle;
                    scene.dispatchCreate(sceneBundle);
                    ViewGroup containerView = groupScene.findContainerById(groupScene.getGroupSceneManager().findSceneViewId(scene));
                    scene.dispatchCreateView(sceneBundle, containerView);
                    containerView.addView(scene.getView());
                    if (record.isHidden()) {
                        setSceneViewVisibility(scene, View.GONE);
                    }
                    moveState(groupScene, scene, to, forceRemove, endAction);
                    break;
                case VIEW_CREATED:
                    record = groupScene.getGroupSceneManager().findByScene(scene);
                    sceneBundle = record.bundle;
                    scene.dispatchActivityCreated(sceneBundle);
                    record.bundle = null;
                    moveState(groupScene, scene, to, forceRemove, endAction);
                    break;
                case ACTIVITY_CREATED:
                    scene.dispatchStart();
                    moveState(groupScene, scene, to, forceRemove, endAction);
                    break;
                case STARTED:
                    scene.dispatchResume();
                    moveState(groupScene, scene, to, forceRemove, endAction);
                    break;
            }
        } else {
            switch (currentState) {
                case RESUMED:
                    scene.dispatchPause();
                    moveState(groupScene, scene, to, forceRemove, endAction);
                    break;
                case STARTED:
                    scene.dispatchStop();
                    moveState(groupScene, scene, to, forceRemove, endAction);
                    break;
                case ACTIVITY_CREATED:
                    if (to == State.VIEW_CREATED) {
                        throw new IllegalArgumentException("cant switch state ACTIVITY_CREATED to VIEW_CREATED");
                    }
                    //continue
                case VIEW_CREATED:
                    View view = scene.getView();
                    scene.dispatchDestroyView();
                    if (forceRemove) {
                        /*
                         * case 1: Scene is removed from parent GroupScene, we should remove its view from parent's view hierarchy
                         * case 2: Parent GroupScene is pop from grandparent NavigationScene, this child Scene's state will sync to
                         * destroy state, but its view should not be removed, otherwise, pop animation will be hard to see.
                         */
                        Utility.removeFromParentView(view);
                    }
                    scene.dispatchDestroy();
                    scene.dispatchDetachScene();
                    scene.dispatchDetachActivity();
                    moveState(groupScene, scene, to, forceRemove, endAction);
                    break;
            }
        }
    }
}
