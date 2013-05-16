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

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.PropertiesBuilder;

import com.oracle.tools.runtime.java.container.Container;
import com.oracle.tools.runtime.java.container.ContainerClassLoader;
import com.oracle.tools.runtime.java.util.CallableStaticMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An {@link ContainerBasedJavaApplicationBuilder} is a {@link com.oracle.tools.runtime.java.JavaApplicationBuilder}
 * that realizes {@link com.oracle.tools.runtime.java.JavaApplication}s isolated with in the current Java
 * Virtual Machine in the same manner as a regular Java EE application server
 * or container.
 * <p>
 * Scope of Application occurs through the use of a {@link ContainerBasedJavaProcess}
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
public class ContainerBasedJavaApplicationBuilder<A extends JavaApplication<A>, S extends JavaApplicationSchema<A, S>>
    extends AbstractJavaApplicationBuilder<A, S> implements JavaApplicationBuilder<A, S>
{
    /**
     * Constructs a {@link ContainerBasedJavaApplicationBuilder}.
     */
    public ContainerBasedJavaApplicationBuilder()
    {
        super();
    }


    /**
     * Sets if diagnostic information should be logged/output for {@link Application}s
     * produced by this builder.
     *
     * @param isDiagnosticsEnabled  should diagnostics be output
     *
     * @return  the builder (so that we can perform method chaining)
     */
    public ContainerBasedJavaApplicationBuilder setDiagnosticsEnabled(boolean isDiagnosticsEnabled)
    {
        m_isDiagnosticsEnabled = isDiagnosticsEnabled;

        return this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public A realize(S                  schema,
                     String             applicationName,
                     ApplicationConsole console) throws IOException
    {
        try
        {
            // establish the System Properties for the ContainerBasedJavaApplication
            Properties systemProperties = schema.getSystemPropertiesBuilder().realize();

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
            }

            // establish the ContainerBasedJavaProcess
            ContainerBasedJavaProcess process = new ContainerBasedJavaProcess(classLoader, controller);

            // start the process
            process.start();

            // the environment variables for the ContainerBasedJavaApplication
            // will be the environment variables for the Java Virtual Machine
            Properties environmentVariables = PropertiesBuilder.fromCurrentEnvironmentVariables().realize();

            // delegate Application creation to the Schema
            final A application = schema.createJavaApplication(process,
                                                               applicationName,
                                                               console,
                                                               environmentVariables,
                                                               systemProperties);

            // let interceptors know that the application has been realized
            raiseApplicationLifecycleEvent(application, Application.EventKind.REALIZED);

            return application;
        }
        catch (Exception e)
        {
            throw new IOException("Failed to start ContainerBasedJavaProcess", e);
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
         * @param application the {@link ControllableApplication} to start
         *
         * @return a {@link Future} that may be used to determine the status of
         *         or wait for the application to start
         */
        public Future<?> start(ControllableApplication application);


        /**
         * Asynchronously destroys a {@link ControllableApplication}.
         * <p>
         * Should an exception be raised during the execution of this method, the
         * application is assumed unusable and in an unknown state, including
         * that of being destroyed.
         *
         * @param application the {@link ControllableApplication} to destroy
         *
         * @return a {@link Future} that may be used to determine the status of
         *         or wait for the application to be destroyed
         */
        public Future<?> destroy(ControllableApplication application);
    }


    /**
     * A representation of a container-based application to allow it to be controlled
     * outside of the {@link ContainerClassLoader} in which they are scoped.
     */
    public static interface ControllableApplication
    {
        /**
         * Obtains the {@link ClassLoader} used to load and start the application.
         *
         * @return the application {@link ClassLoader}
         */
        public ClassLoader getClassLoader();


        /**
         * Submits the specified {@link Callable} for asynchronous execution with
         * in the context of the {@link #getClassLoader()} of the application,
         * returning a Future representing the result.
         * <p>
         * Note: When the {@link java.util.concurrent.Callable#call()} method is
         * invoked, the {@link Thread#getContextClassLoader()} will be that of
         * the {@link com.oracle.tools.runtime.java.ContainerBasedJavaApplicationBuilder.ControllableApplication#getClassLoader()}s
         *
         * @param callable  the {@link Callable} to submit
         * @param <T>       the return type of the {@link Callable}
         * @return          a Future providing a means to access the asynchronous
         *                  result of the invocation
         */
        public <T> Future<T> submit(Callable<T> callable);
    }


    /**
     * An implementation of a {@link JavaProcess} to represent and control a
     * Java application running with in a Java Virtual Machine, as part of a
     * container, much like a Java EE application.
     */
    public static class ContainerBasedJavaProcess implements JavaProcess, ControllableApplication
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
         * The {@link Future} that represents the result of starting the
         * application.  (may be null if the {@link ApplicationController}
         * didn't produce a {@link Future}).
         */
        private Future<?> m_startFuture;

        /**
         * The {@link Future} that represents the result of starting the
         * application.  (may be null if the {@link ApplicationController}
         * didn't produce a {@link Future}).
         */
        private Future<?> m_destroyFuture;


        /**
         * Constructs an {@link ContainerBasedJavaProcess}.
         *
         * @param classLoader           the {@link ClassLoader} in which to run the application
         * @param controller            the {@link ApplicationController}
         */
        public ContainerBasedJavaProcess(ContainerClassLoader  classLoader,
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


        /**
         * {@inheritDoc}
         */
        public ClassLoader getClassLoader()
        {
            return m_classLoader;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public long getId()
        {
            return -1;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public OutputStream getOutputStream()
        {
            return m_classLoader.getContainerScope().getStandardInputOutputStream();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream getInputStream()
        {
            return m_classLoader.getContainerScope().getStandardOutputInputStream();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public InputStream getErrorStream()
        {
            return m_classLoader.getContainerScope().getStandardErrorInputStream();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public int waitFor() throws InterruptedException
        {
            // here we simply try to wait for the application's start future
            // to complete executing.
            try
            {
                if (m_startFuture != null)
                {
                    m_startFuture.get();
                }
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }

            return 0;
        }


        /**
         * {@inheritDoc}
         */
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
                m_startFuture = null;
            }
            else
            {
                m_startFuture = m_controller.start(this);
            }
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void destroy()
        {
            if (m_controller != null)
            {
                try
                {
                    Future<?> future = m_controller.destroy(this);

                    if (future != null)
                    {
                        future.get();
                    }
                }
                catch (Exception e)
                {
                    // TODO: log this exception
                    e.printStackTrace();
                }

                m_controller = null;
            }

            // close the scope
            m_classLoader.getContainerScope().close();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public <T> Future<T> submit(final Callable<T> callable)
        {
            if (m_controller == null)
            {
                throw new IllegalStateException("Attempting to submit to a ContainerBasedJavaProcess that has been destroyed");
            }
            else
            {
                final ClassLoader classLoader     = m_classLoader;
                Callable<T>       scopingCallable = new Callable<T>()
                {
                    @Override
                    public T call() throws Exception
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

                            // then execute the Callable as usual
                            return callable.call();
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

                return m_executorService.submit(scopingCallable);
            }
        }
    }


    /**
     * An {@link ApplicationController} that will call specific methods to start
     * and destroy a {@link ContainerBasedJavaProcess}.
     */
    public static class CustomController implements ApplicationController
    {
        /**
         * The {@link CallableStaticMethod} to start the application.
         */
        private CallableStaticMethod<?> m_callableStartStaticMethod;

        /**
         * The {@link CallableStaticMethod} to destroy the application.
         */
        private CallableStaticMethod<?> m_callableDestroyStaticMethod;


        /**
         * Constructs a CustomController.
         *
         * @param callableStartStaticMethod    the {@link CallableStaticMethod} to
         *                                     start the application (may be null)
         */
        public CustomController(CallableStaticMethod<?> callableStartStaticMethod)
        {
            this(callableStartStaticMethod, null);
        }


        /**
         * Constructs a CustomController.
         *
         * @param callableStartStaticMethod    the {@link CallableStaticMethod} to
         *                                     start the application (may be null)
         * @param callableDestroyStaticMethod  the {@link CallableStaticMethod} to
         *                                     destroy the application (may be null)
         *
         */
        public CustomController(CallableStaticMethod<?> callableStartStaticMethod,
                                CallableStaticMethod<?> callableDestroyStaticMethod)
        {
            m_callableStartStaticMethod   = callableStartStaticMethod;
            m_callableDestroyStaticMethod = callableDestroyStaticMethod;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Future<?> start(ControllableApplication application)
        {
            return m_callableStartStaticMethod == null ? null : application.submit(m_callableStartStaticMethod);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Future<?> destroy(ControllableApplication application)
        {
            return m_callableDestroyStaticMethod == null ? null : application.submit(m_callableDestroyStaticMethod);
        }
    }


    /**
     * An {@link ApplicationController} that does nothing to a
     * {@link ContainerBasedJavaProcess}.
     */
    public static class NullController implements ApplicationController
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public Future<?> start(ControllableApplication application)
        {
            // SKIP: there's nothing to do to the application
            return null;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Future<?> destroy(ControllableApplication application)
        {
            // SKIP: there's nothing to do to the application
            return null;
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
        private String m_applicationClassName;

        /**
         * The arguments for the main method.
         */
        private List<String> m_arguments;


        /**
         * Constructs a StandardController.
         *
         * @param arguments  the arguments for the main method
         */
        public StandardController(String       applicationClassName,
                                  List<String> arguments)
        {
            m_applicationClassName = applicationClassName;
            m_arguments            = arguments == null ? new ArrayList<String>(0) : new ArrayList<String>(arguments);
        }


        /**
         * Obtains the name of the class that defines the application main method.
         *
         * @return the application class name
         */
        public String getApplicationClassName()
        {
            return m_applicationClassName;
        }


        /**
         * Obtains a copy of the arguments used to start the application.
         *
         * @return a list of arguments
         */
        public List<String> getArguments()
        {
            return new ArrayList<String>(m_arguments);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Future<?> start(ControllableApplication application)
        {
            Callable<Void> callable = new CallableStaticMethod<Void>(m_applicationClassName, "main", m_arguments);
            Future<Void>   future   = application.submit(callable);

            return future;
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public Future<?> destroy(ControllableApplication application)
        {
            // SKIP: there's no standard method to stop and destroy a
            // regular Java console application running in a container
            return null;
        }
    }
}
