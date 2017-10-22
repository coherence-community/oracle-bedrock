/*
 * File: JUnitTestRunner.java
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

package com.oracle.bedrock.testsupport.junit;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.testsupport.junit.options.TestClasses;
import com.oracle.bedrock.testsupport.junit.options.Tests;
import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;
import com.oracle.bedrock.runtime.concurrent.options.StreamName;
import com.oracle.bedrock.runtime.java.JavaVirtualMachine;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A class that runs a set of JUnit tests.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JUnitTestRunner implements Runnable
{
    /**
     * The name of the event stream that will be used to send JUnit test events.
     */
    public static final StreamName STREAM_NAME = StreamName.of("JUnit");

    /**
     * The singleton instance of the {@link JUnitTestRunner}.
     */
    public static final JUnitTestRunner INSTANCE = new JUnitTestRunner();

    /**
     * The {@link RemoteChannel} to use to publish JUnit test events.
     */
    public static RemoteChannel channel;

    /**
     * The current state of this {@link JUnitTestRunner}.
     */
    private State state = State.NotRunning;

    /**
     * The lock used for various synchronized operations.
     */
    private final Object MONITOR = new Object();

    /**
     * The set of {@link OptionsByType} controlling the test run
     */
    private OptionsByType optionsByType;


    /**
     * The different states that this {@link JUnitTestRunner} can be in.
     */
    public enum State
    {
        /**
         * Waiting to start run.
         */
        NotRunning,

        /**
         * Waiting to start tests.
         */
        Waiting,

        /**
         * Running a set of tests.
         */
        Running,

        /**
         * Completed a set of tests.
         */
        Completed,

        /**
         * The application has stopped.
         */
        Stopped;
    }


    /**
     * Obtain the current {@link State}.
     *
     * @return  the current {@link State}
     */
    public State getState()
    {
        return state;
    }


    /**
     * The main run method.
     */
    @Override
    public void run()
    {
        synchronized (MONITOR)
        {
            // Make sure we have not already changed to Stopped
            if (state == State.NotRunning)
            {
                // Change to waiting
                state = State.Waiting;
            }

            // Wait for the state to change from waiting to something else
            awaitStateChangeFrom(State.Waiting);
        }

        // If the state changed to running the run the tests
        if (state == State.Running)
        {
            long start = System.currentTimeMillis();

            try
            {
                if (channel != null)
                {
                    // Raise an event to signal that the tests are starting
                    channel.raise(JUnitTestListener.Event.junitStarted(), STREAM_NAME);
                }

                runTests(this.optionsByType);
            }
            catch (Throwable e)
            {
                e.printStackTrace();
            }
            finally
            {
                synchronized (MONITOR)
                {
                    if (state == State.Running)
                    {
                        state = State.Completed;
                        MONITOR.notifyAll();
                    }
                }

                long end = System.currentTimeMillis();
                RemoteChannel.AcknowledgeWhen acknowledge = (state != State.Stopped)
                                                            ? RemoteChannel.AcknowledgeWhen.PROCESSED
                                                            : RemoteChannel.AcknowledgeWhen.SENT;

                if (channel != null)
                {
                    // Raise an event to signal that the tests are complete
                    // We will use the future to change the final state
                    CompletableFuture<Void> future = channel.raise(JUnitTestListener.Event.junitCompleted(end - start),
                                                                   STREAM_NAME,
                                                                   acknowledge);

                    // When the future completes, regardless of how, change the state to stopped
                    future.whenComplete((_void, throwable) -> changeState(State.Stopped));
                }
                else
                {
                    changeState(State.Stopped);
                }
            }

            // Wait for the state to change to Stopped. Either we will already be stopped
            // or the state will chage when the futre returned from the final event
            // is completed.
            awaitStateChangeTo(State.Stopped);
        }
    }


    private void changeState(State state)
    {
        synchronized (MONITOR)
        {
            this.state = state;

            MONITOR.notifyAll();
        }
    }


    private void awaitStateChangeFrom(State currentState)
    {
        synchronized (MONITOR)
        {
            while (state == currentState)
            {
                try
                {
                    MONITOR.wait(1000);
                }
                catch (InterruptedException e)
                {
                    state = State.Stopped;
                    break;
                }
            }
        }
    }


    private void awaitStateChangeTo(State nextState)
    {
        synchronized (MONITOR)
        {
            while (state != nextState)
            {
                try
                {
                    MONITOR.wait(1000);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }
        }
    }


    /**
     * Run the tests specified by the given {@link OptionsByType}.
     *
     * @param optionsByType  the {@link OptionsByType} controlling the test run
     */
    private void runTests(OptionsByType optionsByType)
    {
        // If the options are null there is nothing to do
        if (optionsByType == null)
        {
            return;
        }

        Tests tests = optionsByType.get(Tests.class);

        // If there are no tests to execute then return
        if (tests.isEmpty())
        {
            return;
        }

        JUnitCore jUnitCore = new JUnitCore();
        Result    result    = new Result();
        Listener  listener  = new Listener();

        jUnitCore.addListener(result.createListener());
        jUnitCore.addListener(listener);

        for (RunListener runListener : optionsByType.getInstancesOf(RunListener.class))
        {
            jUnitCore.addListener(runListener);
        }

        // Run the tests in each TestClasses instance
        for (TestClasses testClasses : tests)
        {
            Set<Class<?>> classes = testClasses.resolveTestClasses();
            Filter        filter  = testClasses.getTestFilter();

            for (Class<?> testClass : classes)
            {
                Request request = Request.aClass(testClass);

                if (filter != null)
                {
                    request = request.filterWith(filter);
                }

                jUnitCore.run(request);

                // If the state has changed to stopped then we should exit
                if (state == State.Stopped)
                {
                    return;
                }
            }
        }
    }


    /**
     * Wait until the {@link JUnitTestRunner} is in the {@link State#Waiting} state
     * and then start a test run using the specified {@link OptionsByType}.
     *
     * @param optionsByType  the {@link OptionsByType} controlling the test run
     */
    public void run(OptionsByType optionsByType)
    {
        synchronized (MONITOR)
        {
            while (state == State.NotRunning)
            {
                try
                {
                    MONITOR.wait(1000);
                }
                catch (InterruptedException e)
                {
                    break;
                }
            }

            if (state == State.Waiting)
            {
                this.optionsByType = optionsByType;
                this.state         = State.Running;

                MONITOR.notifyAll();
            }
            else
            {
                throw new IllegalStateException("JUnit runner is not in a valid state to accept tests. State=" + state);
            }
        }
    }


    /**
     * Set the current state to {@link State#Stopped}, which will cause
     * the application to exit.
     * <p>
     * If a test run is in progress it will be halted after the current test class has completed.
     */
    public void stop()
    {
        if (state == State.Stopped)
        {
            return;
        }

        synchronized (MONITOR)
        {
            state = State.Stopped;
            MONITOR.notifyAll();
        }
    }


    /**
     * The main entry point for this application.
     *
     * @param args  the application arguments
     */
    public static void main(String[] args)
    {
        // acquire the RemoteChannel for the JUnitTestRunner
        channel = RemoteChannel.get();

        // run the JUnit tests
        JUnitTestRunner.INSTANCE.run();
    }


    /**
     * An instance of a JUnit {@link RunListener} that listens for JUnit run events
     * and forwards them on the the {@link RemoteChannel} as {@link JUnitTestListener.Event}s.
     */
    public static class Listener extends RunListener
    {
        /**
         * The {@link Description} of the current test run.
         */
        private final ThreadLocal<Description> testRunDescription = new InheritableThreadLocal<>();

        /**
         * The name of the current class being tested.
         */
        private final ThreadLocal<String> currentTestClass = new InheritableThreadLocal<>();

        /**
         * A flag indicating whether the current test failed.
         */
        private final ThreadLocal<Boolean> testFailed = new InheritableThreadLocal<>();

        /**
         * The start time of the current test run.
         */
        private final ThreadLocal<Long> runStartTime = new InheritableThreadLocal<>();

        /**
         * The start time for the current test class.
         */
        private final ThreadLocal<Long> classStartTime = new InheritableThreadLocal<>();

        /**
         * The start time of the current test.
         */
        private final ThreadLocal<Long> testStartTime = new InheritableThreadLocal<>();


        /**
         * Obtain the {@link Description} of the current test run.
         *
         * @return  the {@link Description} of the current test run
         */
        public Description getTestRunDescription()
        {
            return testRunDescription.get();
        }


        /**
         * Obtain the name of the current test class.
         *
         * @return  the name of the current test class
         */
        public String getCurrentTestClass()
        {
            return currentTestClass.get();
        }


        /**
         * Obtain the start time of the current test class.
         *
         * @return  the start time of the current test class
         */
        public Long getClassStartTime()
        {
            return classStartTime.get();
        }


        /**
         * Obtain the start time of the current test run.
         *
         * @return  the start time of the current test run
         */
        public Long getRunStartTime()
        {
            return runStartTime.get();
        }


        /**
         * Obtain the start time of the current test.
         *
         * @return  the start time of the current test
         */
        public Long getTestStartTime()
        {
            return testStartTime.get();
        }


        /**
         * Determine whether the current test has failed.
         *
         * @return  <code>true</code> if the current test
         *          has failed, otherwise <code>false</code>
         */
        public Boolean hasTestFailed()
        {
            return testFailed.get();
        }


        /**
         * Set whether the current test has failed.
         *
         * @param failed  <code>true</code> if the test has
         *                failed, <code>false</code> if the
         *                test has not failed
         */
        public void setTestFailed(boolean failed)
        {
            testFailed.set(failed);
        }


        @Override
        public void testRunStarted(Description description) throws Exception
        {
            currentTestClass.set(null);
            setTestFailed(false);
            testRunDescription.set(description);

            raiseEvent(JUnitTestListener.Event.testRunStarted(description.getDisplayName(), System.getProperties()));

            runStartTime.set(System.currentTimeMillis());
        }


        @Override
        public void testRunFinished(Result result) throws Exception
        {
            long        endTime          = System.currentTimeMillis();
            long        time             = endTime - runStartTime.get();
            Description description      = testRunDescription.get();
            Long        classStart       = classStartTime.get();
            long        classRunTime     = classStart != null ? endTime - classStart : 0L;
            String      currentClassName = currentTestClass.get();

            if (currentClassName != null)
            {
                raiseEvent(JUnitTestListener.Event.testClassFinished(currentClassName, classRunTime));
            }

            raiseEvent(JUnitTestListener.Event.testRunFinsihed(description.getDisplayName(), time));
        }


        @Override
        public void testStarted(Description description) throws Exception
        {
            checkClassChange(description);

            setTestFailed(false);

            raiseEvent(JUnitTestListener.Event.testStarted(description.getDisplayName(),
                                                           JUnitUtils.findClassName(description)));

            testStartTime.set(System.currentTimeMillis());
        }


        @Override
        public void testFinished(Description description) throws Exception
        {
            // If there were no failures, errors or Assume errors then raise a success event
            Boolean isFailure = hasTestFailed();

            if (isFailure == null ||!isFailure)
            {
                long endTime = System.currentTimeMillis();
                long time    = endTime - testStartTime.get();

                raiseEvent(JUnitTestListener.Event.testSucceded(description.getDisplayName(),
                                                                JUnitUtils.findClassName(description),
                                                                time));
            }
        }


        @Override
        public void testIgnored(Description description) throws Exception
        {
            checkClassChange(description);

            raiseEvent(JUnitTestListener.Event.ignored(description.getDisplayName(),
                                                       JUnitUtils.findClassName(description),
                                                       JUnitUtils.getIgnoredMessage(description)));
        }


        @Override
        public void testAssumptionFailure(Failure failure)
        {
            long        endTime     = System.currentTimeMillis();
            long        time        = endTime - testStartTime.get();
            Description description = failure.getDescription();
            Throwable   throwable   = failure.getException();
            String      testHeader  = failure.getTestHeader();

            if (testHeader == null || "null".equals(testHeader))
            {
                testHeader = "Failed to construct test";
            }

            raiseEvent(JUnitTestListener.Event.assumptionFailure(testHeader,
                                                                 JUnitUtils.findClassName(description),
                                                                 time,
                                                                 throwable.getMessage(),
                                                                 throwable.getStackTrace()));
            setTestFailed(true);
        }


        @Override
        public void testFailure(Failure failure) throws Exception
        {
            long        endTime     = System.currentTimeMillis();
            long        time        = endTime - testStartTime.get();
            Description description = failure.getDescription();
            Throwable   cause       = failure.getException();

            // If the cause was an AssertionError then the test failed otherwise it was an error
            if (cause instanceof AssertionError)
            {
                raiseEvent(JUnitTestListener.Event.failure(JUnitUtils.getFailureMessage(failure),
                                                           JUnitUtils.findClassName(description),
                                                           time,
                                                           cause.getClass().getCanonicalName(),
                                                           cause.getMessage(),
                                                           cause.getStackTrace()));
            }
            else
            {
                raiseEvent(JUnitTestListener.Event.error(JUnitUtils.getFailureMessage(failure),
                                                         JUnitUtils.findClassName(description),
                                                         time,
                                                         cause.getClass().getCanonicalName(),
                                                         cause.getMessage(),
                                                         cause.getStackTrace()));
            }

            setTestFailed(true);
        }


        /**
         * Determine from the {@link Description} whether the {@link Class} being
         * tested has changed.
         * <p>
         * If the the previous test class is not null then a {@link JUnitTestListener.Event}
         * will be raised to signal the end of that class.
         * If the class has changed the a {@link JUnitTestListener.Event} will be raised
         * to signify the start of a test class.
         *
         * @param description  the JUnit {@link Description} to use to check for a
         *                     change of test {@link Class}.
         */
        protected void checkClassChange(Description description)
        {
            long   endTime   = System.currentTimeMillis();
            String testClass = JUnitUtils.findClassName(description);
            String current   = currentTestClass.get();

            if (current == null ||!current.equals(testClass))
            {
                if (current != null)
                {
                    long time = endTime - classStartTime.get();

                    raiseEvent(JUnitTestListener.Event.testClassFinished(current, time));
                }

                raiseEvent(JUnitTestListener.Event.testClassStarted(JUnitUtils.findClassName(description)));

                classStartTime.set(System.currentTimeMillis());

                currentTestClass.set(testClass);
            }
        }


        /**
         * Raise the specified {@link JUnitTestListener.Event} on
         * the {@link #STREAM_NAME} event stream.
         *
         * @param event  the {@link JUnitTestListener.Event} to raise
         */
        protected void raiseEvent(JUnitTestListener.Event event)
        {
            if (channel != null)
            {
                channel.raise(event, STREAM_NAME);
            }
        }
    }


    /**
     * A {@link RemoteCallable} to use to start a test run.
     */
    public static class StartTests implements RemoteCallable<Boolean>
    {
        /**
         * The {@link JUnitTestRunner} to use to run tests.
         */
        private transient JUnitTestRunner runner = JUnitTestRunner.INSTANCE;

        /**
         * The {@link Option}s to use to start the test run.
         */
        private Option[] options;


        /**
         * Create a {@link StartTests} runnable with the
         * specified options.
         *
         * @param optionsByType  the {@link OptionsByType}s to use
         */
        public StartTests(OptionsByType optionsByType)
        {
            List<Option>           list          = new ArrayList<>();
            Iterable<Serializable> serializables = optionsByType.getInstancesOf(Serializable.class);

            serializables.forEach((opt) -> list.add(opt instanceof Option ? (Option) opt : Decoration.of(opt)));

            this.options = list.toArray(new Option[list.size()]);
        }


        /**
         * Obtain the {@link Option}s that will be used to start
         * the test run.
         *
         * @return  the {@link Option}s that will be used to start
         *          the test run
         */
        Option[] getOptions()
        {
            return options;
        }


        @Override
        public Boolean call()
        {
            if (runner == null)
            {
                runner = JUnitTestRunner.INSTANCE;
            }

            runner.run(OptionsByType.of(options));

            return true;
        }


        /**
         * Set the {@link JUnitTestRunner} to use to run tests.
         *
         * @param runner  the {@link JUnitTestRunner} to use to
         *                run tests
         */
        public void setRunner(JUnitTestRunner runner)
        {
            this.runner = runner;
        }


        /**
         * We need to have custom Java serialization logic otherwise this
         * class fails to serialize properly if run inside the containerised
         * {@link JavaVirtualMachine} platform.
         *
         * @param out  the stream to serialize to
         *
         * @throws IOException  if an error occurs
         */
        private void writeObject(ObjectOutputStream out) throws IOException
        {
            out.writeInt(options.length);

            for (Option option : options)
            {
                out.writeObject(option);
            }
        }


        /**
         * We need to have custom Java serialization logic otherwise this
         * class fails to deserialize properly if run inside the containerised
         * {@link JavaVirtualMachine} platform.
         *
         * @param in  the stream to deserialize from
         *
         * @throws IOException             when the option can't be read
         * @throws ClassNotFoundException  when the option class can't be found
         */
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
        {
            int length = in.readInt();

            options = new Option[length];

            for (int i = 0; i < length; i++)
            {
                options[i] = (Option) in.readObject();
            }
        }
    }


    /**
     * A {@link RemoteCallable} to use to stop a test run.
     */
    public static class StopTests implements RemoteRunnable
    {
        /**
         * The {@link JUnitTestRunner} to use.
         */
        private transient JUnitTestRunner runner = JUnitTestRunner.INSTANCE;


        /**
         * Set the {@link JUnitTestRunner} to use to run tests.
         *
         * @param runner  the {@link JUnitTestRunner} to use to
         *                run tests
         */
        public void setRunner(JUnitTestRunner runner)
        {
            this.runner = runner;
        }


        @Override
        public void run()
        {
            if (runner == null)
            {
                runner = JUnitTestRunner.INSTANCE;
            }

            runner.stop();
        }
    }
}
