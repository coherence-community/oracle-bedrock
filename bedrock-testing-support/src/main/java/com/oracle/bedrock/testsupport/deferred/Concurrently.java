/*
 * File: Concurrently.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting https://oss.oracle.com/licenses/CDDL
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

package com.oracle.bedrock.testsupport.deferred;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.deferred.Deferred;
import com.oracle.bedrock.deferred.DeferredFunction;
import com.oracle.bedrock.deferred.DeferredHelper;
import com.oracle.bedrock.testsupport.deferred.options.FailFast;
import com.oracle.bedrock.deferred.options.InitialDelay;
import com.oracle.bedrock.deferred.options.MaximumRetryDelay;
import com.oracle.bedrock.deferred.options.RetryFrequency;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.java.JavaApplication;
import org.hamcrest.Matcher;

import java.util.function.Function;

import static com.oracle.bedrock.deferred.DeferredHelper.eventually;
import static com.oracle.bedrock.deferred.DeferredHelper.valueOf;

/**
 * Commonly used "assertThat" methods for the purposes of concurrently asserting
 * the state of {@link Deferred} values using the style <code>Concurrently.assertThat(...)</code>
 * <p>
 * <code>
 * try (Concurrent.Assertion assertion = Concurrently.assertThat(invoking(someObject).get(), is(someValue))) {
 *
 *      // ...perform some operations...
 *
 *      assertion.check();   // ensure the assertion still holds
 *
 *      // ...perform some other operations...
 *
 *      assertion.check();   // ensure the assertion still holds
 * }
 * </code>
 * <p>
 * <code>Concurrently.assertThat(...)</code> methods are intended be used within
 * try-with-resources blocks.  When not, they should always be closed to ensure correct
 * clean-up of background resources used for concurrent assertion.
 * <p>
 * To control "fail-fast" semantics, where by the {@link Concurrent.Assertion} may interrupt the
 * {@link Thread} that created the {@link Concurrent.Assertion}, the {@link FailFast} option
 * should be specified.
 * <code>
 * try (Concurrent.Assertion assertion = Concurrently.assertThat(invoking(someObject).get(),
 *                                                               is(someValue),
 *                                                               FailFast.enabled())) {
 *
 *      // ...perform some long running operations... (will be interrupted if the assertion fails)
 *
 * } catch (InterruptedException e) {
 *     // determine if the InterruptedException was due to AssertionError
 *     Concurrent.Assertion.check(e);
 * }
 * </code>
 * <p>
 * To customize assertion behavior, this class allows extensive use of
 * {@link Option}s.   For example, the following {@link Option}s may be used
 * to customize timeout constraints when asserting {@link Deferred} values;
 * {@link Timeout}, {@link MaximumRetryDelay}, Initial {@link InitialDelay} and
 * {@link RetryFrequency}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @see Eventually
 * @see Repetitively
 *
 * @author Brian Oliver
 */
public class Concurrently
{
    /**
     * Creates a background thread to repetitively assert that a value satisfies the
     * specified {@link Matcher} using the specified {@link Option}s.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>      the type of the value
     *
     * @param value    the value
     * @param matcher  the {@link Matcher} for the value
     * @param options  the {@link Option}s for the assertion
     *
     * @return an {@link Concurrent.Assertion} to be used to determine the current success
     *         of the concurrent assertion
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> Concurrent.Assertion assertThat(T                  value,
                                                      Matcher<? super T> matcher,
                                                      Option...          options) throws AssertionError
    {
        return assertThat(null, eventually(value), matcher, options);
    }


    /**
     * Creates a background thread to repetitively assert that a value will
     * satisfy the specified {@link Matcher}.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>       the type of the value
     *
     * @param message   the message for the AssertionError (<code>null</code> ok)
     * @param value     the value
     * @param matcher   the {@link Matcher} for the value
     *
     * @return an {@link Concurrent.Assertion} to be used to determine the current success
     *         of the concurrent assertion
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> Concurrent.Assertion assertThat(String             message,
                                                      T                  value,
                                                      Matcher<? super T> matcher) throws AssertionError
    {
        return assertThat(message, eventually(value), matcher);
    }


    /**
     * Creates a background thread to repetitively assert that a value satisfies the
     * specified {@link Matcher} using the provided {@link Option}s.
     * <p>
     * Should the value be the result of a call to {@link DeferredHelper#invoking(Deferred)}
     * the result is unwrapped into a {@link Deferred} that is then used for the assert.
     *
     * @param <T>     the type of the value
     *
     * @param message  the message for the AssertionError (<code>null</code> ok)
     * @param value    the value
     * @param matcher  the {@link Matcher} for the value
     * @param options  the {@link Option}s
     *
     * @return an {@link Concurrent.Assertion} to be used to determine the current success
     *         of the concurrent assertion
     *
     * @throws AssertionError if the assertion fails
     */
    public static <T> Concurrent.Assertion assertThat(String             message,
                                                      T                  value,
                                                      Matcher<? super T> matcher,
                                                      Option...          options) throws AssertionError
    {
        return assertThat(message, eventually(value), matcher, options);
    }


