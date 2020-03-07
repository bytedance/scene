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

import com.bytedance.scenerouter.annotation.RouterValue;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

class BindMethodProcessor {
    private Messager mMessager;
    private Elements mElementUtils;
    private boolean mDebug;

    BindMethodProcessor(Messager messager, Elements elementUtils, boolean debug) {
        this.mMessager = messager;
        this.mElementUtils = elementUtils;
        this.mDebug = debug;
    }

    JavaFile createBindMethod(Element element, TypeElement typeElement) {
        List<? extends Element> members = mElementUtils.getAllMembers(typeElement);
        if (members == null || members.size() == 0) {
            return null;
        }
        MethodSpec.Builder bindViewMethodSpecBuilder = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(TypeName.VOID)
                .addParameter(ClassName.get(typeElement.asType()), "scene");

        CodeBlock.Builder blockBuilder = CodeBlock.builder()
                .beginControlFlow("if (scene.getArguments()!=null)");
        for (Element item : members) {
            RouterValue routerValue = item.getAnnotation(RouterValue.class);
            if (routerValue == null) {
                continue;
            }

            String variableName = item.getSimpleName().toString();
            String sceneArgumentsKeyName = routerValue.value();
            if (sceneArgumentsKeyName == null || sceneArgumentsKeyName.trim().equals("")) {
                sceneArgumentsKeyName = variableName;
            }

            TypeMirror itemTypeMirror = item.asType();
            TypeKind typeKind = itemTypeMirror.getKind();//DECLARED or ARRAY
            TypeName className = ClassName.get(itemTypeMirror);

            log("typeKind 输出 " + className.toString());
            log("typeKind 输出 " + typeKind.toString());

            //todo 按照普通，数组，list进行分类
            String type = className.toString();
            String methodBody = "scene.%s = scene.getArguments().";
            switch (type) {
                case "int":
                    methodBody = methodBody + "getInt";
                    break;
                case "int[]":
                    methodBody = methodBody + "getIntArray";
                    break;
                case "java.util.ArrayList<java.lang.Integer>":
                    methodBody = methodBody + "getIntegerArrayList";
                    break;
                case "float":
                    methodBody = methodBody + "getFloat";
                    break;
                case "float[]":
                    methodBody = methodBody + "getFloatArray";
                    break;
                case "double":
                    methodBody = methodBody + "getDouble";
                    break;
                case "double[]":
                    methodBody = methodBody + "getDoubleArray";
                    break;
                case "long":
                    methodBody = methodBody + "getLong";
                    break;
                case "long[]":
                    methodBody = methodBody + "getLongArray";
                    break;
                case "char":
                    methodBody = methodBody + "getChar";
                    break;
                case "char[]":
                    methodBody = methodBody + "getCharArray";
                    break;
                case "short":
                    methodBody = methodBody + "getShort";
                    break;
                case "short[]":
                    methodBody = methodBody + "getShortArray";
                    break;
                case "boolean":
                    methodBody = methodBody + "getBoolean";
                    break;
                case "boolean[]":
                    methodBody = methodBody + "getBooleanArray";
                    break;
                case "byte":
                    methodBody = methodBody + "getByte";
                    break;
                case "byte[]":
                    methodBody = methodBody + "getByteArray";
                    break;
                case "java.lang.String":
                    methodBody = methodBody + "getString";
                    break;
                case "java.lang.String[]":
                    methodBody = methodBody + "getStringArray";
                    break;
                case "java.util.ArrayList<java.lang.String>":
                    methodBody = methodBody + "getStringArrayList";
                    break;
                case "java.lang.CharSequence":
                    methodBody = methodBody + "getCharSequence";
                    break;
                case "java.lang.CharSequence[]":
                    methodBody = methodBody + "getCharSequenceArray";
                    break;
                case "java.util.ArrayList<java.lang.CharSequence>":
                    methodBody = methodBody + "getCharSequenceArrayList";
                    break;
                case "android.os.Bundle":
                    methodBody = methodBody + "getBundle";
                    break;
                default:
                    if (isParcelableArrayList(itemTypeMirror)) {
                        methodBody = methodBody + "getParcelableArrayList";
                    } else if (isParcelable(item)) {
                        methodBody = methodBody + "getParcelable";
                    } else if (isSerializable(item)) {
                        methodBody = "scene.%s = " + "(" + type + ")scene.getArguments().";
                        methodBody = methodBody + "getSerializable";
                    } else if (isParcelableArray(itemTypeMirror)) {
                        methodBody = "scene.%s = " + "(" + type + ")scene.getArguments().";
                        methodBody = methodBody + "getParcelableArray";
                    } else {
                        throw new IllegalArgumentException("@RouterValue not support type " + type);
                    }
            }
            methodBody = methodBody + "(\"%s\")";
            blockBuilder.addStatement(String.format(methodBody, variableName, sceneArgumentsKeyName));
        }
        blockBuilder.endControlFlow();
        bindViewMethodSpecBuilder.addCode(blockBuilder.build());

        TypeSpec typeSpec = TypeSpec.classBuilder("SceneRouter_" + element.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(bindViewMethodSpecBuilder.build())
                .build();
        JavaFile javaFile = JavaFile.builder(getPackageName(typeElement), typeSpec)
                .addFileComment("Code generated by SceneRouter. Do not edit.")
                .build();
        return javaFile;
    }

    private static boolean isParcelable(Element item) {
        TypeMirror typeMirror = item.asType();
        if (!(typeMirror instanceof DeclaredType)) {
            return false;
        }

        Element elt = ((DeclaredType) typeMirror).asElement();
        if (!(elt instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) elt;
        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        for (TypeMirror interfaceDeclaredType : interfaces) {
            if (!(interfaceDeclaredType instanceof DeclaredType)) {
                continue;
            }
            if ("android.os.Parcelable".equals(ClassName.get(interfaceDeclaredType).toString())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isParcelable(TypeMirror singleElementTypeMirror) {
        if (singleElementTypeMirror instanceof DeclaredType) {
            Element elt = ((DeclaredType) singleElementTypeMirror).asElement();
            if (elt instanceof TypeElement) {
                TypeElement singleElementTypeElement = (TypeElement) elt;
                List<? extends TypeMirror> interfaces = singleElementTypeElement.getInterfaces();

                for (TypeMirror interfaceDeclaredType : interfaces) {
                    if (!(interfaceDeclaredType instanceof DeclaredType)) {
                        continue;
                    }
                    if ("android.os.Parcelable".equals(ClassName.get(interfaceDeclaredType).toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isParcelableArray(TypeMirror itemTypeMirror) {
        if (itemTypeMirror.getKind() == TypeKind.ARRAY) {
            TypeMirror singleElementTypeMirror = ((ArrayType) itemTypeMirror).getComponentType();
            if (singleElementTypeMirror instanceof DeclaredType) {
                Element elt = ((DeclaredType) singleElementTypeMirror).asElement();
                if (elt instanceof TypeElement) {
                    TypeElement singleElementTypeElement = (TypeElement) elt;
                    List<? extends TypeMirror> interfaces = singleElementTypeElement.getInterfaces();

                    for (TypeMirror interfaceDeclaredType : interfaces) {
                        if (!(interfaceDeclaredType instanceof DeclaredType)) {
                            continue;
                        }
                        if ("android.os.Parcelable".equals(ClassName.get(interfaceDeclaredType).toString())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isParcelableArrayList(TypeMirror itemTypeMirror) {
        if (itemTypeMirror instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) itemTypeMirror;
            Element element = declaredType.asElement();
            if (!(element instanceof TypeElement)) {
                return false;
            }
            TypeElement typeElement = (TypeElement) element;
            String clazzName = typeElement.getQualifiedName().toString();
            if ("java.util.ArrayList".equals(clazzName)) {
//                this.mMessager.printMessage(Diagnostic.Kind.WARNING, (((DeclaredType) itemTypeMirror).asElement().getSimpleName()));  // List
                for (TypeMirror arg : ((DeclaredType) itemTypeMirror).getTypeArguments()) {
                    log("w " + arg.toString());  // List
                    if (isParcelable(arg)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean isSerializable(Element item) {
        TypeMirror typeMirror = item.asType();
        if (!(typeMirror instanceof DeclaredType)) {
            return false;
        }

        Element elt = ((DeclaredType) typeMirror).asElement();
        if (!(elt instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) elt;
        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        for (TypeMirror interfaceDeclaredType : interfaces) {
            if (!(interfaceDeclaredType instanceof DeclaredType)) {
                continue;
            }
            if ("java.io.Serializable".equals(ClassName.get(interfaceDeclaredType).toString())) {
                return true;
            }
        }
        return false;
    }

    private String getPackageName(TypeElement type) {
        return mElementUtils.getPackageOf(type).getQualifiedName().toString();
    }

    private void log(String log) {
        if (!this.mDebug) {
            return;
        }
        this.mMessager.printMessage(Diagnostic.Kind.WARNING, log);
    }
}
