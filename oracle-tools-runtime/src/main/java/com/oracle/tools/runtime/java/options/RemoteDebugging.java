/*
 * File: RemoteDebugging.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.Option;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.JavaVirtualMachine;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import com.oracle.tools.util.Capture;

import java.net.UnknownHostException;

/**
 * An {@link Option} to define the {@link Platform} specific remote debugging configuration
 * for a {@link JavaApplication}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class RemoteDebugging implements Option
{
    /**
     * Defines how a {@link JavaApplication} will behave when remote-debugging
     * is enabled.
     */
    public static enum Behavior
    {
        /**
         * The {@link JavaApplication} will listen for connections from a debugger.
         */
        LISTEN_FOR_DEBUGGER,

        /**
         * The {@link JavaApplication} will attempt to connect to a debugger.
         */
        ATTACH_TO_DEBUGGER
    }


    /**
     * Should remote debugging be enabled for a {@link JavaApplication}.
     */
    private boolean enabled;

    /**
     * Should the {@link JavaApplication} be suspend when starting
     * (to allow for connection from a debugger).
     */
    private boolean startSuspended;

    /**
     * The {@link JavaApplication} {@link Behavior} for remote debugging.
     */
    private Behavior behavior;

    /**
     * The remote debug ports to be used by {@link JavaApplication}s for opening
     * debugger connections.
     */
    private AvailablePortIterator remoteDebuggingPorts;

    /**
     * The port a {@link JavaApplication} should connect to when
     * using the {@link Behavior#ATTACH_TO_DEBUGGER}, -1 when not in use.
     */
    private int remoteDebuggerPort;


    /**
     * Privately constructs a {@link RemoteDebugging} {@link Option}.
     *
     * @param enabled               is remote debugging enabled?
     * @param startSuspended        should the remotely debugged application start in suspended mode?
     * @param behavior              the {@link Behavior} of the application with respect to a debugger
     * @param remoteDebuggingPorts  an {@link AvailablePortIterator} to provide debugger ports
     */
    private RemoteDebugging(boolean               enabled,
                            boolean               startSuspended,
                            Behavior              behavior,
                            AvailablePortIterator remoteDebuggingPorts)
    {
        this.enabled              = enabled;
        this.startSuspended       = startSuspended;
        this.behavior             = behavior;
        this.remoteDebuggingPorts = remoteDebuggingPorts;
        this.remoteDebuggerPort   = -1;
    }


    /**
     * Determines if {@link RemoteDebugging} is enabled.
     *
     * @return  <code>true</code> if {@link RemoteDebugging} is enabled, <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Determines if the {@link JavaApplication} should be started in "suspend" mode
     * when created to allow for debuggers to connect.
     *
     * @return  <code>true</code> if the {@link JavaApplication} should be started in "suspend" mode,
     *          <code>false</code> if the {@link JavaApplication} should be started normally
     */
    public boolean isStartSuspended()
    {
        return startSuspended;
    }


    /**
     * Obtains the {@link RemoteDebugging} {@link Behavior}.
     *
     * @return  the {@link Behavior}
     */
    public Behavior getBehavior()
    {
        return behavior;
    }


    /**
     * Obtain the port that {@link JavaApplication}s using this {@link RemoteDebugging} {@link Option}
     * will listen on for remote debugger connections if started with remote debug enabled and the
     * remote debugging {@link Behavior} is {@link Behavior#LISTEN_FOR_DEBUGGER}.
     *
     * @return the port that {@link JavaApplication}s will listen on for remote debugger connections.
     *         A different value may be returned each time this method is called. A value of 0 will
     *         be returned if no ports are available
     */
    public int getListenPort()
    {
        return remoteDebuggingPorts != null && remoteDebuggingPorts.hasNext() ? remoteDebuggingPorts.next() : 0;
    }


    /**
     * Obtain the port that {@link JavaApplication}s using this {@link RemoteDebugging} {@link Option}
     * will use to connect back to a remote debugger if started with remote debug enabled and the
     * remote debugging {@link Behavior} is {@link Behavior#ATTACH_TO_DEBUGGER}.
     *
     * @return the port that {@link JavaApplication}s will use to attach to a remote debugger.
     *         The same value will be returned each time this method is called, or -1 if not configured
     */
    public int getAttachPort()
    {
        return remoteDebuggerPort;
    }


    /**
     * Set the {@link Behavior} of the {@link RemoteDebugging} {@link Option}
     * to attach to the debugger.
     *
     * @return the {@link RemoteDebugging} {@link Option} to allow fluent-method calls
     */
    public RemoteDebugging attachToDebugger(int remoteDebuggerPort)
    {
        this.behavior           = Behavior.ATTACH_TO_DEBUGGER;
        this.remoteDebuggerPort = remoteDebuggerPort;

        return this;
    }


    /**
     * Set the {@link Behavior} of the {@link RemoteDebugging} {@link Option}
     * to listen for a debugger connection.
     *
     * @return the {@link RemoteDebugging} {@link Option} to allow fluent-method calls
     */
    public RemoteDebugging listenForDebugger()
    {
        this.behavior           = Behavior.LISTEN_FOR_DEBUGGER;
        this.remoteDebuggerPort = -1;

        return this;
    }


    /**
     * Sets if the {@link JavaApplication} should be started in suspended mode,
     * thus allowing debuggers to connect.
     *
     * @param startSuspended  <code>true</code> to start suspended, <code>false</code> to start in normal mode
     *
     * @return the {@link RemoteDebugging} {@link Option} to allow fluent-method calls
     */
    public RemoteDebugging startSuspended(boolean startSuspended)
    {
        this.startSuspended = startSuspended;

        return this;
    }


    @Override
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if (!(other instanceof RemoteDebugging))
        {
            return false;
        }

        RemoteDebugging that = (RemoteDebugging) other;

        if (enabled != that.enabled)
        {
            return false;
        }

        if (remoteDebuggerPort != that.remoteDebuggerPort)
        {
            return false;
        }

        if (startSuspended != that.startSuspended)
        {
            return false;
        }

        if (behavior != that.behavior)
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = (enabled ? 1 : 0);

        result = 31 * result + (startSuspended ? 1 : 0);
        result = 31 * result + behavior.hashCode();
        result = 31 * result + remoteDebuggerPort;

        return result;
    }


    /**
     * Obtains a default {@link RemoteDebugging} {@link Option}, with debugging enabled.
     *
     * @return  a {@link RemoteDebugging} {@link Option}
     */
    public static RemoteDebugging enabled()
    {
        return new RemoteDebugging(true,
                                   false,
                                   Behavior.LISTEN_FOR_DEBUGGER,
                                   LocalPlatform.getInstance().getAvailablePorts());
    }


    /**
     * Obtains a disabled {@link RemoteDebugging} {@link Option}.
     *
     * @return  a {@link RemoteDebugging} {@link Option}
     */
    public static RemoteDebugging disabled()
    {
        return new RemoteDebugging(false,
                                   false,
                                   Behavior.LISTEN_FOR_DEBUGGER,
                                   LocalPlatform.getInstance().getAvailablePorts());
    }


    /**
     * Obtains a {@link RemoteDebugging} {@link Option}, auto-detecting if it should be
     * enabled based on the Java process in which the thread is executing.
     *
     * @return  a {@link RemoteDebugging} {@link Option}
     */
    public static RemoteDebugging autoDetect()
    {
        return new RemoteDebugging(JavaVirtualMachine.getInstance().shouldEnableRemoteDebugging(),
                                   false,
                                   Behavior.LISTEN_FOR_DEBUGGER,
                                   LocalPlatform.getInstance().getAvailablePorts());
    }
}