    /**
     * Creates a background thread to repetitively assert that a {@link Deferred}
     * value, when available, satisfies the specified {@link Matcher} using
     * constraints defined by the provided {@link Option}s.
     *
     * @param <T>       the type of the value
     *
     * @param message   the message for the AssertionError (<code>null</code> ok)
     * @param deferred  the {@link Deferred} value
     * @param matcher   the {@link Matcher} for the value
     * @param options   the {@link Option}s
     *
     * @return an {@link Concurrent.Assertion} providing the ability to check the status and
     *         control the evaluation of the concurrent {@link Concurrent.Assertion}
     */
    public static <T> Concurrent.Assertion assertThat(String             message,
                                                      Deferred<T>        deferred,
                                                      Matcher<? super T> matcher,
                                                      Option...          options)
    {
        // create the assertion
        ConcurrentAssertion<T> assertion = new ConcurrentAssertion<T>(message, deferred, matcher, options);

        // start the assertion in the background
        assertion.setDaemon(true);
        assertion.setName("ConcurrentAssertion");

        assertion.start();

        return assertion;
    }


    /**
     * Asserts that a specified function, when applied to a value, will concurrently satisfy a
     * {@link Matcher} using the provided {@link Option}s.
     *
     * @param <T>       the type of the value
     * @param <R>       the return type of the function
     *
     * @param value     the value
     * @param function  the function to apply to the value
     * @param matcher   the {@link Matcher}
     * @param options   the {@link Option}s
     *
     * @return an {@link Concurrent.Assertion} providing the ability to check the status and
     *         control the evaluation of the concurrent {@link Concurrent.Assertion}
     */
    public static <T, R> Concurrent.Assertion assertThat(T                  value,
                                                         Function<T, R>     function,
                                                         Matcher<? super R> matcher,
                                                         Option...          options)
    {
        return assertThat(eventually(value), function, matcher, options);
    }


    /**
     * Asserts that a specified function, when applied to a {@link Deferred} value, will
     * concurrently satisfy a {@link Matcher} using the provided {@link Option}s.
     *
     * @param <T>       the type of the {@link Deferred} value
     * @param <R>       the return type of the function
     *
     * @param deferred  the {@link Deferred}
     * @param function  the function to apply to the value
     * @param matcher   the {@link Matcher}
     * @param options   the {@link Option}s
     *
     * @return an {@link Concurrent.Assertion} providing the ability to check the status and
     *         control the evaluation of the concurrent {@link Concurrent.Assertion}
     */
    public static <T, R> Concurrent.Assertion assertThat(Deferred<T>        deferred,
                                                         Function<T, R>     function,
                                                         Matcher<? super R> matcher,
                                                         Option...          options)
    {
        return assertThat(null, new DeferredFunction<>(deferred, function), matcher, options);
    }


    /**
     * Creates a background thread to repetitively assert that the specified {@link RemoteCallable}
     * submitted to the {@link JavaApplication} matches the specified {@link Matcher}.
     *
     * @param <T>          the type of the value
     *
     * @param application  the {@link JavaApplication} to which the {@link RemoteCallable} will be submitted
     * @param callable     the {@link RemoteCallable}
     * @param matcher      the {@link Matcher} representing the desire condition to match
     *
     * @return an {@link Concurrent.Assertion} providing the ability to check the status and
     *         control the evaluation of the concurrent {@link Concurrent.Assertion}
     */
    public static <T> Concurrent.Assertion assertThat(JavaApplication    application,
                                                      RemoteCallable<T>  callable,
                                                      Matcher<? super T> matcher)
    {
        return assertThat(DeferredHelper.valueOf(new DeferredRemoteExecution<T>(application, callable)), matcher);
    }


