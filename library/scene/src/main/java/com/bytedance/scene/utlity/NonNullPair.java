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
package com.bytedance.scene.utlity;

import androidx.annotation.NonNull;

public class NonNullPair<F, S> {
    @NonNull
    public final F first;
    @NonNull
    public final S second;

    private NonNullPair(@NonNull F first, @NonNull S second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NonNullPair)) {
            return false;
        }
        NonNullPair<?, ?> p = (NonNullPair<?, ?>) o;
        return objectsEqual(p.first, first) && objectsEqual(p.second, second);
    }

    private static boolean objectsEqual(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public int hashCode() {
        return first.hashCode() ^ second.hashCode();
    }

    @Override
    public String toString() {
        return "Pair{" + String.valueOf(first) + " " + String.valueOf(second) + "}";
    }

    @NonNull
    public static <A, B> NonNullPair<A, B> create(@NonNull A a, @NonNull B b) {
        return new NonNullPair<A, B>(a, b);
    }
}
