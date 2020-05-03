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
package com.bytedance.scenerouter.apt;

import com.bytedance.scenerouter.annotation.SceneUrl;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class SceneRouterProcessor extends AbstractProcessor {
    private static final String SCENE_ROUTER_MODULE_MAP_CLASS_NAME = "Generated_SceneRouter_Map";
    private static final String SCENE_BASE_CLASS_NAME = "com.bytedance.scene.Scene";
    private static final Class<SceneUrl> SCENE_ROUTE_URI = SceneUrl.class;
    private static final String SCENE_ROUTER_MODULE_NAME_OPTION = "SCENE_ROUTER_MODULE_NAME";
    private static final String SCENE_ROUTER_DEBUG_OPTION = "SCENE_ROUTER_DEBUG";
    private Messager mMessager;
    private Elements mElements;
    private String mModuleName;
    private boolean mDebug = false;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(SCENE_ROUTE_URI.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> set = new HashSet<>();
        set.add(SCENE_ROUTER_MODULE_NAME_OPTION);
        set.add(SCENE_ROUTER_DEBUG_OPTION);
        return set;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.mMessager = processingEnv.getMessager();
        this.mElements = processingEnv.getElementUtils();

        Map<String, String> options = processingEnv.getOptions();
        this.mModuleName = options != null ? options.get(SCENE_ROUTER_MODULE_NAME_OPTION) : null;
        this.mDebug = "true".equalsIgnoreCase(options != null ? options.get(SCENE_ROUTER_DEBUG_OPTION) : null);
        if (Utility.isEmpty(this.mModuleName)) {
            throw new RuntimeException("SceneRouter::Compiler >>> No module name, add following code to your Module build.gradle \n" +
                    "javaCompileOptions {\n" +
                    "            annotationProcessorOptions {\n" +
                    "                arguments = [SCENE_ROUTER_MODULE_NAME: project.getName()]\n" +
                    "            }\n" +
                    "        }");
        }
        log(Diagnostic.Kind.NOTE, "SCENE_ROUTER_MODULE_NAME: " + this.mModuleName);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elementSet = roundEnv.getElementsAnnotatedWith(SCENE_ROUTE_URI);
        if (elementSet == null || elementSet.size() == 0) {
            return false;
        }

        Map<String, String> map = new HashMap<>();
        for (Element element : elementSet) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            if (!isSceneSubClass(typeElement.asType())) {
                throw new IllegalArgumentException("@SceneUrl can't target to " + typeElement.getQualifiedName());
            }

            SceneUrl sceneUrl = element.getAnnotation(SCENE_ROUTE_URI);
            String[] urlArray = sceneUrl.value();
            String clazzName = typeElement.getQualifiedName().toString();

            log(Diagnostic.Kind.WARNING, "输出 " + clazzName);
            for (String url : urlArray) {
                String urlAfterTrim = url.trim();
                if (urlAfterTrim.equals("")) {
                    throw new IllegalArgumentException("@SceneUrl Url scheme can't be empty " + typeElement.getQualifiedName());
                }
                Utility.throwExceptionIfUrlIncorrect(urlAfterTrim);
                if (map.get(urlAfterTrim) != null) {
                    String previousSceneClazzName = map.get(urlAfterTrim);
                    throw new IllegalArgumentException("@SceneUrl " + previousSceneClazzName + " and " + clazzName + " url scheme duplicate " + urlAfterTrim);
                }
                map.put(urlAfterTrim, clazzName);
            }

            JavaFile javaFile = new BindMethodProcessor(this.mMessager, this.mElements, this.mDebug).createBindMethod(element, typeElement);
            if (javaFile != null) {
                writeFile(javaFile);
            }
        }

        MethodSpec.Builder getClazzListMethod = MethodSpec.methodBuilder("call")
                .addException(Exception.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(HashMap.class)
                .addStatement("HashMap<String, String> map = new HashMap<>()");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            getClazzListMethod.addStatement("map.put($S,$S)", entry.getKey(), entry.getValue());
        }
        getClazzListMethod.addStatement("return map");

        String packageName = processingEnv.getElementUtils().getPackageOf(elementSet.iterator().next()).toString();

        TypeSpec builder = TypeSpec.classBuilder(SCENE_ROUTER_MODULE_MAP_CLASS_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(getClazzListMethod.build())
                .addSuperinterface(Callable.class)
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, builder)
                .addFileComment("Code generated by SceneRouter. Do not edit.")
                .build();

        writeFile(javaFile);
        return false;
    }

    private static boolean isSceneSubClass(TypeMirror typeMirror) {
        String className = ClassName.get(typeMirror).toString();
        if ("java.lang.Object".equals(className)) {
            return false;
        } else if (SCENE_BASE_CLASS_NAME.equals(className)) {
            return true;
        } else {
            if (typeMirror instanceof DeclaredType) {
                TypeMirror parent = ((TypeElement) (((DeclaredType) typeMirror).asElement())).getSuperclass();
                return isSceneSubClass(parent);
            }
        }
        return false;
    }

    private void writeFile(JavaFile javaFile) {
        Filer filer = processingEnv.getFiler();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            Messager messager = processingEnv.getMessager();
            String message = String.format("Unable to write file: %s",
                    e.getMessage());
            messager.printMessage(Diagnostic.Kind.ERROR, message);
        }
    }

    private void log(Diagnostic.Kind kind, String log) {
        if (!this.mDebug) {
            return;
        }

        this.mMessager.printMessage(kind, log);
    }
}
