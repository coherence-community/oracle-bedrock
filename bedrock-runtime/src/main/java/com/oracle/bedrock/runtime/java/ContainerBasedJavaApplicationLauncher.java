/*
 * File: ContainerBasedJavaApplicationLauncher.java
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

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.Version;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.options.Variable;
import com.oracle.bedrock.runtime.ApplicationListener;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.Profile;
import com.oracle.bedrock.runtime.Profiles;
import com.oracle.bedrock.runtime.PropertiesBuilder;
import com.oracle.bedrock.runtime.concurrent.PipeBasedRemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteEvent;
import com.oracle.bedrock.runtime.concurrent.RemoteEventListener;
import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;
import com.oracle.bedrock.runtime.concurrent.callable.RemoteCallableStaticMethod;
import com.oracle.bedrock.runtime.java.container.Container;
import com.oracle.bedrock.runtime.java.container.ContainerClassLoader;
import com.oracle.bedrock.runtime.java.container.ContainerScope;
import com.oracle.bedrock.runtime.java.features.JmxFeature;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.RemoteEvents;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.profiles.CommercialFeatures;
import com.oracle.bedrock.runtime.java.profiles.RemoteDebugging;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.table.Cell;
import com.oracle.bedrock.table.Table;
import com.oracle.bedrock.util.ReflectionHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An {@link ContainerBasedJavaApplicationLauncher} is a {@link JavaApplicationLauncher}
 * that launches {@link JavaApplication}s with in the current Java Virtual Machine, isolated by class-loader
 * in the same manner as a regular Java EE application server
 * or container.
 * <p>
 * Scope of Application occurs through the use of a {@link ContainerBasedJavaApplicationProcess}
 * and a specialized child-first class loader provided by a {@link ContainerClassLoader}.
 * <p>
 * <strong>Caution:</strong> Care should be taken using this {@link JavaApplicationLauncher}
 * as all classes used by the application will be re-loaded in the Perm Generation
 * (on pre Java 8 editions of Java).  Without a large Perm Generation, out-of-memory
 * exceptions may be thrown.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
@Internal
public class ContainerBasedJavaApplicationLauncher<A extends JavaApplication> implements JavaApplicationLauncher<A>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(ContainerBasedJavaApplicationLauncher.class.getName());


    /**
     * Constructs a {@link ContainerBasedJavaApplicationLauncher}.
     *
     */
    public ContainerBasedJavaApplicationLauncher()
    {
    }


