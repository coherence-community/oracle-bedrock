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
import com.oracle.tools.predicate.Predicate;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.actions.Block;
import com.oracle.tools.runtime.actions.InteractiveActionExecutor;
import com.oracle.tools.runtime.coherence.CoherenceCluster;
import com.oracle.tools.runtime.coherence.CoherenceClusterBuilder;
import com.oracle.tools.runtime.coherence.CoherenceClusterMember;
import com.oracle.tools.runtime.coherence.ServiceStatus;
import com.oracle.tools.runtime.coherence.actions.RestartCoherenceClusterMemberAction;
import com.oracle.tools.runtime.coherence.callables.GetAutoStartServiceNames;
import com.oracle.tools.runtime.coherence.callables.GetServiceStatus;
import com.oracle.tools.runtime.coherence.options.ClusterName;
import com.oracle.tools.runtime.coherence.options.ClusterPort;
import com.oracle.tools.runtime.coherence.options.LocalStorage;
import com.oracle.tools.runtime.coherence.options.Multicast;
import com.oracle.tools.runtime.coherence.options.RoleName;
import com.oracle.tools.runtime.console.SystemApplicationConsole;
import com.oracle.tools.runtime.java.options.Headless;
import com.oracle.tools.runtime.java.options.HeapSize;
import com.oracle.tools.runtime.java.options.HotSpot;
import com.oracle.tools.runtime.java.options.SystemProperty;
import com.oracle.tools.runtime.options.ApplicationClosingBehavior;
import com.oracle.tools.runtime.options.DisplayName;
import com.oracle.tools.util.Capture;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.ConfigurableCacheFactory;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Set;

