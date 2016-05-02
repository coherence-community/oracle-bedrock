/*
 * File: SoapConnectionTest.java
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

package com.oracle.bedrock.runtime.remote.winrm;

import com.microsoft.wsman.shell.CommandResponse;

import com.oracle.bedrock.Options;

import com.oracle.bedrock.options.HttpProxy;

import com.oracle.bedrock.runtime.remote.http.HttpBasedAuthentication;

import org.junit.Test;

import org.mockito.ArgumentCaptor;

import org.w3c.soap.envelope.Body;
import org.w3c.soap.envelope.Envelope;
import org.w3c.soap.envelope.Fault;
import org.w3c.soap.envelope.Header;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.Marshaller;

/**
 * Tests for {@link SoapConnection}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SoapConnectionTest
{
    @Test
    public void shouldHaveCorrectURL() throws Exception
    {
        SoapConnection connection = new SoapConnection("oracle.com", 80, "/foo", "dummy", null);

        assertThat(connection.getUrl(), is(new URL("http://oracle.com:80/foo")));
    }


    @Test
    public void shouldOpenAuthenticatedConnection() throws Exception
    {
        URL                     url            = new URL("http://oracle.com:80/foo");
        Envelope                envelope       = mock(Envelope.class);
        String                  useName        = "dummy";
        HttpBasedAuthentication authentication = mock(HttpBasedAuthentication.class);
        HttpURLConnection       httpConnection = mock(HttpURLConnection.class);

        when(authentication.openConnection(any(URL.class), anyString(), any(Options.class))).thenReturn(httpConnection);

        SoapConnection realConnection = new SoapConnection("oracle.com", 80, "/foo", useName, authentication)
        {
            @Override
            public List<Object> send(Envelope          envelope,
                                     HttpURLConnection httpConnection) throws IOException
            {
                return Collections.emptyList();
            }
        };

        SoapConnection connection = spy(realConnection);

        connection.send(envelope);

        verify(authentication).openConnection(eq(url), eq(useName), any(Options.class));
        verify(connection).send(same(envelope), same(httpConnection));
    }


    @Test
    public void shouldOpenNonAuthenticatedConnectionWithNoProxy() throws Exception
    {
        URL            url            = new URL("http://oracle.com:80/foo");
        Envelope       envelope       = mock(Envelope.class);
        String         useName        = "dummy";

        SoapConnection realConnection = new SoapConnection("oracle.com", 80, "/foo", useName, null)
        {
            @Override
            public List<Object> send(Envelope          envelope,
                                     HttpURLConnection httpConnection) throws IOException
            {
                return Collections.emptyList();
            }
        };

        SoapConnection connection = spy(realConnection);

        connection.send(envelope);

        ArgumentCaptor<HttpURLConnection> captor = ArgumentCaptor.forClass(HttpURLConnection.class);

        verify(connection).send(same(envelope), captor.capture());

        HttpURLConnection httpConnection = captor.getValue();

        assertThat(httpConnection.getURL(), is(url));
        assertThat(httpConnection.usingProxy(), is(false));
    }


    @Test
    public void shouldOpenNonAuthenticatedConnectionWithProxy() throws Exception
    {
        Envelope          envelope       = mock(Envelope.class);
        URL               url            = new URL("http://oracle.com:80/foo");
        String            useName        = "dummy";
        HttpURLConnection httpConnection = mock(HttpURLConnection.class);
        HttpProxy         httpProxy      = mock(HttpProxy.class);

        when(httpProxy.openConnection(any(URL.class))).thenReturn(httpConnection);

        SoapConnection realConnection = new SoapConnection("oracle.com", 80, "/foo", useName, null, httpProxy)
        {
            @Override
            public List<Object> send(Envelope          envelope,
                                     HttpURLConnection httpConnection) throws IOException
            {
                return Collections.emptyList();
            }
        };

        SoapConnection connection = spy(realConnection);

        connection.send(envelope);

        ArgumentCaptor<HttpURLConnection> captor = ArgumentCaptor.forClass(HttpURLConnection.class);

        verify(httpProxy).openConnection(eq(url));
        verify(connection).send(same(envelope), captor.capture());

        HttpURLConnection result = captor.getValue();

        assertThat(result, is(sameInstance(httpConnection)));
    }


    @Test
    public void shouldUnmarshalSoapResponse() throws Exception
    {
        SoapConnection  connection = new SoapConnection("http://oracle.com", 80, "/foo", "dummy", null);
        Marshaller      marshaller = connection.createMarshaller();

        Envelope        envelope   = ObjectFactories.SOAP.createEnvelope();
        Header          header     = ObjectFactories.SOAP.createHeader();
        Body            body       = ObjectFactories.SOAP.createBody();

        CommandResponse response   = ObjectFactories.SHELL.createCommandResponse();

        response.setCommandId("1234");

        envelope.setHeader(header);
        envelope.setBody(body);

        body.getAny().add(ObjectFactories.SHELL.createCommandResponse(response));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        marshaller.marshal(ObjectFactories.SOAP.createEnvelope(envelope), outputStream);

        InputStream stream   = new ByteArrayInputStream(outputStream.toByteArray());

        List<?>     contents = connection.getSOAPBodyContents(stream);

        assertThat(contents, is(notNullValue()));
        assertThat(contents.size(), is(1));
        assertThat(contents.get(0), is(instanceOf(CommandResponse.class)));

        CommandResponse content = (CommandResponse) contents.get(0);

        assertThat(content.isSetCommandId(), is(true));
        assertThat(content.getCommandId(), is("1234"));
    }


    @Test(expected = SoapFaultException.class)
    public void shouldUnmarshalSoapFault() throws Exception
    {
        SoapConnection connection = new SoapConnection("http://oracle.com", 80, "/foo", "dummy", null);
        Marshaller     marshaller = connection.createMarshaller();

        Envelope       envelope   = ObjectFactories.SOAP.createEnvelope();
        Header         header     = ObjectFactories.SOAP.createHeader();
        Body           body       = ObjectFactories.SOAP.createBody();
        Fault          fault      = ObjectFactories.SOAP.createFault();

        envelope.setHeader(header);
        envelope.setBody(body);

        body.getAny().add(ObjectFactories.SOAP.createFault(fault));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        marshaller.marshal(ObjectFactories.SOAP.createEnvelope(envelope), outputStream);

        InputStream stream = new ByteArrayInputStream(outputStream.toByteArray());

        connection.getSOAPBodyContents(stream);
    }
}
