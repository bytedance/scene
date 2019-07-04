package com.bytedance.scene.utlity;

import android.os.Build;
import android.support.v4.os.OperationCanceledException;

public class CancellationSignal {
    private boolean mIsCanceled;
    private OnCancelListener mOnCancelListener;
    private Object mCancellationSignalObj;
    private boolean mCancelInProgress;

    /**
     * Creates a cancellation signal, initially not canceled.
     */
    public CancellationSignal() {
    }

    /**
     * Returns true if the operation has been canceled.
     *
     * @return True if the operation has been canceled.
     */
    public boolean isCanceled() {
        synchronized (this) {
            return mIsCanceled;
        }
    }

    /**
     * Throws {@link android.support.v4.os.OperationCanceledException} if the operation has been canceled.
     *
     * @throws OperationCanceledException if the operation has been canceled.
     */
    public void throwIfCanceled() {
        if (isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    /**
     * Cancels the operation and signals the cancellation listener.
     * If the operation has not yet started, then it will be canceled as soon as it does.
     */
    public void cancel() {
        final OnCancelListener listener;
        final Object obj;
        synchronized (this) {
            if (mIsCanceled) {
                return;
            }
            mIsCanceled = true;
            mCancelInProgress = true;
            listener = mOnCancelListener;
            obj = mCancellationSignalObj;
        }

        try {
            if (listener != null) {
                listener.onCancel();
            }
            if (obj != null && Build.VERSION.SDK_INT >= 16) {
                ((android.os.CancellationSignal) obj).cancel();
            }
        } finally {
            synchronized (this) {
                mCancelInProgress = false;
                notifyAll();
            }
        }
    }

    /**
     * Sets the cancellation listener to be called when canceled.
     * <p>
     * This method is intended to be used by the recipient of a cancellation signal
     * such as a database or a content provider to handle cancellation requests
     * while performing a long-running operation.  This method is not intended to be
     * used by applications themselves.
     * <p>
     * If {@link CancellationSignal#cancel} has already been called, then the provided
     * listener is invoked immediately.
     * <p>
     * This method is guaranteed that the listener will not be called after it
     * has been removed.
     *
     * @param listener The cancellation listener, or null to remove the current listener.
     */
    public void setOnCancelListener(OnCancelListener listener) {
        synchronized (this) {
            waitForCancelFinishedLocked();

            if (mOnCancelListener == listener) {
                return;
            }
            mOnCancelListener = listener;
            if (!mIsCanceled || listener == null) {
                return;
            }
        }
        listener.onCancel();
    }

    /**
     * Gets the framework {@link android.os.CancellationSignal} associated with this object.
     * <p>
     * Framework support for cancellation signals was added in
     * {@link android.os.Build.VERSION_CODES#JELLY_BEAN} so this method will always
     * return null on older versions of the platform.
     * </p>
     *
     * @return A framework cancellation signal object, or null on platform versions
     * prior to Jellybean.
     */
    public Object getCancellationSignalObject() {
        if (Build.VERSION.SDK_INT < 16) {
            return null;
        }
        synchronized (this) {
            if (mCancellationSignalObj == null) {
                mCancellationSignalObj = new android.os.CancellationSignal();
                if (mIsCanceled) {
                    ((android.os.CancellationSignal) mCancellationSignalObj).cancel();
                }
            }
            return mCancellationSignalObj;
        }
    }

    private void waitForCancelFinishedLocked() {
        while (mCancelInProgress) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Listens for cancellation.
     */
    public interface OnCancelListener {
        /**
         * Called when {@link CancellationSignal#cancel} is invoked.
         */
        void onCancel();
    }
}