import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static org.hamcrest.core.Is.is;

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
{
    /**
     * The number of storage enable members that will be started in the cluster.
     */
    private int storageMemberCount = 2;

    /**
     * The {@link LocalPlatform} on which the Coherence Cluster will be orchestrated.
     */
    private LocalPlatform platform;

    /**
     * The {@link Options} to use as a basis for constructing a variety of {@link CoherenceClusterMember}s.
     */
    private Options commonMemberOptions;

    /**
     * The {@link Options} to use as a basis for constructing storage-enabled {@link CoherenceClusterMember}s.
     */
    private Options storageMemberOptions;

    /**
     * The {@link Options} to use as a basis for constructing proxy {@link CoherenceClusterMember}s.
     */
    private Options proxyMemberOptions;

    /**
     * The {@link Options} to use when creating the {@link CoherenceCluster}.
     */
    private Options clusterCreationOptions;

    /**
     * The {@link Options} to use when closing the {@link CoherenceCluster}.
     */
    private Options clusterClosingOptions;

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
        this.platform = LocalPlatform.get();

        // establish a Cluster port
        this.clusterPort = new Capture<>(platform.getAvailablePorts());

        // establish the Extend port (is the same as the cluster port)
        this.extendPort = clusterPort;

        // establish a common member options on which to base storage enabled and proxy members
        this.commonMemberOptions = new Options();

        // establish the common server schema address and port details
        String hostAddress = platform.getLoopbackAddress().getHostAddress();

        // TODO: remove when NameService respects localhost system property
        // this.commonMemberOptions.add(LocalHost.of(hostAddress));
        this.commonMemberOptions.add(ClusterPort.of(clusterPort));
        this.commonMemberOptions.add(Multicast.ttl(0));

        // we also define the proxy configuration (this will only be used if it's enabled)
        this.commonMemberOptions.add(SystemProperty.of("tangosol.coherence.extend.address", hostAddress));
        this.commonMemberOptions.add(SystemProperty.of("tangosol.coherence.extend.port", extendPort));

        // establish default java process configuration
        this.commonMemberOptions.add(Headless.enabled());
        this.commonMemberOptions.add(HotSpot.Mode.SERVER);
        this.commonMemberOptions.add(HeapSize.of(256, HeapSize.Units.MB, 1024, HeapSize.Units.MB));

        // by default we'll use the SystemApplicationConsole
        this.commonMemberOptions.add(SystemApplicationConsole.builder());

        // by default we don't have any special options for storage or proxy members
        this.storageMemberOptions = new Options();
        this.proxyMemberOptions   = new Options();

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
        if (commonMemberOptions.get(ClusterName.class) == null)
        {
            commonMemberOptions.add(ClusterName.of(description.getClassName()));
        }

        return super.apply(base, description);
    }


    @Override
    protected void before() throws Throwable
    {
        // establish a CoherenceClusterBuilder with the required configuration
        CoherenceClusterBuilder clusterBuilder = new CoherenceClusterBuilder();

        // define the options for the storage enabled members of the cluster
        Options storageServerOptions = createStorageEnabledMemberOptions();

        storageServerOptions.addAll(clusterCreationOptions);

        clusterBuilder.include(storageMemberCount,
                               platform,
                               CoherenceClusterMember.class,
                               storageServerOptions.asArray());

        // define the schema for the proxy enabled members of the cluster
        Options proxyServerOptions = createProxyServerOptions();

        proxyServerOptions.addAll(clusterCreationOptions);

        int proxyMemberCount = 1;

        clusterBuilder.include(proxyMemberCount, platform, CoherenceClusterMember.class, proxyServerOptions.asArray());

        int preferredClusterSize = storageMemberCount + proxyMemberCount;

        // establish the cluster
        cluster = clusterBuilder.build();

        // ensure that the cluster has been orchestrated correctly
        Eventually.assertThat(invoking(cluster).getClusterSize(), is(preferredClusterSize));

        // ensure that all services marked as autostart on the proxy have started
        CoherenceClusterMember proxyServer     = cluster.get("proxy-1");

        Set<String>            setServiceNames = proxyServer.invoke(new GetAutoStartServiceNames());

        for (String sServiceName : setServiceNames)
        {
            Eventually.assertThat(invoking(proxyServer).isServiceRunning(sServiceName), is(true));
        }

        // let's ensure that we don't have a local cluster member
        CacheFactory.setCacheFactoryBuilder(null);

        CacheFactory.shutdown();

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
                session.dispose();

                CacheFactory.getCacheFactoryBuilder().release(session);
            }
        }

        CacheFactory.shutdown();

        CacheFactory.setCacheFactoryBuilder(null);

        // close the cluster
        cluster.close(clusterClosingOptions.asArray());

        // let the super-class perform it's cleanup as well
        super.after();
    }


    protected Options createStorageEnabledMemberOptions()
    {
        // define the options for the storage enabled members of the cluster
        Options options = new Options(commonMemberOptions);

        options.add(DisplayName.of("storage"));
        options.add(RoleName.of("storage"));
        options.add(LocalStorage.enabled());

        options.addAll(storageMemberOptions);

        return options;
    }


    protected Options createProxyServerOptions()
    {
        Options options = new Options(commonMemberOptions);

        options.add(DisplayName.of("proxy"));
        options.add(RoleName.of("proxy"));
        options.add(LocalStorage.disabled());
        options.add(SystemProperty.of("tangosol.coherence.extend.enabled", true));

        options.addAll(proxyMemberOptions);

        return options;
    }


    /**
     * Sets the {@link Option}s to be used when closing the {@link CoherenceCluster}.
     *
     * @param options the {@link Option}s
     *
     * @return the {@link CoherenceClusterOrchestration} to permit fluent-style method-calls
     *
     * @see ApplicationClosingBehavior
     */
    public CoherenceClusterOrchestration withClosingOptions(Option... options)
    {
        this.clusterClosingOptions = new Options(options);

        return this;
    }


    /**
     * Sets the {@link Option}s to be passed to the {@link CoherenceClusterBuilder} when building the
     * {@link CoherenceCluster}.
     *
     * @param options the {@link Option}s
     *
     * @return the {@link CoherenceClusterOrchestration} to permit fluent-style method-calls
     */
    public CoherenceClusterOrchestration withBuilderOptions(Option... options)
    {
        this.clusterCreationOptions = new Options(options);

        return this;
    }


    /**
     * Obtains the {@link CoherenceCluster} that was established by the orchestration.
     *
     * @return the {@link CoherenceCluster}
     */
    public CoherenceCluster getCluster()
    {
        return cluster;
    }


    /**
     * Obtains the {@link LocalPlatform} on which the {@link CoherenceClusterOrchestration} will
     * create sessions.
     * <p>
     * NOTE: The orchestrated {@link CoherenceCluster} may be orchestrated on one or more other
     * {@link Platform}s.
     *
     * @return the {@link LocalPlatform}
     */
    public LocalPlatform getLocalPlatform()
    {
        return platform;
    }


    /**
     * Adds the specified {@link Option}s to the {@link CoherenceClusterOrchestration} for
     * launching all {@link CoherenceClusterMember}s.
     *
     * @param options  the {@link Option}s
     *
     * @return  the {@link CoherenceClusterOrchestration}
     */
    public CoherenceClusterOrchestration withOptions(Option... options)
    {
        commonMemberOptions.addAll(options);

        return this;
    }


    /**
     * Adds the specified {@link Option}s to the {@link CoherenceClusterOrchestration} for
     * launching all storage-enabled {@link CoherenceClusterMember}s.
     *
     * @param options  the {@link Option}s for storage-enabled {@link CoherenceClusterMember}s
     *
     * @return  the {@link CoherenceClusterOrchestration}
     */
    public CoherenceClusterOrchestration withStorageMemberOptions(Option... options)
    {
        storageMemberOptions.addAll(options);

        return this;
    }


    /**
     * Adds the specified {@link Option}s to the {@link CoherenceClusterOrchestration} for
     * launching all proxy server {@link CoherenceClusterMember}s.
     *
     * @param options  the {@link Option}s for proxy server {@link CoherenceClusterMember}s
     *
     * @return  the {@link CoherenceClusterOrchestration}
     */
    public CoherenceClusterOrchestration withProxyMemberOptions(Option... options)
    {
        proxyMemberOptions.addAll(options);

        return this;
    }


    /**
     * Obtains a session (represented as a {@link ConfigurableCacheFactory)} against the
     * orchestrated Coherence Cluster for interacting with Coherence.
     * <p>
     * Only a single session may be created by a {@link CoherenceClusterOrchestration}
     * against an orchestrated {@link CoherenceCluster}.
     * <p>
     * Attempts to request a session multiple times with the same {@link SessionBuilder}
     * will return the same session.
     *
     * @param builder the builder for the specific type of session
     *
     * @return a {@link ConfigurableCacheFactory} representing the Coherence Session.
     *
     * @throws IllegalStateException when an attempt to request sessions for
     *                               different {@link SessionBuilder}s is made
     */
    public synchronized ConfigurableCacheFactory getSessionFor(SessionBuilder builder)
    {
        ConfigurableCacheFactory session = sessions.get(builder);

        if (session == null)
        {
            Options options = new Options(commonMemberOptions);

            options.add(RoleName.of("client"));
            options.add(LocalStorage.disabled());

            session = builder.build(platform, this, options.asArray());

            sessions.put(builder, session);
        }

        return session;
    }


    /**
     * Sets the number of storage enabled members that will be started in the cluster.
     *
     * @param count the number of storage enabled members
     *
     * @return the {@link CoherenceClusterOrchestration}
     */
    public CoherenceClusterOrchestration setStorageMemberCount(int count)
    {
        storageMemberCount = count;

        return this;
    }


    /**
     * Perform a rolling restart of all of the storage members of the cluster.
     * <p>
     * This method performs a safe rolling restart, ensuring that the distributed
     * cache services are in a "safe" state before restarting the next member.
     * <p>
     * Rolling restart is not supported on a cluster with only a single storage
     * enabled member.
     *
     * @param options the {@link Option}s to use when realizing each new {@link CoherenceClusterMember}
     *
     * @throws IllegalStateException if the cluster has only a single storage member.
     */
    public void restartStorageMembers(Option... options)
    {
        Predicate<CoherenceClusterMember> predicate = new Predicate<CoherenceClusterMember>()
        {
            @Override
            public boolean evaluate(CoherenceClusterMember member)
            {
                Set<String> setServiceNames = member.invoke(new GetAutoStartServiceNames());

                for (String sServiceName : setServiceNames)
                {
                    ServiceStatus status = member.invoke(new GetServiceStatus(sServiceName));

                    if (status == ServiceStatus.ENDANGERED
                        || status == ServiceStatus.ORPHANED
                        || status == ServiceStatus.STOPPED
                        || status == ServiceStatus.UNKNOWN)
                    {
                        return false;
                    }
                }

                return true;
            }
        };

        restartStorageMembers(predicate, options);
    }


    /**
     * Perform a rolling restart of all of the storage members of the cluster.
     * <p>
     * Rolling restart is not supported on a cluster with only a single storage
     * enabled member.
     *
     * @param predicate the {@link Predicate} that must evaluate to true before
     *                  each member of the cluster is restarted.
     * @param options   the {@link Option}s to use when realizing each new {@link CoherenceClusterMember}
     *
     * @throws IllegalStateException if the cluster has only a single storage member.
     */
    public void restartStorageMembers(Predicate<CoherenceClusterMember> predicate,
                                      Option...                         options)
    {
        if (storageMemberCount < 2)
        {
            throw new IllegalStateException("Cannot perform a rolling restart in a cluster with less than two "
                                            + "storage enabled members");
        }

        Block block = new Block();

        for (int i = 1; i <= storageMemberCount; i++)
        {
            Options launchOptions = createStorageEnabledMemberOptions();

            launchOptions.addAll(options);

            block.add(new RestartCoherenceClusterMemberAction("storage", predicate, platform, launchOptions.asArray()));
        }

        InteractiveActionExecutor<CoherenceClusterMember, CoherenceCluster> executor =
            new InteractiveActionExecutor<>(cluster,
                                            block);

        executor.executeAll();
    }
}
