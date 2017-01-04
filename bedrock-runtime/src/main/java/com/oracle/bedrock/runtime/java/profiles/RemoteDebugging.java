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

package com.oracle.bedrock.runtime.java.profiles;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.io.NetworkHelper;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.JavaVirtualMachine;
import com.oracle.bedrock.runtime.java.options.Freeform;
import com.oracle.bedrock.runtime.java.options.WaitToStart;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.util.Capture;
import com.oracle.bedrock.util.PerpetualIterator;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Defines a {@link Profile} to enable/disable Remote Java Debugging for {@link JavaApplication}s.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteDebugging implements Profile, Option
{
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
     * An optional {@link TransportAddress} to use for {@link RemoteDebugging}.
     *
     * If it's not provided (null), the {@link RemoteDebugging} profile will attempt to resolve
     * one from the provided {@link OptionsByType} and if that's not available, it will a port
     * decided by the underlying {@link Platform}.
     */
    private TransportAddress transportAddress;


    /**
     * Defines how a {@link JavaApplication} will behave when remote-debugging
     * is enabled.
     */
    public enum Behavior
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
     * Privately constructs a {@link RemoteDebugging} {@link Profile}.
     *
     * @param enabled           is remote debugging enabled?
     * @param startSuspended    should the remotely debugged application start in suspended mode?
     * @param behavior          the {@link Behavior} of the application with respect to a debugger
     * @param transportAddress  the optional {@link TransportAddress}
     */
    private RemoteDebugging(boolean          enabled,
                            boolean          startSuspended,
                            Behavior         behavior,
                            TransportAddress transportAddress)
    {
        this.enabled          = enabled;
        this.startSuspended   = startSuspended;
        this.behavior         = behavior;
        this.transportAddress = transportAddress;
    }


    /**
     * Obtains if {@link RemoteDebugging} is enabled.
     *
     * @return  <code>true</code> if {@link RemoteDebugging} is enabled, <code>false</code> otherwise
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Obtains if {@link RemoteDebugging} will start a Java Virtual Machine in suspended mode.
     *
     * @return  <code>true</code> if {@link RemoteDebugging} suspends a Java Virtual Machine on start,
     *          <code>false</code> otherwise
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
     * Obtains the {@link RemoteDebugging} {@link TransportAddress}.
     *
     * @return the {@link TransportAddress}
     */
    public TransportAddress getTransportAddress()
    {
        return transportAddress;
    }


    @Override
    public void onLaunching(Platform      platform,
                            MetaClass     metaClass,
                            OptionsByType optionsByType)
    {
        if (enabled)
        {
            // determine the TransportAddress to use
            TransportAddress transportAddress = this.transportAddress == null
                                                ? optionsByType.get(TransportAddress.class) : this.transportAddress;

            // create one if one hasn't been provided
            if (transportAddress == null)
            {
                if (behavior == Behavior.LISTEN_FOR_DEBUGGER)
                {
                    // when we don't have an address we use the platform provides
                    transportAddress = new TransportAddress(LocalPlatform.get().getAvailablePorts());

                    // add the TransportAddress as an Option
                    optionsByType.add(transportAddress);

                }
                else
                {
                    throw new IllegalStateException("Failed to specify a RemoteDebugging.TransportAddress option for attaching the debugger.");
                }
            }

            // determine the transport address
            String address;

            if (transportAddress.getInetAddress() == null)
            {
                address = transportAddress.getPort().get().toString();
            }
            else
            {
                address = transportAddress.getInetAddress().getHostAddress() + ":" + transportAddress.getPort().get();
            }

            // determine if we're going to be in Server Mode
            boolean isDebugServer = behavior == Behavior.LISTEN_FOR_DEBUGGER;

            // construct the agentlib
            String agentlib = String.format("-agentlib:jdwp=transport=dt_socket,server=%s,suspend=%s,address=%s",
                                            (isDebugServer ? "y" : "n"),
                                            (startSuspended ? "y" : "n"),
                                            address);

            // replace the TransportAddress with the one we've resolved / created
            optionsByType.add(transportAddress);

            // add the agent as a Freeform JvmOption
            optionsByType.add(new Freeform(agentlib));

            // disable waiting for the application to start if we're in suspend mode
            if (startSuspended)
            {
                optionsByType.add(WaitToStart.disabled());
            }
        }
    }


    @Override
    public void onLaunched(Platform      platform,
                           Application   application,
                           OptionsByType optionsByType)
    {
    }


    @Override
    public void onClosing(Platform      platform,
                          Application   application,
                          OptionsByType optionsByType)
    {
    }


    /**
     * Obtains a {@link RemoteDebugging} {@link Profile}, with debugging enabled and
     * not suspended.
     *
     * @return  a {@link RemoteDebugging} {@link Option}
     */
    public static RemoteDebugging enabled()
    {
        return new RemoteDebugging(true, false, Behavior.LISTEN_FOR_DEBUGGER, null);
    }


    /**
     * Obtains a {@link RemoteDebugging} {@link Profile}, with debugging disabled.
     *
     * @return  a {@link RemoteDebugging} {@link Option}
     */
    public static RemoteDebugging disabled()
    {
        return new RemoteDebugging(false, false, Behavior.LISTEN_FOR_DEBUGGER, null);
    }


    /**
     * Obtains a {@link RemoteDebugging} {@link Profile}, with debugging enabled but
     * with starting optionally suspended.
     *
     * @param startSuspended  should {@link RemoteDebugging} start suspended
     *
     * @return  a {@link RemoteDebugging} {@link Option}
     */
    public RemoteDebugging startSuspended(boolean startSuspended)
    {
        return new RemoteDebugging(true, startSuspended, Behavior.LISTEN_FOR_DEBUGGER, null);
    }


    /**
     * Obtains a {@link RemoteDebugging} profile that configures attaching to a debugger.
     *
     * @return a new {@link RemoteDebugging} profile
     */
    public RemoteDebugging attach()
    {
        return new RemoteDebugging(enabled, startSuspended, Behavior.ATTACH_TO_DEBUGGER, null);
    }


    /**
     * Obtains a {@link RemoteDebugging} profile that configures listening for a debugger.
     *
     * @return a new {@link RemoteDebugging} profile
     */
    public RemoteDebugging listen()
    {
        return new RemoteDebugging(enabled, startSuspended, Behavior.LISTEN_FOR_DEBUGGER, null);
    }


    /**
     * Obtains a {@link RemoteDebugging} profile that configures a specific {@link TransportAddress}.
     *
     * @param transportAddress   the {@link TransportAddress}
     *
     * @return a new {@link RemoteDebugging} profile
     */
    public RemoteDebugging at(TransportAddress transportAddress)
    {
        return new RemoteDebugging(enabled, startSuspended, behavior, transportAddress);
    }


    /**
     * Obtains a {@link RemoteDebugging} profile that configures a specific {@link TransportAddress}
     * using an {@link AvailablePortIterator}
     *
     * @param ports  the {@link AvailablePortIterator}
     *
     * @return a new {@link RemoteDebugging} profile
     */
    public RemoteDebugging at(AvailablePortIterator ports)
    {
        return new RemoteDebugging(enabled, startSuspended, behavior, new TransportAddress(ports));
    }


    /**
     * Obtains a {@link RemoteDebugging} profile that configures a specific {@link TransportAddress}
     * at a designated port on the {@link Platform}.
     *
     * @param port  the port
     *
     * @return a new {@link RemoteDebugging} profile
     */
    public RemoteDebugging at(Capture<Integer> port)
    {
        return new RemoteDebugging(enabled, startSuspended, behavior, new TransportAddress(port));
    }


    /**
     * Obtains a {@link RemoteDebugging} profile that configures a specific {@link TransportAddress}
     * at a designated port and address.
     *
     * @param address  the {@link InetAddress}
     * @param port     the port
     *
     * @return a new {@link RemoteDebugging} profile
     */
    public RemoteDebugging at(InetAddress      address,
                              Capture<Integer> port)
    {
        return new RemoteDebugging(enabled, startSuspended, behavior, new TransportAddress(address, port));
    }


    /**
     * Obtains a {@link RemoteDebugging} profile that configures a specific {@link TransportAddress}
     * at a designated port and address.
     *
     * @param address  the {@link InetAddress}
     * @param port     the port
     *
     * @return a new {@link RemoteDebugging} profile
     */
    public RemoteDebugging at(InetAddress address,
                              int         port)
    {
        return new RemoteDebugging(enabled, startSuspended, behavior, new TransportAddress(address, port));
    }


    /**
     * Obtains a {@link RemoteDebugging} {@link Profile}, auto-detecting if it should be
     * enabled based on the Java process in which the thread is executing.
     *
     * @return  a {@link RemoteDebugging} {@link Option}
     */
    @OptionsByType.Default
    public static RemoteDebugging autoDetect()
    {
        return new RemoteDebugging(JavaVirtualMachine.get().shouldEnableRemoteDebugging(),
                                   false,
                                   Behavior.LISTEN_FOR_DEBUGGER,
                                   null);
    }


    /**
     * The transport address for {@link RemoteDebugging}.
     */
    public static class TransportAddress implements Option
    {
        /**
         * The optional address for attaching/connecting/listening for the Remote Debugger.
         * (when null this implies a local address)
         */
        private InetAddress address;

        /**
         * The port for attaching/connecting/listening for the Remote Debugger.
         */
        private Capture<Integer> port;


        /**
         * Constructs a local {@link TransportAddress}, choosing a port from the
         * {@link AvailablePortIterator}.
         *
         * @param ports  the available ports
         */
        public TransportAddress(AvailablePortIterator ports)
        {
            // NOTE: Currently Java Debugging only supports IPv4 on the server-side so we must
            // filter the AvailablePortIterator addresses into those that are IPv4
            this(NetworkHelper.getInetAddresses(ports.getInetAddresses(), NetworkHelper.IPv4_ADDRESS).iterator().next(),
                 new Capture<>(ports));
        }


        /**
         * Constructs a local {@link TransportAddress} for the specified port.
         *
         * @param port  the port
         */
        public TransportAddress(Capture<Integer> port)
        {
            this(null, port);
        }


        /**
         * Constructs a local {@link TransportAddress} for the specified port.
         *
         * @param port  the port
         */
        public TransportAddress(int port)
        {
            this(null, new Capture<>(new PerpetualIterator<>(port)));
        }


        /**
         * Constructs a {@link TransportAddress} with the specified port.
         *
         * @param address  the address
         * @param port     the port
         */
        public TransportAddress(InetAddress      address,
                                Capture<Integer> port)
        {
            if (port == null)
            {
                throw new NullPointerException("The port for an address can't be null");
            }
            else
            {
                this.address = address;
                this.port    = port;
            }
        }


        /**
         * Constructs a {@link TransportAddress} with the specified port.
         *
         * @param address  the address
         * @param port     the port
         */
        public TransportAddress(InetAddress address,
                                int         port)
        {
            this(address, new Capture<>(new PerpetualIterator<>(port)));
        }


        /**
         * Obtains the address for the {@link RemoteDebugging} transport (which may be null).
         *
         * @return the {@link InetAddress}
         */
        public InetAddress getInetAddress()
        {
            return address;
        }


        /**
         * Obtains the {@link InetSocketAddress} for the {@link RemoteDebugging} transport (which may be null).
         *
         * @return  an {@link InetSocketAddress}
         */
        public InetSocketAddress getSocketAddress()
        {
            return getInetAddress() == null ? null : new InetSocketAddress(getInetAddress(), port.get());
        }


        /**
         * Obtains the port for the {@link RemoteDebugging} transport.
         *
         * @return the port
         */
        public Capture<Integer> getPort()
        {
            return port;
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (!(o instanceof TransportAddress))
            {
                return false;
            }

            TransportAddress other = (TransportAddress) o;

            if (address != null ? !address.equals(other.address) : other.address != null)
            {
                return false;
            }

            return port.equals(other.port);

        }


        @Override
        public int hashCode()
        {
            int result = address != null ? address.hashCode() : 0;

            result = 31 * result + port.hashCode();

            return result;
        }
    }
}
