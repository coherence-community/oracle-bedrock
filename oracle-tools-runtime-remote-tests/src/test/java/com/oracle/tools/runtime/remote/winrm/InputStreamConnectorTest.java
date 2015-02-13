/*
 * File: InputStreamConnectorTest.java
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

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Tests for {@link InputStreamConnector}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class InputStreamConnectorTest
{
    /**
     *
     */
    @Test
    public void shouldCloseStream() throws Exception
    {
        WindowsSession       session   = mock(WindowsSession.class);
        InputStream          stream    = mock(InputStream.class);

        InputStreamConnector connector = new InputStreamConnector(session, stream);

        connector.close();

        verify(stream).close();
    }


    /**
     *
     */
    @Test
    public void shouldSendInput() throws Exception
    {
        WindowsSession session = mock(WindowsSession.class);
        InputStream    stream  = mock(InputStream.class);
        BufferedReader reader  = mock(BufferedReader.class);

        when(reader.readLine()).thenReturn("Line 1");

        InputStreamConnector connector = new InputStreamConnector(session, stream);
        boolean              result    = connector.pollForInput(reader);

        assertThat(result, is(true));

        verify(session).writeToInputStream("Line 1");
    }


    /**
     *
     */
    @Test
    public void shouldNotSendInputWhenStreamEnds() throws Exception
    {
        WindowsSession session = mock(WindowsSession.class);
        InputStream    stream  = mock(InputStream.class);
        BufferedReader reader  = mock(BufferedReader.class);

        when(reader.readLine()).thenReturn(null);

        InputStreamConnector connector = new InputStreamConnector(session, stream);
        boolean              result    = connector.pollForInput(reader);

        assertThat(result, is(false));

        verify(session, never()).writeToInputStream(anyString());
    }


    /**
     *
     */
    @Test
    public void shouldRunInputStreamConnector() throws Exception
    {
        WindowsSession       session   = mock(WindowsSession.class);
        InputStream          stream    = new ByteArrayInputStream("Line 1\nLine 2".getBytes());

        InputStreamConnector connector = new InputStreamConnector(session, stream);

        connector.start();
        connector.join();

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(session, times(2)).writeToInputStream(captor.capture());

        assertThat(captor.getAllValues(), contains("Line 1", "Line 2"));
    }
}
