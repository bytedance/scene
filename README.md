AndroidManifest define scheme
```xml
            <intent-filter android:label="@string/filter_view_http_gizmos">
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <!-- Accepts URIs that begin with "http://www.example.com/demo” -->
                <data
                    android:scheme="http"
                    android:host="www.example.com"
                    android:pathPrefix="/demo" />
                <!-- note that the leading "/" is required for pathPrefix-->
            </intent-filter>
            <intent-filter android:label="@string/filter_view_example_gizmos">
                <action android:name="android.intent.action.VIEW" />

                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <!-- Accepts URIs that begin with "example://demo” -->
                <data
                    android:scheme="example"
                    android:host="gizmos" />
            </intent-filter>
```

SceneUrl
```kotlin
@SceneUrl("/test2")
class BScene : GroupScene() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle?): ViewGroup {
        return FrameLayout(requireSceneContext()).apply {
            setBackgroundColor(Color.YELLOW)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}
```

adb shell am start -W -a android.intent.action.VIEW -d http://www.example.com/demo/test2 com.bytedance.scene.deeplinks