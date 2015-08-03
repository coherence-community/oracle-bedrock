/*
 * File: ContainerBasedJavaApplicationBuilder.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.PropertiesBuilder;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.callable.RemoteCallableStaticMethod;

import com.oracle.tools.runtime.java.container.Container;
import com.oracle.tools.runtime.java.container.ContainerClassLoader;
import com.oracle.tools.runtime.java.container.ContainerScope;
import com.oracle.tools.runtime.java.io.Serialization;

import com.oracle.tools.util.CompletionListener;
import com.oracle.tools.util.FutureCompletionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An {@link ContainerBasedJavaApplicationBuilder} is a {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
 * that realizes {@link com.oracle.tools.runtime.java.JavaApplication}s isolated with in the current Java
 * Virtual Machine in the same manner as a regular Java EE application server
 * or container.
 * <p>
 * Scope of Application occurs through the use of a {@link ContainerBasedJavaApplicationProcess}
 * and a specialized child-first class loader provided by a {@link com.oracle.tools.runtime.java.container.ContainerClassLoader}.
 * <p>
 * <strong>Caution:</strong> Care should be taken using this {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
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
public class ContainerBasedJavaApplicationBuilder<A extends JavaApplication>
    extends AbstractJavaApplicationBuilder<A, JavaVirtualMachine>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(ContainerBasedJavaApplicationBuilder.class.getName());


    /**
     * Constructs a {@link ContainerBasedJavaApplicationBuilder} for the
     * specified {@link JavaVirtualMachine} platform.
     *
     * @param platform the {@link JavaVirtualMachine} platform
     */
    public ContainerBasedJavaApplicationBuilder(JavaVirtualMachine platform)
    {
        super(platform);
    }


    /**
     * Performs a sanity check on the specified {@link JavaApplicationSchema}.
     * <p>
     * It's particularly important to perform sanity checks on {@link JavaApplicationSchema}s
     * prior to realizing them in a container as some settings may not be appropriate or
     * achievable.
     *
     * @param schema            the {@link JavaApplicationSchema}
     * @param schemaProperties  the system {@link Properties} that have been realized for the
     *                          {@link JavaApplicationSchema}
     */
    protected <T extends A, S extends ApplicationSchema<T>> void sanityCheck(S          schema,
                                                                             Properties schemaProperties)
    {
        // ensure that if JAVA_NET_PREFER_IPV4_STACK is requested by the schema it has also been
        // established at the  platform level as this is only where it can actually be achieved with Java 7+.
        String schemaPreferIPv4Stack = schemaProperties.getProperty(JavaApplication.JAVA_NET_PREFER_IPV4_STACK);

        schemaPreferIPv4Stack = schemaPreferIPv4Stack == null ? "" : schemaPreferIPv4Stack.trim().toLowerCase();

        String systemPreferIPv4Stack = System.getProperty(JavaApplication.JAVA_NET_PREFER_IPV4_STACK);

        systemPreferIPv4Stack = systemPreferIPv4Stack == null ? "" : systemPreferIPv4Stack.trim().toLowerCase();

        if (systemPreferIPv4Stack.isEmpty() &&!schemaPreferIPv4Stack.isEmpty())
        {
            LOGGER.warning("The schema [" + schema + "] defines the " + JavaApplication.JAVA_NET_PREFER_IPV4_STACK
                + " system property but it is not defined by the current process."
                + "Container-based applications requiring this system property must have it defined at the operating system level."
                + "eg: In your case it should be defined as; -D" + JavaApplication.JAVA_NET_PREFER_IPV4_STACK + "="
                + schemaPreferIPv4Stack);
        }
        else if (!systemPreferIPv4Stack.equals(schemaPreferIPv4Stack))
        {
            LOGGER.warning("The schema [" + schema + "] defines the " + JavaApplication.JAVA_NET_PREFER_IPV4_STACK
                + " system property but it is not defined by the current process in the same manner."
                + "Container-based applications requiring this system property must have it in the same manner at the operating system level."
                + "eg: In your case it should be defined as; -D" + JavaApplication.JAVA_NET_PREFER_IPV4_STACK + "="
                + schemaPreferIPv4Stack);
        }

        // TODO: check that the JavaHome is not set or not different from the current platform setting
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> T realize(S                  applicationSchema,
                                                                   String             applicationName,
                                                                   ApplicationConsole console,
                                                                   Option...          applicationOptions)
    {
        // TODO: this should be a safe cast but we should also check to make sure
        JavaApplicationSchema<T> schema = (JavaApplicationSchema) applicationSchema;

        // ---- establish the Options for the Application -----

        // add the platform options
        Options options = new Options(platform == null ? null : platform.getOptions().asArray());

        // add the schema options
        options.addAll(applicationSchema.getOptions().asArray());

        // add the schema options (based on the platform)
        options.addAll(applicationSchema.getPlatformSpecificOptions(platform).asArray());

        // add the custom application options
        options.addAll(applicationOptions);

        try
        {
            // establish the System Properties for the ContainerBasedJavaApplication
            Properties systemProperties = schema.getSystemProperties(platform);

            // sanity check the schema and realized properties
            sanityCheck(schema, systemProperties);

            // establish the ContainerClassLoader for the application
            ContainerClassLoader classLoader = ContainerClassLoader.newInstance(applicationName,
                                                                                schema.getClassPath(),
                                                                                systemProperties);

            // determine the ApplicationController to use to control the process
            ApplicationController controller;

            if (schema instanceof ApplicationController)
            {
                controller = (ApplicationController) schema;
            }
            else
            {
                // when the Schema doesn't define a special way to control
                // ContainerBasedJavaProcesses, we default to using the
                // standard approach of executing the "main" method on the
                // specified Application Class
                controller = new StandardController(schema.getApplicationClassName(), schema.getArguments());

                // as a courtesy let's make sure the application class is accessible via the classloader
                Class<?> applicationClass = classLoader.loadClass(schema.getApplicationClassName());
            }

            // establish the ContainerBasedJavaProcess
            ContainerBasedJavaApplicationProcess process = new ContainerBasedJavaApplicationProcess(classLoader,
                                                                                                    controller);

            // notify the container of the scope to manage
            Container.manage(classLoader.getContainerScope());

            // start the process
            process.start();

            // the environment variables for the ContainerBasedJavaApplication
            // will be the environment variables for the Java Virtual Machine
            Properties environmentVariables = PropertiesBuilder.fromCurrentEnvironmentVariables().realize();

            // delegate Application creation to the Schema
            final T application = schema.createJavaApplication(process,
                                                               applicationName,
                                                               platform,
                                                               options,
                                                               console,
                                                               environmentVariables,
                                                               systemProperties,
                                                               -1);

            // raise life-cycle events for the application
            raiseOnRealizedFor(application);

            return application;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to start ContainerBasedJavaProcess", e);
        }
    }


    /**
     * Provides the ability to start and destroy a container-based
     * {@link ControllableApplication} application.
     */
    public static interface ApplicationController
    {
        /**
         * Asynchronously starts a {@link ControllableApplication}.
         * <p>
         * Should an exception be raised during the execution of this method, the
         * application is assumed unusable and in an unknown state, including
         * that of being destroyed.
         *
         * @param application  the {@link ControllableApplication} to start
         * @param listener     the {@link CompletionListener} to notify when the
         *                     application is started (or an exception occurs)
         */
        public void start(ControllableApplication  application,
                          CompletionListener<Void> listener);


        /**
         * Asynchronously destroys a {@link ControllableApplication}.
         * <p>
         * Should an exception be raised during the execution of this method, the
         * application is assumed unusable and in an unknown state, including
         * that of being destroyed.
         *
         * @param application  the {@link ControllableApplication} to destroy
         * @param listener     the {@link CompletionListener} to notify when the
         *                     application has been destroyed (or an exception occurs)
         */
        public void destroy(ControllableApplication  application,
                            CompletionListener<Void> listener);
    }


    /**
     * A representation of a container-based application that may be controlled
     * outside of the {@link ContainerClassLoader} which started the said application.
     */
    public static interface ControllableApplication extends RemoteExecutor
    {
        /**
         * Obtains the {@link ClassLoader} used to load and start the application.
         *
         * @return the application {@link ClassLoader}
         */
        public ClassLoader getClassLoader();
    }


    /**
     * An implementation of a {@link JavaApplicationProcess} to represent and control a
     * Java application running with in a Java Virtual Machine, as part of a
     * container, much like a Java EE application.
     */
    public static class ContainerBasedJavaApplicationProcess implements JavaApplicationProcess, ControllableApplication
    {
        /**
         * An {@link ExecutorService} to use for requesting asynchronous
         * execution of tasks in the contained application.   Typically this
         * is used for executing start and destroy functionality.
         */
        private ExecutorService m_executorService;

        /**
         * The {@link ClassLoader} that will be used to contain, scope and isolate
         * the executing application.
         */
        private ContainerClassLoader m_classLoader;

        /**
         * The {@link ApplicationController} that will be used to start/destroy
         * the application.
         */
        private ApplicationController m_controller;

        /**
         * The {@link CompletionListener} to be called back when an application
         * is being started.
         */
        private FutureCompletionListener<Void> m_startListener;

        /**
         * The {@link CompletionListener} to be called back when an application
         * is being destroyed.
         */
        private FutureCompletionListener<Void> m_destroyListener;


        /**
         * Constructs an {@link ContainerBasedJavaApplicationProcess}.
         *
         * @param classLoader           the {@link ClassLoader} in which to run the application
         * @param controller            the {@link ApplicationController}
         */
        public ContainerBasedJavaApplicationProcess(ContainerClassLoader  classLoader,
                                                    ApplicationController controller)
        {
            if (controller == null)
            {
                throw new NullPointerException("ApplicationController must not be null");
            }

            // establish an ExecutorService that we can use to asynchronously submit
            // requests against the ContainerBasedJavaProcess
            m_executorService = Executors.newCachedThreadPool();

            m_classLoader     = classLoader;
            m_controller      = controller;
        }


        @Override
        public ClassLoader getClassLoader()
        {
            return m_classLoader;
        }


        @Override
        public long getId()
        {
            return -1;
        }


        @Override
        public OutputStream getOutputStream()
        {
            return m_classLoader.getContainerScope().getStandardInputOutputStream();
        }


        @Override
        public InputStream getInputStream()
        {
            return m_classLoader.getContainerScope().getStandardOutputInputStream();
        }


        @Override
        public InputStream getErrorStream()
        {
            return m_classLoader.getContainerScope().getStandardErrorInputStream();
        }


        @Override
        public int waitFor(Option... options)
        {
            // when there's no application controller we don't have to wait to terminate
            // (as we've already been terminated)
            if (m_controller != null)
            {
                // here we simply try to wait for the application's start future
                // to complete executing.
                try
                {
                    if (m_startListener != null)
                    {
                        m_startListener.get();
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
         */
        public void start()
        {
            if (m_controller == null)
            {
                m_startListener = null;
            }
            else
            {
                m_startListener = new FutureCompletionListener<Void>();
                m_controller.start(this, m_startListener);
            }
        }


        @Override
        public void close()
        {
            if (m_controller != null)
            {
                // now try to stop
                try
                {
                    m_destroyListener = new FutureCompletionListener<Void>();
                    m_controller.destroy(this, m_destroyListener);

                    m_destroyListener.get();
                }
                catch (Exception e)
                {
                    LOGGER.log(Level.WARNING, "An exception occurred while closing the application", e);
                }

                m_controller = null;
            }

            ContainerScope scope = m_classLoader.getContainerScope();

            // close the scope
            scope.close();

            // notify the container to stop managing the scope
            Container.unmanage(scope);
        }


        @Override
        @Deprecated
        public void destroy()
        {
            close();
        }


        @Override
        public <T> void submit(RemoteCallable<T>           callable,
                               final CompletionListener<T> listener)
        {
            if (m_controller == null)
            {
                IllegalStateException e =
                    new IllegalStateException("Attempting to submit to a ContainerBasedJavaProcess that has been destroyed");

                if (listener != null)
                {
                    listener.onException(e);
                }

                throw e;
            }
            else
            {
                try
                {
                    // serialize the Callable so that we can deserialize it in the container
                    // to use the correct ClassLoader
                    final byte[] serializedCallable = Serialization.toByteArray(callable);

                    Runnable     scopedRunnable     = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // remember the current context ClassLoader of the thread
                            // (so that we can return it back to normal when we're finished executing)
                            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

                            try
                            {
                                // set the context ClassLoader of the Thread to be that of the
                                // ContainerClassLoader
                                Thread.currentThread().setContextClassLoader(m_classLoader);

                                // and associate the Thread with the Scope in the Container
                                Container.associateThreadWith(m_classLoader.getContainerScope());

                                // deserialize the callable (so that we can use the container-based class loader)
                                Callable<T> callable = Serialization.fromByteArray(serializedCallable,
                                                                                   Callable.class,
                                                                                   m_classLoader);

                                // then call the Callable as usual
                                T result = callable.call();

                                // serialize the result (so that we can use the application class loader)
                                byte[] serializedResult = Serialization.toByteArray(result);

                                // notify the listener (if there is one) of the result
                                if (listener != null)
                                {
                                    listener.onCompletion((T) Serialization.fromByteArray(serializedResult,
                                                                                          Object.class,
                                                                                          originalClassLoader));
                                }
                            }
                            catch (Exception e)
                            {
                                // TODO: write the exception to the platform (if diagnostics are on?)

                                // notify the listener (if there is one) of the exception
                                if (listener != null)
                                {
                                    listener.onException(e);
                                }
                            }
                            finally
                            {
                                // afterwards dissociate the Thread from the Scope in the Container
                                Container.dissociateThread();

                                // and return the current context ClassLoader back to normal
                                Thread.currentThread().setContextClassLoader(originalClassLoader);
                            }
                        }
                    };

                    m_executorService.submit(scopedRunnable);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to serialize the Callable: " + callable, e);
                }
            }
        }


        @Override
        public void submit(RemoteRunnable runnable) throws IllegalStateException
        {
            if (m_controller == null)
            {
                throw new IllegalStateException("Attempting to submit to a ContainerBasedJavaProcess that has been destroyed");
            }
            else
            {
                try
                {
                    // serialize the Runnable so that we can deserialize it in the container
                    // to use the correct ClassLoader
                    final byte[] serializedRunnable = Serialization.toByteArray(runnable);

                    Runnable     scopedRunnable     = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            // remember the current context ClassLoader of the thread
                            // (so that we can return it back to normal when we're finished executing)
                            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

                            try
                            {
                                // set the context ClassLoader of the Thread to be that of the
                                // ContainerClassLoader
                                Thread.currentThread().setContextClassLoader(m_classLoader);

                                // and associate the Thread with the Scope in the Container
                                Container.associateThreadWith(m_classLoader.getContainerScope());

                                // deserialize the runnable (so that we can use the container-based class loader)
                                Runnable runnable = Serialization.fromByteArray(serializedRunnable,
                                                                                Runnable.class,
                                                                                m_classLoader);

                                // then call the Callable as usual
                                runnable.run();
                            }
                            catch (IOException e)
                            {
                                // TODO: write the exception to the platform (if diagnostics are on?)
                            }
                            finally
                            {
                                // afterwards dissociate the Thread from the Scope in the Container
                                Container.dissociateThread();

                                // and return the current context ClassLoader back to normal
                                Thread.currentThread().setContextClassLoader(originalClassLoader);
                            }
                        }
                    };

                    m_executorService.submit(scopedRunnable);
                }
                catch (IOException e)
                {
                    throw new RuntimeException("Failed to serialize the Runnable: " + runnable, e);
                }
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
        public void start(ControllableApplication  application,
                          CompletionListener<Void> listener)
        {
            if (m_callableStartStaticMethod == null)
            {
                if (listener != null)
                {
                    listener.onCompletion(null);
                }
            }
            else
            {
                application.submit(m_callableStartStaticMethod, listener);
            }
        }


        @Override
        public void destroy(ControllableApplication  application,
                            CompletionListener<Void> listener)
        {
            if (m_callableDestroyStaticMethod == null)
            {
                if (listener != null)
                {
                    listener.onCompletion(null);
                }
            }
            else
            {
                application.submit(m_callableDestroyStaticMethod, listener);
            }
        }
    }


    /**
     * An {@link ApplicationController} that does nothing to a
     * {@link ContainerBasedJavaApplicationProcess}.
     */
    public static class NullController implements ApplicationController
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void start(ControllableApplication  application,
                          CompletionListener<Void> listener)
        {
            if (listener != null)
            {
                listener.onCompletion(null);
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void destroy(ControllableApplication  application,
                            CompletionListener<Void> listener)
        {
            if (listener != null)
            {
                listener.onCompletion(null);
            }
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
         * @param arguments  the arguments for the main method
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
        public void start(ControllableApplication  application,
                          CompletionListener<Void> listener)
        {
            RemoteCallable<Void> callable = new RemoteCallableStaticMethod<Void>(applicationClassName,
                                                                                 "main",
                                                                                 arguments);

            application.submit(callable, listener);
        }


        @Override
        public void destroy(ControllableApplication  application,
                            CompletionListener<Void> listener)
        {
            // SKIP: there's no standard method to stop and destroy a
            // regular Java console application running in a container
            if (listener != null)
            {
                listener.onCompletion(null);
            }
        }
    }
}
