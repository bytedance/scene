### Introduction

`scene-router` is the official router for [Bytedance Scene](https://github.com/bytedance/scene)


## Demo
```
cd ./library/scenerouter_plugin
../../gradlew clean uploadArchives
cd ../..
./gradlew clean installDebug
```
## Get Started
Configuration
```java
@SceneUrl({"/test1", "/test2", "/test3"})
public class LibraryScene extends Scene
```
Open
```java
       SceneRouter sceneRouter = SceneRouters.of(YourScene.this);
        sceneRouter.url(targetUrl).argument("argKey", "argValue").open(new OpenCallback() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFail(@Nullable Exception exception) {
                if (exception instanceof SceneNotFoundException) {

                }
            }
        });
```

## Important
 NOT PRODUCTION READY CODE

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
