/*
 * File: LocalPlatform.java
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

import com.oracle.tools.io.NetworkHelper;

import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.LocalJavaApplicationBuilder;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The {@link Platform} in which this Java Virtual Machine is running.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class LocalPlatform extends AbstractPlatform
{
    /**
     * The singleton instance of {@link LocalPlatform}.
     */
    private static LocalPlatform  INSTANCE = new LocalPlatform();

    private AvailablePortIterator availablePortIterator;

    /**
     * The {@link InetAddress} of the {@link Platform}.
     */
    private InetAddress address;


    /**
     * Construct a new {@link LocalPlatform}.
     */
    private LocalPlatform()
    {
        super("Local");

        // ----- establish InetAddress of the LocalPlatform -----

        // attempt to use the system property that may have been defined
        // (in the future we may use a PlatformAddress Option to achieve this)
        String addressSystemProperty = System.getProperty("oracletools.runtime.address");

        if (addressSystemProperty == null)
        {
            this.address = NetworkHelper.getFeasibleLocalHost();
        }
        else
        {
            try
            {
                this.address = InetAddress.getByName(addressSystemProperty);
            }
            catch (UnknownHostException e)
            {
                // TODO: log that the specified address can't be resolved, defaulting to the feasible localhost
                this.address = NetworkHelper.getFeasibleLocalHost();
            }
        }

        // ----- establish an AvailablePortIterator for the LocalPlatform ------
        this.availablePortIterator = new AvailablePortIterator(30000, AvailablePortIterator.MAXIMUM_PORT);
    }


    @Override
    public InetAddress getAddress()
    {
        return address;
    }


    /**
     * Obtains the {@link AvailablePortIterator} for the {@link LocalPlatform}.
     *
     * @return the {@link AvailablePortIterator}
     */
    public AvailablePortIterator getAvailablePorts()
    {
        return availablePortIterator;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <A extends Application, B extends ApplicationBuilder<A>> B getApplicationBuilder(Class<A> applicationClass)
    {
        if (JavaApplication.class.isAssignableFrom(applicationClass))
        {
            return (B) new LocalJavaApplicationBuilder();
        }
        else
        {
            return (B) new SimpleApplicationBuilder();
        }
    }


    /**
     * Obtain the singleton instance of the {@link LocalPlatform}.
     *
     * @return the singleton instance of the {@link LocalPlatform}
     */
    public static LocalPlatform getInstance()
    {
        return INSTANCE;
    }
}
