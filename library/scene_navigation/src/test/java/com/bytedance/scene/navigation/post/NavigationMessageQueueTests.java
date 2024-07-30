package com.bytedance.scene.navigation.post;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

import android.os.Handler;
import android.os.Looper;

import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.queue.NavigationRunnable;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

/**
 * Created by jiangqi on 2023/11/27
 *
 * @author jiangqi@bytedance.com
 */

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NavigationMessageQueueTests {

    @LooperMode(PAUSED)
    @Test
    public void testSinglePost() {
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });

        log.append("0");

        Assert.assertEquals(log.toString(), "0");

        shadowOf(getMainLooper()).idle();//execute Handler posted task

        Assert.assertEquals(log.toString(), "01");
    }

    @LooperMode(PAUSED)
    @Test
    public void testTwoPost() {
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });

        log.append("0");

        Assert.assertEquals(log.toString(), "0");

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("2");
            }
        });

        Assert.assertEquals(log.toString(), "0");

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        Assert.assertEquals(log.toString(), "01");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "012");
    }

    @LooperMode(PAUSED)
    @Test
    public void testSinglePostSync() {
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postSync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("0");
            }
        });

        log.append("1");

        Assert.assertEquals(log.toString(), "01");
    }

    @LooperMode(PAUSED)
    @Test
    public void testPostAtHead() {
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("2");
            }
        });

        messageQueue.postAsyncAtHead(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });

        log.append("0");

        Assert.assertEquals(log.toString(), "0");

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        Assert.assertEquals(log.toString(), "01");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "012");
    }

    @LooperMode(PAUSED)
    @Test
    public void testPostSyncAfterPostAsync() {
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("3");
            }
        });

        log.append("0");

        messageQueue.postAsyncAtHead(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("2");
            }
        });

        log.append("1");

        messageQueue.postSync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("4");
            }
        });

        Assert.assertEquals(log.toString(), "01234");
    }

    @LooperMode(PAUSED)
    @Test
    public void testPostSyncBeforeNormalPost() {
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        Handler handler = new Handler(Looper.getMainLooper());

        messageQueue.postSync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("0");
            }
        });

        log.append("1");

        handler.post(new Runnable() {
            @Override
            public void run() {
                log.append("2");
            }
        });

        Assert.assertEquals(log.toString(), "01");

        shadowOf(getMainLooper()).idle();//execute Handler posted task

        Assert.assertEquals(log.toString(), "012");
    }

    @LooperMode(PAUSED)
    @Test
    public void testMultiPostSyncAfterPostAsync() {
        final StringBuilder log = new StringBuilder();

        final NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");

                messageQueue.postAsync(new NavigationRunnable() {
                    @Override
                    public void run() {
                        log.append("3");
                    }
                });

                messageQueue.postAsyncAtHead(new NavigationRunnable() {
                    @Override
                    public void run() {
                        log.append("2");
                    }
                });
            }
        });

        messageQueue.postAsyncAtHead(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("0");
            }
        });

        messageQueue.postSync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("4");
            }
        });

        Assert.assertEquals(log.toString(), "01234");
    }

    @LooperMode(PAUSED)
    @Test
    public void testPostAsyncBeforeNormalPost() {
        final StringBuilder log = new StringBuilder();
        Handler handler = new Handler(Looper.getMainLooper());

        final NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("0");
            }
        });

        handler.post(new Runnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });

        Assert.assertEquals(log.toString(), "");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "0");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "01");
    }

    @LooperMode(PAUSED)
    @Test
    public void testPostAsyncAfterNormalPost() {
        final StringBuilder log = new StringBuilder();
        Handler handler = new Handler(Looper.getMainLooper());

        final NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        handler.post(new Runnable() {
            @Override
            public void run() {
                log.append("0");
            }
        });

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });

        Assert.assertEquals(log.toString(), "");

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        Assert.assertEquals(log.toString(), "0");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "01");
    }

    @LooperMode(PAUSED)
    @Test
    public void testPostAsyncAtHeadWithNormalPost() {
        final StringBuilder log = new StringBuilder();
        final Handler handler = new Handler(Looper.getMainLooper());

        final NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("0");

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        log.append("3");
                    }
                });

                //this message will be delayed to process "log.append("4")" to make sure navigation operation order
                //postAsyncAtHead will be executed before other postAsync
                messageQueue.postAsyncAtHead(new NavigationRunnable() {
                    @Override
                    public void run() {
                        log.append("2");
                    }
                });
            }
        });

        handler.post(new Runnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });

        //this message will be used to process "log.append("2")" to make sure navigation operation order
        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("4");
            }
        });

        Assert.assertEquals(log.toString(), "");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "0");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "01");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "012");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "0123");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "01234");
    }

    @LooperMode(PAUSED)
    @Test
    public void testPostAsyncNestPostSync() {
        final StringBuilder log = new StringBuilder();
        final Handler handler = new Handler(Looper.getMainLooper());

        final NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("0");

                messageQueue.postAsync(new NavigationRunnable() {
                    @Override
                    public void run() {
                        log.append("2");
                    }
                });

                messageQueue.postSync(new NavigationRunnable() {
                    @Override
                    public void run() {
                        log.append("3");
                    }
                });

                messageQueue.postAsync(new NavigationRunnable() {
                    @Override
                    public void run() {
                        log.append("4");
                    }
                });
            }
        });

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });

        Assert.assertEquals(log.toString(), "");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "0123");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "01234");
    }
}
