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

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.*;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytedance.scene.Scene;
import com.bytedance.scene.State;
import com.bytedance.scene.animation.AnimationOrAnimator;
import com.bytedance.scene.animation.AnimationOrAnimatorFactory;
import com.bytedance.scene.interfaces.ChildSceneLifecycleCallbacks;
import com.bytedance.scene.utlity.*;

import java.util.ArrayList;
import java.util.List;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * Created by JiangQi on 8/1/18.
 *
 * The entire process of the synchronize child Scene:
 *
 * When entering:
 *
 * 1. Parent: onAttach -> onCreate -> onCreateView -> onViewCreated
 *    (synchronize the state of child Scene)
 * 2. Child: onAttach -> onCreate -> onCreateView -> onViewCreated
 * 3. Parent: onActivityCreated
 *    (synchronize the state of child Scene)
 * 4. Child: onActivityCreated
 * 5. Parent: onStart
 *    (synchronize the state of child Scene)
 * 6. Child: onStart
 * 7. Parent: onResume
 *    (synchronize the state of child Scene)
 * 8. Child: onResume
 *
 * When exiting:
 *
 *    (Force set state of Scene to State.STARTED)
 * 1. Child: onPause
 * 2. Parent: onPause
 *    (Force set state of Scene to State.ACTIVITY_CREATED)
 * 3. Child: onStop
 * 4. Parent: onStop
 *    (Force set state of Scene to State.NONE)
 * 3. Child: onDestroyView -> onDestroy -> onDetach
 * 4. Parent: onDestroyView -> onDestroy -> onDetach
 *
 * TODO: Must support transaction, so we can add and hide without trigger onResume().
 *       Otherwise, ViewPager will be difficult to handle.
 *
 *
 * common usage
 * ```
 * class TestChildScene : Scene() {
 *     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
 *         return View(requireSceneContext())
 *     }
 * }
 *
 * class TestGroupScene : GroupScene() {
 *     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
 *         val layout = FrameLayout(requireSceneContext())
 *         layout.id = View.generateViewId()
 *         return layout
 *     }
 *
 *     override fun onActivityCreated(savedInstanceState: Bundle?) {
 *         super.onActivityCreated(savedInstanceState)
 *         add(view.id, TestChildScene(), "Child_TAG")
 *     }
 * }
 * ```
 */
public abstract class GroupScene extends Scene {
    @NonNull
    private final GroupSceneManager mGroupSceneManager;
    @NonNull
    private final List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> mLifecycleCallbacks = new ArrayList<>();

    public GroupScene() {
        this.mGroupSceneManager = new GroupSceneManager(this);
    }

    @NonNull
    GroupSceneManager getGroupSceneManager() {
        return mGroupSceneManager;
    }

    @NonNull
    public final List<Scene> getSceneList() {
        return this.mGroupSceneManager.getChildSceneList();
    }

    public final void beginTransaction() {
        this.mGroupSceneManager.beginTransaction();
    }

    public final void commitTransaction() {
        this.mGroupSceneManager.commitTransaction();
    }

    private static final AnimationOrAnimatorFactory EMPTY_ANIMATION_FACTORY = new AnimationOrAnimatorFactory() {
        @Override
        public AnimationOrAnimator getAnimationOrAnimator() {
            return null;
        }
    };

    public final void add(@IdRes final int viewId, @NonNull final Scene scene, @NonNull final String tag) {
        add(viewId, scene, tag, EMPTY_ANIMATION_FACTORY);
    }

    public final void add(@IdRes final int viewId, @NonNull final Scene scene, @NonNull final String tag, @AnimRes @AnimatorRes final int animationResId) {
        add(viewId, scene, tag, buildAnimatorFactory(scene, animationResId));
    }

    private void add(@IdRes final int viewId, @NonNull final Scene scene, @NonNull final String tag, @NonNull AnimationOrAnimatorFactory animationOrAnimatorFactory) {
        ThreadUtility.checkUIThread();

        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag can't be empty");
        }

        if (scene == this) {
            throw new IllegalArgumentException("GroupScene can't be added to itself");
        }

        /*
         * TODO: It is possible for GroupScene to perform find + add 2 times,
         *       as the operations are all cached. (Fragment has this problem too)
         */

