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
package com.bytedance.scene.ktx

import androidx.annotation.MainThread
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.bytedance.scene.Scene
import kotlin.reflect.KClass

/**
 * Returns a [Lazy] delegate to access the Scene's ViewModel, if [factoryProducer]
 * is specified then [ViewModelProvider.Factory] returned by it will be used
 * to create [ViewModel] first time.
 *
 * scoped to this Scene
 * ```
 * class TestScene : Scene() {
 *     val viewmodel: TestViewModel by viewModels()
 * }
 * ```
 *
 * scoped to parent Scene
 * ```
 * class TestScene : Scene() {
 *     val viewmodel: TestViewModel by viewModels ({requireParentScene()})
 * }
 * ```
 *
 * scoped to root NavigationScene
 * ```
 * class TestScene : Scene() {
 *     val viewmodel: TestViewModel by viewModels ({requireNavigationScene()})
 * }
 * ```
 *
 * scoped to Activity
 * ```
 * class TestScene : Scene() {
 *     val viewmodel: TestViewModel by activityViewModels()
 * }
 * ``
 *
 * This property can be accessed only after the Scene is attached,
 * and access prior to that will result in IllegalArgumentException.
 */
@MainThread
public inline fun <reified VM : ViewModel> Scene.activityViewModels(
        noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null): kotlin.Lazy<VM> {
    return createViewModelLazy(VM::class, {
        val activity = activity ?: throw IllegalArgumentException(
                "ViewModel can be accessed only when Scene is attached"
        )

        val fragmentActivity: FragmentActivity = activity as? FragmentActivity ?: throw IllegalArgumentException(
                "Activity should be FragmentActivity subclass"
        )
        fragmentActivity.viewModelStore
    }, factoryProducer)
}

@MainThread
public inline fun <reified VM : ViewModel> Scene.viewModels(
        noinline ownerProducer: () -> ViewModelStoreOwner = { this },
        noinline factoryProducer: (() -> ViewModelProvider.Factory)? = null): kotlin.Lazy<VM> {
    return createViewModelLazy(VM::class, { ownerProducer().viewModelStore }, factoryProducer)
}

@MainThread
fun <VM : ViewModel> Scene.createViewModelLazy(
        viewModelClass: KClass<VM>,
        storeProducer: () -> ViewModelStore,
        factoryProducer: (() -> ViewModelProvider.Factory)? = null
): Lazy<VM> {
    val factoryPromise = factoryProducer ?: {
        val application = activity?.application ?: throw IllegalStateException(
                "ViewModel can be accessed only when Scene is attached"
        )
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
    return ViewModelLazy(viewModelClass, storeProducer, factoryPromise)
}

@MainThread
private inline fun <reified VM : ViewModel> ViewModelProvider.get() = get(VM::class.java)

class ViewModelLazy<VM : ViewModel>(
        private val viewModelClass: KClass<VM>,
        private val storeProducer: () -> ViewModelStore,
        private val factoryProducer: () -> ViewModelProvider.Factory
) : Lazy<VM> {
    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                ViewModelProvider(store, factory).get(viewModelClass.java).also {
                    cached = it
                }
            } else {
                viewModel
            }
        }

    override fun isInitialized() = cached != null
}