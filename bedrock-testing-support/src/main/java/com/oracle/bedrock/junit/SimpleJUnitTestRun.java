/*
 * File: SimpleJUnitTestRun.java
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

package com.oracle.bedrock.junit;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.ApplicationProcess;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.JavaApplicationProcess;
import com.oracle.bedrock.runtime.java.SimpleJavaApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of a {@link JavaApplication} that runs
 * one or more JUnit tests.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleJUnitTestRun extends SimpleJavaApplication implements JUnitTestRun
{
    /**
     * The {@link List} of {@link JUnitTestListener}s that will receive
     * {@link JUnitTestListener.Event}s during the test run.
     */
    private final List<JUnitTestListener> runListeners;


    /**
     * Create a {@link SimpleJUnitTestRun} application.
     *
     * @param platform       the {@link Platform} on which the {@link Application} was launched
     * @param process        the underlying {@link ApplicationProcess} representing the {@link Application}
     * @param optionsByType  the {@link OptionsByType} used to launch the {@link Application}
     */
    public SimpleJUnitTestRun(Platform               platform,
                              JavaApplicationProcess process,
                              OptionsByType          optionsByType)
    {
        super(platform, process, optionsByType);

        runListeners = new ArrayList<>();

        // Add any listeners from the options
        for (JUnitTestListener listener : optionsByType.getInstancesOf(JUnitTestListener.class))
        {
            runListeners.add(listener);
        }

        addListener(new EventListener(), JUnitTestRunner.STREAM_NAME);
    }


    /**
     * Obtain an immutable {@link List} of the {@link JUnitTestListener}s
     * that have been added to this application.
     *
     * @return  an immutable {@link List} of the {@link JUnitTestListener}s
     *          that have been added to this application
     */
    public List<JUnitTestListener> getRunListeners()
    {
        return Collections.unmodifiableList(runListeners);
    }


    @Override
    public void startTests(OptionsByType optionsByType)
    {
        submit(new JUnitTestRunner.StartTests(optionsByType));
    }


    protected void fireJUnitStarted(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.junitStarted(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireJUnitCompleted(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.junitCompleted(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestRunStarted(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testRunStarted(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestRunFinished(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testRunFinished(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestClassStarted(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testClassStarted(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestClassFinished(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testClassFinished(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestStarted(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testStarted(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestSucceeded(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testSucceeded(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestIgnored(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testIgnored(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestFailure(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testFailed(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestError(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testError(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    protected void fireTestAssumptionFailure(JUnitTestListener.Event event)
    {
        for (JUnitTestListener listener : runListeners)
        {
            try
            {
                listener.testAssumptionFailure(event);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    private class EventListener implements RemoteEventListener
    {
        @Override
        public void onEvent(RemoteEvent event)
        {
            if (runListeners.isEmpty())
            {
                return;
            }

            JUnitTestListener.Event jUnitEvent = (JUnitTestListener.Event) event;

            switch (jUnitEvent.getType())
            {
            case JUnitStarted :
                fireJUnitStarted(jUnitEvent);
                break;

            case JUnitCompleted :
                fireJUnitCompleted(jUnitEvent);
                break;

            case testRunStarted :
                fireTestRunStarted(jUnitEvent);
                break;

            case testRunFinished :
                fireTestRunFinished(jUnitEvent);
                break;

            case testClassStarted :
                fireTestClassStarted(jUnitEvent);
                break;

            case testClassFinished :
                fireTestClassFinished(jUnitEvent);
                break;

            case testStarted :
                fireTestStarted(jUnitEvent);
                break;

            case testSuccess :
                fireTestSucceeded(jUnitEvent);
                break;

            case testIgnored :
                fireTestIgnored(jUnitEvent);
                break;

            case testAssumptionFailure :
                fireTestAssumptionFailure(jUnitEvent);
                break;

            case testFailure :
                fireTestFailure(jUnitEvent);
                break;

            case testError :
                fireTestError(jUnitEvent);
                break;
            }
        }
    }
}
