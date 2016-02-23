/*
 * File: SoapConnection.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.options.HttpProxy;

import com.oracle.tools.runtime.remote.Authentication;
import com.oracle.tools.runtime.remote.http.HttpBasedAuthentication;

import org.w3c.soap.envelope.Envelope;
import org.w3c.soap.envelope.Fault;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import javax.xml.ws.http.HTTPException;

/**
 * This class encapsulate the sending of a SOAP message and
 * the processing of the response. This encapsulation is
 * primarily to allow mocks and stubs to be used for testing.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SoapConnection
{
    /**
     * The {@link URL} of the WinRM SOAP service
     */
    private final URL url;

    /**
     * The port that the default WinRM service is listening on
     */
    private final String userName;

    /**
     * The {@link HttpBasedAuthentication} to use to authenticate SOAP connections
     */
    private final HttpBasedAuthentication authentication;

    /**
     * The set of {@link Options} to use to control the session
     */
    private final Options options;

    /**
     * The JAXB context to use for handling the SOAP messages.
     */
    private JAXBContext jaxbContext;

    /**
     * The {@link Marshaller} to use to marshal objects to XML
     */
    private final Marshaller marshaller;

    /**
     * The {@link Unmarshaller} to use to un-marshal XML to objects
     */
    private final Unmarshaller unmarshaller;


    /**
     * Create a {@link SoapConnection} that uses the specified connection
     * details.
     *
     * @param hostName       the host name of the host running the SOAP service
     * @param port           the port that the SOAP service is listening on
     * @param userName       the name of the user to use to connect to the WinRM service
     * @param authentication the authentication to use to connect to the WinRM
     * @param options        the {@link com.oracle.tools.Option}s controlling the session
     */
    public SoapConnection(String         hostName,
                          int            port,
                          String         servicePath,
                          String         userName,
                          Authentication authentication,
                          Option...      options)
    {
        this.userName = userName;
        this.options  = new Options(options);

        if (authentication instanceof HttpBasedAuthentication)
        {
            this.authentication = (HttpBasedAuthentication) authentication;
        }
        else if (authentication == null)
        {
            this.authentication = null;
        }
        else
        {
            throw new IllegalArgumentException("Authentication parameter must implement HttpBasedAuthentication");
        }

        try
        {
            this.url = new URL("http", hostName, port, servicePath);

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Properties  props       = new Properties();

            props.load(classLoader.getResourceAsStream("ws-man.properties"));

            jaxbContext  = JAXBContext.newInstance(props.getProperty("ws-man.packages"), classLoader);
            marshaller   = createMarshaller();
            unmarshaller = createUnmarshaller();
        }
        catch (IOException | JAXBException e)
        {
            throw new RuntimeException("Could not create SoapConnection", e);
        }
    }


    /**
     * Obtain the {@link URL} od the SOAP service.
     *
     * @return the {@link URL} od the SOAP service
     */
    public URL getUrl()
    {
        return url;
    }


    /**
     * Obtain the user name used by this {@link SoapConnection}
     * to connect to the SOAP service.
     *
     * @return the user name used by this {@link SoapConnection}
     *         to connect to the SOAP service
     */
    public String getUserName()
    {
        return userName;
    }


    /**
     * Obtain the user name used by this {@link SoapConnection}
     * to connect to the SOAP service.
     *
     * @return the user name used by this {@link SoapConnection}
     *         to connect to the SOAP service
     */
    public HttpBasedAuthentication getAuthentication()
    {
        return authentication;
    }

    /**
     * Obtain a {@link Marshaller} to use to marshal
     * objects to XML.
     *
     * @return a {@link Marshaller} to use to marshal
     * objects to XML
     */
    Marshaller createMarshaller() throws JAXBException
    {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

        return marshaller;
    }

    /**
     * Obtain an {@link Unmarshaller} to use to un-marshal
     * XML to objects.
     *
     * @return an {@link Unmarshaller} to use to un-marshal
     *         XML to objects
     */
    Unmarshaller createUnmarshaller() throws JAXBException
    {
        return jaxbContext.createUnmarshaller();
    }


    /**
     * Send the specified SOAP {@link Envelope} to the SOAP service
     * listening on the specified {@link URL}.
     *
     * @param envelope the {@link org.w3c.soap.envelope.Envelope} to send
     *
     * @return the un-marshaled result returned from the SOAP service
     *
     * @throws IOException if an error occurs
     */
    public List<Object> send(Envelope envelope) throws IOException
    {
        HttpURLConnection httpConnection;

        if (authentication == null)
        {
            HttpProxy proxy = options.get(HttpProxy.class, HttpProxy.none());

            httpConnection = proxy.openConnection(this.url);
        }
        else
        {
            httpConnection = authentication.openConnection(this.url, userName, options);
        }

        return send(envelope, httpConnection);
    }


    /**
     * Send the specified SOAP {@link Envelope} to the SOAP service
     * listening on the specified {@link HttpURLConnection}.
     *
     * @param envelope       the SOAP {@link Envelope} to send
     * @param httpConnection the {@link HttpURLConnection} to send the message to
     *
     * @return the response from the SOAP service
     *
     * @throws IOException if an error occurs
     */
    protected List<Object> send(Envelope          envelope,
                                HttpURLConnection httpConnection) throws IOException
    {
        httpConnection.setDoOutput(true);
        httpConnection.setRequestMethod("POST");
        httpConnection.setRequestProperty("Content-Type", "application/soap+xml;charset=UTF-8");

        try
        {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpConnection.getOutputStream())))
            {
                synchronized (marshaller)
                {
                    marshaller.marshal(ObjectFactories.SOAP.createEnvelope(envelope), writer);
                }
            }

            int responseCode = httpConnection.getResponseCode();

            switch (responseCode)
            {
            case HttpURLConnection.HTTP_INTERNAL_ERROR :
                return getSOAPBodyContents(httpConnection.getErrorStream());

            case HttpURLConnection.HTTP_OK :
                return getSOAPBodyContents(httpConnection.getInputStream());

            case HttpURLConnection.HTTP_UNAUTHORIZED :
                throw new SecurityException("Unauthorized. Cannot connect to " + httpConnection.getURL());

            default :
                throw new HTTPException(responseCode);
            }
        }
        catch (JAXBException | SoapFaultException e)
        {
            throw new IOException("Error sending SOAP message", e);
        }
        finally
        {
            httpConnection.disconnect();
        }
    }


    /**
     * Un-marshall the SOAP response from the specified {@link InputStream}.
     *
     * @param stream the {@link InputStream} containing the XML to un-marshall
     *
     * @return the un-marshaled JAXB objects
     */
    protected List<Object> getSOAPBodyContents(InputStream stream) throws JAXBException, IOException, SoapFaultException
    {
        Object result;

        try
        {
            synchronized (unmarshaller)
            {
                result = unmarshaller.unmarshal(stream);
            }
        }
        finally
        {
            stream.close();
        }

        if (result instanceof JAXBElement)
        {
            JAXBElement elt = (JAXBElement) result;

            if (elt.getValue() instanceof Envelope)
            {
                List<Object> list = ((Envelope) elt.getValue()).getBody().getAny();

                for (int i = 0; i < list.size(); i++)
                {
                    Object o = list.get(i);

                    if (o instanceof JAXBElement)
                    {
                        o = ((JAXBElement) o).getValue();
                        list.set(i, o);
                    }

                    if (o instanceof Fault)
                    {
                        throw new SoapFaultException((Fault) o);
                    }
                }

                return list;
            }
            else
            {
                System.out.println("Unsupported element contents: " + elt.getValue().getClass().getName());
            }
        }
        else
        {
            System.out.println("Unsupported class: " + result.getClass().getName());
        }

        return null;

    }
}
