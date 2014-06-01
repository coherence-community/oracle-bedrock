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

import com.oracle.tools.runtime.java.FluentJavaApplicationSchema;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.concurrent.TimeUnit;

/**
 * A base implementation of a {@link FluentApplicationSchema}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application} that can be configured by the {@link ApplicationSchema}
 * @param <S>  the type of {@link AbstractApplicationSchema} that will be returned from fluent methods
 */
public abstract class AbstractApplicationSchema<A extends Application, S extends AbstractApplicationSchema<A, S>>
    implements FluentApplicationSchema<A, S>
{
    /**
     * The name of the executable that will be run.
     */
    private String executableName;

    /**
     * The working directory for the application.
     */
    private File workingDirectory;

    /**
     * The {@link PropertiesBuilder} defining custom environment variables to
     * establish when realizing the {@link Application} using this
     * {@link ApplicationSchema}.
     */
    private PropertiesBuilder environmentVariablesBuilder;

    /**
     * Should the Error Stream be redirected to the Standard Output stream?
     */
    private boolean isErrorStreamRedirected;

    /**
     * Should diagnostic information be enabled for the {@link Application}s
     * produced by this {@link ApplicationSchema}.
     */
    private boolean isDiagnosticsEnabled;

    /**
     * The default {@link Application} timeout duration.
     */
    private long defaultTimeout;

    /**
     * The default {@link Application} timeout units
     */
    private TimeUnit defaultTimeoutUnits;

    /**
     * Should environment variables be inherited from the current executing process
     * for the {@link Application}s produced from this {@link ApplicationSchema}.
     */
    private boolean isEnvironmentInherited;

    /**
     * The arguments for the {@link Application}.
     */
    private ArrayList<String> applicationArguments;

    /**
     * The {@link LifecycleEventInterceptor}s for {@link Application}s
     * realized from the {@link ApplicationSchema}.
     */
    private LinkedList<LifecycleEventInterceptor<? super A>> lifecycleInterceptors;


    /**
     * Constructs an {@link AbstractApplicationSchema}.
     *
     * @param executableName  the name of the executable for the {@link Application}s
     *                        produced from this {@link ApplicationSchema}
     */
    public AbstractApplicationSchema(String executableName)
    {
        this.executableName              = executableName;
        this.environmentVariablesBuilder = new PropertiesBuilder();
        this.isEnvironmentInherited      = false;
        this.applicationArguments        = new ArrayList<String>();
        this.isErrorStreamRedirected     = false;
        this.isDiagnosticsEnabled        = false;
        this.defaultTimeout              = 1;
        this.defaultTimeoutUnits         = TimeUnit.MINUTES;
        this.lifecycleInterceptors       = new LinkedList<LifecycleEventInterceptor<? super A>>();
    }


    @Override
    public String getExecutableName()
    {
        return executableName;
    }


    @Override
    public File getWorkingDirectory()
    {
        return workingDirectory;
    }


    @Override
    public PropertiesBuilder getEnvironmentVariablesBuilder()
    {
        return environmentVariablesBuilder;
    }


    /**
     * Sets the working directory in which the {@link Application} will start.
     *
     * @param  workingDirectory the working directory to use
     *
     * @return the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    public S setWorkingDirectory(File workingDirectory)
    {
        this.workingDirectory = workingDirectory;

        return (S) this;
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
    public S setEnvironmentVariable(String      name,
                                    Iterator<?> iterator)
    {
        environmentVariablesBuilder.setProperty(name, iterator);

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
    public S setEnvironmentVariable(String name,
                                    Object value)
    {
        environmentVariablesBuilder.setProperty(name, value);

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
    public S setEnvironmentVariables(PropertiesBuilder environmentVariablesBuilder)
    {
        this.environmentVariablesBuilder.addProperties(environmentVariablesBuilder);

        return (S) this;
    }


    /**
     * Clears the custom environment variables defined for the {@link ApplicationSchema}.
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @Deprecated
    public S clearEnvironmentVariables()
    {
        environmentVariablesBuilder.clear();

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
    @Deprecated
    public S setEnvironmentInherited(boolean isInherited)
    {
        this.isEnvironmentInherited = isInherited;

        return (S) this;
    }


    @Deprecated
    public boolean isEnvironmentInherited()
    {
        return isEnvironmentInherited;
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
        this.isDiagnosticsEnabled = isDiagnosticsEnabled;

        return (S) this;
    }


    @Override
    public boolean isDiagnosticsEnabled()
    {
        return isDiagnosticsEnabled;
    }


    @Override
    public boolean isErrorStreamRedirected()
    {
        return isErrorStreamRedirected;
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
        this.isErrorStreamRedirected = isErrorStreamRedirected;

        return (S) this;
    }


    @Override
    public long getDefaultTimeout()
    {
        return defaultTimeout;
    }


    @Override
    public TimeUnit getDefaultTimeoutUnits()
    {
        return defaultTimeoutUnits;
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
    public S setDefaultTimeout(long     defaultTimeout,
                               TimeUnit defaultTimeoutUnit)
    {
        this.defaultTimeout      = defaultTimeout;
        this.defaultTimeoutUnits = defaultTimeoutUnit;

        return (S) this;
    }


    /**
     * Adds an argument to use when starting the {@link Application}.
     *
     * @param argument The argument for the {@link Application}
     */
    public void addArgument(String argument)
    {
        applicationArguments.add(argument);
    }


    @Override
    public List<String> getArguments()
    {
        return applicationArguments;
    }


    /**
     * Adds an argument to use when starting the {@link Application}.
     *
     * @param argument  the argument for the {@link Application}
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    public S setArgument(String argument)
    {
        addArgument(argument);

        return (S) this;
    }


    @Override
    public Iterable<LifecycleEventInterceptor<? super A>> getLifecycleInterceptors()
    {
        return lifecycleInterceptors;
    }


    @Override
    public S addLifecycleInterceptor(LifecycleEventInterceptor<? super A> interceptor)
    {
        lifecycleInterceptors.add(interceptor);

        return (S) this;
    }
}
