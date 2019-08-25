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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiangQi on 9/6/18.
 */
public final class CancellationSignalList extends CancellationSignal {
    private List<CancellationSignal> mList = new ArrayList<>();

    public void add(CancellationSignal signal) {
        ThreadUtility.checkUIThread();
        this.mList.add(signal);
    }

    public void remove(CancellationSignal signal) {
        ThreadUtility.checkUIThread();
        this.mList.remove(signal);
    }

    @Override
    public void cancel() {
        for (CancellationSignal signal : mList) {
            signal.cancel();
        }
        super.cancel();
    }

    public CancellationSignal getChildCancellationSignal() {
        CancellationSignal cancellationSignal = new CancellationSignal();
        add(cancellationSignal);
        return cancellationSignal;
    }
}
