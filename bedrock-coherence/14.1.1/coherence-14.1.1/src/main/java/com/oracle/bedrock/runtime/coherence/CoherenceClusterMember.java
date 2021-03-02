/*
 * File: CoherenceClusterMember.java
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

package com.oracle.bedrock.runtime.coherence;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.coherence.options.LocalHost;
import com.oracle.bedrock.runtime.coherence.options.MachineName;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.callable.RemoteCallableStaticMethod;
import com.oracle.bedrock.runtime.java.ContainerBasedJavaApplicationLauncher;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.container.ContainerClassLoader;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.Headless;
import com.oracle.bedrock.runtime.java.options.IPv4Preferred;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.util.Trilean;
import com.tangosol.net.NamedCache;
import com.tangosol.util.UID;

import java.io.NotSerializableException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * A specialized {@link JavaApplication} that represents an Oracle Coherence Cluster Member
 * at runtime.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface CoherenceClusterMember extends JavaApplication
{
    /**
     * Obtains the number of members in the cluster in which the member represented
     * by this {@link CoherenceClusterMember} belongs.
     *
     * @return the number of members in the cluster
     */
    int getClusterSize();


    /**
     * Obtains the local member id for the {@link CoherenceClusterMember}.
     *
     * @return the local member id
     */
    public int getLocalMemberId();


    /**
     * Obtains the local member {@link UID} for the {@link CoherenceClusterMember}.
     *
     * @return the local member {@link UID}
     */
    UID getLocalMemberUID();


    /**
     * Obtains the member {@link UID}s for the {@link CoherenceCluster} in which the
     * {@link CoherenceClusterMember} is operating.
     *
     * @return a {@link Set} of {@link UID}, one for each {@link CoherenceClusterMember}
     */
    Set<UID> getClusterMemberUIDs();


    /**
     * Obtains the role name for the local member.
     *
     * @return the role name
     */
    String getRoleName();


    /**
     * Obtains the site name for the local member.
     *
     * @return the site name
     */
    String getSiteName();


    /**
     * Obtains the cluster name for the local member.
     *
     * @return the site name
     */
    String getClusterName();


    /**
     * Obtains a proxy of the specified {@link NamedCache} available in the
     * {@link CoherenceClusterMember}.
     * <p>
     * WARNING: Some methods on the returned {@link NamedCache} proxy are not
     * available for remote execution and are thus not supported.  Attempts to
     * use such methods will thrown {@link UnsupportedOperationException}.
     * <p>
     * The unsupported methods are: getCacheService, addMapListener,
     * removeMapListener, entrySet, values, addIndex and removeIndex.
     * <p>
     * Additionally note that method invocations taking unserializable parameters
     * or returning unserializable values will throw {@link NotSerializableException}s.
     *
     * @param cacheName the name of the {@link NamedCache}
     *
     * @return a proxy to the {@link NamedCache}
     */
    NamedCache getCache(String cacheName);


    /**
     * Obtains a proxy of a strongly-typed {@link NamedCache} available in the
     * {@link CoherenceClusterMember}.
     * <p>
     * WARNING: Some methods on the returned {@link NamedCache} proxy are not
     * available for remote execution and are thus not supported.  Attempts to
     * use such methods will thrown {@link UnsupportedOperationException}.
     * <p>
     * The unsupported methods are: getCacheService, addMapListener,
     * removeMapListener, entrySet, values, addIndex and removeIndex.
     * <p>
     * Additionally note that method invocations taking unserializable parameters
     * or returning unserializable values will throw {@link NotSerializableException}s.
     *
     * @param cacheName   the name of the {@link NamedCache}
     * @param keyClass    the type of the keys for the {@link NamedCache}
     * @param valueClass  the type of the values for the {@link NamedCache}
     *
     * @param <K>         the type of the key class
     * @param <V>         the type of the value class
     *
     * @return  a proxy to the {@link NamedCache}
     */
    <K, V> NamedCache<K, V> getCache(String   cacheName,
                                     Class<K> keyClass,
                                     Class<V> valueClass);


    /**
     * Determines if a specified service is being run by the {@link CoherenceClusterMember}.
     *
     * @param serviceName the name of the service
     *
     * @return <code>true</code> if the service is running, <code>false</code> otherwise
     */
    boolean isServiceRunning(String serviceName);


    /**
     * Determines if a specified service is storage enabled.
     *
     * @param serviceName the name of the service
     *
     * @return a {@link Trilean} indicating if the service is storage enabled {@link Trilean#TRUE},
     *         disabled {@link Trilean#FALSE} or unknown / undefined / not applicable {@link Trilean#UNKNOWN}
     */
    Trilean isStorageEnabled(String serviceName);


    /**
     * Determines the status of a service being run by the {@link CoherenceClusterMember}.
     *
     * @param serviceName the name of the service
     *
     * @return the {@link ServiceStatus}
     */
    ServiceStatus getServiceStatus(String serviceName);


    /**
     * The {@link com.oracle.bedrock.runtime.MetaClass} for {@link CoherenceClusterMember}s.
     */
    class MetaClass implements com.oracle.bedrock.runtime.MetaClass<CoherenceClusterMember>,
                               ContainerBasedJavaApplicationLauncher.ApplicationController
    {
        /**
         * The com.tangosol.net.DefaultCacheServer classname.
         */
        public static final String DEFAULT_CACHE_SERVER_CLASSNAME = "com.tangosol.net.DefaultCacheServer";

        /**
         * The com.tangosol.net.CacheFactory classname.
         */
        public static final String CACHE_FACTORY_CLASSNAME = "com.tangosol.net.CacheFactory";


        /**
         * Constructs a {@link MetaClass} for a {@link CoherenceClusterMember}.
         */
        @OptionsByType.Default
        public MetaClass()
        {
        }


        @Override
        public Class<? extends CoherenceClusterMember> getImplementationClass(Platform      platform,
                                                                              OptionsByType optionsByType)
        {
            return CoherenceCacheServer.class;
        }


        @Override
        public void onLaunching(Platform      platform,
                                OptionsByType optionsByType)
        {
            // automatically define the default cache server as the default class
            optionsByType.addIfAbsent(ClassName.of(DEFAULT_CACHE_SERVER_CLASSNAME));

            // automatically define IPv4
            optionsByType.addIfAbsent(IPv4Preferred.yes());

            // cache servers are always headless
            optionsByType.add(Headless.enabled());

            SystemProperties systemProperties = optionsByType.get(SystemProperties.class);

            systemProperties = systemProperties.addIfAbsent(SystemProperty.of(LocalHost.PROPERTY,
                                                                              new SystemProperty.ContextSensitiveValue()
                                                                              {
                                                                                  @Override
                                                                                  public Object resolve(String        name,
                                                                                                        Platform      platform,
                                                                                                        OptionsByType optionsByType)
                                                                                  {
                                                                                      if (platform
                                                                                          instanceof RemotePlatform)
                                                                                      {
                                                                                          InetAddress inetAddress =
                                                                                              platform.getAddress();

                                                                                          if (inetAddress == null)
                                                                                          {
                                                                                              return null;    // property doesn't exist
                                                                                          }
                                                                                          else
                                                                                          {
                                                                                              return inetAddress.getHostAddress();
                                                                                          }
                                                                                      }
                                                                                      else
                                                                                      {
                                                                                          return null;    // property doesn't exist
                                                                                      }

                                                                                  }
                                                                              }));

            systemProperties = systemProperties.addIfAbsent(SystemProperty.of(MachineName.PROPERTY,
                                                                              new SystemProperty.ContextSensitiveValue()
                                                                              {
                                                                                  @Override
                                                                                  public Object resolve(String        name,
                                                                                                        Platform      platform,
                                                                                                        OptionsByType optionsByType)
                                                                                  {
                                                                                      if (platform
                                                                                          instanceof RemotePlatform)
                                                                                      {
                                                                                          return platform.getName();
                                                                                      }
                                                                                      else
                                                                                      {
                                                                                          return null;
                                                                                      }
                                                                                  }
                                                                              }));

            // update the system properties as it may have been modified
            optionsByType.add(systemProperties);
        }


        @Override
        public void onLaunch(Platform      platform,
                             OptionsByType optionsByType)
        {
            // there's nothing to do before launching the application
        }


        @Override
        public void onLaunched(Platform               platform,
                               CoherenceClusterMember member,
                               OptionsByType          optionsByType)
        {
            // nothing to do after launch
        }


        @Override
        public CompletableFuture<Void> start(ContainerBasedJavaApplicationLauncher.ControllableApplication application)
        {
            RemoteCallable<Void> callable = new RemoteCallableStaticMethod<>(DEFAULT_CACHE_SERVER_CLASSNAME, "start");

            return application.submit(callable);
        }


        @Override
        public CompletableFuture<Void> destroy(ContainerBasedJavaApplicationLauncher
            .ControllableApplication application)
        {
            RemoteCallable<Void> callable = new RemoteCallableStaticMethod<>(DEFAULT_CACHE_SERVER_CLASSNAME,
                                                                             "shutdown");

            return application.submit(callable);
        }


        @Override
        public void configure(ContainerClassLoader containerClassLoader,
                              PipedOutputStream    pipedOutputStream,
                              PipedInputStream     pipedInputStream,
                              OptionsByType        optionsByType)
        {
            ClassName className = optionsByType.getOrSetDefault(ClassName.class,
                                                                ClassName.of(DEFAULT_CACHE_SERVER_CLASSNAME));

            ContainerBasedJavaApplicationLauncher.configureRemoteChannel(containerClassLoader,
                                                                         pipedOutputStream,
                                                                         pipedInputStream,
                                                                         className.getName());
        }
    }
}
