package com.bytedance.scene;


import android.content.Intent;
import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.concurrent.atomic.AtomicReference;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
@LooperMode(PAUSED)
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
        shadowOf(getMainLooper()).idle();

        assertEquals("VALUE", atomicReference.get());
    }
}
