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
