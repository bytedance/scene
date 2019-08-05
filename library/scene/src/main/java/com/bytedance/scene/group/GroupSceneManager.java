package com.bytedance.scene.group;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationOrAnimator;
import com.bytedance.scene.animation.AnimationOrAnimatorFactory;
import com.bytedance.scene.parcel.ParcelConstants;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.SceneInstanceUtility;
import com.bytedance.scene.utlity.SceneInternalException;
import com.bytedance.scene.utlity.Utility;

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

    String className;

    @Nullable
    Bundle bundle;

    protected GroupRecord(@NonNull Parcel in) {
        viewId = in.readInt();
        tag = Utility.requireNonNull(in.readString(), "tag not found in Parcel");
        isHidden = in.readByte() != 0;
        isCurrentFocus = in.readByte() != 0;
        className = Utility.requireNonNull(in.readString(), "class name not found in Parcel");
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
        dest.writeString(scene.getClass().getName());
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

    public void saveToBundle(Bundle bundle) {
        bundle.putParcelableArrayList(KEY_TAG, new ArrayList<Parcelable>(mSceneList));
    }

    public void restoreFromBundle(Context context, Bundle bundle) {
        if (this.mSceneList != null && this.mSceneList.size() > 0) {
            throw new IllegalStateException("mSceneList size is not zero, Scene is added before restore");
        }
        this.mSceneList = new ArrayList<>(bundle.<GroupRecord>getParcelableArrayList(KEY_TAG));
        for (GroupRecord record : this.mSceneList) {
            record.scene = SceneInstanceUtility.getInstanceFromClassName(context, record.className, null);
        }
    }

    public void clear() {
        this.mSceneList.clear();
    }
}

class GroupSceneManager {
    private GroupScene mGroupScene;
    private ViewGroup mView;
    private GroupRecordList mSceneList = new GroupRecordList();
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static final HashMap<Scene, CancellationSignal> SCENE_RUNNING_ANIMATION_CANCELLATION_SIGNAL_MAP = new HashMap<>();
    private final Set<Scene> mCurrentTrackMoveStateSceneSet = new HashSet<>();

    GroupSceneManager() {

    }

