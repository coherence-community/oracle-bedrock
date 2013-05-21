/*
 * File: AbstractApplicationSchema.java
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

package com.oracle.tools.runtime;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.TimeUnit;

/**
 * An {@link AbstractApplicationSchema} is a base implementation of an
 * {@link ApplicationSchema}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractApplicationSchema<A extends Application<A>, S extends ApplicationSchema<A, S>>
    implements ApplicationSchema<A, S>
{
    /**
     * The name of the executable that will be run
     */
    private String m_executableName;

    /**
     * The working directory for the application
     */
    private File m_workingDirectory;

    /**
     * The {@link PropertiesBuilder} containing the environment variables to
     * be used when realizing the {@link Application}.
     */
    private PropertiesBuilder m_propertiesBuilder;

    /**
     * Should the Error Stream be redirected to the Standard Output stream?
     */
    private boolean m_isErrorStreamRedirected;

    /**
     * Should diagnostic information be enabled for the {@link Application}s
     * produced by this {@link ApplicationSchema}.
     */
    private boolean m_isDiagnosticsEnabled;

    /**
     * The default {@link Application} timeout duration.
     */
    private long m_defaultTimeout;

    /**
     * The default {@link Application} timeout units
     */
    private TimeUnit m_defaultTimeoutUnits;

    /**
     * Should environment variables be inherited from the current executing process
     * for the {@link Application}s produced from this {@link ApplicationSchema}.
     */
    private boolean m_isEnvironmentInherited;

    /**
     * The arguments for the {@link Application}.
     */
    private ArrayList<String> m_applicationArguments;

    /**
     * The {@link LifecycleEventInterceptor}s for {@link Application}s
     * realized from the {@link ApplicationSchema}.
     */
    private LinkedList<LifecycleEventInterceptor<A>> m_lifecycleInterceptors;


    /**
     * Constructs an {@link AbstractApplicationSchema}.
     *
     * @param executableName  the name of the executable for the {@link Application}s
     *                        produced from this {@link ApplicationSchema}
     */
    public AbstractApplicationSchema(String executableName)
    {
        m_executableName          = executableName;
        m_propertiesBuilder       = new PropertiesBuilder();
        m_applicationArguments    = new ArrayList<String>();
        m_isErrorStreamRedirected = false;
        m_isDiagnosticsEnabled    = false;
        m_defaultTimeout          = 1;
        m_defaultTimeoutUnits     = TimeUnit.MINUTES;
        m_isEnvironmentInherited  = true;
        m_lifecycleInterceptors   = new LinkedList<LifecycleEventInterceptor<A>>();
    }


    /**
     * {@inheritDoc}
     */
    public String getExecutableName()
    {
        return m_executableName;
    }


    /**
     * {@inheritDoc}
     */
    public File getWorkingDirectory()
    {
        return m_workingDirectory;
    }


    /**
     * Sets the working directory in which the {@link Application} will start.
     *
     * @param  workingDirectory the working directory to use
     *
     * @return the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings({"unchecked"})
    public S setWorkingDirectory(File workingDirectory)
    {
        this.m_workingDirectory = workingDirectory;

        return (S) this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public PropertiesBuilder getEnvironmentVariablesBuilder()
    {
        return m_propertiesBuilder;
    }


    /**
     * Sets the specified environment variable to use an {@link Iterator} from
     * which to retrieve it's values.
     *
     * @param name      the name of the environment variable
     * @param iterator  an {@link Iterator} providing values for the environment
     *                  variable
     *
     * @return the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings("unchecked")
    public S setEnvironmentVariable(String      name,
                                    Iterator<?> iterator)
    {
        m_propertiesBuilder.setProperty(name, iterator);

        return (S) this;
    }


    /**
     * Sets the specified environment variable to the specified value.
     *
     * @param name   the name of the environment variable
     * @param value  the value of the environment variable
     *
     * @return the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings("unchecked")
    public S setEnvironmentVariable(String name,
                                    Object value)
    {
        m_propertiesBuilder.setProperty(name, value);

        return (S) this;
    }


    /**
     * Adds/Overrides the current environment variables with those specified by
     * the {@link PropertiesBuilder}.
     *
     * @param environmentVariablesBuilder  the environment variables to
     *                                     add/override on the {@link ApplicationBuilder}
     *
     * @return the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings("unchecked")
    public S setEnvironmentVariables(PropertiesBuilder environmentVariablesBuilder)
    {
        m_propertiesBuilder.addProperties(environmentVariablesBuilder);

        return (S) this;
    }


    /**
     * Clears the currently registered environment variables from the
     * {@link ApplicationBuilder}.
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings("unchecked")
    public S clearEnvironmentVariables()
    {
        m_propertiesBuilder.clear();

        return (S) this;
    }


    /**
     * Sets whether the environment variables from the currently executing
     * process should be inherited and used as the base environment variables
     * when realizing the {@link Application} from this {@link ApplicationSchema}.
     *
     * @param isInherited  <code>true</code> if the {@link ApplicationSchema}
     *                     should inherit the environment variables from the
     *                     currently executing process or <code>false</code>
     *                     if a clean/empty environment should be used
     *                     (containing only those variables defined by this
     *                     {@link ApplicationSchema})
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings({"unchecked"})
    public S setEnvironmentInherited(boolean isInherited)
    {
        this.m_isEnvironmentInherited = isInherited;

        return (S) this;
    }


    /**
     * {@inheritDoc}
     */
    public boolean isEnvironmentInherited()
    {
        return m_isEnvironmentInherited;
    }


    /**
     * Sets if diagnostic information should be logged/output for {@link Application}s
     * produced by this {@link ApplicationSchema}.
     *
     * @param isDiagnosticsEnabled  should diagnostics for the {@link Application}
     *                              be enabled
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    public S setDiagnosticsEnabled(boolean isDiagnosticsEnabled)
    {
        m_isDiagnosticsEnabled = isDiagnosticsEnabled;

        return (S) this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDiagnosticsEnabled()
    {
        return m_isDiagnosticsEnabled;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isErrorStreamRedirected()
    {
        return m_isErrorStreamRedirected;
    }


    /**
     * Sets whether the Standard Error Stream will be redirected to
     * Standard Out for {@link Application}s produced by this schema.
     *
     * @param isErrorStreamRedirected  <code>true</code> means redirect stderr to stdout
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    public S setErrorStreamRedirected(boolean isErrorStreamRedirected)
    {
        m_isErrorStreamRedirected = isErrorStreamRedirected;

        return (S) this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long getDefaultTimeout()
    {
        return m_defaultTimeout;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public TimeUnit getDefaultTimeoutUnits()
    {
        return m_defaultTimeoutUnits;
    }


    /**
     * Sets the timeout {@link Application}s produced using this
     * {@link ApplicationSchema} will use as a default.
     *
     * @param defaultTimeout      the default timeout duration
     * @param defaultTimeoutUnit  the default timeout duration {@link TimeUnit}
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings({"unchecked"})
    public S setDefaultTimout(long     defaultTimeout,
                              TimeUnit defaultTimeoutUnit)
    {
        m_defaultTimeout      = defaultTimeout;
        m_defaultTimeoutUnits = defaultTimeoutUnit;

        return (S) this;
    }


    /**
     * Adds an argument to use when starting the {@link Application}.
     *
     * @param argument The argument for the {@link Application}
     */
    public void addArgument(String argument)
    {
        m_applicationArguments.add(argument);
    }


    /**
     * {@inheritDoc}
     */
    public List<String> getArguments()
    {
        return m_applicationArguments;
    }


    /**
     * Adds an argument to use when starting the {@link Application}.
     *
     * @param argument  the argument for the {@link Application}
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings("unchecked")
    public S setArgument(String argument)
    {
        addArgument(argument);

        return (S) this;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<LifecycleEventInterceptor<A>> getLifecycleInterceptors()
    {
        return m_lifecycleInterceptors;
    }


    /**
     * Adds an {@link LifecycleEventInterceptor} to the {@link ApplicationSchema}
     * those of which will be executed when certain {@link LifecycleEvent}s
     * occur on {@link Application}s created with the {@link ApplicationSchema}.
     *
     * @param interceptor  the {@link LifecycleEventInterceptor}
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings("unchecked")
    public S addLifecycleInterceptor(LifecycleEventInterceptor<A> interceptor)
    {
        m_lifecycleInterceptors.add(interceptor);

        return (S) this;
    }
}
