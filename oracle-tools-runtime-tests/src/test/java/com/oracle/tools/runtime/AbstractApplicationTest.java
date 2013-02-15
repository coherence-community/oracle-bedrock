/*
 * File: AbstractApplicationTest.java
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

package com.oracle.tools.runtime;

import com.oracle.tools.junit.AbstractTest;
import com.oracle.tools.runtime.java.process.VirtualProcess;
import com.oracle.tools.util.Pair;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Unit and Functional tests for {@link AbstractApplication}s.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class AbstractApplicationTest extends AbstractTest
{
    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetProcess() throws Exception
    {
        String              applicationName = "Test-App";
        Properties          properties      = new Properties();
        Process             process         = mock(Process.class);
        ApplicationConsole  console         = mock(ApplicationConsole.class);

        AbstractApplication application     = new AbstractApplicationStub(process,
                                                                          applicationName,
                                                                          console,
                                                                          properties);

        assertThat(application.getProcess(), is(sameInstance(process)));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldSetName() throws Exception
    {
        String              applicationName = "Test-App";
        Properties          properties      = new Properties();
        Process             process         = mock(Process.class);
        ApplicationConsole  console         = mock(ApplicationConsole.class);

        AbstractApplication application     = new AbstractApplicationStub(process,
                                                                          applicationName,
                                                                          console,
                                                                          properties);

        assertThat(application.getName(), is(applicationName));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldDestroyProcess() throws Exception
    {
        String             applicationName = "Test-App";
        Properties         properties      = new Properties();
        Process            process         = mock(Process.class);
        ApplicationConsole console         = mock(ApplicationConsole.class);

        InputStream        inputStream     = mock(InputStream.class);
        InputStream        errorStream     = mock(InputStream.class);
        OutputStream       outputStream    = mock(OutputStream.class);

        when(process.getInputStream()).thenReturn(inputStream);
        when(process.getErrorStream()).thenReturn(errorStream);
        when(process.getOutputStream()).thenReturn(outputStream);

        AbstractApplication application = new AbstractApplicationStub(process, applicationName, console, properties);

        application.destroy();

        verify(process).destroy();
        verify(inputStream).close();
        verify(errorStream).close();
        verify(outputStream).close();
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldReturnExitValue() throws Exception
    {
        String             applicationName = "Test-App";
        Properties         properties      = new Properties();
        Process            process         = mock(Process.class);
        ApplicationConsole console         = mock(ApplicationConsole.class);

        when(process.exitValue()).thenReturn(19);

        AbstractApplication application = new AbstractApplicationStub(process, applicationName, console, properties);

        assertThat(application.exitValue(), is(19));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldWaitForProcess() throws Exception
    {
        String              applicationName = "Test-App";
        Properties          properties      = new Properties();
        Process             process         = mock(Process.class);
        ApplicationConsole  console         = mock(ApplicationConsole.class);

        AbstractApplication application     = new AbstractApplicationStub(process,
                                                                          applicationName,
                                                                          console,
                                                                          properties);

        application.waitFor();

        verify(process).waitFor();
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test(expected = InterruptedException.class)
    public void shouldThrowInterruptedExceptionWhenCallingWaitFor() throws Exception
    {
        String             applicationName = "Test-App";
        Properties         properties      = new Properties();
        Process            process         = mock(Process.class);
        ApplicationConsole console         = mock(ApplicationConsole.class);

        when(process.waitFor()).thenThrow(new InterruptedException());

        AbstractApplication application = new AbstractApplicationStub(process, applicationName, console, properties);

        application.waitFor();
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldGetPidForInternalProcess() throws Exception
    {
        String              applicationName = "Test-App";
        Properties          properties      = new Properties();
        VirtualProcess      process         = mock(VirtualProcess.class);
        ApplicationConsole  console         = mock(ApplicationConsole.class);

        AbstractApplication application     = new AbstractApplicationStub(process,
                                                                          applicationName,
                                                                          console,
                                                                          properties);

        assertThat(application.getPid(), is(-1L));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldGetEnvironmentVariables() throws Exception
    {
        String             applicationName = "Test-App";
        Properties         properties      = new Properties();
        VirtualProcess     process         = mock(VirtualProcess.class);
        ApplicationConsole console         = mock(ApplicationConsole.class);

        properties.setProperty("key-1", "value-1");
        properties.setProperty("key-2", "value-3");

        AbstractApplication application = new AbstractApplicationStub(process, applicationName, console, properties);

        assertThat(application.getEnvironmentVariables(), is(properties));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCaptureStandardOut() throws Exception
    {
        String      applicationName = "Test-App";
        Properties  properties      = new Properties();
        Process     process         = mock(Process.class);

        InputStream inputStream     = new ByteArrayInputStream("Out Test...".getBytes());
        InputStream errorStream     = mock(InputStream.class);

        when(process.getInputStream()).thenReturn(inputStream);
        when(process.getErrorStream()).thenReturn(errorStream);

        ApplicationConsoleStub console = new ApplicationConsoleStub();

        new AbstractApplicationStub(process, applicationName, console, properties);

        console.waitForLineCount(3, 10000);

        Object[] args = console.lines.get(0).getY();

        assertThat(args[0], is((Object) applicationName));
        assertThat(args[1], is((Object) "out"));
        assertThat(args[3], is((Object) 1L));
        assertThat(args[4], is((Object) "Out Test..."));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldTerminateStandardOut() throws Exception
    {
        String      applicationName = "Test-App";
        Properties  properties      = new Properties();
        Process     process         = mock(Process.class);

        InputStream inputStream     = mock(InputStream.class);
        InputStream errorStream     = mock(InputStream.class);

        when(process.getInputStream()).thenReturn(inputStream);
        when(process.getErrorStream()).thenReturn(errorStream);

        ApplicationConsoleStub console = new ApplicationConsoleStub();

        new AbstractApplicationStub(process, applicationName, console, properties);

        console.waitForLineCount(2, 10000);

        String format = console.lines.get(0).getX();

        assertThat(format, is(containsString("(terminated)")));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCaptureStandardErr() throws Exception
    {
        String      applicationName = "Test-App";
        Properties  properties      = new Properties();
        Process     process         = mock(Process.class);

        InputStream errorStream     = new ByteArrayInputStream("Err Test...".getBytes());
        InputStream inputStream     = mock(InputStream.class);

        when(process.getInputStream()).thenReturn(inputStream);
        when(process.getErrorStream()).thenReturn(errorStream);

        ApplicationConsoleStub console = new ApplicationConsoleStub();

        new AbstractApplicationStub(process, applicationName, console, properties);

        console.waitForLineCount(3, 10000);

        Object[] args = console.lines.get(1).getY();

        assertThat(args[0], is((Object) applicationName));
        assertThat(args[1], is((Object) "err"));
        assertThat(args[3], is(not((Object) 0L)));
    }


    private class AbstractApplicationStub extends AbstractApplication
    {
        private AbstractApplicationStub(Process            process,
                                        String             name,
                                        ApplicationConsole console,
                                        Properties         environmentVariables)
        {
            super(process, name, console, environmentVariables);
        }
    }


    private class ApplicationConsoleStub implements ApplicationConsole
    {
        private List<Pair<String, Object[]>> lines  = new ArrayList<Pair<String, Object[]>>();
        private final Object                 WAITER = new Object();
        private volatile int                 expectedCount;


        /**
         * Method description
         *
         * @param format
         * @param args
         */
        @Override
        public void printf(String    format,
                           Object... args)
        {
            synchronized (WAITER)
            {
                lines.add(new Pair<String, Object[]>(format, args));

                if (lines.size() >= expectedCount)
                {
                    WAITER.notifyAll();
                }
            }
        }


        /**
         * Method description
         *
         * @param count
         * @param time
         *
         * @throws InterruptedException
         */
        public void waitForLineCount(int  count,
                                     long time) throws InterruptedException
        {
            synchronized (WAITER)
            {
                this.expectedCount = count;

                if (lines.size() < expectedCount)
                {
                    WAITER.wait(time);
                }
            }

            if (lines.size() < expectedCount)
            {
                fail("Expected number of lines not printed expected=" + expectedCount + " actual=" + lines.size());
            }
        }
    }
}