    public void setGroupScene(GroupScene groupScene) {
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
        operation.execute(EMPTY_RUNNABLE);
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
    public void commitTransaction() {
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

                if (initState == dstState) {
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

                    executeOperation(new MoveStateOperation(scene, addOperation.viewId, addOperation.tag, dstState, forceShow, forceHide, forceRemove));
                } else {
                    executeOperation(new MoveStateOperation(scene, View.NO_ID, null, dstState, forceShow, forceHide, forceRemove));
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
        if (this.mCurrentTrackMoveStateSceneSet.contains(scene)) {
            throw new IllegalStateException("Cant add/remove/show/hide " + scene.getClass().getSimpleName() + " before it finish previous add/remove/show/hide operation or in its lifecycle method");
        }
    }

    private void beginTrackSceneStateChange(@NonNull Scene scene) {
        if (this.mCurrentTrackMoveStateSceneSet.contains(scene)) {
            throw new SceneInternalException("Target scene is already tracked");
        }
        this.mCurrentTrackMoveStateSceneSet.add(scene);
    }

    private void endTrackSceneStateChange(@NonNull Scene scene) {
        if (!this.mCurrentTrackMoveStateSceneSet.contains(scene)) {
            throw new SceneInternalException("Target scene is not tracked");
        }
        this.mCurrentTrackMoveStateSceneSet.remove(scene);
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

    public GroupRecord findByScene(Scene scene) {
        return mSceneList.findByScene(scene);
    }

    public GroupRecord findByTag(String tag) {
        return mSceneList.findByTag(tag);
    }

    public GroupRecord findByView(View view) {
        return mSceneList.findByView(view);
    }

    public int findSceneViewId(Scene scene) {
        return mSceneList.findByScene(scene).viewId;
    }

    public String findSceneTag(Scene scene) {
        return mSceneList.findByScene(scene).tag;
    }

    public List<Scene> getChildSceneList() {
        return mSceneList.getChildSceneList();
    }

    public List<GroupRecord> getChildSceneRecordList() {
        return mSceneList.getChildSceneRecordList();
    }

    private static final String KEY_TAG = ParcelConstants.KEY_GROUP_SCENE_MANAGER_TAG;

    public void saveToBundle(Bundle bundle) {
        this.mSceneList.saveToBundle(bundle);

        ArrayList<Bundle> bundleList = new ArrayList<>();
        List<Scene> childSceneList = this.getChildSceneList();
        for (int i = 0; i <= childSceneList.size() - 1; i++) {
            Scene scene = childSceneList.get(i);

            Bundle sceneBundle = new Bundle();
            scene.onSaveInstanceState(sceneBundle);
            bundleList.add(sceneBundle);
        }

        bundle.putParcelableArrayList(KEY_TAG, bundleList);
    }

    public void restoreFromBundle(Context context, Bundle bundle) {
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
            moveState(this.mGroupScene, scene, mGroupScene.getState(), true, new Runnable() {
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

    private class MoveStateOperation extends Operation {
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
        void execute(@NonNull Runnable operationEndAction) {
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

            boolean executeStateChange = scene.getState() != dstState;
            if (executeStateChange) {
                executeOnStart();
            }

            beginTrackSceneStateChange(scene);
            moveState(mGroupScene, scene, dstState, true, new Runnable() {
                @Override
                public void run() {
                    endTrackSceneStateChange(scene);
                }
            });

            if (forceShow) {
                mSceneList.findByScene(scene).isHidden = false;
            }
            if (forceHide) {
                mSceneList.findByScene(scene).isHidden = true;
            }
            if (forceRemove) {
                mSceneList.remove(mSceneList.findByScene(scene));
            }

            if (executeStateChange) {
                executeOnFinish();
            }
            operationEndAction.run();
        }

        protected void executeOnStart() {

        }

        protected void executeOnFinish() {

        }
    }

    private static State getMinState(State groupState, State state) {
        if (groupState.value < state.value) {
            return groupState;
        } else {
            return state;
        }
    }

    private class AddOperation extends MoveStateOperation {
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
        protected void executeOnFinish() {
            super.executeOnFinish();
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

    private class RemoveOperation extends MoveStateOperation {
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
        protected void executeOnStart() {
            super.executeOnStart();
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
        protected void executeOnFinish() {
            super.executeOnFinish();
            if (!this.isAnimating) {
                return;
            }
            this.dstVisibility = this.sceneView.getVisibility();
            this.sceneView.setVisibility(View.VISIBLE);
        }
    }

    private class HideOperation extends MoveStateOperation {
        private final AnimationOrAnimatorFactory animationOrAnimatorFactory;

        private HideOperation(Scene scene, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
            super(scene, View.NO_ID, null, getMinState(State.ACTIVITY_CREATED, mGroupScene.getState()), false, true, false);
            this.animationOrAnimatorFactory = animationOrAnimatorFactory;
        }

        @Override
        protected void executeOnFinish() {
            super.executeOnFinish();
            final View sceneView = this.scene.getView();
            if (sceneView == null) {
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

    private class ShowOperation extends MoveStateOperation {
        private final AnimationOrAnimatorFactory animationOrAnimatorFactory;

        private ShowOperation(Scene scene, AnimationOrAnimatorFactory animationOrAnimatorFactory) {
            super(scene, View.NO_ID, null, getMinState(State.RESUMED, mGroupScene.getState()), true, false, false);
            this.animationOrAnimatorFactory = animationOrAnimatorFactory;
        }

        @Override
        protected void executeOnFinish() {
            super.executeOnFinish();
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

    //modifyViewHierarchy，因为GroupScene自己生命周期引发的同步Child状态时候是false
    //其他情况下是true，需要修改View属性，比如添加删除，显示隐藏View
    //原因在于，Pop动画，如果因为生命周期引发的同步移除了View，那么Pop动画就没法看了，因为View都被移除了
    //同样是到CREATED，手动的remove必须移除View，但是Pop parent引发的，必须View还在

    //原则上，从低到高，那么必须走完流程设置状态
    //从高到底，第一个方法就应该设置好状态
    private static void moveState(@NonNull GroupScene groupScene,
                                  @NonNull Scene scene, @NonNull State to,
                                  boolean modifyViewHierarchy,
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
                    scene.dispatchAttachActivity(groupScene.getActivity());
                    scene.dispatchAttachScene(groupScene);
                    record = groupScene.getGroupSceneManager().findByScene(scene);
                    sceneBundle = record.bundle;
                    scene.dispatchCreate(sceneBundle);
                    ViewGroup containerView = groupScene.findContainerById(groupScene.getGroupSceneManager().findSceneViewId(scene));
                    scene.dispatchCreateView(sceneBundle, containerView);
                    /*
                     * Usually the lifecycle triggered state does not modify the View state,
                     * But if this Scene was added long ago, it is not in the View tree at the beginning,
                     * we need to add it in this case.
                     */
                    if (modifyViewHierarchy || scene.getView().getParent() == null) {
                        containerView.addView(scene.getView());
                        // It is possible to be in this state when recovery after destroying, so set it to GONE.
                        scene.getView().setVisibility(View.GONE);
                    }
                    moveState(groupScene, scene, to, modifyViewHierarchy, endAction);
                    break;
                case VIEW_CREATED:
                    record = groupScene.getGroupSceneManager().findByScene(scene);
                    sceneBundle = record.bundle;
                    scene.dispatchActivityCreated(sceneBundle);
                    record.bundle = null;
                    moveState(groupScene, scene, to, modifyViewHierarchy, endAction);
                    break;
                case ACTIVITY_CREATED:
                    // Whether modifyViewHierarchy is true or not, it must be set to visible
                    scene.getView().setVisibility(View.VISIBLE);
                    scene.dispatchStart();
                    moveState(groupScene, scene, to, modifyViewHierarchy, endAction);
                    break;
                case STARTED:
                    scene.dispatchResume();
                    moveState(groupScene, scene, to, modifyViewHierarchy, endAction);
                    break;
            }
        } else {
            switch (currentState) {
                case RESUMED:
                    scene.dispatchPause();
                    moveState(groupScene, scene, to, modifyViewHierarchy, endAction);
                    break;
                case STARTED:
                    scene.dispatchStop();
                    if (modifyViewHierarchy) {
                        scene.getView().setVisibility(View.GONE);
                    }
                    moveState(groupScene, scene, to, modifyViewHierarchy, endAction);
                    break;
                case ACTIVITY_CREATED:
                    if (to == State.VIEW_CREATED) {
                        throw new IllegalArgumentException("cant switch state ACTIVITY_CREATED to VIEW_CREATED");
                    }
                    //continue
                case VIEW_CREATED:
                    View view = scene.getView();
                    scene.dispatchDestroyView();
                    if (modifyViewHierarchy) {
                        /*
                         * We don't want to remove all the View all when in the stop() triggered by the life cycle.
                         * Otherwise, Pop animation will be hard to see.
                         */
                        Utility.removeFromParentView(view);
                    }
                    scene.dispatchDestroy();
                    scene.dispatchDetachScene();
                    scene.dispatchDetachActivity();
                    moveState(groupScene, scene, to, modifyViewHierarchy, endAction);
                    break;
            }
        }
    }
}
