# Scene

[简体中文版说明 >>>](/README_cn.md)

[![GitHub license](https://img.shields.io/github/license/bytedance/scene)](https://github.com/bytedance/scene/blob/master/LICENSE) 
[![](https://jitpack.io/v/bytedance/scene.svg)](https://jitpack.io/#bytedance/scene)
[![API](https://img.shields.io/badge/api-14%2B-green)](https://developer.android.com/about/dashboards)

Scene Framework は、ビューに基づいた軽量のナビゲーションおよびUIコンポジションライブラリです。

1. シンプルで便利なナビゲーションとスタック管理、マルチスタック対応
2. 改善されたライフサイクル管理と配信
3. 複雑なカットシーンアニメーションの実装が容易
4. アクティビティおよびウィンドウのプロパティの変更と復元をサポート
5. シーン間の戻り値をサポートし、シーン内での権限リクエストと付与をサポート
6. シーンの状態の保存と復元をサポート

[デモのダウンロード](https://github.com/bytedance/scene/releases/download/v1.0.2/demo-debug.apk)

## Sceneを使用しているアプリ

| <img src="misc/douyin.png" alt="douyin" width="100"/> | <img src="misc/xigua.png" alt="xigua" width="100"/> | <img src="misc/toutiao.png" alt="toutiao" width="100"/> |
|:-----------:|:-------:|:-------:|
| Tik Tok | Xigua Video |  Toutiao |

## はじめに

Sceneは、ナビゲーションとページ分割においてActivityとFragmentの使用を置き換えることを目的としています。

Activityの主な問題点：

1. Activityのスタック管理は弱く、IntentとLaunchModeが混乱しており、さまざまなハックを使用しても、ブラックスクリーンなどの問題を完全に回避することはできません。
2. Activityのパフォーマンスは低く、空のActivityの平均起動時間は60ms以上です（Samsung S9でのテスト）。
3. Activityは状態の復元を強制的にサポートするため、いくつかの問題が発生します：
    - トランジションアニメーションの能力が制限されており、複雑なインタラクティブアニメーションの実装が難しい。
    - 共有要素アニメーションは基本的に使用できず、Androidフレームワークで解決できないクラッシュがあります。
    - 新しいActivityを開始するたびに、前のActivityのonSaveInstance()が完全に実行される必要があり、パフォーマンスが低下します。
4. ActivityはManifestファイルに依存しているため、注入が困難であり、Activityの動的化にはさまざまなハックが必要です。

Fragmentの主な問題点：

1. Google公式が長期間解決できないクラッシュが多く、Fragmentを使用しなくても、AppCompatActivityのOnBackPressed()でクラッシュが発生する可能性があります。
2. add/remove/hide/show操作はすぐに実行されず、ネストされたFragmentではcommitNow()を使用しても、サブFragmentの状態更新を保証できません。
3. アニメーションのサポートが不十分で、切り替え時にZ軸の順序を保証できません。
4. ナビゲーション管理が弱く、基本的なプッシュとポップ以外の高度なスタック管理がありません。
5. ネイティブFragmentとSupport-v4パッケージのFragmentのライフサイクルは完全に同じではありません。

Sceneフレームワークは、上記のActivityとFragmentの問題を解決しようとしています。

シンプルで信頼性が高く、拡張性のあるAPIを提供し、軽量のナビゲーションおよびページ分割ソリューションを実現します。

同時に、開発者がActivityとFragmentからSceneに段階的に移行するのを支援する一連の移行ソリューションを提供します。

## 始める

リポジトリを追加します：
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

依存関係を追加します：
```gradle
dependencies {
	implementation 'com.github.bytedance.scene:scene:$latest_version'
	implementation 'com.github.bytedance.scene:scene_navigation:$latest_version'
	implementation 'com.github.bytedance.scene:scene_ui:$latest_version'
	implementation 'com.github.bytedance.scene:scene_dialog:$latest_version'
	implementation 'com.github.bytedance.scene:scene_shared_element_animation:$latest_version'
	implementation 'com.github.bytedance.scene:scene_ktx:$latest_version'
}
```

Sceneには2つのサブクラスがあります：NavigationSceneとGroupScene：

1. NavigationSceneはナビゲーションをサポートします
2. GroupSceneはUIコンポジションをサポートします

Scene             | NavigationScene             |  GroupScene 
:-------------------------:|:-------------------------:|:-------------------------:
<img src="http://p3.pstatp.com/origin/2dd480002cd0b8584730a" width="400">  | <img src="http://p3.pstatp.com/origin/2dd450002cc210965bb20" width="400">  |  <img src="http://p3.pstatp.com/origin/2dd450002cbd848281018" width="400">

シンプルな使用法のために、ActivityをSceneActivityから継承させます：

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

シンプルなSceneの例：

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

## Sceneへの移行

新しいアプリは、SceneActivityを直接継承することでSceneを使用できます。

ただし、既存のActivityの継承関係を変更するのが難しい場合は、SceneActivityのコードを参照して、SceneDelegateを直接使用してSceneを処理できます。

XiguaVideoのホームページ移行プランを例にとります：

まず、ホームActivityのXMLでSceneを格納するレイアウトを宣言します：scene_container

```xml
<?xml version="1.0" encoding="utf-8"?>
<merge xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
 
    <...>
    
    <...>
 
    <!-- 上記はActivityの既存のレイアウトです -->
 
    <FrameLayout
        android:id="@+id/scene_container"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
 
</merge>
```

次に、ルートSceneとして透明なSceneを作成します：

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

この透明なSceneをR.id.scene_containerにバインドします：

```java
mSceneActivityDelegate = NavigationSceneUtility.setupWithActivity(this, R.id.scene_container, null,
        new NavigationSceneOptions().setDrawWindowBackground(false)
                .setFixSceneWindowBackgroundEnabled(true)
                .setSceneBackground(R.color.material_default_window_bg)
                .setRootScene(EmptyHolderScene.class, null), false);
```

本質的には、Activityの上に透明なSceneがカバーされていますが、視覚的には見えません。

次に、ActivityにPushメソッドを提供します：

```java
public void push(@NonNull Scene scene, @Nullable PushOptions pushOptions) {
    if (mSceneActivityDelegate != null) {
        mSceneActivityDelegate.getNavigationScene().push(scene, pushOptions);
    }
}
```

これで基本的な移行が完了し、このActivityで新しいSceneページを直接開くことができます。

## 問題

### ダイアログ

通常のダイアログのウィンドウは独立しており、アクティビティのウィンドウの前にあります。
したがって、開いているダイアログでシーンをプッシュしようとすると、シーンがその背後に表示されます。
クリック時にダイアログボックスを閉じるか、シーンを使用してダイアログを実装してシステムダイアログの代わりにすることができます。

### SurfaceViewとTextureView

シーンがポップされると、シーンのライフサイクルが実行された後にアニメーションが実行されます。
ただし、SurfaceViewまたはTextureViewがある場合、このプロセスによりSurfaceView/TextureViewが黒くなります。

アニメーションが終了する前にSurfaceを取得して再割り当てすることで、TextureViewの問題を回避できます。
SurfaceViewの場合、最後のビットマップをキャプチャしてImageViewに設定することで問題を回避できます。

### ステータスバー関連

Android P以前のノッチスクリーンには公式APIがなく、各ベンダーには独自の実装があります。

WindowFlagまたはViewのUiVisibilityを使用してステータスバーを非表示にしようとすると、アクティビティ全体のレイアウトが再トリガーされます。

これにより、内部のシーンのレイアウトが変更され、一部のケースでは予期しない動作が発生する可能性があります。

## ライセンス
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
