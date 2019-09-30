package com.bytedance.scene;


import android.content.Intent;
import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MessengerHandlerTests {
    @Test
    public void test() {
        Intent intent = new Intent();
        final AtomicReference<String> atomicReference = new AtomicReference<>();
        MessengerHandler.Callback callback = new MessengerHandler.Callback() {
            @Override
            public void onResult(Bundle result) {
                atomicReference.set(result.getString("KEY", ""));
            }
        };
        MessengerHandler.put(intent, callback);

        Bundle result = new Bundle();
        result.putString("KEY", "VALUE");
        MessengerHandler.from(intent).sendResult(result);
        assertEquals("VALUE", atomicReference.get());
    }
}
