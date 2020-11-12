# Scene

[简体中文版说明 >>>](/README_cn.md)

[![GitHub license](https://img.shields.io/github/license/bytedance/scene)](https://github.com/bytedance/scene/blob/master/LICENSE) 
![Maven metadata URL](https://img.shields.io/maven-metadata/v?color=green&label=version&metadataUrl=http%3A%2F%2Fjcenter.bintray.com%2Fcom%2Fbytedance%2Fscene%2Fscene%2Fmaven-metadata.xml)
[![API](https://img.shields.io/badge/api-14%2B-green)](https://developer.android.com/about/dashboards)

Scene is a lightweight library of navigation and ui composition based on view.

1. Simple and convenient navigation and stack management, support multi-stack
2. Improved lifecycle management and distribution
3. Easier to implement complex cut-scenes animation 
4. Support properties modification and recovery of Activity and Window 
5. Support return value between Scenes, support request and grant permissions in Scene
6. Support save and recovery state of Scene

[Download the Demo](https://github.com/bytedance/scene/releases/download/v1.0.2/demo-debug.apk)

## Apps using Scene

| <img src="misc/xigua.png" alt="xigua" width="100"/> | <img src="misc/douyin.png" alt="douyin" width="100"/> | <img src="http://p3.pstatp.com/origin/2e94f00098d334c3c3fe1" alt="lv" width="100"/> | 
|:-----------:|:-------:|:-------:|
| Xigua Video | Tik Tok | Viamaker | 

## Introduction

Scene is designed to replace the use of Activity and Fragment on navigation and page segmentation.

The main problems of Activity:

1. The stack management of Activity is weak, Intent and LaunchMode are confusing, even if various of hacks still can't completely avoid issues like black screen 
2. The performance of Activity is poor, average startup time of an empty Activity is more than 60ms (on Samsung S9)
3. Because the Activity is forced to support states recovery, it causes some problems:
    - Transition animation has limited ability, difficult to implement complex interactive animations.
    - Shared-element animation is basically unavailable, and there are some crashes unresolved in Android Framework.
    - Every time starting a new Activity, onSaveInstance() of the previous Activity must be executed completely first, which will lose much performance.
4. Activity relies on the Manifest file to cause injection difficulties, which also result in that Activity dynamics requires a variety of hacks

The main problems of Fragment:

1. There are many crashes that the Google official can't solve for a long time. Even if you don't use Fragment, it may still trigger a crash in the OnBackPressed() of AppCompatActivity.
2. The add/remove/hide/show operation is not executed immediately. With nest Fragments even if you use commitNow(), the status update of the sub Fragments cannot be guaranteed.
3. The support of animation is poor, Z-axis order cannot be guaranteed when switching
4. Navigation management is weak, there is no advanced stack management except for basic push and pop
5. The lifecycle of Fragment in native Fragment and Support-v4 packages is not exactly the same

The Scene framework tries to solve these problems of the Activity and Fragment mentioned above.

Provides a simple, reliable, and extensible API for a lightweight navigation and page segmentation solution

At the same time, we provide a series of migration solutions to help developers gradually migrate from Activity and Fragment to Scene.

## Get Started

Add to your build.gradle:

```gradle
implementation 'com.bytedance.scene:scene:$latest_version'
implementation 'com.bytedance.scene:scene-ui:$latest_version'
implementation 'com.bytedance.scene:scene-shared-element-animation:$latest_version'
implementation 'com.bytedance.scene:scene-ktx:$latest_version'
```

Scene has 2 subclasses: NavigationScene and GroupScene:

1. NavigationScene supports navigation
2. GroupScene supports ui composition

Scene             | NavigationScene             |  GroupScene 
:-------------------------:|:-------------------------:|:-------------------------:
<img src="http://p3.pstatp.com/origin/2dd480002cd0b8584730a" width="400">  | <img src="http://p3.pstatp.com/origin/2dd450002cc210965bb20" width="400">  |  <img src="http://p3.pstatp.com/origin/2dd450002cbd848281018" width="400">

For simple usage, just let your Activity inherit from SceneActivity:

```kotlin
class MainActivity : SceneActivity() {
    override fun getHomeSceneClass(): Class<out Scene> {
        return MainScene::class.java
    }

    override fun supportRestore(): Boolean {
        return false
    }
}
```

A simple Scene example:

```kotlin
class MainScene : AppCompatScene() {
    private lateinit var mButton: Button
    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val frameLayout = FrameLayout(requireSceneContext())
        mButton = Button(requireSceneContext())
        mButton.text = "Click"
        frameLayout.addView(mButton, FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
        return frameLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle("Main")
        toolbar?.navigationIcon = null
        mButton.setOnClickListener {
            navigationScene?.push(SecondScene())
        }
    }
}

class SecondScene : AppCompatScene() {
    private val mId: Int by lazy { View.generateViewId() }

    override fun onCreateContentView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View? {
        val frameLayout = FrameLayout(requireSceneContext())
        frameLayout.id = mId
        return frameLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setTitle("Second")
        add(mId, ChildScene(), "TAG")
    }
}

class ChildScene : Scene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): View {
        val view = View(requireSceneContext())
        view.setBackgroundColor(Color.GREEN)
        return view
    }
}
```

## Migration to Scene

A new app can use Scene by directly inheriting the SceneActivity.

But if your existing Activity is not convenient to change the inheritance relationship, you can directly using SceneDelegate to handle Scenes refer to the code of SceneActivity.

Take the homepage migration plan of XiguaVideo as an example:

First, declares a layout in the XML of the home Activity for storing the Scene: scene_container

```xml
<?xml version="1.0" encoding="utf-8"?>
<merge xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
 
    <...>
    
    <...>
 
    <!-- The above is the existing layout of the Activity -->
 
    <FrameLayout
        android:id="@+id/scene_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
 
</merge>
```

Then create a transparent Scene as the root Scene:

```java
public static class EmptyHolderScene extends Scene {
    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return new View(getActivity());
    }
 
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().setBackgroundColor(Color.TRANSPARENT);
    }
 
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ArticleMainActivity activity = (ArticleMainActivity) requireActivity();
        activity.createSceneLifecycleCallbacksToDispatchLifecycle(getNavigationScene());
    }
}
```

Bind this transparent Scene to R.id.scene_container:

```java
mSceneActivityDelegate = NavigationSceneUtility.setupWithActivity(this, R.id.scene_container, null,
        new NavigationSceneOptions().setDrawWindowBackground(false)
                .setFixSceneWindowBackgroundEnabled(true)
                .setSceneBackground(R.color.material_default_window_bg)
                .setRootScene(EmptyHolderScene.class, null), false);
```

In essence, there is a transparent Scene cover on the Activity, but it is not visually visible.

Then provide the Push method in the Activity:

```java
public void push(@NonNull Class<? extends Scene> clazz, @Nullable Bundle argument, @Nullable PushOptions pushOptions) {
    if (mSceneActivityDelegate != null) {
        mSceneActivityDelegate.getNavigationScene().push(clazz, argument, pushOptions);
    }
}
```

This completes the basic migration, you can open a new Scene page directly in this Activity.

## Issues

### Dialog

A normal Dialog's Window is independent and in front of the Activity's Window,
so if try to push a Scene in a opening Dialog, it will cause the Scene to appear behind it. 
You can close the dialog box when click, or use Scene to implement the dialog instead of a system Dialog.

### SurfaceView and TextureView

When the Scene is popping, the animation will be executed after the Scene life cycle is executed.
However, if there is a SurfaceView or a TextureView, this process will cause the SurfaceView/TextureView to turn to black.

You can get and re-assign the Surface before the animation end to avoid issues on TextureView, 
and capture the last bitmap and set to a ImageView to avoid issues on SurfaceView.

### Status Bar related

There is no official API of notch screen before Android P, and each vendor has its own implementation.

If you try to hide the status bar with WindowFlag or View's UiVisibility, it will trigger the re-layout of the entire Activity.

This may causes the layout change of the Scene inside, and the behaviors may be not as expected in some cases.

## License
~~~
Copyright (c) 2019 ByteDance Inc

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
~~~