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
package com.bytedance.scene;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.xmlpull.v1.XmlPullParser;

final class SceneLayoutInflater extends LayoutInflater {
    private final Scene mScene;
    private LayoutInflater mLayoutInflater;

    SceneLayoutInflater(Context context, Scene scene) {
        super(context);
        this.mScene = scene;
    }

    @Override
    public void setFactory(LayoutInflater.Factory factory) {
        super.setFactory(factory);
        if (this.mLayoutInflater != null) {
            this.mLayoutInflater.setFactory(factory);
        }
    }

    @Override
    public void setFactory2(Factory2 factory) {
        super.setFactory2(factory);
        if (this.mLayoutInflater != null) {
            this.mLayoutInflater.setFactory2(factory);
        }
    }

    @Override
    public View inflate(int resource, @Nullable ViewGroup root) {
        createLayoutInflaterIfNeeded();
        return this.mLayoutInflater.inflate(resource, root);
    }

    @Override
    public View inflate(XmlPullParser parser, @Nullable ViewGroup root) {
        createLayoutInflaterIfNeeded();
        return this.mLayoutInflater.inflate(parser, root);
    }

    @Override
    public View inflate(int resource, @Nullable ViewGroup root, boolean attachToRoot) {
        createLayoutInflaterIfNeeded();
        return this.mLayoutInflater.inflate(resource, root, attachToRoot);
    }

    @Override
    public View inflate(XmlPullParser parser, @Nullable ViewGroup root, boolean attachToRoot) {
        createLayoutInflaterIfNeeded();
        return this.mLayoutInflater.inflate(parser, root, attachToRoot);
    }

    @Override
    public LayoutInflater cloneInContext(Context newContext) {
        return new SceneLayoutInflater(newContext, this.mScene);
    }

    private void createLayoutInflaterIfNeeded() {
        if (this.mLayoutInflater != null) {
            return;
        }

        Context context = null;
        if (this.mScene.getTheme() == 0) {
            context = this.mScene.requireActivity();
        } else {
            context = this.mScene.requireSceneContext();
        }
        //create new LayoutInflater
        this.mLayoutInflater = this.mScene.requireActivity().getLayoutInflater().cloneInContext(context);

        LayoutInflater.Filter filter = getFilter();
        if (filter != null) {
            this.mLayoutInflater.setFilter(filter);
        }

        LayoutInflater.Factory2 factory2 = getFactory2();
        if (factory2 != null) {
            this.mLayoutInflater.setFactory2(factory2);
        } else {
            LayoutInflater.Factory factory = getFactory();
            if (factory != null) {
                this.mLayoutInflater.setFactory(factory);
            }
        }
    }
}