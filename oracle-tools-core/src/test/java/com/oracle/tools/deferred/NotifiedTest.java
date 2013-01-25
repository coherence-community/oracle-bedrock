/*
 * File: NotifiedTest.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
 *
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file LICENSE.txt.
 *
 * MODIFICATIONS:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 */

package com.oracle.tools.deferred;

import org.junit.Assert;
import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.notified;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Notified}s.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class NotifiedTest
{
    /**
     * Ensure that an object is notified after a period of time.
     *
     * @throws InterruptedException
     */
    @Test
    public void testNotified() throws InterruptedException
    {
        Object           objectToNotify = new Object();

        Notifier<Object> notifier       = new Notifier<Object>(objectToNotify);

        Deferred<Object> deferred       = notified(objectToNotify, 1, TimeUnit.SECONDS);

        notifier.notifyLater(Ensured.DEFAULT_RETRY_DURATION_MS, TimeUnit.MILLISECONDS);

        TimeUnit.MILLISECONDS.sleep(500);

        Assert.assertThat(deferred.get(), is(objectToNotify));
    }


    /**
     * Ensure that an object is notified after a period of time.
     *
     * @throws InterruptedException
     */
    @Test
    public void testNotifiedEventually() throws InterruptedException
    {
        Object           objectToNotify = new Object();

        Notifier<Object> notifier       = new Notifier<Object>(objectToNotify);

        Deferred<Object> deferred       = notified(objectToNotify, 2, TimeUnit.SECONDS);

        Assert.assertThat(deferred.get(), nullValue());

        notifier.notifyLater(1, TimeUnit.SECONDS);

        TimeUnit.MILLISECONDS.sleep(500);

        Assert.assertThat(deferred.get(), nullValue());

        TimeUnit.SECONDS.sleep(1);

        Assert.assertThat(deferred.get(), is(objectToNotify));
    }


    /**
     * Ensure that an object is never notified.
     */
    @Test(expected = ObjectNotAvailableException.class)
    public void testNeverNotified() throws InterruptedException
    {
        Object           objectToNotify = new Object();

        Deferred<Object> deferred       = notified(objectToNotify, 500, TimeUnit.MILLISECONDS);

        TimeUnit.SECONDS.sleep(1);

        // attempting to get the notified object should result in an exception
        deferred.get();

        Assert.fail("The object should not have been notified");
    }


    /**
     * A {@link Notifier} will notify a specified object after a period of time.
     */
    public static class Notifier<T>
    {
        /**
         * The object to notify.
         */
        private T m_objectToNotify;


        /**
         * Constructs a {@link Notifier}.
         *
         * @param objectToNotify  the object to notify
         */
        public Notifier(T objectToNotify)
        {
            m_objectToNotify = objectToNotify;
        }


        /**
         * Requests the {@link Notifier} to attempt to asynchronously notify
         * the object (on a background thread) after the specified amount of time.
         *
         * @param waitDuration
         * @param waitDurationUnit
         */
        public void notifyLater(long waitDuration,
                                TimeUnit waitDurationUnit)
        {
            Thread thread = new Thread(new Runner(waitDuration, waitDurationUnit));

            thread.setDaemon(true);
            thread.start();
        }


        /**
         * Requests the {@link Notifier} to immediately notify the specified
         * object (on the calling thread)
         */
        public void notifyNow()
        {
            new Runner().run();
        }


        /**
         * The {@link Runnable} that can be used to notify the object.
         */
        private class Runner implements Runnable
        {
            /**
             * The duration to wait before notifying.
             */
            private long m_waitDuration;

            /**
             * The units of the duration to wait.
             */
            private TimeUnit m_waitDurationUnit;


            /**
             * Constructs a {@link Runner} that won't wait to notify the object.
             */
            public Runner()
            {
                m_waitDuration     = 0;
                m_waitDurationUnit = TimeUnit.SECONDS;
            }


            /**
             * Constructs a {@link Runner} that will wait the specified
             * duration before notifying the object.
             *
             * @param waitDuration      the duration to wait
             * @param waitDurationUnit  the {@link TimeUnit} of the duration
             */
            public Runner(long waitDuration,
                          TimeUnit waitDurationUnit)
            {
                m_waitDuration     = waitDuration;
                m_waitDurationUnit = waitDurationUnit;
            }


            /**
             * {@inheritDoc}
             */
            @Override
            public void run()
            {
                synchronized (m_objectToNotify)
                {
                    try
                    {
                        if (m_waitDuration > 0)
                        {
                            m_waitDurationUnit.timedWait(m_objectToNotify, m_waitDuration);
                        }

                        m_objectToNotify.notifyAll();
                    }
                    catch (InterruptedException e)
                    {
                        System.err.println("Failed to wait for the specified duration as we were interrupted.\n" + e);
                    }
                }
            }
        }
    }
}
