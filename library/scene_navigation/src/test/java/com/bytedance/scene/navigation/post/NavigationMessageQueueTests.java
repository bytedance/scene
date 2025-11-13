package com.bytedance.scene.navigation.post;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;

import android.os.Handler;
import android.os.Looper;

import com.bytedance.scene.SceneGlobalConfig;
import com.bytedance.scene.queue.NavigationMessageQueue;
import com.bytedance.scene.queue.NavigationRunnable;
import com.bytedance.scene.utlity.CancellationSignal;
import com.bytedance.scene.utlity.TaskStartSignal;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;

import java.util.concurrent.TimeUnit;

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
    public void testMultiPostAtHead() {
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsyncAtHead(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });

        messageQueue.postAsyncAtHead(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("2");
            }
        });

        log.append("0");

        Assert.assertEquals(log.toString(), "0");

        shadowOf(getMainLooper()).runOneTask();//execute Handler posted task
        Assert.assertEquals(log.toString(), "02");

        shadowOf(getMainLooper()).runOneTask();
        Assert.assertEquals(log.toString(), "021");
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

    @LooperMode(PAUSED)
    @Test
    public void testExecuteWhenIdleOrTimeLimitWithBug() {
        SceneGlobalConfig.fixNavigationMessageQueueMessageOrderIssue = false;
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });
        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("2");
            }
        });
        messageQueue.executeWhenIdleOrTimeLimit(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("3");
            }
        }, new TaskStartSignal(), null, 5000L);//use TaskStartSignal disable Looper.myQueue().addIdleHandler

        log.append("0");

        Assert.assertEquals("0", log.toString());

        ShadowLooper.idleMainLooper(1000, TimeUnit.MILLISECONDS);
        Assert.assertEquals("031", log.toString());

        ShadowLooper.idleMainLooper(5000, TimeUnit.MILLISECONDS);
        Assert.assertEquals("0312", log.toString());
        Assert.assertEquals(0, messageQueue.getDelayMessageCount());

        SceneGlobalConfig.fixNavigationMessageQueueMessageOrderIssue = false;
    }

    @LooperMode(PAUSED)
    @Test
    public void testExecuteWhenIdleOrTimeLimitWithBugFixed() {
        SceneGlobalConfig.fixNavigationMessageQueueMessageOrderIssue = true;
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        });
        messageQueue.postAsync(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("2");
            }
        });
        messageQueue.executeWhenIdleOrTimeLimit(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("3");
            }
        }, new TaskStartSignal(), null, 5000L);//use TaskStartSignal disable Looper.myQueue().addIdleHandler

        log.append("0");

        Assert.assertEquals("0", log.toString());

        ShadowLooper.idleMainLooper(1000, TimeUnit.MILLISECONDS);//execute Handler posted task
        Assert.assertEquals("0", log.toString());//nothing happened
        Assert.assertEquals(2, messageQueue.getDelayMessageCount());//the first and second messages is delayed

        ShadowLooper.idleMainLooper(6000, TimeUnit.MILLISECONDS);//execute Handler posted task
        Assert.assertEquals("0312", log.toString());
        Assert.assertEquals(0, messageQueue.getDelayMessageCount());

        SceneGlobalConfig.fixNavigationMessageQueueMessageOrderIssue = false;
    }

    @LooperMode(PAUSED)
    @Test
    public void testExecuteWhenIdleOrTimeLimitCancel() {
        final StringBuilder log = new StringBuilder();

        NavigationMessageQueue messageQueue = new NavigationMessageQueue();

        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                log.append("2");
            }
        });
        messageQueue.executeWhenIdleOrTimeLimit(new NavigationRunnable() {
            @Override
            public void run() {
                log.append("1");
            }
        }, new TaskStartSignal(), cancellationSignal, 5000L);//use TaskStartSignal disable Looper.myQueue().addIdleHandler

        log.append("0");

        Assert.assertEquals("0", log.toString());

        ShadowLooper.idleMainLooper(1000, TimeUnit.MILLISECONDS);//execute Handler posted task
        Assert.assertEquals("0", log.toString());//nothing happened

        messageQueue.forceExecuteIdleTask();
        cancellationSignal.cancel();
        Assert.assertEquals("021", log.toString());
    }
}
