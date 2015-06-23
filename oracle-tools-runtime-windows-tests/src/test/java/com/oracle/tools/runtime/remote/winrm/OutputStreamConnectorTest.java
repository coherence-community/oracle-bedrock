/*
 * File: OutputStreamConnectorTest.java
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

package com.oracle.tools.runtime.remote.winrm;

import com.microsoft.wsman.shell.CommandStateType;
import com.microsoft.wsman.shell.ReceiveResponse;
import com.microsoft.wsman.shell.StreamType;

import com.oracle.tools.deferred.Eventually;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for {@link OutputStreamConnector}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class OutputStreamConnectorTest
{
    /**
     *
     */
    @Test
    public void shouldPollForOutputAndPipeToStreams() throws Exception
    {
        ByteArrayOutputStream stdOut     = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr     = new ByteArrayOutputStream();
        WindowsSession        session    = mock(WindowsSession.class);
        ReceiveResponse       response   = new ReceiveResponse();
        StreamType            outStream1 = new StreamType();
        StreamType            outStream2 = new StreamType();
        StreamType            errStream1 = new StreamType();
        StreamType            errStream2 = new StreamType();

        outStream1.setName("stdout");
        outStream1.setValue("StdOut Line 1".getBytes());
        outStream1.setEnd(false);
        outStream2.setName("stdout");
        outStream2.setValue("StdOut Line 2".getBytes());
        outStream2.setEnd(true);

        errStream1.setName("stderr");
        errStream1.setValue("StdErr Line 1".getBytes());
        errStream1.setEnd(false);
        errStream2.setName("stderr");
        errStream2.setValue("StdErr Line 2".getBytes());
        errStream2.setEnd(true);

        response.getStream().add(outStream1);
        response.getStream().add(errStream1);
        response.getStream().add(outStream2);
        response.getStream().add(errStream2);

        when(session.readOutputStreams()).thenReturn(response);

        OutputStreamConnector connector = new OutputStreamConnector(session, stdOut, stdErr);

        boolean               result    = connector.pollOutput();

        assertThat(result, is(true));
        assertThat(connector.getExitCode(), is(-1));

        assertThat(new String(stdOut.toByteArray()), is("StdOut Line 1StdOut Line 2"));
        assertThat(new String(stdErr.toByteArray()), is("StdErr Line 1StdErr Line 2"));
    }


    /**
     *
     */
    @Test
    public void shouldPollForOutputWhenEmptyOutputReturned() throws Exception
    {
        ByteArrayOutputStream stdOut     = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr     = new ByteArrayOutputStream();
        WindowsSession        session    = mock(WindowsSession.class);
        ReceiveResponse       response   = new ReceiveResponse();
        CommandStateType      state      = new CommandStateType();
        StreamType            outStream1 = new StreamType();
        StreamType            errStream1 = new StreamType();

        response.getStream().add(outStream1);
        response.getStream().add(errStream1);
        response.setCommandState(state);

        when(session.readOutputStreams()).thenReturn(response);

        OutputStreamConnector connector = new OutputStreamConnector(session, stdOut, stdErr);

        boolean               result    = connector.pollOutput();

        assertThat(result, is(true));
        assertThat(connector.getExitCode(), is(-1));

        assertThat(new String(stdOut.toByteArray()), is(""));
        assertThat(new String(stdErr.toByteArray()), is(""));
    }


    /**
     *
     */
    @Test
    public void shouldPollForOutputWhenNoOutputReturned() throws Exception
    {
        ByteArrayOutputStream stdOut   = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr   = new ByteArrayOutputStream();
        WindowsSession        session  = mock(WindowsSession.class);
        ReceiveResponse       response = new ReceiveResponse();

        when(session.readOutputStreams()).thenReturn(response);

        OutputStreamConnector connector = new OutputStreamConnector(session, stdOut, stdErr);

        boolean               result    = connector.pollOutput();

        assertThat(result, is(true));
        assertThat(connector.getExitCode(), is(-1));

        assertThat(new String(stdOut.toByteArray()), is(""));
        assertThat(new String(stdErr.toByteArray()), is(""));
    }


    /**
     *
     */
    @Test
    public void shouldHandleCommandStateWithNoExitCode() throws Exception
    {
        ByteArrayOutputStream stdOut   = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr   = new ByteArrayOutputStream();
        WindowsSession        session  = mock(WindowsSession.class);
        ReceiveResponse       response = new ReceiveResponse();
        CommandStateType      state    = new CommandStateType();

        response.setCommandState(state);

        when(session.readOutputStreams()).thenReturn(response);

        OutputStreamConnector connector = new OutputStreamConnector(session, stdOut, stdErr);

        boolean               result    = connector.pollOutput();

        assertThat(result, is(true));
        assertThat(connector.getExitCode(), is(-1));
    }


    /**
     *
     */
    @Test
    public void shouldHandleCommandStateWithExitCodeSet() throws Exception
    {
        ByteArrayOutputStream stdOut   = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr   = new ByteArrayOutputStream();
        WindowsSession        session  = mock(WindowsSession.class);
        ReceiveResponse       response = new ReceiveResponse();
        CommandStateType      state    = new CommandStateType();

        state.setExitCode(new BigInteger("2"));
        response.setCommandState(state);

        when(session.readOutputStreams()).thenReturn(response);

        OutputStreamConnector connector = new OutputStreamConnector(session, stdOut, stdErr);

        boolean               result    = connector.pollOutput();

        assertThat(result, is(false));
        assertThat(connector.getExitCode(), is(2));
    }


    /**
     *
     */
    @Test
    public void shouldReturnExitCodeFromWaitFor() throws Exception
    {
        ByteArrayOutputStream stdOut   = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr   = new ByteArrayOutputStream();
        WindowsSession        session  = mock(WindowsSession.class);
        ReceiveResponse       response = new ReceiveResponse();
        CommandStateType      state    = new CommandStateType();

        state.setExitCode(new BigInteger("2"));
        response.setCommandState(state);

        when(session.readOutputStreams()).thenReturn(response);

        OutputStreamConnector connector = new OutputStreamConnector(session, stdOut, stdErr);

        connector.pollOutput();

        assertThat(connector.waitFor(), is(2));
    }

    /**
     *
     */
    @Test
    public void shouldWaitForCompletion() throws Exception
    {
        ByteArrayOutputStream stdOut   = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr   = new ByteArrayOutputStream();
        WindowsSession        session  = mock(WindowsSession.class);
        ReceiveResponse       response = new ReceiveResponse();
        CommandStateType      state    = new CommandStateType();

        state.setExitCode(new BigInteger("99"));
        response.setCommandState(state);

        when(session.readOutputStreams()).thenReturn(response);

        final OutputStreamConnector connector = new OutputStreamConnector(session, stdOut, stdErr);
        final AtomicInteger         exitCode  = new AtomicInteger();

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                exitCode.set(connector.waitFor());
            }
        };

        t.start();
        connector.start();

        t.join();
        connector.join();

        assertThat(exitCode.get(), is(99));
    }

    /**
     *
     */
    @Test
    public void shouldCloseConnector() throws Exception
    {
        ByteArrayOutputStream stdOut    = new ByteArrayOutputStream();
        ByteArrayOutputStream stdErr    = new ByteArrayOutputStream();
        WindowsSession        session   = mock(WindowsSession.class);
        ReceiveResponse       response  = new ReceiveResponse();
        StreamType            outStream = new StreamType();

        outStream.setName("stdout");
        outStream.setValue("x".getBytes());
        outStream.setEnd(true);

        response.getStream().add(outStream);

        when(session.readOutputStreams()).thenReturn(response);

        OutputStreamConnector connector = new OutputStreamConnector(session, stdOut, stdErr);

        connector.start();

        // Give the connector time to get started
        Eventually.assertThat(invoking(stdOut).size(), is(greaterThan(5)));

        connector.close();

        connector.join();

        assertThat(connector.getExitCode(), is(0));
    }

}
