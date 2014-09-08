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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.options.Timeout;

import java.io.File;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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
     * Should the Error Stream be redirected to the Standard Output stream?
     */
    private boolean isErrorStreamRedirected;

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
     * The default {@link Option}s for use by {@link Application}s.
     */
    private Options options;


    /**
     * Constructs an {@link AbstractApplicationSchema} based on another
     * {@link ApplicationSchema}.
     *
     * @param schema  the other {@link ApplicationSchema}
     */
    public AbstractApplicationSchema(ApplicationSchema<A> schema)
    {
        this.executableName          = schema.getExecutableName();
        this.workingDirectory        = schema.getWorkingDirectory();
        this.isErrorStreamRedirected = schema.isErrorStreamRedirected();
        this.applicationArguments    = new ArrayList<String>(schema.getArguments());
        this.lifecycleInterceptors   = new LinkedList<LifecycleEventInterceptor<? super A>>();
        this.options                 = new Options(schema.getOptions().asArray());

        for (LifecycleEventInterceptor<? super A> interceptor : schema.getLifecycleInterceptors())
        {
            this.lifecycleInterceptors.add(interceptor);
        }
    }


    /**
     * Constructs an {@link AbstractApplicationSchema}.
     *
     * @param executableName   the name of the executable for the {@link Application}s
     *                         produced from this {@link ApplicationSchema}
     */
    public AbstractApplicationSchema(String executableName)
    {
        this.executableName          = executableName;
        this.applicationArguments    = new ArrayList<String>();
        this.isErrorStreamRedirected = false;
        this.lifecycleInterceptors   = new LinkedList<LifecycleEventInterceptor<? super A>>();
        this.options                 = new Options();

        // set default application options
        this.options.add(Timeout.autoDetect());
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


    /**
     * Sets the working directory in which the {@link Application} will start.
     *
     * @param  workingDirectory the working directory to use
     *
     * @return the {@link ApplicationSchema} (so that we can perform method chaining)
     */
    @SuppressWarnings("unchecked")
    public S setWorkingDirectory(File workingDirectory)
    {
        this.workingDirectory = workingDirectory;

        return (S) this;
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
    @SuppressWarnings("unchecked")
    public S setErrorStreamRedirected(boolean isErrorStreamRedirected)
    {
        this.isErrorStreamRedirected = isErrorStreamRedirected;

        return (S) this;
    }


    @Override
    public S addArgument(String argument)
    {
        applicationArguments.add(argument);

        return (S) this;
    }


    @Override
    public S addArguments(String... arguments)
    {
        if (arguments != null)
        {
            for (String argument : arguments)
            {
                applicationArguments.add(argument);
            }
        }

        return (S) this;
    }


    @Override
    public S addArguments(List<String> arguments)
    {
        if (arguments != null)
        {
            for (String argument : arguments)
            {
                applicationArguments.add(argument);
            }
        }

        return (S) this;
    }


    @Override
    public S setArguments(String... arguments)
    {
        applicationArguments.clear();

        return addArguments(arguments);
    }


    @Override
    public S setArguments(List<String> arguments)
    {
        applicationArguments.clear();

        return addArguments(arguments);
    }


    @Override
    public List<String> getArguments()
    {
        return applicationArguments;
    }


    /**
     * Adds an additional argument to use when starting the {@link Application}.
     *
     * @param argument  the additional argument for the {@link Application}
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     *
     * @deprecated  use {@link #addArgument(String)} instead
     */
    @Deprecated
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
    @SuppressWarnings("unchecked")
    public S addLifecycleInterceptor(LifecycleEventInterceptor<? super A> interceptor)
    {
        lifecycleInterceptors.add(interceptor);

        return (S) this;
    }


    @Override
    public Options getOptions()
    {
        return options;
    }


    @Override
    public S addOption(Option option)
    {
        options.add(option);

        return (S) this;
    }


    @Override
    public S addOptions(Option... options)
    {
        if (options != null)
        {
            for (Option option : options)
            {
                this.options.add(option);
            }
        }

        return (S) this;
    }


    @Override
    public S addOptionIfAbsent(Option option)
    {
        options.addIfAbsent(option);

        return (S) this;
    }


    @Override
    public S setOptions(Option... options)
    {
        this.options = new Options(options);

        return (S) this;
    }


    @Override
    public Options getPlatformSpecificOptions(Platform platform)
    {
        return new Options(options.asArray());
    }
}
