/*
 * File: WindowsSessionTest.java
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

import com.microsoft.wsman.shell.*;

import org.dmtf.wsman.AttributableDuration;
import org.dmtf.wsman.AttributableURI;
import org.dmtf.wsman.MaxEnvelopeSizeType;
import org.dmtf.wsman.OptionSet;
import org.dmtf.wsman.OptionType;
import org.dmtf.wsman.SelectorSetType;
import org.dmtf.wsman.SelectorType;

import org.junit.Test;

import org.mockito.ArgumentCaptor;

import org.w3c.soap.envelope.Envelope;
import org.w3c.soap.envelope.Header;

import org.xmlsoap.ws.addressing.AttributedURI;
import org.xmlsoap.ws.addressing.EndpointReferenceType;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import java.math.BigInteger;

import java.net.URL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBElement;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

/**
 * Tests for {@link WindowsSession}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsSessionTest
{
    /**
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateEnvelopeWitNullAction() throws Exception
    {
        SoapConnection connection = mock(SoapConnection.class);
        WindowsSession session    = new WindowsSession(connection);

        session.createEnvelope(null);
    }


    /**
     *
     */
    @Test(expected = IllegalArgumentException.class)
    public void shouldNotCreateEnvelopeWitBlankAction() throws Exception
    {
        SoapConnection connection = mock(SoapConnection.class);
        WindowsSession session    = new WindowsSession(connection);

        session.createEnvelope("");
    }


    /**
     *
     */
    @Test
    public void shouldCreateEnvelope() throws Exception
    {
        URL            url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection = mock(SoapConnection.class);

        when(connection.getUrl()).thenReturn(url);

        WindowsSession session  = new WindowsSession(connection);
        Envelope       envelope = session.createEnvelope("fooAction");

        assertThat(envelope.getBody(), is(notNullValue()));

        Header header = envelope.getHeader();

        assertThat(header, is(notNullValue()));

        List<Object> headerElements = header.getAny();

        assertThat(headerElements, is(notNullValue()));

        AttributedURI actionURI = findElement(headerElements, "Action");

        assertThat(actionURI, is(notNullValue()));
        assertThat(actionURI.getValue(), is("fooAction"));

        AttributedURI toURI = findElement(headerElements, "To");

        assertThat(toURI, is(notNullValue()));
        assertThat(toURI.getValue(), is(url.toExternalForm()));

        EndpointReferenceType endpoint = findElement(headerElements, "ReplyTo");

        assertThat(endpoint, is(notNullValue()));

        AttributedURI replyToURI = endpoint.getAddress();

        assertThat(replyToURI, is(notNullValue()));
        assertThat(replyToURI.getValue(), is(WindowsSession.URI_REPLY_TO));

        AttributedURI messageId = findElement(headerElements, "MessageID");

        assertThat(messageId, is(notNullValue()));
        assertThat(messageId.getValue(), is(notNullValue()));

        AttributableURI resourceURI = findElement(headerElements, "ResourceURI");

        assertThat(resourceURI, is(notNullValue()));
        assertThat(resourceURI.getValue(), is(WindowsSession.URI_WINRM_RESOURCE));
    }


    /**
     *
     */
    @Test
    public void shouldCreateEnvelopeWithDefaultSoapOptions() throws Exception
    {
        URL            url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection = mock(SoapConnection.class);

        when(connection.getUrl()).thenReturn(url);

        WindowsSession session  = new WindowsSession(connection);
        Envelope       envelope = session.createEnvelope("foo");
        Header         header   = envelope.getHeader();

        assertThat(header, is(notNullValue()));

        List<Object> headerElements = header.getAny();

        assertThat(headerElements, is(notNullValue()));

        MaxEnvelopeSizeType size = findElement(headerElements, "MaxEnvelopeSize");

        assertThat(size, is(notNullValue()));
        assertThat(size.getValue(), is(WindowsSoapOptions.DEFAULT_MAX_ENVELOPE_SIZE));

        AttributableDuration timeout = findElement(headerElements, "OperationTimeout");

        assertThat(timeout, is(notNullValue()));
        assertThat(timeout.getValue(), is(WindowsSoapOptions.DEFAULT_TIMEOUT));
    }


    /**
     *
     */
    @Test
    public void shouldCreateEnvelopeWithSpecifiedSoapOptions() throws Exception
    {
        URL            url             = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection      = mock(SoapConnection.class);
        Duration       expectedTimeout = ObjectFactories.DATATYPE.newDuration(true, 0, 0, 2, 3, 4, 5);
        long           timeoutMillis   = expectedTimeout.getTimeInMillis(new Date(0));

        when(connection.getUrl()).thenReturn(url);

        WindowsSession session = new WindowsSession(connection,
                                                    WindowsSoapOptions.basic()
                                                        .withMaxEnvelopeSize(new BigInteger("100"))
                                                        .withTimeout(timeoutMillis, TimeUnit.MILLISECONDS));

        Envelope envelope = session.createEnvelope("foo");
        Header   header   = envelope.getHeader();

        assertThat(header, is(notNullValue()));

        List<Object> headerElements = header.getAny();

        assertThat(headerElements, is(notNullValue()));

        MaxEnvelopeSizeType size = findElement(headerElements, "MaxEnvelopeSize");

        assertThat(size, is(notNullValue()));
        assertThat(size.getValue(), is(new BigInteger("100")));

        AttributableDuration timeout = findElement(headerElements, "OperationTimeout");

        assertThat(timeout, is(notNullValue()));
        assertThat(timeout.getValue().compare(expectedTimeout), is(0));
    }


    /**
     *
     */
    @Test
    public void shouldCreateEnvelopeWithShellIdAfterConnection() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);
        String         shellId      = "9876";

        response.setShellId(shellId);

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session               = new WindowsSession(connection);

        Envelope       envelopeBeforeConnect = session.createEnvelope("foo");

        session.connect();

        Envelope envelopeAfterConnect = session.createEnvelope("foo");

        Header   headerBeforeConnect  = envelopeBeforeConnect.getHeader();
        Header   headerAfterConnect   = envelopeAfterConnect.getHeader();

        assertThat(headerBeforeConnect, is(notNullValue()));
        assertThat(headerAfterConnect, is(notNullValue()));

        List<Object> headerElementsBeforeConnect = headerBeforeConnect.getAny();
        List<Object> headerElementsAfterConnect  = headerAfterConnect.getAny();

        assertThat(headerElementsBeforeConnect, is(notNullValue()));
        assertThat(headerElementsAfterConnect, is(notNullValue()));

        SelectorSetType selectorBeforeConnect = findElement(headerElementsBeforeConnect, "SelectorSet");
        SelectorSetType selectorAfterConnect  = findElement(headerElementsAfterConnect, "SelectorSet");

        assertThat(selectorBeforeConnect, is(nullValue()));
        assertThat(selectorAfterConnect, is(notNullValue()));

        List<SelectorType> selectorTypes = selectorAfterConnect.getSelector();

        assertThat(selectorTypes, is(notNullValue()));
        assertThat(selectorTypes.size(), is(1));

        SelectorType selectorType = selectorTypes.get(0);

        assertThat(selectorType.getName(), is("ShellId"));

        List<Serializable> content = selectorType.getContent();

        assertThat(content, is(notNullValue()));
        assertThat(content.size(), is(1));
        assertThat(content.get(0), is((Serializable) shellId));
    }


    /**
     *
     */
    @Test
    public void shouldConnectUsingDefaultWorkingDirectory() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session = new WindowsSession(connection);

        session.connect();

        assertThat(session.getShellReferenceId(), is(response.getShellId()));

        ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);

        verify(connection).send(captor.capture());

        Envelope envelope = captor.getValue();

        assertEnvelope(envelope, WindowsSession.ACTION_CREATE, null);

        ShellType shellType = (ShellType) ((JAXBElement) envelope.getBody().getAny().get(0)).getValue();

        assertThat(shellType.getWorkingDirectory(), is("%USERPROFILE%"));
    }


    /**
     *
     */
    @Test
    public void shouldConnectWithCorrectShellLifetime() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session = new WindowsSession(connection);

        session.connect();

        assertThat(session.getShellReferenceId(), is(response.getShellId()));

        ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);

        verify(connection).send(captor.capture());

        Envelope  envelope      = captor.getValue();

        ShellType shellType     = (ShellType) ((JAXBElement) envelope.getBody().getAny().get(0)).getValue();

        Duration  thirtyMinutes = DatatypeFactory.newInstance().newDuration(true, 0, 0, 0, 0, 30, 0);

        assertThat(shellType.getLifetime(), is(thirtyMinutes));
    }


    /**
     *
     */
    @Test
    public void shouldConnectAndRequestAllStreams() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session = new WindowsSession(connection);

        session.connect();

        assertThat(session.getShellReferenceId(), is(response.getShellId()));

        ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);

        verify(connection).send(captor.capture());

        Envelope  envelope  = captor.getValue();

        ShellType shellType = (ShellType) ((JAXBElement) envelope.getBody().getAny().get(0)).getValue();

        assertThat(shellType.getInputStreams(), containsInAnyOrder("stdin"));
        assertThat(shellType.getOutputStreams(), containsInAnyOrder("stdout", "stderr"));
    }


    /**
     *
     */
    @Test
    public void shouldConnectAndApplyBasicOptionsByDefault() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session = new WindowsSession(connection);

        session.connect();

        assertThat(session.getShellReferenceId(), is(response.getShellId()));

        ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);

        verify(connection).send(captor.capture());

        Envelope     envelope       = captor.getValue();
        Header       header         = envelope.getHeader();
        List<Object> headerElements = header.getAny();
        OptionSet    optionSet      = null;

        for (Object o : headerElements)
        {
            if (o instanceof OptionSet)
            {
                optionSet = (OptionSet) o;
                break;
            }
        }

        assertThat(optionSet, is(notNullValue()));

        SortedMap        expectedOptions = new TreeMap<>(WindowsShellOptions.basic().getBuilder().realize());
        SortedMap        actualOptions   = new TreeMap<>();
        List<OptionType> optionTypes     = optionSet.getOption();

        assertThat(optionTypes, is(notNullValue()));
        assertThat(optionTypes.size(), is(expectedOptions.size()));

        for (OptionType type : optionTypes)
        {
            actualOptions.put(type.getName(), type.getValue());
        }

        assertThat(actualOptions, is(expectedOptions));

        ShellType shellType = (ShellType) ((JAXBElement) envelope.getBody().getAny().get(0)).getValue();

        assertThat(shellType.getLifetime().compare(WindowsShellOptions.DEFAULT_SHELL_LIFETIME), is(0));
    }


    /**
     *
     */
    @Test
    public void shouldConnectAndApplySpecifiedOptionsByDefault() throws Exception
    {
        URL            url            = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection     = mock(SoapConnection.class);
        ShellType      response       = new ShellType();
        List<Object>   responseList   = Arrays.asList((Object) response);
        long           lifetimeMillis = 12345L;
        Duration       lifetime       = ObjectFactories.DATATYPE.newDuration(lifetimeMillis);
        WindowsShellOptions shellOptions = WindowsShellOptions.basic().set("Opt-1",
                                                                           "Value-1").set("Opt-2",
                                                                                          "Value-2")
                                                                                              .withLifetime(lifetimeMillis,
                                                                                                            TimeUnit
                                                                                                                .MILLISECONDS);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session = new WindowsSession(connection, shellOptions);

        session.connect();

        assertThat(session.getShellReferenceId(), is(response.getShellId()));

        ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);

        verify(connection).send(captor.capture());

        Envelope     envelope       = captor.getValue();
        Header       header         = envelope.getHeader();
        List<Object> headerElements = header.getAny();
        OptionSet    optionSet      = null;

        for (Object o : headerElements)
        {
            if (o instanceof OptionSet)
            {
                optionSet = (OptionSet) o;
                break;
            }
        }

        assertThat(optionSet, is(notNullValue()));

        SortedMap        expectedOptions = new TreeMap<>(shellOptions.getBuilder().realize());
        SortedMap        actualOptions   = new TreeMap<>();
        List<OptionType> optionTypes     = optionSet.getOption();

        assertThat(optionTypes, is(notNullValue()));
        assertThat(optionTypes.size(), is(expectedOptions.size()));

        for (OptionType type : optionTypes)
        {
            actualOptions.put(type.getName(), type.getValue());
        }

        assertThat(actualOptions, is(expectedOptions));

        ShellType shellType = (ShellType) ((JAXBElement) envelope.getBody().getAny().get(0)).getValue();

        assertThat(shellType.getLifetime().compare(lifetime), is(0));
    }


    /**
     *
     */
    @Test(expected = IllegalStateException.class)
    public void shouldNotConnectTwice() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session = new WindowsSession(connection);

        session.connect();
        session.connect();
    }


    /**
     *
     */
    @Test
    public void shouldConnectUsingSpecifiedWorkingDirectory() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session = new WindowsSession(connection);

        session.connect("C:\\test-dir");

        assertThat(session.getShellReferenceId(), is(response.getShellId()));

        ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);

        verify(connection).send(captor.capture());

        Envelope  envelope  = captor.getValue();

        ShellType shellType = (ShellType) ((JAXBElement) envelope.getBody().getAny().get(0)).getValue();

        assertThat(shellType.getWorkingDirectory(), is("C:\\test-dir"));
    }


    /**
     *
     */
    @Test
    public void shouldConnectUsingNoEnvironmentVariables() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session = new WindowsSession(connection);

        session.connect();

        assertThat(session.getShellReferenceId(), is(response.getShellId()));

        ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);

        verify(connection).send(captor.capture());

        Envelope  envelope  = captor.getValue();

        ShellType shellType = (ShellType) ((JAXBElement) envelope.getBody().getAny().get(0)).getValue();

        assertThat(shellType.getEnvironment(), is(nullValue()));
    }


    /**
     *
     */
    @Test
    public void shouldConnectUsingEnvironmentVariables() throws Exception
    {
        URL            url          = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection   = mock(SoapConnection.class);
        ShellType      response     = new ShellType();
        List<Object>   responseList = Arrays.asList((Object) response);

        response.setShellId("1234");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(responseList);

        WindowsSession session   = new WindowsSession(connection);

        Properties     variables = new Properties();

        variables.setProperty("VAR-1", "VALUE-1");
        variables.setProperty("VAR-2", "VALUE-2");

        session.connect(null, variables);

        assertThat(session.getShellReferenceId(), is(response.getShellId()));

        ArgumentCaptor<Envelope> captor = ArgumentCaptor.forClass(Envelope.class);

        verify(connection).send(captor.capture());

        Envelope                  envelope  = captor.getValue();
        ShellType                 shellType = (ShellType) ((JAXBElement) envelope.getBody().getAny().get(0)).getValue();
        List<EnvironmentVariable> results   = new ArrayList<>(shellType.getEnvironment().getVariable());

        Collections.sort(results, new EnvironmentVariableComparator());

        EnvironmentVariable v1 = results.get(0);
        EnvironmentVariable v2 = results.get(1);

        assertThat(v1.getName(), is("VAR-1"));
        assertThat(v1.getValue(), is("VALUE-1"));
        assertThat(v2.getName(), is("VAR-2"));
        assertThat(v2.getValue(), is("VALUE-2"));
    }


    /**
     *
     */
    @Test
    public void shouldTerminateCommandAndDeleteShellOnClose() throws Exception
    {
        URL                      url                   = new URL("http", "localhost", 80, "/foo");
        SoapConnection           connection            = mock(SoapConnection.class);
        ArgumentCaptor<Envelope> captor                = ArgumentCaptor.forClass(Envelope.class);
        OutputStreamConnector    outputStreamConnector = mock(OutputStreamConnector.class);
        InputStreamConnector     inputStreamConnector  = mock(InputStreamConnector.class);
        WindowsSession           session               = new WindowsSession(connection);

        when(connection.getUrl()).thenReturn(url);

        session.setShellReferenceId("Shell-9876");
        session.setCommandId("Command-1234");
        session.setOutputStreamConnector(outputStreamConnector);
        session.setInputStreamConnector(inputStreamConnector);
        session.close();

        verify(connection, times(2)).send(captor.capture());
        verify(outputStreamConnector).close();
        verify(inputStreamConnector).close();

        List<Envelope> envelopes         = captor.getAllValues();

        Envelope       terminateEnvelope = envelopes.get(0);
        Envelope       deleteEnvelope    = envelopes.get(1);

        assertEnvelope(terminateEnvelope, WindowsSession.ACTION_SIGNAL, "Shell-9876");
        assertEnvelope(deleteEnvelope, WindowsSession.ACTION_DELETE, "Shell-9876");

        Signal signal = findElement(terminateEnvelope.getBody().getAny(), "Signal");

        assertThat(signal, is(notNullValue()));

        assertThat(signal.getCommandId(), is("Command-1234"));
        assertThat(signal.getCode(), is(WindowsSession.SIGNAL_TERM));
    }


    /**
     *
     */
    @Test
    public void shouldNotTerminateCommandIfNotRunningButShouldDeleteShellOnClose() throws Exception
    {
        URL                      url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection           connection = mock(SoapConnection.class);
        ArgumentCaptor<Envelope> captor     = ArgumentCaptor.forClass(Envelope.class);
        WindowsSession           session    = new WindowsSession(connection);

        when(connection.getUrl()).thenReturn(url);

        session.setShellReferenceId("Shell-9876");
        session.setCommandId(null);
        session.close();

        verify(connection, times(1)).send(captor.capture());

        List<Envelope> envelopes      = captor.getAllValues();

        Envelope       deleteEnvelope = envelopes.get(0);

        assertEnvelope(deleteEnvelope, WindowsSession.ACTION_DELETE, "Shell-9876");
    }


    /**
     *
     */
    @Test
    public void shouldNotTeminateShellIfNotConnected() throws Exception
    {
        URL            url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection = mock(SoapConnection.class);
        WindowsSession session    = new WindowsSession(connection);

        when(connection.getUrl()).thenReturn(url);

        session.setShellReferenceId(null);
        session.setCommandId(null);
        session.close();

        verifyNoMoreInteractions(connection);
    }


    /**
     *
     */
    @Test
    public void shouldPipeTextToStdIn() throws Exception
    {
        URL                      url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection           connection = mock(SoapConnection.class);
        ArgumentCaptor<Envelope> captor     = ArgumentCaptor.forClass(Envelope.class);
        WindowsSession           session    = new WindowsSession(connection);

        when(connection.getUrl()).thenReturn(url);

        session.setShellReferenceId("Shell-1");
        session.setCommandId("Command-1");

        session.writeToInputStream("Some text...");

        verify(connection).send(captor.capture());

        Envelope envelope = captor.getValue();

        assertEnvelope(envelope, WindowsSession.ACTION_SEND, "Shell-1");

        Send send = findElement(envelope.getBody().getAny(), "Send");

        assertThat(send, is(notNullValue()));

        List<StreamType> list = send.getStream();

        assertThat(list, is(notNullValue()));
        assertThat(list.size(), is(1));

        StreamType streamType = list.get(0);

        assertThat(streamType, is(notNullValue()));
        assertThat(streamType.getName(), is("stdin"));
        assertThat(streamType.getCommandId(), is("Command-1"));
        assertThat(streamType.getValue(), is("Some text...".getBytes()));
        assertThat(streamType.isSetEnd(), is(false));
    }


    /**
     *
     */
    @Test(expected = IllegalStateException.class)
    public void shouldNotPipeTextToStdInIfNotRunningCommand() throws Exception
    {
        SoapConnection connection = mock(SoapConnection.class);
        WindowsSession session    = new WindowsSession(connection);

        session.setShellReferenceId("Shell-1");
        session.setCommandId(null);

        session.writeToInputStream("Some text...");
    }


    /**
     *
     */
    @Test
    public void shouldReadOutputStreams() throws Exception
    {
        URL                      url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection           connection = mock(SoapConnection.class);
        ReceiveResponse          response   = new ReceiveResponse();
        ArgumentCaptor<Envelope> captor     = ArgumentCaptor.forClass(Envelope.class);

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(Arrays.asList((Object) response));

        WindowsSession session = new WindowsSession(connection);

        session.setShellReferenceId("Shell-1");
        session.setCommandId("Command-1234");

        ReceiveResponse result = session.readOutputStreams();

        assertThat(result, is(sameInstance(response)));

        verify(connection).send(captor.capture());

        Envelope envelope = captor.getValue();

        assertEnvelope(envelope, WindowsSession.ACTION_RECEIVE, "Shell-1");

        Receive receive = findElement(envelope.getBody().getAny(), "Receive");

        assertThat(receive, is(notNullValue()));

        DesiredStreamType desiredStream = receive.getDesiredStream();

        assertThat(desiredStream, is(notNullValue()));
        assertThat(desiredStream.getCommandId(), is("Command-1234"));
        assertThat(desiredStream.getValue(), containsInAnyOrder("stdout", "stderr"));
    }


    /**
     *
     */
    @Test
    public void shouldReturnExitValueOfCommand() throws Exception
    {
        URL                   url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection        connection = mock(SoapConnection.class);
        OutputStreamConnector connector  = mock(OutputStreamConnector.class);

        when(connection.getUrl()).thenReturn(url);
        when(connector.getExitCode()).thenReturn(99);

        WindowsSession session = new WindowsSession(connection);

        session.setOutputStreamConnector(connector);

        assertThat(session.exitValue(), is(99));
    }


    /**
     *
     */
    @Test
    public void shouldReturnMinusOneExitValueNoCommand() throws Exception
    {
        URL            url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection connection = mock(SoapConnection.class);

        when(connection.getUrl()).thenReturn(url);

        WindowsSession session = new WindowsSession(connection);

        session.setOutputStreamConnector(null);

        assertThat(session.exitValue(), is(-1));
    }


    /**
     *
     */
    @Test
    public void shouldWaitForCommandCompletion() throws Exception
    {
        URL                   url        = new URL("http", "localhost", 80, "/foo");
        SoapConnection        connection = mock(SoapConnection.class);
        OutputStreamConnector connector  = mock(OutputStreamConnector.class);

        when(connection.getUrl()).thenReturn(url);
        when(connector.waitFor()).thenReturn(99);
        when(connector.getExitCode()).thenReturn(99);

        WindowsSession session = new WindowsSession(connection);

        session.setOutputStreamConnector(connector);

        assertThat(session.waitFor(), is(99));
        assertThat(session.exitValue(), is(99));

        verify(connector).waitFor();
    }


    /**
     *
     */
    @Test
    public void shouldExecuteCommandWithArguments() throws Exception
    {
        URL                         url                   = new URL("http", "localhost", 80, "/foo");
        SoapConnection              connection            = mock(SoapConnection.class);
        final InputStreamConnector  inputStreamConnector  = mock(InputStreamConnector.class);
        final OutputStreamConnector outputStreamConnector = mock(OutputStreamConnector.class);
        InputStream                 stdin                 = mock(InputStream.class);
        OutputStream                stdout                = mock(OutputStream.class);
        OutputStream                stderr                = mock(OutputStream.class);
        CommandResponse             response              = new CommandResponse();
        ArgumentCaptor<Envelope>    captor                = ArgumentCaptor.forClass(Envelope.class);

        response.setCommandId("Command-9876");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(Arrays.asList((Object) response));

        WindowsSession session = new WindowsSession(connection)
        {
            @Override
            protected InputStreamConnector createInputStreamConnector(InputStream stdIn)
            {
                return inputStreamConnector;
            }

            @Override
            protected OutputStreamConnector createOutputStreamConnector(OutputStream stdOut,
                                                                        OutputStream stdErr)
            {
                return outputStreamConnector;
            }
        };

        session.setShellReferenceId("Shell-1");

        session.execute("the-command", Arrays.asList("arg1", "arg2"), stdin, stdout, stderr);

        assertThat(session.getCommandId(), is("Command-9876"));
        assertThat(session.getInputStreamConnector(), is(sameInstance(inputStreamConnector)));
        assertThat(session.getOutputStreamConnector(), is(sameInstance(outputStreamConnector)));

        verify(inputStreamConnector).start();
        verify(outputStreamConnector).start();

        verify(connection).send(captor.capture());

        Envelope envelope = captor.getValue();

        assertEnvelope(envelope, WindowsSession.ACTION_COMMAND, "Shell-1");

        CommandLine commandLine = findElement(envelope.getBody().getAny(), "CommandLine");

        assertThat(commandLine, is(notNullValue()));

        assertThat(commandLine.getCommand(), is("the-command"));
        assertThat(commandLine.getArguments(), contains("arg1", "arg2"));
    }


    /**
     *
     */
    @Test
    public void shouldExecuteCommandWithNoArguments() throws Exception
    {
        URL                         url                   = new URL("http", "localhost", 80, "/foo");
        SoapConnection              connection            = mock(SoapConnection.class);
        final InputStreamConnector  inputStreamConnector  = mock(InputStreamConnector.class);
        final OutputStreamConnector outputStreamConnector = mock(OutputStreamConnector.class);
        InputStream                 stdin                 = mock(InputStream.class);
        OutputStream                stdout                = mock(OutputStream.class);
        OutputStream                stderr                = mock(OutputStream.class);
        CommandResponse             response              = new CommandResponse();
        ArgumentCaptor<Envelope>    captor                = ArgumentCaptor.forClass(Envelope.class);

        response.setCommandId("Command-9876");

        when(connection.getUrl()).thenReturn(url);
        when(connection.send(any(Envelope.class))).thenReturn(Arrays.asList((Object) response));

        WindowsSession session = new WindowsSession(connection)
        {
            @Override
            protected InputStreamConnector createInputStreamConnector(InputStream stdIn)
            {
                return inputStreamConnector;
            }

            @Override
            protected OutputStreamConnector createOutputStreamConnector(OutputStream stdOut,
                                                                        OutputStream stdErr)
            {
                return outputStreamConnector;
            }
        };

        session.setShellReferenceId("Shell-1");

        session.execute("the-command", Collections.<String>emptyList(), stdin, stdout, stderr);

        assertThat(session.getCommandId(), is("Command-9876"));
        assertThat(session.getInputStreamConnector(), is(sameInstance(inputStreamConnector)));
        assertThat(session.getOutputStreamConnector(), is(sameInstance(outputStreamConnector)));

        verify(inputStreamConnector).start();
        verify(outputStreamConnector).start();

        verify(connection).send(captor.capture());

        Envelope envelope = captor.getValue();

        assertEnvelope(envelope, WindowsSession.ACTION_COMMAND, "Shell-1");

        CommandLine commandLine = findElement(envelope.getBody().getAny(), "CommandLine");

        assertThat(commandLine, is(notNullValue()));

        assertThat(commandLine.getCommand(), is("the-command"));
        assertThat(commandLine.getArguments().size(), is(0));
    }


    /**
     *
     */
    @Test
    public void shouldCreateInputStreamConnector() throws Exception
    {
        SoapConnection       connection = mock(SoapConnection.class);
        InputStream          stdin      = mock(InputStream.class);

        WindowsSession       session    = new WindowsSession(connection);

        InputStreamConnector connector  = session.createInputStreamConnector(stdin);

        assertThat(connector.getSession(), is(sameInstance(session)));
        assertThat(connector.getInputStream(), is(sameInstance(stdin)));
    }


    /**
     *
     */
    @Test
    public void shouldCreateOutputStreamConnector() throws Exception
    {
        SoapConnection        connection = mock(SoapConnection.class);
        OutputStream          stdout     = mock(OutputStream.class, "out");
        OutputStream          stderr     = mock(OutputStream.class, "err");

        WindowsSession        session    = new WindowsSession(connection);

        OutputStreamConnector connector  = session.createOutputStreamConnector(stdout, stderr);

        assertThat(connector.getSession(), is(sameInstance(session)));
        assertThat(connector.getOutputStream(), is(sameInstance(stdout)));
        assertThat(connector.getErrorStream(), is(sameInstance(stderr)));
    }


    private void assertEnvelope(Envelope envelope,
                                String   action,
                                String   shellId)
    {
        Header        header         = envelope.getHeader();
        List<Object>  headerElements = header.getAny();
        AttributedURI actionURI      = findElement(headerElements, "Action");

        assertThat(actionURI, is(notNullValue()));
        assertThat(actionURI.getValue(), is(action));

        SelectorSetType selectorSet = findElement(headerElements, "SelectorSet");

        if (shellId == null)
        {
            assertThat(selectorSet, is(nullValue()));
        }
        else
        {
            assertThat(selectorSet, is(notNullValue()));

            List<SelectorType> selectorTypes = selectorSet.getSelector();

            assertThat(selectorTypes, is(notNullValue()));
            assertThat(selectorTypes.size(), is(1));

            SelectorType selectorType = selectorTypes.get(0);

            assertThat(selectorType.getName(), is("ShellId"));

            List<Serializable> content = selectorType.getContent();

            assertThat(content, is(notNullValue()));
            assertThat(content.size(), is(1));
            assertThat(content.get(0), is((Serializable) shellId));
        }
    }


    /**
     * Find the first instance of the given {@link Class}
     * within the specified {@link List}.
     *
     * @param list the {@link List} to search
     * @param <T>  the type to return
     *
     * @return the first instance of the given {@link Class}
     *         within the specified {@link List}
     */
    private <T> T findElement(List   list,
                              String name)
    {
        for (Object o : list)
        {
            if (o instanceof JAXBElement)
            {
                JAXBElement element = (JAXBElement) o;

                if (name.equalsIgnoreCase(element.getName().getLocalPart()))
                {
                    return (T) element.getValue();
                }
            }
        }

        return null;
    }


    /**
     * A {@link Comparator} to use to sort {@link EnvironmentVariable}s
     */
    public static class EnvironmentVariableComparator implements Comparator<EnvironmentVariable>
    {
        /**
         *
         */
        @Override
        public int compare(EnvironmentVariable o1,
                           EnvironmentVariable o2)
        {
            return o1.getName().compareTo(o2.getName());
        }
    }


    /**
     * A {@link Comparator} to use to sort {@link OptionType}s
     */
    public static class OptionTypeComparator implements Comparator<OptionType>
    {
        /**
         * Method description
         *
         * @param o1
         * @param o2
         *
         * @return
         */
        @Override
        public int compare(OptionType o1,
                           OptionType o2)
        {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
