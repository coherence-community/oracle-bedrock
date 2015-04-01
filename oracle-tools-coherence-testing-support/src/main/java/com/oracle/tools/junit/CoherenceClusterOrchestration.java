/*
 * File: CoherenceClusterOrchestration.java
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

package com.oracle.tools.junit;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationConsoleBuilder;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.coherence.CoherenceCacheServerSchema;
import com.oracle.tools.runtime.coherence.CoherenceCluster;
import com.oracle.tools.runtime.coherence.CoherenceClusterBuilder;
import com.oracle.tools.runtime.coherence.CoherenceClusterMember;
import com.oracle.tools.runtime.coherence.FluentCoherenceClusterSchema;
import com.oracle.tools.runtime.coherence.callables.GetAutoStartServiceNames;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.options.HeapSize;
import com.oracle.tools.runtime.java.options.HotSpot;

import com.oracle.tools.runtime.options.ApplicationClosingBehavior;

import com.oracle.tools.util.Capture;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.CacheFactoryBuilder;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultCacheFactoryBuilder;
import com.tangosol.net.DefaultConfigurableCacheFactory;

import org.junit.rules.ExternalResource;

import org.junit.runner.Description;

import org.junit.runners.model.Statement;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.core.Is.is;

import java.net.InetAddress;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A JUnit {@link ExternalResource} to represent and orchestrate configuring,
 * establishing, accessing and tearing down a Oracle Coherence Cluster, with
 * support for requesting various ConfigurableCacheFactory implementations to
 * interact with the orchestrated Coherence Cluster.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 * @author Harvey Raja
 * @author Aleks Seovic
 */
