# Bytedance Scene

Scene是一个基于View的轻量级导航和页面切分组件库，主要特性：

1. 简单方便的页面导航和栈管理，支持MultiStack
2. 完善的生命周期的管理和分发
3. 可以更简单的实现复杂的过场动画
4. 支持对Activity和Window属性的修改和恢复
5. 支持页面之间拿返回值，支持在Scene中申请权限
6. 支持页面销毁时保存状态和恢复

[Demo下载地址](https://www.bytedance.com)

## Who's using Scene?

| <img src="misc/xigua.png" alt="xigua" width="100"/> | <img src="misc/douyin.png" alt="douyin" width="100"/> | <img src="misc/musical.ly.png" alt="musical.ly.png" width="90"/> |
|:-------:|:--------:|:----------:|
| 西瓜视频 | 抖音短视频 | Musical.ly |

## Introduction

Scene旨在导航和页面切分上替代Activity和Fragment的使用。

Activity目前存在的主要问题：

1. 栈管理弱，Intent和LaunchMode混乱，即使各种Hack仍然不能完全避免黑屏等问题
2. Activity的性能较差，普通的空白页面启动也平均60ms以上（三星S9测试）
3. 因为Activity被强制需要支持销毁恢复，导致了一些问题：
    - 转场动画能力有限，无法实现较复杂的交互动画，
    - 共享元素动画基本不可用，有Framework层的崩溃无法解决
    - 每次启动新的Activity，都需要上个页面执行完onSaveInstance，损失性能
4. Activity依赖Manifest文件导致注入困难，动态化需要各种Hack

Fragment目前存在的主要问题：

1. 官方长期无法解决的崩溃较多，即使不用Fragment，在AppCompatActivity的onBackPressed()中仍然可能触发崩溃
2. add/remove/hide/show操作不是立刻执行，在嵌套时即使使用commitNow也不能保证子Fragment状态更新
3. 动画支持糟糕，页面切换时无法保证Z轴顺序
4. 导航功能很弱，除了基本的打开和关闭，高级的栈管理
5. 原生Fragment和Support v4包中的Fragment的生命周期并不完全相同

Scene框架尝试去解决上面提到的Activity和Fragment存在的问题

提供简单可靠、易扩展的API，来实现一套轻量的导航和页面切分解决方案

同时我们提供了一系列的迁移方案，来帮助开发者渐进式地从Activity和Fragment迁移到Scene。

## Get Started

在依赖中添加：

```gradle
implementation 'com.bytedance.scene:scene:$latest_version'
implementation 'com.bytedance.scene:scene-ui:$latest_version'
implementation 'com.bytedance.scene:scene-shared-element-animation:$latest_version'
implementation 'com.bytedance.scene:scene-ktx:$latest_version'
```

Scene有2个子类：NavigationScene和GroupScene，其中：

1. NavigationScene支持页面切换
2. GroupScene支持页面切分

简单的接入，让主Activity继承于SceneActivity即可：

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

一个简单的Scene示例：

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

一个新的App可以通过直接继承SceneActivity的方式接入Scene，

但如果已有的Activity不方便更改继承关系，则可参考SceneActivity的代码直接使用SceneDelegate来处理，

以西瓜视频的首页迁移方案为例：

首先在首页的XML申明一个存放Scene的布局：scene_container

```xml
<?xml version="1.0" encoding="utf-8"?>
<merge xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
 
    <...>
    
    <...>

    <!-- 上面是这个Activity的已有布局 -->
 
    <FrameLayout
        android:id="@+id/scene_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
 
</merge>
```

再创建一个透明的Scene作为根Scene

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

绑定这个透明的Scene到 R.id.scene_container

```java
mSceneActivityDelegate = NavigationSceneUtility.setupWithActivity(this, R.id.scene_container, null,
        new NavigationSceneOptions().setDrawWindowBackground(false)
                .setFixSceneWindowBackgroundEnabled(true)
                .setSceneBackground(R.color.material_default_window_bg)
                .setRootScene(EmptyHolderScene.class, null), false);
```

实质上是有个透明的Scene盖在首页，但是视觉上看不出来

然后在Activity中提供Push的方法

```java
public void push(@NonNull Class<? extends Scene> clazz, @Nullable Bundle argument, @Nullable PushOptions pushOptions) {
    if (mSceneActivityDelegate != null) {
        mSceneActivityDelegate.getNavigationScene().push(clazz, argument, pushOptions);
    }
}
```

这样就基本迁移完成，可以在这个Activity中直接打开新的Scene页面了。

## Issues

由于Scene是基于View来实现其功能的，有一些已知但暂时无法解决的问题：

### Dialog

一个正常Dialog的Window是独立于并盖在Activity的Window之上的，

所以如果在Dialog中点击打开一个Scene，就会导致Scene出现在Dialog后面。

可以选择点击的时候关闭对话框，也可以选择使用Scene来实现对话框，来替代系统的Dialog。

### SurfaceView and TextureView

在Scene返回时，会先执行Scene的生命周期后执行动画，

但是如果遇到SurfaceView/TextureView，这个过程会导致SurfaceView/TextureView黑屏，

对于TextureView可以选择结束前，获得Surface，动画前把这个Surface重新赋值

对于SurfaceView，结束前，捕获Bitmap，设置到ImageView，这个过程中因为涉及大的Bitmap创建，

可以Try catch，然后在动画结束后回收这个Bitmap。

### Status Bar related

刘海屏在Android P之前没有官方API，各个厂商有自己的实现

如果用Window Flag或View UiVisibility来隐藏状态栏图标，都会引发整个Activity的重新布局，

这同时也会导致Scene页面的位置变化，某些情况下可能会有不符合预期的行为

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