        if (isAdded(scene)) {
            int currentSceneContainerViewId = mGroupSceneManager.findSceneViewId(scene);
            if (currentSceneContainerViewId != viewId) {
                String currentViewIdName = null;
                try {
                    currentViewIdName = getResources().getResourceName(currentSceneContainerViewId);
                } catch (Resources.NotFoundException exception) {
                    currentViewIdName = String.valueOf(currentSceneContainerViewId);
                }
                throw new IllegalArgumentException("Scene is already added to another container, viewId " + currentViewIdName);
            }

            String currentSceneTag = mGroupSceneManager.findSceneTag(scene);
            if (!currentSceneTag.equals(tag)) {
                throw new IllegalArgumentException("Scene is already added, tag " + currentSceneTag);
            }
        } else {
            Scene previousScene = findSceneByTag(tag);
            if (previousScene != null) {
                throw new IllegalArgumentException("already have a Scene " + previousScene.toString() + " with tag " + tag);
            }
        }

        if (scene.getParentScene() != null && scene.getParentScene() != this) {
            throw new IllegalArgumentException("Scene already has a parent, parent " + scene.getParentScene());
        }

        if (isSupportRestore() && !SceneInstanceUtility.isSupportRestore(scene)) {
            throw new IllegalArgumentException("Scene " + scene.getClass().getName() + " must be a public class or public static class, " +
                    "and have only one parameterless constructor to be properly recreated from instance state.");
        }

