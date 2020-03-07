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
package com.bytedance.scenerouter.plugin;

import com.android.annotations.NonNull;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;

final class RouterTransform extends Transform {
    private static final String SCENE_ROUTER_APT_MODULE_MAP_FILE_NAME = "Generated_SceneRouter_Map.class";
    private static final String SCENE_ROUTER_PLUGIN_CACHE_FILE_NAME = "SceneRouterMapCache.text";
    private static final String SCENE_ROUTER_PLUGIN_MAP_CLASS_NAME = "com.bytedance.scene.scene_router.SceneRouterMap";
    private static final String SCENE_ROUTER_PLUGIN_MAP_FILE_NAME = "SceneRouterMap.class";

    private final boolean debug;

    RouterTransform(boolean debug) {
        this.debug = debug;
    }

    @Override
    public String getName() {
        return "RouterPlugin";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    public interface Action<T> {
        boolean execute(T var1);
    }

    private static boolean scanFileRecurse(File file, Action<File> action) {
        boolean find = false;
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    if (scanFileRecurse(child, action)) {
                        find = true;
                        break;
                    }
                }
            }
        } else {
            find = action.execute(file);
        }
        return find;
    }

    private Map<String, String> fetchUrlMapFromDirectoryInput(
            final ByteArrayClassLoader loader, DirectoryInput directoryInput) {
        System.out.println("directoryInput " + directoryInput.getFile().getAbsolutePath());

        final CheckKeyDuplicateMap map = new CheckKeyDuplicateMap();
        scanFileRecurse(directoryInput.getFile(), new Action<File>() {
            @Override
            public boolean execute(File file) {
                String name = file.getName();
                if (name.equals(SCENE_ROUTER_APT_MODULE_MAP_FILE_NAME)) {
                    try {
                        FileChannel roChannel = new RandomAccessFile(file, "r").getChannel();
                        ByteBuffer bb = roChannel.map(FileChannel.MapMode.READ_ONLY, 0, (int) roChannel.size());
                        Class<?> clazz = loader.findClassByByteBuffer(bb);
                        Callable<Map<String, String>> object = (Callable<Map<String, String>>) clazz.newInstance();
                        Map<String, String> moduleMap = object.call();
                        map.fillMap(moduleMap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });
        return map;
    }

    private Map<String, String> fetchUrlMapFromJarInput(final ByteArrayClassLoader loader, JarInput jarInput) throws
            IOException {
        log("jarInput " + jarInput.getName());
        final CheckKeyDuplicateMap map = new CheckKeyDuplicateMap();
        JarFile jarFile = new JarFile(jarInput.getFile());
        Enumeration enumeration = jarFile.entries();
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement();
            String entryName = jarEntry.getName();
            log("entryName " + entryName);
            if (entryName.endsWith(SCENE_ROUTER_APT_MODULE_MAP_FILE_NAME)) {
                InputStream inputStream = jarFile.getInputStream(jarEntry);
                byte[] bytes = IOUtils.toByteArray(inputStream);
                Class<?> clazz = loader.findClassByByteArray(bytes);
                try {
                    Callable<Map<String, String>> object = (Callable<Map<String, String>>) clazz.newInstance();
                    Map<String, String> moduleMap = object.call();
                    map.fillMap(moduleMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return map;
    }

    private static class CheckKeyDuplicateMap extends HashMap<String, String> {
        private void fillMap(Map<String, String> add) {
            for (Entry<String, String> entry : add.entrySet()) {
                if (get(entry.getKey()) != null) {
                    throw new IllegalArgumentException("@SceneUrl " + get(entry.getKey()) + " and " + entry.getValue() + " url scheme duplicate " + entry.getKey());
                } else {
                    put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private class UrlMapCache {
        private HashMap<String, Map<String, String>> value;
        private final Gson gson = new Gson();

        private UrlMapCache(File cacheFile) {
            if (cacheFile.exists()) {
                try {
                    String json = FileUtils.readFileToString(cacheFile);
                    Type typeOfHashMap = new TypeToken<HashMap<String, Map<String, String>>>() {
                    }.getType();
                    value = gson.fromJson(json, typeOfHashMap);
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        FileUtils.forceDelete(cacheFile);
                    } catch (IOException ignored) {
                    }
                }
            }

            if (value == null) {
                value = new HashMap<>();
            }
        }

        private void addJar(String path, Map<String, String> map) {
            if (map.size() == 0) {
                return;
            }
            value.put(path, map);
        }

        private Map<String, String> getJar(String path) {
            return value.get(path);
        }

        private void removeJar(String path) {
            value.remove(path);
        }

        private void writeToFile(File file) {
            String json = gson.toJson(value);
            try {
                FileUtils.writeStringToFile(file, json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void transform(@NonNull TransformInvocation transformInvocation) throws
            TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        log("--------------- RouterPlugin transform start ---------------");

        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
        boolean isIncremental = transformInvocation.isIncremental();
        if (!isIncremental) {
            outputProvider.deleteAll();
        }
        log("isIncremental: " + isIncremental);

        final CheckKeyDuplicateMap map = new CheckKeyDuplicateMap();
        final ByteArrayClassLoader loader = new ByteArrayClassLoader();

        File dest = transformInvocation.getOutputProvider().getContentLocation(
                "scene_router",
                TransformManager.CONTENT_CLASS,
                ImmutableSet.of(QualifiedContent.Scope.PROJECT),
                Format.DIRECTORY);

        dest.mkdirs();

        File urlMapCacheFile = new File(dest.getAbsolutePath() + File.separator + SCENE_ROUTER_PLUGIN_CACHE_FILE_NAME);
        log("Cache file " + urlMapCacheFile.getAbsolutePath());
        final UrlMapCache mUrlMapCache = new UrlMapCache(urlMapCacheFile);

        Collection<TransformInput> inputs = transformInvocation.getInputs();
        for (TransformInput input : inputs) {
            Collection<DirectoryInput> directoryInputs = input.getDirectoryInputs();
            for (DirectoryInput directoryInput : directoryInputs) {
                File destDirectory = outputProvider.getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);
                if (isIncremental) {
                    String srcDirPath = directoryInput.getFile().getAbsolutePath();
                    String destDirPath = destDirectory.getAbsolutePath();
                    Map<File, Status> fileStatusMap = directoryInput.getChangedFiles();
                    for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
                        Status status = changedFile.getValue();
                        File inputFile = changedFile.getKey();
                        String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath);
                        File destFile = new File(destFilePath);
                        switch (status) {
                            case NOTCHANGED:
                                break;
                            case REMOVED:
                                if (destFile.exists()) {
                                    FileUtils.forceDelete(destFile);
                                }
                                break;
                            case ADDED:
                            case CHANGED:
                                FileUtils.copyFile(inputFile, destFile);
                                break;
                        }
                    }
                } else {
                    FileUtils.copyDirectory(directoryInput.getFile(), destDirectory);
                }

                Map<String, String> directoryInputModuleMap = fetchUrlMapFromDirectoryInput(loader, directoryInput);
                map.fillMap(directoryInputModuleMap);
            }

            Collection<JarInput> jarInputCollection = input.getJarInputs();
            for (JarInput jarInput : jarInputCollection) {
                File des = outputProvider.getContentLocation(jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                String jarPath = jarInput.getFile().getAbsolutePath();
                Map<String, String> javaInputMap = null;
                if (isIncremental) {
                    Status status = jarInput.getStatus();
                    switch (status) {
                        case NOTCHANGED:
                            //fetch from cache
                            if (jarPath.endsWith(".jar")) {
                                javaInputMap = mUrlMapCache.getJar(jarPath);
                            }
                            break;
                        case ADDED:
                        case CHANGED:
                            if (jarPath.endsWith(".jar")) {
                                javaInputMap = fetchUrlMapFromJarInput(loader, jarInput);
                                //save to cache
                                mUrlMapCache.addJar(jarPath, javaInputMap);
                            }
                            FileUtils.copyFile(jarInput.getFile(), des);
                            break;
                        case REMOVED:
                            if (des.exists()) {
                                mUrlMapCache.removeJar(jarPath);
                                FileUtils.forceDelete(des);
                            }
                            break;
                    }
                } else {
                    if (jarPath.endsWith(".jar")) {
                        javaInputMap = fetchUrlMapFromJarInput(loader, jarInput);
                        //save to cache
                        mUrlMapCache.addJar(jarPath, javaInputMap);
                    }
                    FileUtils.copyFile(jarInput.getFile(), des);
                }
                if (javaInputMap != null) {
                    map.fillMap(javaInputMap);
                }
            }
        }

        mUrlMapCache.writeToFile(urlMapCacheFile);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println("SceneRouter " + entry.getKey() + " ---> " + entry.getValue());
        }

        try {
            ClassPool classPool = new ClassPool(true);
            CtClass ctClass = classPool.makeClass(SCENE_ROUTER_PLUGIN_MAP_CLASS_NAME);
            ctClass.setInterfaces(new CtClass[]{classPool.get(Callable.class.getName())});
            ctClass.addConstructor(CtNewConstructor.defaultConstructor(ctClass));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("public Object call() throws Exception{");
            stringBuilder.append("\n");
            stringBuilder.append("java.util.Map/*<String, String>*/ map = new java.util.HashMap/*<>*/();");
            for (Map.Entry<String, String> entry : map.entrySet()) {
                stringBuilder.append("\n");
                stringBuilder.append(String.format("map.put(\"%s\",\"%s\");", entry.getKey(), entry.getValue()));
            }
            stringBuilder.append("\n");
            stringBuilder.append("return map;}");

            ctClass.addMethod(CtNewMethod.make(stringBuilder.toString(), ctClass));

            File file = new File(dest.getAbsolutePath() + File.separator + SCENE_ROUTER_PLUGIN_MAP_FILE_NAME);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            ctClass.getClassFile().write(new DataOutputStream(fos));
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log("--------------- RouterPlugin transform end ---------------");
    }

    public static class ByteArrayClassLoader extends ClassLoader {
        public Class findClassByByteBuffer(ByteBuffer ba) {
            return defineClass(null, ba, null);
        }

        public Class findClassByByteArray(byte[] ba) {
            return defineClass(null, ba, 0, ba.length);
        }
    }

    private void log(String log) {
        if (this.debug) {
            System.out.println(log);
        }
    }
}