//  TODO: think about what to do with sanity check
//     /**
//      * Performs a sanity check on the specified {@link JavaApplicationSchema}.
//      * <p>
//      * It's particularly important to perform sanity checks on {@link Options}
//      * prior to realizing them in a container as some settings may not be appropriate or
//      * achievable.
//      *
//      * @param options           the {@link Options}
//      * @param schemaProperties  the system {@link Properties} that have been launched for the
//      *                          {@link JavaApplication}
//      */
//     protected <T extends A, S extends ApplicationSchema<T>> void sanityCheck(Options    options,
//                                                                              Properties schemaProperties)
//     {
//         // ensure that if JAVA_NET_PREFER_IPV4_STACK is requested by the schema it has also been
//         // established at the  platform level as this is only where it can actually be achieved with Java 7+.
//         String schemaPreferIPv4Stack = schemaProperties.getProperty(JavaApplication.JAVA_NET_PREFER_IPV4_STACK);
//
//         schemaPreferIPv4Stack = schemaPreferIPv4Stack == null ? "" : schemaPreferIPv4Stack.trim().toLowerCase();
//
//         String systemPreferIPv4Stack = System.getProperty(JavaApplication.JAVA_NET_PREFER_IPV4_STACK);
//
//         systemPreferIPv4Stack = systemPreferIPv4Stack == null ? "" : systemPreferIPv4Stack.trim().toLowerCase();
//
//         if (systemPreferIPv4Stack.isEmpty() &&!schemaPreferIPv4Stack.isEmpty())
//         {
//             LOGGER.warning("The schema [" + schema + "] defines the " + JavaApplication.JAVA_NET_PREFER_IPV4_STACK
//                 + " system property but it is not defined by the current process."
//                 + "Container-based applications requiring this system property must have it defined at the operating system level."
//                 + "eg: In your case it should be defined as; -D" + JavaApplication.JAVA_NET_PREFER_IPV4_STACK + "="
//                 + schemaPreferIPv4Stack);
//         }
//         else if (!systemPreferIPv4Stack.equals(schemaPreferIPv4Stack))
//         {
//             LOGGER.warning("The schema [" + schema + "] defines the " + JavaApplication.JAVA_NET_PREFER_IPV4_STACK
//                 + " system property but it is not defined by the current process in the same manner."
//                 + "Container-based applications requiring this system property must have it in the same manner at the operating system level."
//                 + "eg: In your case it should be defined as; -D" + JavaApplication.JAVA_NET_PREFER_IPV4_STACK + "="
//                 + schemaPreferIPv4Stack);
//         }
//
//         // TODO: check that the JavaHome is not set or not different from the current platform setting
//     }

    @Override
    public A launch(Platform      platform,
                    MetaClass<A>  metaClass,
                    OptionsByType optionsByType)
    {
        // establish the diagnostics output table
        Table diagnosticsTable = new Table();

        diagnosticsTable.getOptions().add(Table.orderByColumn(0));

        if (platform != null)
        {
            diagnosticsTable.addRow("Target Platform", platform.getName());
        }

        // ----- establish the launch Options for the Application -----

        // add the platform options
        OptionsByType launchOptions = OptionsByType.of(platform.getOptions().asArray());

        // add the meta-class options
        metaClass.onLaunching(platform, launchOptions);

        // add the launch specific options
        launchOptions.addAll(optionsByType);

        // ----- establish an identity for the application -----

        // add a unique runtime id for expression support
        launchOptions.add(Variable.with("bedrock.runtime.id", UUID.randomUUID()));

        // ----- establish default Profiles for this Platform (and Builder) -----

        // java applications can automatically detect the following profiles
        launchOptions.get(RemoteDebugging.class);
        launchOptions.get(CommercialFeatures.class);

        // auto-detect and add externally defined profiles
        launchOptions.addAll(Profiles.getProfiles());

        // ----- notify the Profiles that the application is about to be launched -----

        for (Profile profile : launchOptions.getInstancesOf(Profile.class))
        {
            profile.onLaunching(platform, metaClass, launchOptions);
        }

        // ----- give the MetaClass a last chance to manipulate any options -----

        metaClass.onLaunch(platform, launchOptions);

        // ----- determine the display name for the application -----

        DisplayName displayName = getDisplayName(launchOptions);

        // ----- create and start the application in it's own classloader -----
        try
        {
            Table systemPropertiesTable = new Table();

            systemPropertiesTable.getOptions().add(Table.orderByColumn(0));
            systemPropertiesTable.getOptions().add(Cell.Separator.of(""));

            // establish the System Properties for the ContainerBasedJavaApplication
            Properties systemProperties = launchOptions.get(SystemProperties.class).resolve(platform, launchOptions);

            for (String propertyName : systemProperties.stringPropertyNames())
            {
                String propertyValue = systemProperties.getProperty(propertyName);

                systemPropertiesTable.addRow(propertyName, propertyValue);
            }

            diagnosticsTable.addRow("System Properties", systemPropertiesTable.toString());

            // determine the predefined class path based on the launch options
            ClassPath classPath      = launchOptions.get(ClassPath.class);

            Table     classPathTable = classPath.getTable();

            classPathTable.getOptions().add(Cell.Separator.of(""));
            diagnosticsTable.addRow("Class Path", classPathTable.toString());

            // establish the ContainerClassLoader for the application
            ContainerClassLoader classLoader = ContainerClassLoader.newInstance(displayName.resolve(launchOptions),
                                                                                classPath,
                                                                                systemProperties);

            // Get the command line arguments
            List<String> argList = launchOptions.get(Arguments.class).resolve(platform, launchOptions);

            // Set the actual arguments used back into the options
            launchOptions.add(Arguments.of(argList));

            // determine the application class that we'll start
            ClassName className = launchOptions.get(ClassName.class);

            if (className == null)
            {
                throw new IllegalArgumentException("Java Application ClassName not specified");
            }

            String applicationClassName = className.getName();

            // determine the ApplicationController to use to control the process in the container
            // (either an ApplicationController provided as an Option or use the MetaClass if it's appropriate)
            ApplicationController controller = launchOptions.getOrSetDefault(ApplicationController.class,
                                                                             metaClass instanceof ApplicationController
                                                                             ? (ApplicationController) metaClass
                                                                             : null);

            if (controller == null)
            {
                // when an ApplicationController isn't defined, we default to using the
                // standard approach of executing the "main" method on the
                // specified Application Class
                controller = new StandardController(applicationClassName, argList);

                // as a courtesy let's make sure the application class is accessible via the classloader
                Class<?> applicationClass = classLoader.loadClass(applicationClassName);
            }

            diagnosticsTable.addRow("Application Class", applicationClassName);
            diagnosticsTable.addRow("Application", displayName.resolve(launchOptions));

            String arguments = "";

            for (String argument : argList)
            {
                arguments += argument + " ";
            }

            if (arguments.length() > 0)
            {
                diagnosticsTable.addRow("Application Arguments", arguments);
            }

            diagnosticsTable.addRow("Application Launch Time",
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            if (LOGGER.isLoggable(Level.INFO))
            {
                LOGGER.log(Level.INFO,
                           "Oracle Bedrock " + Version.get() + ": Starting Application...\n"
                           + "------------------------------------------------------------------------\n"
                           + diagnosticsTable.toString() + "\n"
                           + "------------------------------------------------------------------------\n");
            }

            // establish the ContainerBasedJavaProcess
            ContainerBasedJavaApplicationProcess process = new ContainerBasedJavaApplicationProcess(classLoader,
                                                                                                    controller,
                                                                                                    systemProperties);

            // register the defined RemoteEventListeners before the application starts so they can
            // immediately start receiving RemoteEvents
            RemoteEvents remoteEvents = launchOptions.get(RemoteEvents.class);

            remoteEvents.forEach((remoteEventListener, listenerOptions) -> process.addListener(remoteEventListener,
                                                                                               listenerOptions));

            // notify the container of the scope to manage
            Container.manage(classLoader.getContainerScope());

            // start the process
            process.start(launchOptions);

            // the environment variables for the ContainerBasedJavaApplication
            // will be the environment variables for the Java Virtual Machine
            Properties environmentVariables = PropertiesBuilder.fromCurrentEnvironmentVariables().realize();

            diagnosticsTable.addRow("Environment Variables", "(based on this Java Virtual Machine)");

            // determine the application class that will represent the running application
            Class<? extends A> applicationClass = metaClass.getImplementationClass(platform, launchOptions);

            A                  application;

            try
            {
                // attempt to find a constructor(Platform, JavaApplicationProcess, Options)
                Constructor<? extends A> constructor = ReflectionHelper.getCompatibleConstructor(applicationClass,
                                                                                                 platform.getClass(),
                                                                                                 process.getClass(),
                                                                                                 launchOptions.getClass());

                // create the application
                application = constructor.newInstance(platform, process, launchOptions);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Failed to instantiate the Application class specified by the MetaClass:"
                                           + metaClass);
            }

            // ----- enhance the application with java-specific features -----

            if (JmxFeature.isSupportedBy(application))
            {
                application.add(new JmxFeature());
            }

            // ----- notify the MetaClass that the application has been launched -----

            metaClass.onLaunched(platform, application, launchOptions);

            // ----- notify the Profiles that the application has been launched -----

            for (Profile profile : launchOptions.getInstancesOf(Profile.class))
            {
                profile.onLaunched(platform, application, launchOptions);
            }

            // ----- notify all of the application listeners -----

            // notify the ApplicationListener-based Options that the application has been launched
            for (ApplicationListener listener : launchOptions.getInstancesOf(ApplicationListener.class))
            {
                listener.onLaunched(application);
            }

            return application;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to start ContainerBasedJavaProcess", e);
        }
    }


    /**
     * Configures the internal application container {@link RemoteChannel}.
     *
     * @param containerClassLoader  the {@link ContainerClassLoader}
     * @param pipedOutputStream     the {@link PipedOutputStream}
     * @param pipedInputStream      the {@link PipedInputStream}
     * @param targetClassName       the optional target (main) class name
     */
    public static void configureRemoteChannel(ContainerClassLoader containerClassLoader,
                                              PipedOutputStream    pipedOutputStream,
                                              PipedInputStream     pipedInputStream,
                                              String               targetClassName)
    {
        // remember the current context ClassLoader of the thread
        // (so that we can return it back to normal when we're finished executing)
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

        try
        {
            // set the context ClassLoader of the Thread to be that of the
            // ContainerClassLoader
            Thread.currentThread().setContextClassLoader(containerClassLoader);

            // and associate the Thread with the Scope in the Container
            Container.associateThreadWith(containerClassLoader.getContainerScope());

            // create the Remote Channel
            Class<?> remoteChannelClass = containerClassLoader.loadClass(PipeBasedRemoteChannel.class.getName());

            Constructor<?> constructor = remoteChannelClass.getConstructor(PipedOutputStream.class,
                                                                           PipedInputStream.class);

            Object remoteChannel = constructor.newInstance(pipedOutputStream, pipedInputStream);

            // open the RemoteChannel
            remoteChannelClass.getMethod("open").invoke(remoteChannel);

            // inject the RemoteChannel (if we have a target class)
            if (targetClassName != null)
            {
                Class<?> targetClass   = containerClassLoader.loadClass(targetClassName);
                Class<?> injectorClass = containerClassLoader.loadClass(RemoteChannel.Injector.class.getName());
                Class<?> channelClass  = containerClassLoader.loadClass(RemoteChannel.class.getName());
                Method   method        = injectorClass.getMethod("injectChannel", Class.class, channelClass);

                method.invoke(null, targetClass, remoteChannel);
            }
        }
        catch (ClassNotFoundException e)
        {
            // skip publisher injection as required classes are not on the classpath
            e.printStackTrace();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            // afterwards dissociate the Thread from the Scope in the Container
            Container.dissociateThread();

            // and return the current context ClassLoader back to normal
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }


    /**
     * Provides the ability to start and destroy a container-based
     * {@link ControllableApplication} application.
     */
    public interface ApplicationController extends Option
    {
        /**
         * Asynchronously starts a {@link ControllableApplication}.
         * <p>
         * Should an exception be raised during the execution of this method, the
         * application is assumed unusable and in an unknown state, including
         * that of being destroyed.
         *
         * @param application  the {@link ControllableApplication} to start
         *
         * @return  a {@link CompletableFuture} that will be completed when
         *          the application is started, or an error occurs
         */
        CompletableFuture<Void> start(ControllableApplication application);


        /**
         * Asynchronously destroys a {@link ControllableApplication}.
         * <p>
         * Should an exception be raised during the execution of this method, the
         * application is assumed unusable and in an unknown state, including
         * that of being destroyed.
         *
         * @param application  the {@link ControllableApplication} to destroy
         *
         * @return  a {@link CompletableFuture} that will be completed when
         *          the application is started, or an error occurs
         */
        CompletableFuture<Void> destroy(ControllableApplication application);


        /**
         * Configures the {@link JavaApplication} prior to starting.
         *
         * @param containerClassLoader  the {@link ContainerClassLoader} used to isolate the application
         * @param pipedOutputStream     the {@link PipedOutputStream} for sending request to the application
         * @param pipedInputStream      the {@link PipedInputStream} for recieving requests from the application
         * @param optionsByType         the {@link OptionsByType} being used to launch the application
         */
        void configure(ContainerClassLoader containerClassLoader,
                       PipedOutputStream    pipedOutputStream,
                       PipedInputStream     pipedInputStream,
                       OptionsByType        optionsByType);
    }


    /**
     * A representation of a container-based application that may be controlled
     * outside of the {@link ContainerClassLoader} which started the said application.
     */
    public interface ControllableApplication extends RemoteChannel
    {
        /**
         * Obtains the {@link ClassLoader} used to load and start the application.
         *
         * @return the application {@link ClassLoader}
         */
        ClassLoader getClassLoader();
    }


    /**
     * An implementation of a {@link JavaApplicationProcess} to represent and control a
     * Java application running with in a Java Virtual Machine, as part of a
     * container, much like a Java EE application.
     */
    public static class ContainerBasedJavaApplicationProcess implements JavaApplicationProcess, ControllableApplication
    {
        /**
         * The resolved System {@link Properties} provided to the {@link JavaApplicationProcess} when it was launched.
         */
        private Properties systemProperties;

        /**
         * The {@link ClassLoader} that will be used to contain, scope and isolate
         * the executing application.
         */
        private ContainerClassLoader containerClassLoader;

        /**
         * The {@link ApplicationController} that will be used to start/destroy
         * the application.
         */
        private ApplicationController applicationController;

        /**
         * The {@link CompletableFuture} to be completed when an application
         * is being started.
         */
        private CompletableFuture<Void> startListener;

        /**
         * The {@link CompletableFuture} to be completed when an application
         * is being destroyed.
         */
        private CompletableFuture<Void> destroyListener;

        /**
         * The {@link RemoteChannel} over which communication to and from the {@link JavaApplication}
         * will occur.
         */
        private PipeBasedRemoteChannel channel;

        /**
         * The streams for in-bound communication to this channel (not the other side of the channel)
         */
        private PipedInputStream  inboundChannelInputStream;
        private PipedOutputStream inboundChannelOutputStream;

        /**
         * The streams for out-bound communication from this channel (not the other side of the channel)
         */
        private PipedInputStream  outboundChannelInputStream;
        private PipedOutputStream outboundChannelOutputStream;


        /**
         * Constructs an {@link ContainerBasedJavaApplicationProcess}.
         *
         * @param classLoader       the {@link ClassLoader} in which to run the application
         * @param controller        the {@link ApplicationController}
         * @param systemProperties  the resolved System {@link Properties}
         *
         * @throws IOException  when it was not possible to establish the {@link JavaApplication} due to
         *                      an internal IO failure
         */
        public ContainerBasedJavaApplicationProcess(ContainerClassLoader  classLoader,
                                                    ApplicationController controller,
                                                    Properties            systemProperties) throws IOException
        {
            if (controller == null)
            {
                throw new NullPointerException("ApplicationController must not be null");
            }

            this.containerClassLoader  = classLoader;
            this.applicationController = controller;

            int bufferSizeInBytes = 64 * 1024;

            this.inboundChannelInputStream   = new PipedInputStream(bufferSizeInBytes);
            this.inboundChannelOutputStream  = new PipedOutputStream(inboundChannelInputStream);

            this.outboundChannelInputStream  = new PipedInputStream(bufferSizeInBytes);
            this.outboundChannelOutputStream = new PipedOutputStream(outboundChannelInputStream);

            // establish the RemoteChannel for asynchronously communicating with the application
            this.channel          = new PipeBasedRemoteChannel(outboundChannelOutputStream, inboundChannelInputStream);

            this.systemProperties = systemProperties;
        }


        @Override
        public Properties getSystemProperties()
        {
            return systemProperties;
        }


        @Override
        public ClassLoader getClassLoader()
        {
            return containerClassLoader;
        }


        @Override
        public long getId()
        {
            return -1;
        }


        @Override
        public OutputStream getOutputStream()
        {
            return containerClassLoader.getContainerScope().getStandardInputOutputStream();
        }


        @Override
        public InputStream getInputStream()
        {
            return containerClassLoader.getContainerScope().getStandardOutputInputStream();
        }


        @Override
        public InputStream getErrorStream()
        {
            return containerClassLoader.getContainerScope().getStandardErrorInputStream();
        }


        @Override
        public int waitFor(Option... options)
        {
            // when there's no application controller we don't have to wait to terminate
            // (as we've already been terminated)
            if (applicationController != null)
            {
                // here we simply try to wait for the application's start future
                // to complete executing.
                try
                {
                    if (startListener != null)
                    {
                        startListener.get();
                    }
                }
                catch (InterruptedException e)
                {
                    throw new RuntimeException("Interrupted while waiting for application to terminate", e);
                }
                catch (ExecutionException e)
                {
                    throw new RuntimeException(e.getCause());
                }
            }

            return 0;
        }


        @Override
        public int exitValue()
        {
            return 0;
        }


        /**
         * Starts the application.
         *
         * @param optionsByType  the {@link OptionsByType} to use when starting the application
         */
        public void start(OptionsByType optionsByType)
        {
            if (applicationController == null)
            {
                startListener = null;
            }
            else
            {
                applicationController.configure(containerClassLoader,
                                                inboundChannelOutputStream,
                                                outboundChannelInputStream,
                                                optionsByType);

                channel.open();

                startListener = applicationController.start(this);
            }
        }


        @Override
        public void close()
        {
            if (applicationController != null)
            {
                // now try to stop
                try
                {
                    destroyListener = applicationController.destroy(this);

                    destroyListener.get();
                }
                catch (Exception e)
                {
                    LOGGER.log(Level.WARNING, "An exception occurred while closing the application", e);
                }

                applicationController = null;
            }

            channel.close();

            ContainerScope scope = containerClassLoader.getContainerScope();

            // close the scope
            scope.close();

            // notify the container to stop managing the scope
            Container.unmanage(scope);
        }


        @Override
        public <T> CompletableFuture<T> submit(RemoteCallable<T> callable,
                                               Option...         options)
        {
            if (applicationController == null)
            {
                IllegalStateException e =
                    new IllegalStateException("Attempting to submit to a ContainerBasedJavaProcess that has been destroyed");

                CompletableFuture<T> future = new CompletableFuture<>();

                future.completeExceptionally(e);

                return future;
            }
            else
            {
                return channel.submit(callable);
            }
        }


        @Override
        public CompletableFuture<Void> submit(RemoteRunnable runnable,
                                              Option...      options) throws IllegalStateException
        {
            if (applicationController == null)
            {
                throw new IllegalStateException("Attempting to submit to a ContainerBasedJavaProcess that has been destroyed");
            }
            else
            {
                return channel.submit(runnable);
            }
        }


        @Override
        public void addListener(RemoteEventListener listener,
                                Option...           options)
        {
            if (applicationController == null)
            {
                throw new IllegalStateException("Attempting to add a listener to a ContainerBasedJavaProcess that has been destroyed");
            }
            else
            {
                channel.addListener(listener, options);
            }

        }


        @Override
        public void removeListener(RemoteEventListener listener,
                                   Option...           options)
        {
            if (applicationController == null)
            {
                throw new IllegalStateException("Attempting to remove a listener from a ContainerBasedJavaProcess that has been destroyed");
            }
            else
            {
                channel.removeListener(listener, options);
            }

        }


        @Override
        public CompletableFuture<Void> raise(RemoteEvent event,
                                             Option...   options)
        {
            if (applicationController == null)
            {
                throw new IllegalStateException("Attempting to raise to a ContainerBasedJavaProcess that has been destroyed");
            }
            else
            {
                return channel.raise(event, options);
            }
        }
    }


    /**
     * An {@link ApplicationController} that will call specific methods to start
     * and destroy a {@link ContainerBasedJavaApplicationProcess}.
     */
    public static class CustomController implements ApplicationController
    {
        /**
         * The {@link RemoteCallableStaticMethod} to start the application.
         */
        private RemoteCallableStaticMethod<Void> m_callableStartStaticMethod;

        /**
         * The {@link RemoteCallableStaticMethod} to destroy the application.
         */
        private RemoteCallableStaticMethod<Void> m_callableDestroyStaticMethod;


        /**
         * Constructs a CustomController.
         *
         * @param callableStartStaticMethod    the {@link RemoteCallableStaticMethod} to
         *                                     start the application (may be null)
         */
        public CustomController(RemoteCallableStaticMethod<Void> callableStartStaticMethod)
        {
            this(callableStartStaticMethod, null);
        }


        /**
         * Constructs a CustomController.
         *
         * @param callableStartStaticMethod    the {@link RemoteCallableStaticMethod} to
         *                                     start the application (may be null)
         * @param callableDestroyStaticMethod  the {@link RemoteCallableStaticMethod} to
         *                                     destroy the application (may be null)
         *
         */
        public CustomController(RemoteCallableStaticMethod<Void> callableStartStaticMethod,
                                RemoteCallableStaticMethod<Void> callableDestroyStaticMethod)
        {
            m_callableStartStaticMethod   = callableStartStaticMethod;
            m_callableDestroyStaticMethod = callableDestroyStaticMethod;
        }


        @Override
        public CompletableFuture<Void> start(ControllableApplication application)
        {
            if (m_callableStartStaticMethod == null)
            {
                return CompletableFuture.completedFuture(null);
            }
            else
            {
                return application.submit(m_callableStartStaticMethod);
            }
        }


        @Override
        public CompletableFuture<Void> destroy(ControllableApplication application)
        {
            if (m_callableDestroyStaticMethod == null)
            {
                return CompletableFuture.completedFuture(null);
            }
            else
            {
                return application.submit(m_callableDestroyStaticMethod);
            }
        }


        @Override
        public void configure(ContainerClassLoader containerClassLoader,
                              PipedOutputStream    pipedOutputStream,
                              PipedInputStream     pipedInputStream,
                              OptionsByType        optionsByType)
        {
            ContainerBasedJavaApplicationLauncher.configureRemoteChannel(containerClassLoader,
                                                                         pipedOutputStream,
                                                                         pipedInputStream,
                                                                         null);
        }
    }


    /**
     * An {@link ApplicationController} that does nothing to a
     * {@link ContainerBasedJavaApplicationProcess}.
     */
    public static class NullController implements ApplicationController
    {
        @Override
        public CompletableFuture<Void> start(ControllableApplication application)
        {
            return CompletableFuture.completedFuture(null);
        }


        @Override
        public CompletableFuture<Void> destroy(ControllableApplication application)
        {
            return CompletableFuture.completedFuture(null);
        }


        @Override
        public void configure(ContainerClassLoader containerClassLoader,
                              PipedOutputStream    pipedOutputStream,
                              PipedInputStream     pipedInputStream,
                              OptionsByType        optionsByType)
        {
            ContainerBasedJavaApplicationLauncher.configureRemoteChannel(containerClassLoader,
                                                                         pipedOutputStream,
                                                                         pipedInputStream,
                                                                         null);
        }
    }


    /**
     * An {@link ApplicationController} that will start a regular Java
     * Console application (that defines a standard Java main method).
     */
    public static class StandardController implements ApplicationController
    {
        /**
         * The name of the Application class that contains the main method.
         */
        private String applicationClassName;

        /**
         * The arguments for the main method.
         */
        private List<String> arguments;


        /**
         * Constructs a StandardController.
         *
         * @param applicationClassName  the name of the application {@link Class}
         * @param arguments             the arguments for the main method
         */
        public StandardController(String       applicationClassName,
                                  List<String> arguments)
        {
            this.applicationClassName = applicationClassName;
            this.arguments            = arguments == null ? new ArrayList<String>(0) : new ArrayList<String>(arguments);
        }


        /**
         * Obtains the name of the class that defines the application main method.
         *
         * @return the application class name
         */
        public String getApplicationClassName()
        {
            return applicationClassName;
        }


        /**
         * Obtains a copy of the arguments used to start the application.
         *
         * @return a list of arguments
         */
        public List<String> getArguments()
        {
            return new ArrayList<String>(arguments);
        }


        @Override
        public CompletableFuture<Void> start(ControllableApplication application)
        {
            RemoteCallable<Void> callable = new RemoteCallableStaticMethod<Void>(applicationClassName,
                                                                                 "main",
                                                                                 arguments);

            return application.submit(callable);
        }


        @Override
        public CompletableFuture<Void> destroy(ControllableApplication application)
        {
            // SKIP: there's no standard method to stop and destroy a
            // regular Java console application running in a container
            return CompletableFuture.completedFuture(null);
        }


        @Override
        public void configure(ContainerClassLoader containerClassLoader,
                              PipedOutputStream    pipedOutputStream,
                              PipedInputStream     pipedInputStream,
                              OptionsByType        optionsByType)
        {
            ContainerBasedJavaApplicationLauncher.configureRemoteChannel(containerClassLoader,
                                                                         pipedOutputStream,
                                                                         pipedInputStream,
                                                                         applicationClassName);
        }
    }
}
