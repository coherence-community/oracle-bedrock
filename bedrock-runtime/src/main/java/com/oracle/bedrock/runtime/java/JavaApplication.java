/*
 * File: JavaApplication.java
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

package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.callable.RemoteMethodInvocation;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;

import java.io.NotSerializableException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * An {@link Application} specifically representing Java-based applications.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Application
 * @see JavaApplicationLauncher
 */
public interface JavaApplication extends Application, RemoteChannel
{
    /**
     * The java.home JVM property.
     */
    public static final String JAVA_HOME = "java.home";

    /**
     * The java.rmi.server.hostname JVM property.
     */
    public static final String JAVA_RMI_SERVER_HOSTNAME = "java.rmi.server.hostname";

    /**
     * The java.net.preferIPv4Stack JVM property (false by default in most JVMs)
     */
    public static final String JAVA_NET_PREFER_IPV4_STACK = "java.net.preferIPv4Stack";

    /**
     * The java.net.preferIP64Stack JVM property
     */
    public static final String JAVA_NET_PREFER_IPV6_STACK = "java.net.preferIPv6Stack";


    /**
     * Obtains the resolved System {@link Properties} that were provided to the {@link JavaApplication}
     * when it was launched.
     *
     * @return a {@link Properties} of name value pairs, each one representing
     *         a system property provided to the {@link JavaApplication} as
     *         -Dname=value parameters when it was realized
     */
    public Properties getSystemProperties();


    /**
     * Obtains the current value for a system property.
     *
     * @param name  the name of the system property
     *
     * @return the value of the defined system property, or <code>null</code>
     *         if undefined
     */
    public String getSystemProperty(String name);


    /**
     * Invokes the {@link RemoteCallable} in the {@link JavaApplication},
     * waiting the default or provided {@link Timeout} for the result.
     *
     * @param callable  the {@link RemoteCallable}
     * @param options   the {@link Option}s
     *
     * @param <T>       the type of the result
     * @return          the value from the {@link RemoteCallable}
     */
    public default <T> T invoke(RemoteCallable<T> callable,
                                Option...         options)
    {
        // determine the timeout
        Options              invokeOptions = new Options(options);
        Timeout              timeout       = invokeOptions.getOrDefault(Timeout.class, getOptions().get(Timeout.class));

        CompletableFuture<T> future        = submit(callable, options);

        try
        {
            return future.get(timeout.to(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    /**
     * Creates a local proxy of an application owned instance, afterwards the proxy may
     * be used to interact with the application owned instance.
     * <p>
     * Note:  Only methods of the proxy interface that have serializable method parameters
     * and return values should be called using the returned proxy.  Attempts to invoke
     * methods with non-serializable parameters or methods returning non-serializable results
     * may result in {@link NotSerializableException}s being thrown. (depending on
     * the ability for a provided interceptor to transform said values)
     *
     * @param <T>  the type of the proxy
     *
     * @param classToProxy      the class of the proxy
     * @param instanceProducer  a {@link RemoteCallable} that will provide the application instance
     *                          to which proxy method calls should be invoked
     * @param interceptor       an optional (may be null) interceptor to transform values
     *                          used and returned by the proxy
     * @return  a proxy of an application instance
     */
    public <T> T getProxyFor(Class<T>                           classToProxy,
                             RemoteCallable<T>                  instanceProducer,
                             RemoteMethodInvocation.Interceptor interceptor);


    /**
     * The {@link com.oracle.bedrock.runtime.MetaClass} for {@link JavaApplication}s.
     */
    class MetaClass implements com.oracle.bedrock.runtime.MetaClass<JavaApplication>
    {
        /**
         * Constructs a {@link MetaClass} for a {@link JavaApplication}.
         */
        @Options.Default
        public MetaClass()
        {
        }


        @Override
        public Class<? extends JavaApplication> getImplementationClass(Platform platform,
                                                                       Options  options)
        {
            return SimpleJavaApplication.class;
        }


        @Override
        public void onLaunch(Platform platform,
                             Options  options)
        {
            // there's nothing to do before launching the application
        }


        @Override
        public void onLaunching(Platform platform,
                                Options  options)
        {
            // there's nothing to do before launching the application
        }


        @Override
        public void onLaunched(Platform        platform,
                               JavaApplication application,
                               Options         options)
        {
            // there's nothing to do after launching the application
        }
    }
}
