<div align="center">
    <h1>Scene Framework</h1>
    <p>Android Single Activity Framework compatible with Fragment.</p>
    <br/>
</div>


[![GitHub license](https://img.shields.io/github/license/bytedance/scene)](https://github.com/bytedance/scene/blob/master/LICENSE)
[![](https://jitpack.io/v/bytedance/scene.svg)](https://jitpack.io/#bytedance/scene)
[![API](https://img.shields.io/badge/api-21%2B-green)](https://developer.android.com/about/dashboards)

**Scene** is a lightweight library of navigation and UI composition based on view.

- ✅ Fully compatible with the Jetpack Fragment framework
- ✅ Simple navigation stack management, with support for multiple navigation stacks
- ✅ Enhanced lifecycle management and event distribution
- ✅ Simplifies complex cross-page and shared element animations
- ✅ Supports modification and automatic restoration of Activity and Window properties
- ✅ Enables data exchange between Scenes, including permission requests and grants within a Scene
- ✅ Supports saving and restoring Scene state via Parcelable
- ✅ No R8/Proguard configuration required

[Download the latest Sample APK](https://github.com/bytedance/scene/blob/master/misc/latest_sample.apk)

Introduction
-------------
**Scene** is designed to replace the use of Activities and Fragments for navigation and page segmentation in Android applications. It addresses the following issues:
1. **Activity**, Poor performance, with the average startup time of even an empty Activity exceeding 100ms.
2. **Fragment**, Poor compatibility, [Google Navigation Component](https://developer.android.com/guide/navigation) destroys a Fragment’s view when it becomes invisible.

Scene provides a simple, reliable, and extensible API for lightweight, high-performance navigation and page management.
We also offer a set of migration solutions to help developers gradually transition from Activities and Fragments to Scene.

Get Started
-------------
Add it to your root build.gradle at the end of repositories:
```gradle
//build.gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

```kotlin
//or settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add it to your build.gradle, [latest_version](https://github.com/bytedance/scene/releases) :
```gradle
dependencies {
	implementation 'com.github.bytedance:scene:$latest_version'
        //or
	implementation 'com.github.bytedance.scene:scene:$latest_version'
	implementation 'com.github.bytedance.scene:scene_navigation:$latest_version'
	implementation 'com.github.bytedance.scene:scene_ui:$latest_version'
	implementation 'com.github.bytedance.scene:scene_fragment:$latest_version'
	implementation 'com.github.bytedance.scene:scene_dialog:$latest_version'
	implementation 'com.github.bytedance.scene:scene_shared_element_animation:$latest_version'
	implementation 'com.github.bytedance.scene:scene_ktx:$latest_version'
}
```

```kotlin
//or build.gradle.kts
dependencies {
    implementation ("com.github.bytedance:scene:$latest_version")
    //or
    implementation ("com.github.bytedance.scene:scene:$latest_version")
    implementation ("com.github.bytedance.scene:scene_navigation:$latest_version")
    implementation ("com.github.bytedance.scene:scene_ui:$latest_version")
    implementation ("com.github.bytedance.scene:scene_fragment:$latest_version")
    implementation ("com.github.bytedance.scene:scene_dialog:$latest_version")
    implementation ("com.github.bytedance.scene:scene_shared_element_animation:$latest_version")
    implementation ("com.github.bytedance.scene:scene_ktx:$latest_version")
}
```

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

Fragment
-------------

```kotlin
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bytedance.scene.fragment.getNavigationScene
import com.bytedance.scene.fragment.push

class YourFragment : Fragment() {
   override fun onCreateView(
      inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
   ): View {
      return View(requireContext())
   }

   override fun onActivityCreated(savedInstanceState: Bundle?) {
      super.onActivityCreated(savedInstanceState)
      val navigationScene = getNavigationScene()
      requireView().setOnClickListener {
         navigationScene?.push(YourFragment())
      }
   }
}
```

Compose
-------------
https://github.com/bytedance/scene/wiki/Compose

Sample
--------
Scene sample is built using Gradle. On Linux, simply run:

    ./gradlew installDebug

Document
-------------
https://github.com/bytedance/scene/wiki

Issues
-------------
### Dialog

A normal Dialog's Window is independent and in front of the Activity's Window,
so if try to push a Scene in a opening Dialog, it will cause the Scene to appear behind it.
You can close the dialog box when click, or use transparent Scene to implement the dialog instead of a system Dialog.


Apps using Scene
-------------
| <img src="misc/douyin.png" alt="xigua" width="100"/>| <img src="misc/douyin.png" alt="xigua" width="100"/> | <img src="misc/xigua.png" alt="douyin" width="100"/> | <img src="misc/toutiao.png" alt="toutiao" width="100"/> | <img src="misc/kesong.webp" alt="kesong" width="100"/> |
|:-----------:|:-------:|:-------:|:-------:|:------------------------------------------------------:|
| TikTok | Douyin | Xigua Video |  Toutiao |  KeSong |


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