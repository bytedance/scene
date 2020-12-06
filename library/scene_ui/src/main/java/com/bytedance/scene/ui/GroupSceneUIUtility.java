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
package com.bytedance.scene.ui;

import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.bytedance.scene.Scene;
import com.bytedance.scene.group.GroupScene;
import com.bytedance.scene.group.UserVisibleHintGroupScene;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by JiangQi on 8/16/18.
 */
public class GroupSceneUIUtility {
    public static void setupWithBottomNavigationView(@NonNull final BottomNavigationView bottomNavigationView,
                                                     @NonNull final GroupScene groupScene,
                                                     @IdRes final int containerId,
                                                     @NonNull final LinkedHashMap<Integer, Scene> children) {
        if (children.size() == 0) {
            throw new IllegalArgumentException("children can't be empty");
        }

        final List<String> menuIdList = new ArrayList<>();
        int menuSize = bottomNavigationView.getMenu().size();
        for (int i = 0; i < menuSize; i++) {
            menuIdList.add("" + bottomNavigationView.getMenu().getItem(i).getItemId());
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        item.setChecked(true);

                        String tag = "" + item.getItemId();

                        Scene scene = groupScene.findSceneByTag(tag);
                        if (scene == null) {
                            scene = children.get(item.getItemId());
                        }

                        if (!groupScene.isAdded(scene)) {
                            groupScene.add(containerId, scene, tag);
                        } else if (!groupScene.isShow(scene)) {
                            groupScene.show(scene);
                        }

                        for (int i = 0; i < menuIdList.size(); i++) {
                            Scene otherScene = groupScene.findSceneByTag(menuIdList.get(i));
                            if (otherScene != null && otherScene != scene && groupScene.isAdded(otherScene) && groupScene.isShow(otherScene)) {
                                groupScene.hide(otherScene);
                            }
                        }

                        return true;
                    }
                });

        Map.Entry<Integer, Scene> firstItem = children.entrySet().iterator().next();
        String tag = "" + firstItem.getKey();
        Scene scene = groupScene.findSceneByTag(tag);
        if (scene == null) {
            scene = firstItem.getValue();
        }

        if (!groupScene.isAdded(scene)) {
            groupScene.add(containerId, scene, tag);
        } else if (!groupScene.isShow(scene)) {
            groupScene.show(scene);
        }

        bottomNavigationView.getMenu().findItem(firstItem.getKey()).setChecked(true);
    }

    public static void setupWithNavigationView(@NonNull final DrawerLayout drawerLayout,
                                               @NonNull final NavigationView navigationView,
                                               @NonNull final GroupScene groupScene,
                                               @IdRes final int containerId,
                                               @NonNull final LinkedHashMap<Integer, Scene> children,
                                               @Nullable final NavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener) {
        if (children.size() == 0) {
            throw new IllegalArgumentException("children can't be empty");
        }

        final List<String> menuIdList = new ArrayList<>();
        int menuSize = navigationView.getMenu().size();
        for (int i = 0; i < menuSize; i++) {
            menuIdList.add("" + navigationView.getMenu().getItem(i).getItemId());
        }

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (onNavigationItemSelectedListener != null) {
                            onNavigationItemSelectedListener.onNavigationItemSelected(item);
                        }
                        Menu menu = navigationView.getMenu();
                        Set<Integer> menuItemIdSet = children.keySet();
                        for (Integer menuItemId : menuItemIdSet) {
                            MenuItem menuItemById = menu.findItem(menuItemId);
                            if (menuItemById == item) {
                                menuItemById.setChecked(true);
                            } else {
                                menuItemById.setChecked(false);
                            }
                        }
                        drawerLayout.closeDrawer(navigationView);
                        String tag = "" + item.getItemId();

                        Scene scene = groupScene.findSceneByTag(tag);
                        if (scene == null) {
                            scene = children.get(item.getItemId());
                        }

                        if (!groupScene.isAdded(scene)) {
                            groupScene.add(containerId, scene, tag);
                        } else if (!groupScene.isShow(scene)) {
                            groupScene.show(scene);
                        }

                        for (int i = 0; i < menuIdList.size(); i++) {
                            Scene otherScene = groupScene.findSceneByTag(menuIdList.get(i));
                            if (otherScene != null && otherScene != scene && groupScene.isAdded(otherScene) && groupScene.isShow(otherScene)) {
                                groupScene.hide(otherScene);
                            }
                        }
                        return true;
                    }
                });
        Map.Entry<Integer, Scene> firstItem = children.entrySet().iterator().next();
        String tag = "" + firstItem.getKey();
        Scene scene = groupScene.findSceneByTag(tag);
        if (scene == null) {
            scene = firstItem.getValue();
        }

        if (!groupScene.isAdded(scene)) {
            groupScene.add(containerId, scene, tag);
        } else if (!groupScene.isShow(scene)) {
            groupScene.show(scene);
        }
        MenuItem menuItem = navigationView.getMenu().findItem(firstItem.getKey());
        menuItem.setChecked(true);
        if (onNavigationItemSelectedListener != null) {
            onNavigationItemSelectedListener.onNavigationItemSelected(menuItem);
        }
    }

    public static void setupWithViewPager(@NonNull final ViewPager viewPager,
                                          @NonNull final GroupScene groupScene,
                                          @NonNull final List<UserVisibleHintGroupScene> children) {
        if (viewPager.getAdapter() != null) {
            throw new IllegalArgumentException("ViewPager already have a adapter");
        }

        ScenePageAdapter scenePageAdapter = new ScenePageAdapter(groupScene) {

            @Override
            public int getCount() {
                return children.size();
            }

            @Override
            public UserVisibleHintGroupScene getItem(int position) {
                return children.get(position);
            }
        };
        viewPager.setAdapter(scenePageAdapter);
    }

    public static void setupWithViewPager(@NonNull final ViewPager viewPager,
                                          @NonNull final GroupScene groupScene,
                                          @NonNull final LinkedHashMap<String, UserVisibleHintGroupScene> children) {
        if (viewPager.getAdapter() != null) {
            throw new IllegalArgumentException("ViewPager already have a adapter");
        }
        final List<String> titleList = new ArrayList<>(children.keySet());
        final List<UserVisibleHintGroupScene> sceneList = new ArrayList<>();
        for (String key : titleList) {
            sceneList.add(children.get(key));
        }
        ScenePageAdapter scenePageAdapter = new ScenePageAdapter(groupScene) {

            @Override
            public int getCount() {
                return sceneList.size();
            }

            @Override
            public UserVisibleHintGroupScene getItem(int position) {
                return sceneList.get(position);
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titleList.get(position);
            }
        };
        viewPager.setAdapter(scenePageAdapter);
    }
}
