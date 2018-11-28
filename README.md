
# TouchTileImageView
TouchTileImageView aims to help produce an easily usable implementation of a zooming Android ImageView.

## Dependency

Add this in your root `build.gradle` file (**not** your module `build.gradle` file):

```gradle
allprojects {
   repositories {
        maven { url "https://jitpack.io" }
    }
}
```

Then, add the library to your module `build.gradle`
```gradle
dependencies {
    implementation 'com.ixigua.common:touchtileimageview:latest.release.here'
}
```

## Features
- Smooth scroll/zoom/fling
- Works perfectly when used in a scrolling parent (such as ViewPager).
- Perfect PullDownToDismiss gesture like Google Photo, iOS Photos
- Support MultiLevel thumbnail drawable
- Support Subsampling with inBitmap
- Support all Android image async library, Glide/Fresco

Compared with PhotoView and subsampling-scale-image-view:
<table>
<tr><th></th><th>TouchTileImageView</th><th>PhotoView</th><th>subsampling-scale-image-view</th></tr>
<tr><td>Pan/Fling/Scale</td><td>Yes</td><td>Yes</td><td>Yes</td></tr>
<tr><td>Subsampling</td><td>Yes</td><td>No</td><td>Yes</td></tr>
<tr><td>Animation</td><td>Yes</td><td>No</td><td>No</td></tr>
<tr><td>MultiLevel thumbnail</td><td>Yes</td><td>No</td><td>No</td></tr>
<tr><td>Google Photo like gesture</td><td>Yes</td><td>No</td><td>No</td></tr>
<tr><td>inBitmap</td><td>Yes</td><td>No</td><td>No</td></tr>
</table>

## Usage
Simple use cases:
```xml
<com.ixigua.touchtileimageview.TouchTileImageView
    android:id="@+id/image_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```
```java
TouchTileImageView imageView = (TouchTileImageView) findViewById(R.id.image_view);
Drawable drawable = ? ;
imageView.setImageAspectRatio((float) drawable.getIntrinsicWidth() / (float) drawable.getIntrinsicHeight());
imageView.addImageDrawable(drawable);
```


## Subsampling

after add Drawable

```java
File file = ? ;
imageView.setImageFile(file)
```

## Glide/Fresco, PullDownToDismiss gesture, Animation

demo project

```shell
./gradlew :demo:installDebug
```

## Issues With ViewGroups
There are some ViewGroups (ones that utilize onInterceptTouchEvent) that throw exceptions when a TouchTileImageView is placed within them, most notably [ViewPager](http://developer.android.com/reference/android/support/v4/view/ViewPager.html)
```java
public class CatchExceptionViewPager extends ViewPager {
    public CatchExceptionViewPager(Context context) {
        super(context);
    }

    public CatchExceptionViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ignored) {
            ignored.printStackTrace();
            return false;
        }
    }
}
```