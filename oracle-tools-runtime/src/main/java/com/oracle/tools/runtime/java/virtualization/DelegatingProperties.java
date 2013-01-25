/*
 * File: DelegatingProperties.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

package com.oracle.tools.runtime.java.virtualization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * A {@link DelegatingProperties} is a {@link Properties} implementation
 * that delegates {@link Properties} method calls onto appropriate
 * {@link VirtualizedSystem#getProperties()}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
@SuppressWarnings("serial")
public class DelegatingProperties extends Properties
{
    /**
     * Obtains the {@link Properties} on which to delegate calls.
     *
     * @return  the {@link Properties} delegate.
     */
    private Properties getDelegate()
    {
        VirtualizedSystem system = Virtualization.getSystem();

        return system.getProperties();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object setProperty(String key,
                              String value)
    {
        return getDelegate().setProperty(key, value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void load(Reader reader) throws IOException
    {
        getDelegate().load(reader);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void load(InputStream inStream) throws IOException
    {
        getDelegate().load(inStream);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void store(Writer writer,
                      String comments) throws IOException
    {
        getDelegate().store(writer, comments);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void store(OutputStream out,
                      String comments) throws IOException
    {
        getDelegate().store(out, comments);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void loadFromXML(InputStream in) throws IOException
    {
        getDelegate().loadFromXML(in);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void storeToXML(OutputStream os,
                           String comment) throws IOException
    {
        getDelegate().storeToXML(os, comment);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void storeToXML(OutputStream os,
                           String comment,
                           String encoding) throws IOException
    {
        getDelegate().storeToXML(os, comment, encoding);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getProperty(String key)
    {
        return getDelegate().getProperty(key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getProperty(String key,
                              String defaultValue)
    {
        return getDelegate().getProperty(key, defaultValue);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<?> propertyNames()
    {
        return getDelegate().propertyNames();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> stringPropertyNames()
    {
        return getDelegate().stringPropertyNames();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void list(PrintStream out)
    {
        getDelegate().list(out);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void list(PrintWriter out)
    {
        getDelegate().list(out);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return getDelegate().size();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty()
    {
        return getDelegate().isEmpty();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<Object> keys()
    {
        return getDelegate().keys();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Enumeration<Object> elements()
    {
        return getDelegate().elements();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object value)
    {
        return getDelegate().contains(value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsValue(Object value)
    {
        return getDelegate().containsValue(value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Object key)
    {
        return getDelegate().containsKey(key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object get(Object key)
    {
        return getDelegate().get(key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object put(Object key,
                      Object value)
    {
        return getDelegate().put(key, value);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Object remove(Object key)
    {
        return getDelegate().remove(key);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void putAll(Map<?, ?> t)
    {
        getDelegate().putAll(t);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void clear()
    {
        getDelegate().clear();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        return getDelegate().toString();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Object> keySet()
    {
        return getDelegate().keySet();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Map.Entry<Object, Object>> entrySet()
    {
        return getDelegate().entrySet();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Object> values()
    {
        return getDelegate().values();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o)
    {
        return getDelegate().equals(o);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode()
    {
        return getDelegate().hashCode();
    }
}
