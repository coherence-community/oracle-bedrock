/*
 * File: LocalJavaApplicationBuilder.java
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

import com.oracle.tools.deferred.AbstractDeferred;
import com.oracle.tools.deferred.DeferredHelper;
import com.oracle.tools.deferred.InstanceUnavailableException;
import com.oracle.tools.deferred.UnresolvableInstanceException;
import com.oracle.tools.io.NetworkHelper;
import com.oracle.tools.lang.StringHelper;
import com.oracle.tools.predicate.Predicate;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.LocalApplicationProcess;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.PropertiesBuilder;
import com.oracle.tools.runtime.Settings;
import com.oracle.tools.runtime.concurrent.ControllableRemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorServer;
import com.oracle.tools.util.CompletionListener;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.oracle.tools.predicate.Predicates.allOf;

/**
 * A {@link JavaApplicationBuilder} that realizes {@link JavaApplication}s as
 * external, non-child local operating system processes, by default using the
 * environment variables of the current system process.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LocalJavaApplicationBuilder<A extends JavaApplication> extends AbstractJavaApplicationBuilder<A>
{
    /**
     * The {@link Logger} for this class.
     */
    private static Logger LOGGER = Logger.getLogger(LocalJavaApplicationBuilder.class.getName());

    /**
     * Should environment variables be inherited from the current executing process
     * as the basis for {@link JavaApplication}s produced by this {@link LocalJavaApplicationBuilder}.
     */
    private boolean isEnvironmentInherited;

    /**
     * The {@link PropertiesBuilder} defining custom environment variables to
     * establish when realizing a {@link JavaApplication}.
     */
    private PropertiesBuilder environmentVariablesBuilder;

    /**
     * The path for the JAVA_HOME.
     * <p>
     * This is <code>null</code> if the JAVA_HOME of the {@link JavaApplicationSchema}
     * should be used.
     */
    private String javaHome;

    /**
     * Should processes be started in remote debug mode?
     * <p>
     * The default is <code>false</code>.
     */
    private boolean isRemoteDebuggingEnabled;

    /**
     * Should remote debugging processes be started in suspended mode?
     * <p>
     * The default is <code>false</code>.
     */
    private boolean isRemoteStartSuspended;

    /**
     * Should {@link JavaApplication}s produced by this builder allowed
     * to become orphans (when their parent application process is destroyed/killed)?
     * <p>
     * The default is <code>false</code>.
     */
    private boolean areOrphansPermitted;


    /**
     * Constructs a {@link LocalJavaApplicationBuilder}.
     */
    public LocalJavaApplicationBuilder()
    {
        super();

        // by default there are no custom environment variables
        environmentVariablesBuilder = new PropertiesBuilder();

        // by default we always inherit local environment variables
        isEnvironmentInherited = true;

        // use the JAVA HOME of the {@link JavaApplicationSchema}
        javaHome = null;

        // don't start in remote debug mode
        isRemoteDebuggingEnabled = false;

        // don't suspend when in remote debug mode
        isRemoteStartSuspended = false;

        // don't permit orphaned applications
        areOrphansPermitted = false;
    }


    /**
     * Obtains the {@link PropertiesBuilder} defining custom
     * {@link JavaApplication}-specific operating system environment
     * variables to be established when realizing an {@link JavaApplication}.
     *
     * @return {@link PropertiesBuilder}
     */
    public PropertiesBuilder getEnvironmentVariablesBuilder()
    {
        return environmentVariablesBuilder;
    }


    /**
     * Sets whether the environment variables from the currently executing
     * process should be inherited and used as the basis for environment variables
     * when realizing an {@link JavaApplication}.
     *
     * @param isEnvironmentInherited  <code>true</code> if the {@link LocalJavaApplicationBuilder}
     *                                should inherit the environment variables from the
     *                                currently executing process or <code>false</code>
     *                                if a clean/empty environment should be used
     *                                (containing only those custom variables defined by this
     *                                {@link LocalJavaApplicationBuilder} and an
     *                                {@link JavaApplicationSchema})
     *
     * @return this {@link LocalJavaApplicationBuilder} to permit fluent method calls
     */
    public LocalJavaApplicationBuilder setEnvironmentInherited(boolean isEnvironmentInherited)
    {
        this.isEnvironmentInherited = isEnvironmentInherited;

        return this;
    }


    /**
     * Determines if the environment variables of the currently executing
     * process will be inherited and used as a basis for environment variables
     * when realizing a {@link JavaApplication}.
     *
     * @return  <code>true</code> if environment variables are inherited from
     *          the current process when realizing a {@link JavaApplication} or
     *          <code>false</code> if a clean environment is used instead
     */
    public boolean isEnvironmentInherited()
    {
        return isEnvironmentInherited;
    }


    /**
     * Defines a custom environment variable for {@link JavaApplication}s
     * realized by this {@link LocalJavaApplicationBuilder} based on values
     * returned by the {@link Iterator}.
     *
     * @param name      the name of the environment variable
     * @param iterator  an {@link Iterator} providing values for the environment
     *                  variable
     *
     * @return this {@link LocalJavaApplicationBuilder} to permit fluent method calls
     */
    public LocalJavaApplicationBuilder setEnvironmentVariable(String      name,
                                                              Iterator<?> iterator)
    {
        environmentVariablesBuilder.setProperty(name, iterator);

        return this;
    }


    /**
     * Defines a custom environment variable for {@link JavaApplication}s
     * realized by this {@link LocalJavaApplicationBuilder}.
     *
     * @param name   the name of the environment variable
     * @param value  the value of the environment variable
     *
     * @return this {@link LocalJavaApplicationBuilder} to permit fluent method calls
     */
    public LocalJavaApplicationBuilder setEnvironmentVariable(String name,
                                                              Object value)
    {
        environmentVariablesBuilder.setProperty(name, value);

        return this;
    }


    /**
     * Sets if diagnostic information should be logged/output for {@link Application}s
     * produced by this builder.
     *
     * @param isDiagnosticsEnabled  should diagnostics be output
     *
     * @return  the builder (so that we can perform method chaining)
     */
    public LocalJavaApplicationBuilder setDiagnosticsEnabled(boolean isDiagnosticsEnabled)
    {
        this.m_isDiagnosticsEnabled = isDiagnosticsEnabled;

        return this;
    }


    /**
     * Sets the JAVA_HOME to be used to realize the {@link JavaApplication}.
     *
     * @param javaHome  the value for the JAVA_HOME environment variable
     *                  or <code>null</code> if {@link JavaApplicationSchema}
     *                  value should be used instead
     *
     * @return  the builder (so that we can perform method chaining)
     */
    public LocalJavaApplicationBuilder setJavaHome(String javaHome)
    {
        this.javaHome = javaHome;

        return this;
    }


    /**
     * Sets if remote Java debugging is enabled for the next {@link JavaApplication}
     * produced by this builder.
     *
     * @param isRemoteDebuggingEnabled <code>true</code> to enable remote debugging
     *
     * @return  the {@link LocalJavaApplicationBuilder} to allow fluent-method calls
     */
    public LocalJavaApplicationBuilder setRemoteDebuggingEnabled(boolean isRemoteDebuggingEnabled)
    {
        this.isRemoteDebuggingEnabled = isRemoteDebuggingEnabled;

        return this;
    }


    /**
     * Determines if the next {@link JavaApplication} should be started in
     * remote debugging mode.
     *
     * @return <code>true</code> if remote debugging is enabled
     */
    public boolean isRemoteDebuggingEnabled()
    {
        return isRemoteDebuggingEnabled;
    }


    /**
     * Sets if a remote debugging process should be started in a suspended mode.
     *
     * @param isRemoteStartSuspended  <code>true</code> to start in a suspended mode
     *
     * @return  the {@link LocalJavaApplicationBuilder} to allow fluent-method calls
     */
    public LocalJavaApplicationBuilder setRemoteDebuggingStartSuspended(boolean isRemoteStartSuspended)
    {
        this.isRemoteStartSuspended = isRemoteStartSuspended;

        return this;
    }


    /**
     * Determines if a remotely debugged application will be started in suspend
     * mode.
     *
     * @return <code>true</code> if started in suspend mode
     */
    public boolean isRemoteDebuggingStartSuspended()
    {
        return isRemoteStartSuspended;
    }


    /**
     * Sets if {@link JavaApplication}s produced by this {@link JavaApplicationBuilder}
     * can be orphaned (left running without their parent running).  The default
     * is <code>false</code>.
     *
     * @param areOrphansPermitted  <code>true</code> to allow for orphaned applications
     *
     * @return  the {@link LocalJavaApplicationBuilder} to allow fluent-method calls
     */
    public LocalJavaApplicationBuilder setOrphansPermitted(boolean areOrphansPermitted)
    {
        this.areOrphansPermitted = areOrphansPermitted;

        return this;
    }


    /**
     * Determines if {@link JavaApplication}s produced by this {@link JavaApplicationBuilder}
     * are allowed to be orphaned (to keep running if their parent is not running).
     *
     * @return  <code>true</code> if applications can be orphaned, <code>false</code> otherwise
     */
    public boolean areOrphansPermitted()
    {
        return areOrphansPermitted;
    }


    @Override
    public <T extends A, S extends ApplicationSchema<T>> T realize(S                  applicationSchema,
                                                                   String             applicationName,
                                                                   ApplicationConsole console,
                                                                   Platform           platform)
    {
        // TODO: this should be a safe cast but we should also check to make sure
        JavaApplicationSchema<T> schema = (JavaApplicationSchema) applicationSchema;

        // ---- establish the underlying ProcessBuilder -----

        // we'll use the native operating system process builder to create
        // and manage the local application process
        ProcessBuilder processBuilder = new ProcessBuilder(schema.getExecutableName());

        // ----- establish the working directory -----

        // set the working directory for the Process
        File directory = schema.getWorkingDirectory();

        if (directory != null)
        {
            processBuilder.directory(schema.getWorkingDirectory());
        }

        // ----- establish environment variables -----

        // when not inheriting we need to clear the defined environment
        if (!isEnvironmentInherited() &&!schema.isEnvironmentInherited())
        {
            processBuilder.environment().clear();
        }

        // add the environment variables defined by the schema
        Properties environmentVariables = schema.getEnvironmentVariables(platform);

        for (String variableName : environmentVariables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, environmentVariables.getProperty(variableName));
        }

        // add the environment variables defined by the builder
        environmentVariables = getEnvironmentVariablesBuilder().realize();

        for (String variableName : environmentVariables.stringPropertyNames())
        {
            processBuilder.environment().put(variableName, environmentVariables.getProperty(variableName));
        }

        // ----- establish java specific environment variables -----

        // by default we use the java home defined by the schema.  if that's not
        // defined we'll attempt to use the java home defined by this builder.
        String javaHome = this.javaHome == null ? schema.getJavaHome() : this.javaHome;

        // when we still don't have a java home we use what this process defines
        // (using the system property)
        if (javaHome == null)
        {
            javaHome = System.getProperty("java.home", null);
        }

        if (javaHome != null)
        {
            processBuilder.environment().put("JAVA_HOME", javaHome);
        }

        // set the class path (it's an environment variable)
        String classPath = schema.getClassPath().toString();

        processBuilder.environment().put("CLASSPATH", classPath);

        // ----- establish the command to start java -----

        if (javaHome == null)
        {
            // when we don't have a java home we just use the defined executable
            processBuilder.command(schema.getExecutableName());
        }
        else
        {
            // when we have a java home, we prefix the executable name with the java.home/bin/
            javaHome = javaHome.trim();

            if (!javaHome.endsWith(File.separator))
            {
                javaHome = javaHome + File.separator;
            }

            processBuilder.command(javaHome + "bin" + File.separator + schema.getExecutableName());
        }

        // ----- establish the system properties for the java application -----

        // define the system properties based on those defined by the schema
        Properties systemProperties = schema.getSystemProperties(platform);

        for (String propertyName : systemProperties.stringPropertyNames())
        {
            String propertyValue = systemProperties.getProperty(propertyName);

            // filter out (don't set) system properties that start with "oracletools"
            // (we don't want to have "parents" applications effect child applications
            if (!propertyName.startsWith("oracletools"))
            {
                processBuilder.command().add("-D" + propertyName
                                             + (propertyValue.isEmpty()
                                                ? "" : "=" + StringHelper.doubleQuoteIfNecessary(propertyValue)));
            }
        }

        // ----- establish Oracle Tools specific system properties -----

        // configure a server channel to communicate with the native process
        final RemoteExecutorServer server = new RemoteExecutorServer();

        try
        {
            server.open();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create remote execution server for the application", e);
        }

        // add Oracle Tools specific System Properties
        Predicate<InetAddress> preferred = schema.isIPv4Preferred() ? allOf(NetworkHelper.IPv4_ADDRESS,
                                                                            NetworkHelper
                                                                                .NON_LOOPBACK_ADDRESS) : NetworkHelper
                                                                                    .DEFAULT_ADDRESS;

        processBuilder.command().add("-D" + Settings.PARENT_ADDRESS + "="
                                     + server.getInetAddress(preferred).getHostAddress());
        processBuilder.command().add("-D" + Settings.PARENT_PORT + "=" + server.getPort());
        processBuilder.command().add("-D" + Settings.ORPHANABLE + "=" + areOrphansPermitted());

        // ----- establish JVM options -----

        for (String option : schema.getJvmOptions())
        {
            processBuilder.command().add("-" + option);
        }

        // add debug option
        if (this.isRemoteDebuggingEnabled)
        {
            // determine a free debug port
            int debugPort = LocalPlatform.getInstance().getAvailablePorts().next();

            // construct the Java option
            String option = String.format("-agentlib:jdwp=transport=dt_socket,server=y,suspend=%s,address=%d",
                                          (isRemoteStartSuspended ? "y" : "n"),
                                          debugPort);

            processBuilder.command().add(option);
        }

        // ----- establish the application command line to execute -----

        // use the launcher to launch the application
        // (we don't start the application directly itself)
        processBuilder.command().add("com.oracle.tools.runtime.java.JavaApplicationLauncher");

        // set the Java application class name we need to launch
        processBuilder.command().add(schema.getApplicationClassName());

        // add the arguments to the command for the process
        for (String argument : schema.getArguments())
        {
            processBuilder.command().add(argument);
        }

        // should the standard error be redirected to the standard out?
        processBuilder.redirectErrorStream(schema.isErrorStreamRedirected());

        // ----- start the local process -----

        if (LOGGER.isLoggable(Level.INFO))
        {
            StringBuilder commandBuilder = new StringBuilder();

            for (String command : processBuilder.command())
            {
                commandBuilder.append(command);
                commandBuilder.append(" ");
            }

            LOGGER.log(Level.INFO, "Starting Local Process: " + commandBuilder.toString());
        }

        // create and start the native process
        Process process;

        try
        {
            process = processBuilder.start();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to build the underlying native process for the application", e);
        }

        // ----- create the local process and application -----

        // establish a LocalJavaProcess to represent the underlying Process
        LocalJavaProcess localJavaProcess = new LocalJavaProcess(process, server);

        // delegate Application creation to the Schema
        final T application = schema.createJavaApplication(localJavaProcess,
                                                           applicationName,
                                                           console,
                                                           environmentVariables,
                                                           systemProperties);

        // ensure that the launcher process connects back
        DeferredHelper.ensure(new AbstractDeferred<Boolean>()
        {
            @Override
            public Boolean get() throws UnresolvableInstanceException, InstanceUnavailableException
            {
                if (!server.getRemoteExecutors().iterator().hasNext())
                {
                    throw new InstanceUnavailableException(this);
                }
                else
                {
                    return true;
                }
            }
        });

        // ----- notify all of the lifecycle listeners -----

        // let interceptors know that the application has been realized
        raiseApplicationLifecycleEvent(application, Application.EventKind.REALIZED);

        return application;
    }


    /**
     * A {@link LocalApplicationProcess} specifically for Java-based applications.
     */
    public static class LocalJavaProcess extends LocalApplicationProcess implements JavaProcess
    {
        /**
         * The {@link RemoteExecutor} for the {@link LocalJavaProcess}.
         */
        private ControllableRemoteExecutor remoteExecutor;


        /**
         * Constructs a {@link LocalJavaProcess}.
         *
         * @param process         the underlying operating system {@link Process}
         * @param remoteExecutor  the {@link ControllableRemoteExecutor} that may be used
         *                        to submit and control the process remotely
         */
        public LocalJavaProcess(Process                    process,
                                ControllableRemoteExecutor remoteExecutor)
        {
            super(process);

            this.remoteExecutor = remoteExecutor;
        }


        @Override
        public <T> void submit(RemoteCallable<T>     callable,
                               CompletionListener<T> listener)
        {
            remoteExecutor.submit(callable, listener);
        }


        @Override
        public void submit(RemoteRunnable runnable) throws IllegalStateException
        {
            remoteExecutor.submit(runnable);
        }


        @Override
        public void close()
        {
            super.close();

            remoteExecutor.close();
        }
    }
}
