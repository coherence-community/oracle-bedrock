/*
 * File: WindowsSoapOptions.java
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

package com.oracle.bedrock.runtime.remote.windows.winrm;

import com.oracle.bedrock.runtime.PropertiesBuilder;
import com.oracle.bedrock.ComposableOption;
import com.oracle.bedrock.Option;

import javax.xml.datatype.Duration;

import java.math.BigInteger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An {@link Option} that controls various settings in
 * the SOAP envelope header for a WinRM SOAP message.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsSoapOptions implements Option, ComposableOption<WindowsSoapOptions>
{
    /**
     * Enum description
     */
    public static enum Type {MaxEnvelopeSize,
                             Timeout}


    /** 
     * The default maximum envelope size for SOAP messages.
     */
    public static final BigInteger DEFAULT_MAX_ENVELOPE_SIZE = new BigInteger("153600");

    /**
     * The default timeout value for SOAP messages.
     */
    public static final Duration DEFAULT_TIMEOUT = ObjectFactories.DATATYPE.newDuration(true, 0, 0, 0, 0, 5, 0);

    /**
     * A {@link PropertiesBuilder} for the custom SOAP options.
     */
    private Map<Type, Object> options;


    protected WindowsSoapOptions(Map<Type, Object> options)
    {
        this.options = new HashMap<>(options);
    }


    /**
     * Factory method to create the minimum required
     * set of {@link WindowsSoapOptions}.
     *
     * @return a {@link WindowsSoapOptions} containing the
     *         minimum set of required options.
     */
    public static WindowsSoapOptions basic()
    {
        return new WindowsSoapOptions(new HashMap<Type, Object>())
                        .withMaxEnvelopeSize(DEFAULT_MAX_ENVELOPE_SIZE)
                        .withTimeout(5, TimeUnit.MINUTES);
    }


    /**
     * Obtain an unmodifiable {@link Map} of the options
     * contained within this {@link WindowsSoapOptions}.
     *
     * @return an unmodifiable {@link Map} of the options
     *         contained within this {@link WindowsSoapOptions}
     */
    public Map<Type, Object> getOptions()
    {
        return Collections.unmodifiableMap(options);
    }


    /**
     * Set the maximum SOAP envelope size.
     *
     * @param size the maximum SOAP envelope size
     *
     * @return this {@link WindowsSoapOptions}
     */
    public WindowsSoapOptions withMaxEnvelopeSize(BigInteger size)
    {
        options.put(Type.MaxEnvelopeSize, size);

        return this;
    }


    /**
     * Obtain the maximum envelope size to use for SOAP messages.
     *
     * @return the maximum envelope size to use for SOAP messages
     */
    public BigInteger getMaxEnvelopeSize()
    {
        BigInteger size = (BigInteger) options.get(Type.MaxEnvelopeSize);

        return size != null ? size : DEFAULT_MAX_ENVELOPE_SIZE;
    }

    /**
     * Set the message timeout value.
     *
     * @param timeout the timeout duration
     * @param units   the units to apply to the timeout duration
     *
     * @return this {@link WindowsSoapOptions}
     */
    public WindowsSoapOptions withTimeout(long timeout, TimeUnit units)
    {
        options.put(Type.Timeout, ObjectFactories.DATATYPE.newDuration(units.toMillis(timeout)));

        return this;
    }


    /**
     * Obtain the timeout value to use for SOAP messages.
     *
     * @return the timeout value to use for SOAP messages
     */
    public Duration getTimeout()
    {
        Duration timeout = (Duration) options.get(Type.Timeout);

        return timeout != null ? timeout : DEFAULT_TIMEOUT;
    }


    @Override
    public WindowsSoapOptions compose(WindowsSoapOptions other)
    {
        Map<Type, Object> copy = new HashMap<>(this.options);

        copy.putAll(other.options);

        return new WindowsSoapOptions(copy);
    }

}
