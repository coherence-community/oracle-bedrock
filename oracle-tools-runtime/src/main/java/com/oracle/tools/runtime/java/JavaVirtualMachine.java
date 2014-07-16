/*
 * File: JavaVirtualMachine.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.runtime.AbstractPlatform;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationBuilder;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;

/**
 * An implementation of a {@link Platform} for running, deploying and
 * managing {@link JavaApplication}s inside this Java Virtual Machine.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JavaVirtualMachine extends AbstractPlatform
{
    /**
     * The singleton instance of {@link JavaVirtualMachine}.
     */
    private static JavaVirtualMachine INSTANCE = new JavaVirtualMachine();

    private boolean isAutoDebugEnabled = true;

    /**
     * Construct a new {@link JavaVirtualMachine}.
     * This constructor is private as the {@link JavaVirtualMachine}
     * platform is a singleton as there is only one local JVM.
     */
    private JavaVirtualMachine()
    {
        super("JVM");
    }

    @Override
    public InetAddress getPublicInetAddress()
    {
        return  LocalPlatform.getInstance().getPrivateInetAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <A extends Application, B extends ApplicationBuilder<A>> B getApplicationBuilder(Class<A> applicationClass)
    {
        if (JavaApplication.class.isAssignableFrom(applicationClass))
        {
            return (B) new ContainerBasedJavaApplicationBuilder();
        }

        return null;
    }

    /**
     * Determine whether the current JVM is running with a debugger attached.
     * Typically this will be when running in debug mode inside a Java IDE.
     * If an exception is thrown while trying to determine if a debugger is
     * attached then this method will return false.
     *
     * @return true if running in debug mode, otherwise false
     */
    public boolean isRunningWithDebugger()
    {
        try
        {
            return getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
        }
        catch (Throwable t)
        {
            System.err.println("Error trying to determine debug status - " + t.getMessage());
            return false;
        }
    }

    /**
     * Obtain the managed bean for the runtime system of the Java virtual machine.
     *
     * @return a RuntimeMXBean object for the Java virtual machine
     */
    public RuntimeMXBean getRuntimeMXBean()
    {
        return ManagementFactory.getRuntimeMXBean();
    }

    /**
     * Set whether {@link JavaApplication}s started from Oracle Tools will automatically
     * be started with remote debugging enabled if the controlling JVM is also running
     * inside a debugger.
     *
     * @param enabled true if remote debug mode for applications is based on the controlling
     *                JVM or false if it is based only on the {@link JavaApplicationSchema}
     *                that defines the {@link JavaApplication}
     */
    public void setAutoDebugEnabled(boolean enabled)
    {
        isAutoDebugEnabled = enabled;
    }

    /**
     * Obtain the flag indicating whether {@link JavaApplication}s started from Oracle Tools
     * should automatically be started with remote debugging enabled if the controlling JVM
     * is also running inside a debugger.
     *
     * @return true if remote debug mode for applications is based on the controlling
     *              JVM or false if it is based only on the {@link JavaApplicationSchema}
     *              that defines the {@link JavaApplication}
     */
    public boolean getAutoDebugEnabled()
    {
        return isAutoDebugEnabled;
    }

    /**
     * Returns true if {@link JavaApplication}s started from Oracle Tools
     * should run with remote debugging enabled.
     *
     * @return true if {@link JavaApplication}s started from Oracle Tools
     *         should run with remote debugging enabled.
     */
    public boolean shouldEnabledRemoteDebug()
    {
        return getAutoDebugEnabled() && isRunningWithDebugger();
    }

    /**
     * Obtain the singleton instance of the {@link JavaVirtualMachine}.
     *
     * @return the singleton instance of the {@link JavaVirtualMachine}
     */
    public static JavaVirtualMachine getInstance()
    {
        return INSTANCE;
    }
}
