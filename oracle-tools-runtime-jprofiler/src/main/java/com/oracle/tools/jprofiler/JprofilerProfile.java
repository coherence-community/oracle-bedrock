/*
 * File: JprofilerProfile.java
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

package com.oracle.tools.jprofiler;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.Profile;

import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.options.Freeform;
import com.oracle.tools.runtime.java.options.WaitToStart;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import com.oracle.tools.runtime.options.MetaClass;

import com.oracle.tools.util.Capture;
import com.oracle.tools.util.PerpetualIterator;

import java.io.File;

import java.net.InetAddress;

/**
 * The JProfiler {@link Profile}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JprofilerProfile implements Profile, Option
{
    /**
     * JProfiler default listen port.
     */
    private static final int DEFAULT_PORT = 8849;

    /**
     * The default listen address for JProfiler - equates to 0.0.0.0:8849
     */
    private static final ListenAddress DEFAULT_ADDRESS = new ListenAddress(DEFAULT_PORT);

    /**
     * Flag indicating whether this profile is enabled.
     */
    private final boolean enabled;

    /**
     * The location of the JProfiler agent
     */
    private final String agentLibraryFile;

    /**
     * The listen address to use.
     */
    private final ListenAddress listenAddress;

    /**
     * Flag indicating whether the application will start suspended.
     */
    private final boolean startSuspended;

    /**
     * Flaginidcating whether the application will run JProfiler in offline mode.
     */
    private final boolean offline;

    /**
     * The JProfiler configuration file to use in offline mode.
     */
    private final File configurationFile;

    /**
     * The JProfiler session ID to use in offline mode.
     */
    private final int sessionId;

    /**
     * Flag indicating whether JProfiler prints the names of all instrumented classes to stderr
     */
    private final boolean verbose;

    /**
     * Enables the detection of object allocations via JNI calls
     */
    private final boolean jniInterception;

    /**
     * stack size for dynamic instrumentation.
     */
    private final Integer stack;

    /**
     * stack size for sampling.
     */
    private final Integer samplingStack;


    /**
     * Constructs a {@link JprofilerProfile}.
     */
    public JprofilerProfile(boolean       enabled,
                            String        agentLibraryFile,
                            ListenAddress listenAddress,
                            boolean       offline,
                            File          configurationFile,
                            int           sessionId,
                            boolean       startSuspended,
                            boolean       verbose,
                            boolean       jniInterception,
                            Integer       stack,
                            Integer       samplingStack)
    {
        this.enabled           = enabled;
        this.agentLibraryFile  = agentLibraryFile;
        this.listenAddress     = listenAddress;
        this.offline           = offline;
        this.configurationFile = configurationFile;
        this.sessionId         = sessionId;
        this.startSuspended    = startSuspended;
        this.verbose           = verbose;
        this.jniInterception   = jniInterception;
        this.stack             = stack;
        this.samplingStack     = samplingStack;
    }


    /**
     * Obtain whether JProfiler is enabled.
     *
     * @return  true if JProfiler is enabled otherwise false
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    /**
     * Run JProfiler in offline mode using the specified session ID from the file.
     * The configuration file used will be the default JProfiler configuration file
     * located in the .jprofiler directory in the current users home directory.
     *
     * @param sessionId  the session id to use from the specified configuration file
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     *
     * @throws IllegalStateException if start suspended has been set to true
     */
    public JprofilerProfile offlineMode(int sessionId)
    {
        return offlineMode(null, sessionId);
    }


    /**
     * Run JProfiler in offline mode using the specified configuration XML file
     * and the specified session ID from the file.
     *
     * @param configurationFile  the configuration file to use for JProfiler
     * @param sessionId          the session id to use from the specified configuration file
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     *
     * @throws IllegalStateException if start suspended has been set to true
     */
    public JprofilerProfile offlineMode(File configurationFile,
                                        int  sessionId)
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    true,
                                    configurationFile,
                                    sessionId,
                                    false,
                                    this.verbose,
                                    this.jniInterception,
                                    this.stack,
                                    this.samplingStack);
    }


    /**
     * Start the application in listen mode listening on the default
     * address and port.
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile listenMode()
    {
        return listenMode(DEFAULT_ADDRESS);
    }


    /**
     * Start the application in listen mode listening on the specified
     * {@link ListenAddress}.
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile listenMode(ListenAddress listenAddress)
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    listenAddress,
                                    false,
                                    this.configurationFile,
                                    this.sessionId,
                                    this.startSuspended,
                                    this.verbose,
                                    this.jniInterception,
                                    this.stack,
                                    this.samplingStack);
    }


    /**
     * Do not wait for JProfiler to connect when the application JVM starts.
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile noWait()
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    this.offline,
                                    this.configurationFile,
                                    this.sessionId,
                                    false,
                                    this.verbose,
                                    this.jniInterception,
                                    this.stack,
                                    this.samplingStack);
    }


    /**
     * Start the application suspended and wait for JProfiler to connect.
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile startSuspended()
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    this.offline,
                                    this.configurationFile,
                                    this.sessionId,
                                    true,
                                    this.verbose,
                                    this.jniInterception,
                                    this.stack,
                                    this.samplingStack);
    }


    /**
     * JProfiler will print the names of all instrumented classes to stderr.
     *
     * @param verbose  true if JProfiler should run in verbose mode
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile verbose(boolean verbose)
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    this.offline,
                                    this.configurationFile,
                                    this.sessionId,
                                    this.startSuspended,
                                    verbose,
                                    this.jniInterception,
                                    this.stack,
                                    this.samplingStack);
    }


    /**
     * Enable the detection of object allocations via JNI calls.
     *
     * @param jniInterception  flag indicating whether to enable JNI interception
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile jniInterception(boolean jniInterception)
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    this.offline,
                                    this.configurationFile,
                                    this.sessionId,
                                    this.startSuspended,
                                    this.verbose,
                                    jniInterception,
                                    this.stack,
                                    this.samplingStack);
    }


    /**
     * Set the maximum stack size for dynamic instrumentation.
     * Only change this parameter when JProfiler emits corresponding warning messages.
     * The default value is 10000
     *
     * @param stack  the maximum stack size for dynamic instrumentation
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile stack(int stack)
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    this.offline,
                                    this.configurationFile,
                                    this.sessionId,
                                    this.startSuspended,
                                    this.verbose,
                                    this.jniInterception,
                                    stack,
                                    this.samplingStack);
    }


    /**
     * Use the default stack size for dynamic instrumentation.
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile defaultStack()
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    this.offline,
                                    this.configurationFile,
                                    this.sessionId,
                                    this.startSuspended,
                                    this.verbose,
                                    this.jniInterception,
                                    null,
                                    this.samplingStack);
    }


    /**
     * Set the maximum stack size for sampling. Only change this parameter
     * when JProfiler emits corresponding warning messages.
     * The default value is 200.
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile samplingStack(int samplingStack)
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    this.offline,
                                    this.configurationFile,
                                    this.sessionId,
                                    this.startSuspended,
                                    this.verbose,
                                    this.jniInterception,
                                    this.stack,
                                    samplingStack);
    }


    /**
     *  Use the default stack size for sampling.
     *
     * @return this {@link JprofilerProfile} for fluent method chaining
     */
    public JprofilerProfile defaultSamplingStack()
    {
        return new JprofilerProfile(this.enabled,
                                    this.agentLibraryFile,
                                    this.listenAddress,
                                    this.offline,
                                    this.configurationFile,
                                    this.sessionId,
                                    this.startSuspended,
                                    this.verbose,
                                    this.jniInterception,
                                    this.stack,
                                    null);
    }


    @Override
    public void onLaunching(Platform platform,
                            Options  options)
    {
        MetaClass metaClass = options.getOrDefault(MetaClass.class, new JavaApplication.MetaClass());

        if (metaClass != null
            && JavaApplication.class.isAssignableFrom(metaClass.getImplementationClass(platform, options))
            && isEnabled())
        {
            StringBuilder agentLib = new StringBuilder("-agentpath:").append(agentLibraryFile).append('=');

            if (offline)
            {
                agentLib.append("offline,id=").append(sessionId).append(",nowait");

                if (configurationFile != null)
                {
                    agentLib.append(",config=").append(configurationFile);
                }
            }
            else
            {
                ListenAddress address = this.listenAddress == null
                                        ? options.get(ListenAddress.class) : this.listenAddress;

                if (address == null)
                {
                    address = DEFAULT_ADDRESS;
                }

                InetAddress listenAddress = address.getInetAddress();

                if (listenAddress != null)
                {
                    agentLib.append(",address=" + listenAddress.getHostName());
                }

                agentLib.append(",port=").append(address.getPort().get()).append(startSuspended ? "" : ",nowait");

                // replace the TransportAddress with the one we've resolved / created
                options.add(address);
            }

            if (verbose)
            {
                agentLib.append(",verbose-instr");
            }

            if (jniInterception)
            {
                agentLib.append(",jniInterception");
            }

            if (stack != null)
            {
                agentLib.append(",stack=").append(stack);
            }

            if (samplingStack != null)
            {
                agentLib.append(",samplingstack=").append(samplingStack);
            }

            // add the agent as a Freeform JvmOption
            options.add(new Freeform(agentLib.toString()));

            // disable waiting for the application to start if we're in suspend mode
            if (startSuspended)
            {
                options.add(WaitToStart.disabled());
            }
        }
    }


    @Override
    public void onLaunched(Platform    platform,
                           Application application,
                           Options     options)
    {
    }


    @Override
    public void onClosing(Platform    platform,
                          Application application,
                          Options     options)
    {
    }


    /**
     * Create a {@link JprofilerProfile} that is enabled and will use the
     * JProfiler agent at the specified location.
     *
     * @param agentLibraryFile  the location of the JProfiler agent
     *
     * @return an enabled {@link JprofilerProfile}
     */
    public static JprofilerProfile enabled(String agentLibraryFile)
    {
        return new JprofilerProfile(true, agentLibraryFile, null, false, null, 0, true, false, false, null, null);
    }


    /**
     * Create a {@link JprofilerProfile} that is enabled and will use the
     * JProfiler agent at the specified location. The {@link JprofilerProfile}
     * instance will also be configured with the "nowait" setting that means
     * the application will not wait for JProfiler to connect when the JVM starts.
     * This can be changed by calling {@link #startSuspended()}.
     *
     * @param agentLibraryFile  the location of the JProfiler agent
     *
     * @return an enabled {@link JprofilerProfile}
     */
    @Options.Default
    public static JprofilerProfile enabledNoWait(String agentLibraryFile)
    {
        return new JprofilerProfile(true, agentLibraryFile, null, false, null, 0, false, false, false, null, null);
    }


    /**
     * Create a {@link JprofilerProfile} that is disabled.
     *
     * @return a {@link JprofilerProfile} that is disabled.
     */
    public static JprofilerProfile disabled()
    {
        return new JprofilerProfile(false, null, null, false, null, 0, false, false, false, null, null);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        JprofilerProfile profile = (JprofilerProfile) o;

        if (enabled != profile.enabled)
        {
            return false;
        }

        if (startSuspended != profile.startSuspended)
        {
            return false;
        }

        if (offline != profile.offline)
        {
            return false;
        }

        if (sessionId != profile.sessionId)
        {
            return false;
        }

        if (verbose != profile.verbose)
        {
            return false;
        }

        if (jniInterception != profile.jniInterception)
        {
            return false;
        }

        if (agentLibraryFile != null
            ? !agentLibraryFile.equals(profile.agentLibraryFile) : profile.agentLibraryFile != null)
        {
            return false;
        }

        if (listenAddress != null ? !listenAddress.equals(profile.listenAddress) : profile.listenAddress != null)
        {
            return false;
        }

        if (configurationFile != null
            ? !configurationFile.equals(profile.configurationFile) : profile.configurationFile != null)
        {
            return false;
        }

        if (stack != null ? !stack.equals(profile.stack) : profile.stack != null)
        {
            return false;
        }

        return samplingStack != null ? samplingStack.equals(profile.samplingStack) : profile.samplingStack == null;

    }


    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder("JprofilerProfile(");

        if (enabled)
        {
            str.append("agentLibraryFile=").append(agentLibraryFile).append(", listenAddress=").append(listenAddress)
            .append(", offline=").append(offline).append(", configurationFile=").append(configurationFile)
            .append(", sessionId=").append(sessionId).append(", startSuspended=").append(startSuspended)
            .append(", verbose=").append(verbose).append(", jniInterception=").append(jniInterception)
            .append(", stack=")
            .append(stack == null ? "default" : stack).append(", samplingStack=")
            .append(samplingStack == null ? "default" : samplingStack);
        }
        else
        {
            str.append("disabled");
        }

        str.append(')');

        return str.toString();
    }


    @Override
    public int hashCode()
    {
        int result = (enabled ? 1 : 0);

        result = 31 * result + (agentLibraryFile != null ? agentLibraryFile.hashCode() : 0);
        result = 31 * result + (listenAddress != null ? listenAddress.hashCode() : 0);
        result = 31 * result + (startSuspended ? 1 : 0);
        result = 31 * result + (offline ? 1 : 0);
        result = 31 * result + (configurationFile != null ? configurationFile.hashCode() : 0);
        result = 31 * result + sessionId;
        result = 31 * result + (verbose ? 1 : 0);
        result = 31 * result + (jniInterception ? 1 : 0);
        result = 31 * result + (stack != null ? stack.hashCode() : 0);
        result = 31 * result + (samplingStack != null ? samplingStack.hashCode() : 0);

        return result;
    }


    /**
     * The listen address that the application will use to listen
     * for JProfiler connections.
     */
    public static class ListenAddress implements Option
    {
        /**
         * The optional address for listening for the JProfiler
         * (when null this implies all local addresses)
         */
        private InetAddress address;

        /**
         * The port for listening for JProfiler connections.
         */
        private Capture<Integer> port;


        /**
         * Constructs a local {@link ListenAddress}, choosing a port from the
         * {@link AvailablePortIterator}.
         *
         * @param ports  the available ports
         */
        public ListenAddress(AvailablePortIterator ports)
        {
            this(ports.getInetAddresses().iterator().next(), new Capture<>(ports));
        }


        /**
         * Constructs a local {@link ListenAddress} for the specified port.
         *
         * @param port  the port
         */
        public ListenAddress(Capture<Integer> port)
        {
            this(null, port);
        }


        /**
         * Constructs a local {@link ListenAddress} for the specified port.
         *
         * @param port  the port
         */
        public ListenAddress(int port)
        {
            this(null, new Capture<>(new PerpetualIterator<>(port)));
        }


        /**
         * Constructs a {@link ListenAddress} with the specified port.
         *
         * @param address  the address
         * @param port     the port
         */
        public ListenAddress(InetAddress      address,
                             Capture<Integer> port)
        {
            if (port == null)
            {
                new Capture<>(new PerpetualIterator<>(DEFAULT_PORT));
            }
            else
            {
                this.address = address;
                this.port    = port;
            }
        }


        /**
         * Constructs a {@link ListenAddress} with the specified port.
         *
         * @param address  the address
         * @param port     the port
         */
        public ListenAddress(InetAddress address,
                             int         port)
        {
            this(address, new Capture<>(new PerpetualIterator<>(port)));
        }


        /**
         * Obtains the address for the {@link JprofilerProfile}
         * listen address (which may be null).
         *
         * @return the {@link InetAddress}
         */
        public InetAddress getInetAddress()
        {
            return address;
        }


        /**
         * Obtains the port for the {@link JprofilerProfile} listen address.
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

            if (!(o instanceof ListenAddress))
            {
                return false;
            }

            ListenAddress other = (ListenAddress) o;

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