public class CoherenceClusterOrchestration extends ExternalResource
    implements FluentCoherenceClusterSchema<CoherenceClusterOrchestration>
{
    // TODO: Introduce the ability to specify Coherence Closing/Shutdown Options (CacheFactory.shutdown + System.exit / System.halt)

    // TODO: Ensure the start up / shutdown sequence of Coherence Members matches Coherence Abstract Functional Test

    /**
     * The {@link LocalPlatform} on which the Coherence Cluster will be orchestrated.
     */
    private LocalPlatform platform;

    /**
     * The {@link CoherenceCacheServerSchema} to use as a basis for
     * constructing a variety of {@link CoherenceClusterMember}s.
     */
    private CoherenceCacheServerSchema commonServerSchema;

    /**
     * The {@link Options} to use when creating the {@link CoherenceCluster}.
     */
    private Options clusterCreationOptions;

    /**
     * The {@link Options} to use when closing the {@link CoherenceCluster}.
     */
    private Options clusterClosingOptions;

    /**
     * The {@link ApplicationConsoleBuilder} to use for constructing
     * {@link ApplicationConsole}s for each {@link CoherenceClusterMember}.
     */
    private ApplicationConsoleBuilder consoleBuilder;

    /**
     * The {@link CoherenceCluster} established by the orchestrator.
     */
    private CoherenceCluster cluster;

    /**
     * The {@link ConfigurableCacheFactory} sessions that have been
     * created against the orchestrated {@link CoherenceCluster}.
     */
    private HashMap<SessionBuilder, ConfigurableCacheFactory> sessions;

    // the Coherence Cluster Port
    private Capture<Integer> clusterPort;

    // the Coherence *Extend port
    private Capture<Integer> extendPort;


    /**
     * Constructs a {@link CoherenceClusterOrchestration}.
     */
    public CoherenceClusterOrchestration()
    {
        // we're going to orchestrate the cluster using the LocalPlatform
        this.platform = LocalPlatform.getInstance();

        // establish a Cluster port
        this.clusterPort = new Capture<>(platform.getAvailablePorts());

        // establish the Extend port
        this.extendPort = new Capture<>(platform.getAvailablePorts());

        // establish a common server schema on which to base storage enabled and proxy members
        this.commonServerSchema = new CoherenceCacheServerSchema();

        // establish the common server schema address and port details
        String hostAddress = platform.getLoopbackAddress().getHostAddress();

        commonServerSchema.setLocalHostAddress(hostAddress);
        commonServerSchema.setClusterPort(clusterPort);
        commonServerSchema.setMulticastTTL(0);

        // we also define the proxy configuration (this will only be used if it's enabled)
        commonServerSchema.setSystemProperty("tangosol.coherence.extend.address", hostAddress);
        commonServerSchema.setSystemProperty("tangosol.coherence.extend.port", extendPort);

        // establish default java process configuration
        commonServerSchema.setHeadless(true);
        commonServerSchema.addOption(HotSpot.Mode.SERVER);
        commonServerSchema.addOption(HeapSize.of(256, HeapSize.Units.MB, 1024, HeapSize.Units.MB));

        // by default we'll use the SystemApplicationConsole
        this.consoleBuilder = SystemApplicationConsole.builder();

        // by default we don't have a cluster
        this.cluster = null;

        // by default the creation and closing options aren't set
        this.clusterCreationOptions = new Options();
        this.clusterClosingOptions  = new Options();

        // by default we have no sessions
        this.sessions = new HashMap<>();
    }


    @Override
    public Statement apply(Statement   base,
                           Description description)
    {
        // automatically set the cluster name to the test class name
        // if the cluster name isn't configured
        if (commonServerSchema.getClusterName() == null)
        {
            commonServerSchema.setClusterName(description.getClassName());
        }

        return super.apply(base, description);
    }


    @Override
    protected void before() throws Throwable
    {
        // establish a CoherenceClusterBuilder with the required configuration
        CoherenceClusterBuilder clusterBuilder = new CoherenceClusterBuilder();

        // define the schema for the storage enabled members of the cluster
        CoherenceCacheServerSchema storageServerSchema = new CoherenceCacheServerSchema(commonServerSchema);

        storageServerSchema.setRoleName("storage");
        storageServerSchema.setStorageEnabled(true);

        int storageMemberCount = 2;

        clusterBuilder.addSchema("storage",
                                 storageServerSchema,
                                 storageMemberCount,
                                 consoleBuilder,
                                 platform,
                                 clusterCreationOptions.asArray());

        // define the schema for the proxy enabled members of the cluster
        CoherenceCacheServerSchema proxyServerSchema = new CoherenceCacheServerSchema(commonServerSchema);

        proxyServerSchema.setRoleName("proxy");
        proxyServerSchema.setStorageEnabled(false);
        proxyServerSchema.setSystemProperty("tangosol.coherence.extend.enabled", true);

        int proxyMemberCount = 1;

        clusterBuilder.addSchema("proxy",
                                 proxyServerSchema,
                                 proxyMemberCount,
                                 consoleBuilder,
                                 platform,
                                 clusterCreationOptions.asArray());

        int preferredClusterSize = storageMemberCount + proxyMemberCount;

        // establish the cluster
        cluster = clusterBuilder.realize();

        // ensure that the cluster has been orchestrated correctly
        Eventually.assertThat(invoking(cluster).getClusterSize(), is(preferredClusterSize));

        // ensure that all services marked as autostart on the proxy have started
        CoherenceClusterMember proxyServer     = cluster.get("proxy-1");

        Set<String>            setServiceNames = proxyServer.submit(new GetAutoStartServiceNames());

        for (String sServiceName : setServiceNames)
        {
            Eventually.assertThat(invoking(proxyServer).isServiceRunning(sServiceName), is(true));
        }

        // let the super-class perform it's initialization
        super.before();
    }


    @Override
    protected void after()
    {
        // clean up the sessions
        synchronized (sessions)
        {
            for (ConfigurableCacheFactory session : sessions.values())
            {
                CacheFactory.getCacheFactoryBuilder().release(session);
            }
        }

        // close the cluster
        cluster.close(clusterClosingOptions.asArray());

        // let the super-class perform it's cleanup as well
        super.after();
    }


    /**
     * Sets the {@link ApplicationConsoleBuilder} to use for constructing
     * {@link ApplicationConsole}s for each {@link CoherenceClusterMember}
     * that is part of the orchestrated {@link CoherenceCluster}.
     *
     * @param consoleBuilder  the {@link ApplicationConsoleBuilder}
     *
     * @return  the {@link CoherenceClusterOrchestration} to permit fluent-style method-calls
     */
    public CoherenceClusterOrchestration setConsoleBuilder(ApplicationConsoleBuilder consoleBuilder)
    {
        this.consoleBuilder = consoleBuilder;

        return this;
    }


    /**
     * Sets the {@link Option}s to be used when closing the {@link CoherenceCluster}.
     *
     * @param options  the {@link Option}s
     *
     * @see ApplicationClosingBehavior
     *
     * @return  the {@link CoherenceClusterOrchestration} to permit fluent-style method-calls
     */
    public CoherenceClusterOrchestration setClusterClosingOptions(Option... options)
    {
        this.clusterClosingOptions = new Options(options);

        return this;
    }


    /**
     * Sets the {@link Option}s to be used when creating the {@link CoherenceCluster}.
     *
     * @param options  the {@link Option}s
     *
     * @return  the {@link CoherenceClusterOrchestration} to permit fluent-style method-calls
     */
    public CoherenceClusterOrchestration setClusterCreationOptions(Option... options)
    {
        this.clusterCreationOptions = new Options(options);

        return this;
    }


    /**
     * Sets the specified system property.
     *
     * @param name   the name of the system property
     * @param value  the value for the system property
     *
     * @return  the {@link CoherenceClusterOrchestration} to permit fluent-style method-calls
     */
    public CoherenceClusterOrchestration setSystemProperty(String name,
                                                           Object value)
    {
        this.commonServerSchema.setSystemProperty(name, value);

        return this;
    }


    /**
     * Obtains the {@link CoherenceCluster} that was established by the orchestration.
     *
     * @return  the {@link CoherenceCluster}
     */
    public CoherenceCluster getCluster()
    {
        return cluster;
    }


    @Override
    public CoherenceClusterOrchestration setClusterName(String name)
    {
        commonServerSchema.setClusterName(name);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setClusterPort(int port)
    {
        commonServerSchema.setClusterPort(port);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setClusterPort(Iterator<Integer> ports)
    {
        commonServerSchema.setClusterPort(ports);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setMulticastTTL(int ttl)
    {
        commonServerSchema.setMulticastTTL(ttl);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setSiteName(String name)
    {
        commonServerSchema.setSiteName(name);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setWellKnownAddress(String address)
    {
        commonServerSchema.setWellKnownAddress(address);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setWellKnownAddressPort(int port)
    {
        commonServerSchema.setWellKnownAddressPort(port);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setWellKnownAddressPort(Iterator<Integer> ports)
    {
        commonServerSchema.setWellKnownAddressPort(ports);

        return this;
    }


    @Override
    public String getClusterName()
    {
        return commonServerSchema.getClusterName();
    }


    @Override
    public int getMulticastTTL()
    {
        return commonServerSchema.getMulticastTTL();
    }


    @Override
    public String getSiteName()
    {
        return commonServerSchema.getSiteName();
    }


    @Override
    public String getWellKnownAddress()
    {
        return commonServerSchema.getWellKnownAddress();
    }


    @Override
    public CoherenceClusterOrchestration setCacheConfigURI(String cacheConfigURI)
    {
        commonServerSchema.setCacheConfigURI(cacheConfigURI);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setLog(String destination)
    {
        commonServerSchema.setLog(destination);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setLogLevel(int level)
    {
        commonServerSchema.setLogLevel(level);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setOperationalOverrideURI(String operationalOverrideURI)
    {
        commonServerSchema.setOperationalOverrideURI(operationalOverrideURI);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setPofConfigURI(String pofConfigURI)
    {
        commonServerSchema.setPofConfigURI(pofConfigURI);
        commonServerSchema.setPofEnabled(true);

        return this;
    }


    @Override
    public CoherenceClusterOrchestration setPofEnabled(boolean isEnabled)
    {
        commonServerSchema.setPofEnabled(isEnabled);

        return this;
    }


    @Override
    public String getCacheConfigURI()
    {
        return commonServerSchema.getCacheConfigURI();
    }


    @Override
    public String getLog()
    {
        return commonServerSchema.getLog();
    }


    @Override
    public int getLogLevel()
    {
        return commonServerSchema.getLogLevel();
    }


    @Override
    public String getOperationalOverrideURI()
    {
        return commonServerSchema.getOperationalOverrideURI();
    }


    @Override
    public String getPofConfigURI()
    {
        return commonServerSchema.getPofConfigURI();
    }


    @Override
    public boolean isPofEnabled()
    {
        return commonServerSchema.isPofEnabled();
    }


    /**
     * Obtains the {@link LocalPlatform} on which the {@link CoherenceClusterOrchestration} will
     * create sessions.
     * <p>
     * NOTE: The orchestrated {@link CoherenceCluster} may be orchestrated on one or more other
     * {@link Platform}s.
     *
     * @return  the {@link LocalPlatform}
     */
    public LocalPlatform getLocalPlatform()
    {
        return platform;
    }


    /**
     * Obtains a session (represented as a {@link ConfigurableCacheFactory)} against the
     * orchestrated Coherence Cluster for interacting with Coherence.
     * <p>
     * Attempts to request a session multiple times with the same {@link SessionBuilder}
     * will return the same session.
     *
     * @param builder  the builder for the specific type of session
     *
     * @return  a {@link ConfigurableCacheFactory} representing the Coherence Session.
     *
     * @throws IllegalStateException  when an attempt to request sessions for
     *                                different {@link SessionBuilder}s is made
     */
    public synchronized ConfigurableCacheFactory getSessionFor(SessionBuilder builder)
    {
        ConfigurableCacheFactory session = sessions.get(builder);

        if (session == null)
        {
            CoherenceCacheServerSchema sessionSchema = new CoherenceCacheServerSchema(commonServerSchema);

            sessionSchema.setRoleName("client");
            sessionSchema.setStorageEnabled(false);

            session = builder.realize(platform, this, sessionSchema);

            sessions.put(builder, session);
        }

        return session;
    }
}
