package com.bytedance.scene;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

/**
 * Created by JiangQi on 9/4/18.
 */
public class SingeProcessMessengerHandler {
    public static interface Callback {
        void onResult(Object result);
    }

    private static String EXTRA_TAG = "SingeProcessMessengerHandler";
    private Messenger mMessenger;

    private SingeProcessMessengerHandler(Messenger messenger) {
        this.mMessenger = messenger;
    }

    private static Messenger createMessenger(final SingeProcessMessengerHandler.Callback callback) {
        return new Messenger(new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                callback.onResult(msg.obj);
                return true;
            }
        }));
    }

    public static void put(Intent intent, final SingeProcessMessengerHandler.Callback callback) {
        put(intent, callback, EXTRA_TAG);
    }

    public static void put(Intent intent, final SingeProcessMessengerHandler.Callback callback, String tagName) {
        Messenger messenger = createMessenger(callback);
        intent.putExtra(tagName, messenger);
    }

    public static void put(Bundle bundle, final SingeProcessMessengerHandler.Callback callback) {
        put(bundle, callback, EXTRA_TAG);
    }

    public static void put(Bundle bundle, final SingeProcessMessengerHandler.Callback callback, String tagName) {
        Messenger messenger = createMessenger(callback);
        bundle.putParcelable(tagName, messenger);
    }

    public static SingeProcessMessengerHandler from(Intent intent) {
        return from(intent, EXTRA_TAG);
    }

    public static SingeProcessMessengerHandler from(Bundle bundle) {
        return from(bundle, EXTRA_TAG);
    }

    public static SingeProcessMessengerHandler from(Intent intent, String tagName) {
        Messenger messenger = intent.getParcelableExtra(tagName);
        if (messenger != null) {
            return new SingeProcessMessengerHandler(messenger);
        } else {
            return null;
        }
    }

    public static SingeProcessMessengerHandler from(Bundle bundle, String tagName) {
        Messenger messenger = bundle.getParcelable(tagName);
        if (messenger != null) {
            return new SingeProcessMessengerHandler(messenger);
        } else {
            return null;
        }
    }

    public void sendResult(Object value) {
        Message message = Message.obtain();
        message.obj = value;
        try {
            this.mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}