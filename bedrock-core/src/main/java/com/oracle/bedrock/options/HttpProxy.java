package com.oracle.bedrock.options;

import com.oracle.bedrock.Option;

import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * An {@link Option} that can be used to specify a
 * type of HTTP {@link Proxy} to use for HTTP connections.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HttpProxy implements Option
{

    /**
     * The {@link Proxy} to use for HTTP connections
     */
    private Proxy proxy;

    /**
     * Create an {@link HttpProxy} option that uses the specified
     * {@link Proxy} for HTTP connections.
     *
     * @param proxy the {@link Proxy} to use
     */
    protected HttpProxy(Proxy proxy)
    {
        this.proxy = proxy;
    }


    /**
     * Open a connection to the specified {@link URL} using
     * the {@link Proxy} defined by this {@link HttpProxy}.
     *
     * @param url the {@link URL} to connect to
     *
     * @return a {@link HttpURLConnection} for the specified {@link URL}
     *
     * @throws IOException if an error occurs opening the connection
     */
    public HttpURLConnection openConnection(URL url) throws IOException
    {
        if (proxy == null)
        {
            return (HttpURLConnection) url.openConnection();
        }

        return (HttpURLConnection) url.openConnection(proxy);
    }


    /**
     * Do not use a {@link Proxy} for HTTP connections.
     *
     * @return an {@link HttpProxy} that does not create
     *         a {@link Proxy}.
     */
    public static HttpProxy none()
    {
        return new HttpProxy(null);
    }


    /**
     * Use the proxy of the specified {@link Proxy.Type} and
     * {@link InetSocketAddress}.
     *
     * @param type    the {@link Proxy.Type}
     * @param address the {@link InetSocketAddress} of the proxy
     *
     * @return an {@link HttpProxy} that provides a {@link Proxy} of
     *         the specified type on the specified socket
     */
    public static HttpProxy proxy(final Proxy.Type type, final InetSocketAddress address)
    {
        return new HttpProxy(new Proxy(type, address));
    }


    /**
     * Use the specified {@link Proxy} for HTTP connections.
     *
     * @param proxy the {@link Proxy} to use
     *
     * @return and {@link HttpProxy} that uses the specified
     *         {@link Proxy} for HTTP connections
     */
    public static HttpProxy proxy(final Proxy proxy)
    {
        return new HttpProxy(proxy);
    }
}