    /**
     * Creates a background thread to repetitively assert that the specified {@link RemoteCallable}
     * submitted to the {@link JavaApplication} matches the specified {@link Matcher}
     * using the specified {@link Option}s.
     *
     * @param <T>          the type of the value
     *
     * @param application  the {@link JavaApplication} to which the {@link RemoteCallable} will be submitted
     * @param callable     the {@link RemoteCallable}
     * @param matcher      the {@link Matcher} representing the desire condition to match
     * @param options      the {@link Option}s
     *
     * @return an {@link Concurrent.Assertion} providing the ability to check the status and
     *         control the evaluation of the concurrent {@link Concurrent.Assertion}
     */
    public static <T> Concurrent.Assertion assertThat(JavaApplication    application,
                                                      RemoteCallable<T>  callable,
                                                      Matcher<? super T> matcher,
                                                      Option...          options)
    {
        return assertThat(DeferredHelper.valueOf(new DeferredRemoteExecution<T>(application, callable)), matcher, options);
    }


    /**
     * An implementation of a concurrent {@link Concurrent.Assertion}.
     *
     * @param <T>  the type of value being asserted
     */
    static class ConcurrentAssertion<T> extends Thread implements Concurrent.Assertion
    {
        /**
         * The optional message for the {@link AssertionError}.
         */
        private final String message;

        /**
         * The {@link Deferred} value to assert.
         */
        private final Deferred<T> deferred;

        /**
         * The {@link Matcher}.
         */
        private final Matcher<? super T> matcher;

        /**
         * The {@link OptionsByType}.
         */
        private final OptionsByType optionsByType;

        /**
         * The {@link Thread} that created this {@link ConcurrentAssertion}.
         */
        private final Thread creatingThread;

        /**
         * The last encountered {@link AssertionError}.
         */
        private volatile AssertionError assertionError;

        /**
         * A flag indicating if the {@link ConcurrentAssertion} is closing.
         */
        private volatile boolean closing;

        /**
         * A flag indicating if the {@link ConcurrentAssertion} is closed.
         */
        private volatile boolean closed;

        /**
         * A flag indicating if the {@link AssertionError} should be thrown
         * when closing (in the {@link #close()} method).
         */
        private volatile boolean throwAssertionErrorWhenClosing;


        /**
         * Constructs a {@link ConcurrentAssertion}.
         *
         * @param message   the message for the AssertionError (<code>null</code> ok)
         * @param deferred  the {@link Deferred} value
         * @param matcher   the {@link Matcher} for the value
         * @param options   the {@link Option}s
         */
        public ConcurrentAssertion(String             message,
                                   Deferred<T>        deferred,
                                   Matcher<? super T> matcher,
                                   Option...          options)
        {
            this.message                        = message;
            this.deferred                       = deferred;
            this.matcher                        = matcher;
            this.optionsByType                  = OptionsByType.of(options);

            this.creatingThread                 = Thread.currentThread();
            this.assertionError                 = null;
            this.closing                        = false;
            this.closed                         = false;
            this.throwAssertionErrorWhenClosing = true;
        }


        @Override
        public void check() throws AssertionError
        {
            AssertionError assertionError = this.assertionError;

            if (assertionError != null)
            {
                // as we've checked the exception, we no longer need to throw it
                // (to avoid it being thrown twice)
                throwAssertionErrorWhenClosing = false;

                throw assertionError;
            }
        }


        @Override
        public boolean isClosed()
        {
            return closed;
        }


        @Override
        public void close()
        {
            // we're now closing
            closing = true;

            // interrupt ourselves to commence clean up
            this.interrupt();

            // throw the AssertionError (if we have one and we're throwing them)
            // (to allow it to be caught or seen as suppressed)
            if (assertionError != null && throwAssertionErrorWhenClosing)
            {
                throw assertionError;
            }
        }


        @Override
        public void run()
        {
            try
            {
                while (!closing)
                {
                    Repetitively.assertThat(message, deferred, matcher, optionsByType.asArray());
                }
            }
            catch (AssertionError e)
            {
                if (e.getCause() instanceof InterruptedException && closing)
                {
                    // we're ok being interrupted while closing!
                }
                else
                {
                    assertionError = e;

                    if (optionsByType.get(FailFast.class).isEnabled())
                    {
                        // attempt to interrupt the thread that created the ConcurrentAssertion
                        creatingThread.interrupt();
                    }
                }
            }
            finally
            {
                closed = true;
            }
        }
    }
}
