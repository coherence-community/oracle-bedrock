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
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;

import java.io.File;

import java.util.LinkedList;
import java.util.List;

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
     * The {@link ApplicationListener}s for {@link Application}s
     * realized from the {@link ApplicationSchema}.
     */
    private LinkedList<ApplicationListener<? super A>> listeners;

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
        this.executableName       = schema.getExecutableName();
        this.workingDirectory     = schema.getWorkingDirectory();
        this.listeners            = new LinkedList<ApplicationListener<? super A>>();
        this.options              = new Options(schema.getOptions());

        for (ApplicationListener<? super A> interceptor : schema.getApplicationListeners())
        {
            this.listeners.add(interceptor);
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
        this.executableName       = executableName;
        this.listeners            = new LinkedList<ApplicationListener<? super A>>();
        this.options              = new Options();

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
    public S addArgument(Object argument)
    {
        if (argument instanceof Argument)
        {
            options.add((Argument) argument);
        }
        else
        {
            options.add(Argument.of(argument));
        }

        return (S) this;
    }


    @Override
    public S addArguments(Object... arguments)
    {
        if (arguments == null || arguments.length == 0)
        {
            return (S) this;
        }

        Arguments current = options.get(Arguments.class);

        return addOption(current.with(arguments));
    }


    @Override
    public S addArguments(List<? extends Object> arguments)
    {
        if (arguments == null || arguments.isEmpty())
        {
            return (S) this;
        }

        Arguments current = options.get(Arguments.class);

        return addOption(current.with(arguments));
    }


    @Override
    public S setArguments(Object... arguments)
    {
        return addOption(Arguments.of(arguments));
    }


    @Override
    public S setArguments(List<?> arguments)
    {
        return addOption(Arguments.of(arguments));
    }


    /**
     * Adds an additional argument to use when starting the {@link Application}.
     *
     * @param argument  the additional argument for the {@link Application}
     *
     * @return  the {@link ApplicationSchema} (so that we can perform method chaining)
     *
     * @deprecated  use {@link FluentApplicationSchema#addArgument(Object)} instead
     */
    @Deprecated
    public S setArgument(String argument)
    {
        addArgument(argument);

        return (S) this;
    }


    @Override
    public S addApplicationListener(ApplicationListener<? super A> listener)
    {
        listeners.add(listener);

        return (S) this;
    }


    @Override
    public Iterable<ApplicationListener<? super A>> getApplicationListeners()
    {
        return listeners;
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
        Options newOptions = new Options(options);

        newOptions.addIfAbsent(this.options.get(Arguments.class));

        this.options = newOptions;

        return (S) this;
    }


    @Override
    public Options getPlatformSpecificOptions(Platform platform)
    {
        return new Options(options.asArray());
    }
}
