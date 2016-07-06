/*
 * File: JUnitTestRunnerTest.java
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
import com.oracle.bedrock.deferred.Eventually;
import com.oracle.bedrock.junit.options.TestClasses;
import com.oracle.bedrock.options.Decoration;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.mockito.ArgumentCaptor;

import static com.oracle.bedrock.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Functional tests for {@link JUnitTestRunner}
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JUnitTestRunnerTest
{
    @Test
    public void shouldStopIfInterruptedWhileWaiting() throws Exception
    {
        JUnitTestRunner runner = new JUnitTestRunner();
        Thread          thread = new Thread(runner);

        thread.setDaemon(true);

        assertThat(runner.getState(), is(JUnitTestRunner.State.NotRunning));

        thread.start();

        Eventually.assertThat(invoking(runner).getState(), is(JUnitTestRunner.State.Waiting));

        thread.interrupt();

        Eventually.assertThat(invoking(runner).getState(), is(JUnitTestRunner.State.Stopped));
    }


    @Test
    public void shouldRunTest() throws Exception
    {
        JUnitTestRunner runner   = new JUnitTestRunner();
        Thread          thread   = new Thread(runner);
        RunListener     listener = mock(RunListener.class);

        thread.start();

        OptionsByType optionsByType = OptionsByType.of(TestClasses.of(GoodTest.class), Decoration.of(listener));

        runner.run(optionsByType);

        thread.join();

        ArgumentCaptor<Description> captor = ArgumentCaptor.forClass(Description.class);

        verify(listener).testStarted(captor.capture());

        Description description = captor.getValue();

        assertThat(JUnitUtils.findClassName(description), is(GoodTest.class.getCanonicalName()));
    }
}