        mGroupSceneManager.add(viewId, scene, tag, animationOrAnimatorFactory);
    }

    private boolean isSupportRestore() {
        return getNavigationScene() != null
                && getNavigationScene().isSupportRestore();
    }

    @Nullable
    public final <T extends Scene> T findSceneByTag(@NonNull String tag) {
        ThreadUtility.checkUIThread();

        if (tag == null) {
            return null;
        }

        GroupRecord record = this.mGroupSceneManager.findByTag(tag);
        if (record != null) {
            return (T) record.scene;
        } else {
            return null;
        }
    }

    public final void remove(@NonNull Scene scene) {
        this.remove(scene, EMPTY_ANIMATION_FACTORY);
    }

    public final void remove(@NonNull final Scene scene, @AnimRes @AnimatorRes final int animationResId) {
        this.remove(scene, buildAnimatorFactory(scene, animationResId));
    }

    private static AnimationOrAnimatorFactory buildAnimatorFactory(final Scene scene, @AnimRes @AnimatorRes final int animationResId) {
        return new AnimationOrAnimatorFactory() {
            @Override
            public AnimationOrAnimator getAnimationOrAnimator() {
                if (animationResId == 0) {
                    return null;
                }
                return AnimationOrAnimator.loadAnimation(scene.requireActivity(), animationResId);
            }
        };
    }

    private void remove(@NonNull final Scene scene, final AnimationOrAnimatorFactory factory) {
        ThreadUtility.checkUIThread();
        this.mGroupSceneManager.remove(scene, factory);
    }

    public final void hide(@NonNull Scene scene) {
        this.hide(scene, EMPTY_ANIMATION_FACTORY);
    }

    public final void hide(@NonNull final Scene scene, @AnimRes @AnimatorRes final int animationResId) {
        this.hide(scene, buildAnimatorFactory(scene, animationResId));
    }

    private void hide(@NonNull Scene scene, @NonNull AnimationOrAnimatorFactory factory) {
        ThreadUtility.checkUIThread();
        this.mGroupSceneManager.hide(scene, factory);
    }

    public final void show(@NonNull Scene scene) {
        this.show(scene, EMPTY_ANIMATION_FACTORY);
    }

    public final void show(@NonNull final Scene scene, @AnimRes @AnimatorRes final int animationResId) {
        this.show(scene, buildAnimatorFactory(scene, animationResId));
    }

    private void show(@NonNull Scene scene, @NonNull AnimationOrAnimatorFactory factory) {
        ThreadUtility.checkUIThread();
        this.mGroupSceneManager.show(scene, factory);
    }

    public final boolean isAdded(@NonNull Scene scene) {
        return this.mGroupSceneManager.findByScene(scene) != null;
    }

    public final boolean isShow(@NonNull Scene scene) {
        GroupRecord record = this.mGroupSceneManager.findByScene(scene);
        if (record == null) {
            return false;
        }
        return !record.isHidden;
    }

    @NonNull
    final ViewGroup findContainerById(int viewId) {
        ViewGroup viewGroup = (ViewGroup) getView().findViewById(viewId);
        if (viewGroup == null) {
            try {
                String viewIdName = getResources().getResourceName(viewId);
                throw new IllegalArgumentException(" " + viewIdName + " view not found");
            } catch (Resources.NotFoundException exception) {
                throw new IllegalArgumentException(" " + viewId + " view not found");
            }
        } else {
            ViewGroup tmp = viewGroup;
            while (tmp != null) {
                if (tmp == getView()) {
                    break;
                }
                GroupRecord record = mGroupSceneManager.findByView(tmp);
                if (record != null) {
                    throw new IllegalArgumentException(String.format("cant add Scene to child Scene %s view hierarchy ", record.scene.toString()));
                }
                tmp = (ViewGroup) tmp.getParent();
            }
        }
        return viewGroup;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.mGroupSceneManager.restoreFromBundle(requireActivity(), savedInstanceState);
        }
    }

    @Override
    public final void dispatchAttachActivity(@NonNull Activity activity) {
        super.dispatchAttachActivity(activity);
    }

    @Override
    public final void dispatchAttachScene(@Nullable Scene parentScene) {
        super.dispatchAttachScene(parentScene);
    }

    @Override
    public final void dispatchCreate(@Nullable Bundle savedInstanceState) {
        super.dispatchCreate(savedInstanceState);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchCreateView(@Nullable Bundle savedInstanceState, @NonNull ViewGroup container) {
        super.dispatchCreateView(savedInstanceState, container);
        View view = getView();
        if (!(view instanceof ViewGroup)) {
            throw new IllegalArgumentException("GroupScene onCreateView view must be ViewGroup");
        }
        this.mGroupSceneManager.setView((ViewGroup) getView());
        dispatchChildrenState(State.VIEW_CREATED);
        replacePlaceHolderViewToTargetScene();
    }

    private void replacePlaceHolderViewToTargetScene() {
        List<ScenePlaceHolderView> holderViewList = new ArrayList<>();
        extractScenePlaceHolder(holderViewList, (ViewGroup) requireView());
        if (holderViewList.size() == 0) {
            return;
        }

        if (isSupportRestore()) {
            //We can't handle user remove Scene, then add same tag Scene to another ViewGroup
            throw new IllegalStateException("ScenePlaceHolderView can only be used when support restore is disabled");
        }

        for (int i = 0, N = holderViewList.size(); i < N; i++) {
            ScenePlaceHolderView holderView = holderViewList.get(i);
            ViewGroup parent = (ViewGroup) holderView.getParent();
            int parentId = parent.getId();
            if (parentId == View.NO_ID) {
                throw new IllegalArgumentException("ScenePlaceHolderView parent id can't be View.NO_ID");
            }
            ViewGroup.LayoutParams layoutParams = holderView.getLayoutParams();
            String name = holderView.getSceneName();
            String tag = holderView.getSceneTag();

            Scene scene = SceneInstanceUtility.getInstanceFromClassName(requireSceneContext(), name, holderView.getArguments());
            int index = parent.indexOfChild(holderView);
            parent.removeView(holderView);
            add(parentId, scene, tag);
            View sceneView = scene.requireView();
            if (holderView.getId() != View.NO_ID) {
                if (sceneView.getId() == View.NO_ID) {
                    sceneView.setId(holderView.getId());
                } else {
                    String holderViewIdName = Utility.getIdName(requireSceneContext(), holderView.getId());
                    String sceneViewIdName = Utility.getIdName(requireSceneContext(), holderView.getId());
                    throw new IllegalStateException(String.format("ScenePlaceHolderView's id %s is different from Scene root view's id %s"
                            , holderViewIdName, sceneViewIdName));
                }
            }
            parent.removeView(sceneView);
            parent.addView(sceneView, index, layoutParams);
        }
    }

    private static void extractScenePlaceHolder(List<ScenePlaceHolderView> list, ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        if (count == 0) {
            return;
        }
        for (int i = 0; i < count; i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof ScenePlaceHolderView) {
                list.add((ScenePlaceHolderView) view);
            } else if (view instanceof ViewGroup) {
                extractScenePlaceHolder(list, (ViewGroup) view);
            }
        }
    }

    @NonNull
    public abstract ViewGroup onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container,
                                           @Nullable Bundle savedInstanceState);

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchActivityCreated(@Nullable Bundle savedInstanceState) {
        super.dispatchActivityCreated(savedInstanceState);
        // Child scene's savedInstanceState is saved separately (in GroupRecord).
        dispatchChildrenState(State.ACTIVITY_CREATED);
        onPostActivityCreated();
    }

    /**
     * Method order:
     * <p>
     * Parent onActivityCreated()
     * Child onActivityCreated()
     * Child onPostActivityCreated()
     * Parent onPostActivityCreated()
     * <p>
     * At this moment, all children Scene have finished onActivityCreated()
     */
    @Experimental
    protected void onPostActivityCreated() {

    }

    @Override
    public final void dispatchSaveInstanceState(Bundle outState) {
        super.dispatchSaveInstanceState(outState);
    }

    @CallSuper
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mGroupSceneManager.saveToBundle(outState);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public final void dispatchStart() {
        super.dispatchStart();
        dispatchVisibleChildrenState(State.STARTED);
        onPostStart();
    }

    /**
     * Method order:
     * <p>
     * Parent onStart()
     * Child onStart()
     * Child onPostStart()
     * Parent onPostStart()
     * <p>
     * At this moment, all children Scene have finished onStart()
     */
    @Experimental
    protected void onPostStart() {

    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public final void dispatchResume() {
        super.dispatchResume();
        dispatchVisibleChildrenState(State.RESUMED);
        onPostResume();
    }

    /**
     * Method order:
     * <p>
     * Parent onResume()
     * Child onResume()
     * Child onPostResume()
     * Parent onPostResume()
     * <p>
     * At this moment, all children Scene have finished onResume()
     */
    @Experimental
    protected void onPostResume() {

    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public final void dispatchPause() {
        dispatchVisibleChildrenState(State.STARTED);
        super.dispatchPause();
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public final void dispatchStop() {
        dispatchVisibleChildrenState(State.ACTIVITY_CREATED);
        super.dispatchStop();
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY)
    @Override
    public final void dispatchDestroyView() {
        dispatchChildrenState(State.NONE);
        super.dispatchDestroyView();
    }

    @Override
    public final void dispatchDestroy() {
        super.dispatchDestroy();
    }

    @Override
    public final void dispatchDetachScene() {
        super.dispatchDetachScene();
    }

    @Override
    public final void dispatchDetachActivity() {
        super.dispatchDetachActivity();
    }

    private void dispatchChildrenState(@NonNull State state) {
        this.mGroupSceneManager.dispatchChildrenState(state);
    }

    private void dispatchVisibleChildrenState(@NonNull State state) {
        this.mGroupSceneManager.dispatchVisibleChildrenState(state);
    }

    @NonNull
    public final <T extends Scene> T createOrReuse(@NonNull String tag, @NonNull Creator<T> creator) {
        Scene scene = findSceneByTag(tag);
        if (scene == null) {
            scene = creator.call();
        }
        return (T) scene;
    }

    public final void registerChildSceneLifecycleCallbacks(@NonNull ChildSceneLifecycleCallbacks cb, boolean recursive) {
        ThreadUtility.checkUIThread();
        this.mLifecycleCallbacks.add(NonNullPair.create(cb, recursive));
    }

    public final void unregisterChildSceneLifecycleCallbacks(@NonNull ChildSceneLifecycleCallbacks cb) {
        ThreadUtility.checkUIThread();
        NonNullPair<ChildSceneLifecycleCallbacks, Boolean> target = null;
        for (int i = 0, N = this.mLifecycleCallbacks.size(); i < N; i++) {
            if (this.mLifecycleCallbacks.get(i).first == cb) {
                target = this.mLifecycleCallbacks.get(i);
                break;
            }
        }
        if (target != null) {
            this.mLifecycleCallbacks.remove(target);
        }
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneCreated(scene, savedInstanceState);
                }
            }
        }

        super.dispatchOnSceneCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneActivityCreated(@NonNull Scene scene, @Nullable Bundle savedInstanceState, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneActivityCreated(scene, savedInstanceState);
                }
            }
        }

        super.dispatchOnSceneActivityCreated(scene, savedInstanceState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneStarted(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneStarted(scene);
                }
            }
        }

        super.dispatchOnSceneStarted(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneResumed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneResumed(scene);
                }
            }
        }

        super.dispatchOnSceneResumed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneStopped(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneStopped(scene);
                }
            }
        }

        super.dispatchOnSceneStopped(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnScenePaused(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onScenePaused(scene);
                }
            }
        }

        super.dispatchOnScenePaused(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneSaveInstanceState(@NonNull Scene scene, @NonNull Bundle outState, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneSaveInstanceState(scene, outState);
                }
            }
        }

        super.dispatchOnSceneSaveInstanceState(scene, outState, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneViewDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneViewDestroyed(scene);
                }
            }
        }

        super.dispatchOnSceneViewDestroyed(scene, directChild);
    }

    /**
     * @hide
     */
    @RestrictTo(LIBRARY_GROUP)
    @Override
    public final void dispatchOnSceneDestroyed(@NonNull Scene scene, boolean directChild) {
        if (scene != this) {
            List<NonNullPair<ChildSceneLifecycleCallbacks, Boolean>> list = new ArrayList<>(mLifecycleCallbacks);
            for (NonNullPair<ChildSceneLifecycleCallbacks, Boolean> pair : list) {
                if (directChild || pair.second) {
                    pair.first.onSceneDestroyed(scene);
                }
            }
        }

        super.dispatchOnSceneDestroyed(scene, directChild);
    }
}
