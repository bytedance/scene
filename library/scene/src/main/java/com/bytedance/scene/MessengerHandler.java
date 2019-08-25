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
public class MessengerHandler {
    public static interface Callback {
        void onResult(Bundle result);
    }

    private static String EXTRA_TAG = "MessengerHandler";
    private static String EXTRA_DATA = "Data";
    private Messenger mMessenger;

    private MessengerHandler(Messenger messenger) {
        this.mMessenger = messenger;
    }

    private static Messenger createMessenger(final MessengerHandler.Callback callback) {
        return new Messenger(new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Bundle intent = msg.getData().getParcelable(EXTRA_DATA);
                callback.onResult(intent);
                return true;
            }
        }));
    }

    public static void put(Intent intent, final MessengerHandler.Callback callback) {
        put(intent, callback, EXTRA_TAG);
    }

    public static void put(Intent intent, final MessengerHandler.Callback callback, String tagName) {
        Messenger messenger = createMessenger(callback);
        intent.putExtra(tagName, messenger);
    }

    public static void put(Bundle bundle, final MessengerHandler.Callback callback) {
        put(bundle, callback, EXTRA_TAG);
    }

    public static void put(Bundle bundle, final MessengerHandler.Callback callback, String tagName) {
        Messenger messenger = createMessenger(callback);
        bundle.putParcelable(tagName, messenger);
    }

    public static MessengerHandler from(Intent intent) {
        return from(intent, EXTRA_TAG);
    }

    public static MessengerHandler from(Bundle bundle) {
        return from(bundle, EXTRA_TAG);
    }

    public static MessengerHandler from(Intent intent, String tagName) {
        Messenger messenger = intent.getParcelableExtra(tagName);
        if (messenger != null) {
            return new MessengerHandler(messenger);
        } else {
            return null;
        }
    }

    public static MessengerHandler from(Bundle bundle, String tagName) {
        Messenger messenger = bundle.getParcelable(tagName);
        if (messenger != null) {
            return new MessengerHandler(messenger);
        } else {
            return null;
        }
    }

    public void sendResult(Bundle value) {
        Message message = Message.obtain();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_DATA, value);
        message.setData(bundle);
        try {
            this.mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}