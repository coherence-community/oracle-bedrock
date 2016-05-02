/*
 * File: AbstractDockerCommand.java
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

package com.oracle.bedrock.runtime.docker.commands;

import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.SimpleApplication;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.Executable;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.docker.Docker;
import com.oracle.bedrock.runtime.MetaClass;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A {@link MetaClass} representing a Docker command.
 * <p>
 * Instances of {@link AbstractDockerCommand} are <strong>immutable</strong>.
 * All mutating operations return new instances of {@link AbstractDockerCommand}
 * with the mutations applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class AbstractDockerCommand<C extends AbstractDockerCommand> implements MetaClass<Application>
{
    /**
     * The {@link Argument}s representing the options for the command.
     */
    private Arguments commandArguments;

    /**
     * The {@link Timeout} to use when executing the command.
     */
    private Timeout timeout = Timeout.after(2, TimeUnit.MINUTES);


    /**
     * Constructs a {@link AbstractDockerCommand} for a given
     * Docker command.
     *
     * @param command  the Docker command that will be executed
     */
    public AbstractDockerCommand(String command)
    {
        this(Arguments.of(Argument.of(command)));
    }


    /**
     * Constructs a {@link AbstractDockerCommand} given the
     * specified {@link Arguments}.
     *
     * @param commandArguments  the {@link Argument}s representing the options for the command
     */
    public AbstractDockerCommand(Arguments commandArguments)
    {
        this.commandArguments = (commandArguments != null) ? commandArguments : Arguments.empty();
    }


    @Override
    public Class<? extends Application> getImplementationClass(Platform platform, Options options)
    {
        return SimpleApplication.class;
    }


    @Override
    public void onLaunching(Platform platform, Options options)
    {
        // there is nothing to do here
    }


    @Override
    public void onLaunch(Platform platform, Options options)
    {
        Docker environment = options.get(Docker.class);
        Arguments arguments   = Arguments.of(environment.getArguments())
                                         .with(commandArguments);

        // Set the executable name to "docker"
        options.add(Executable.named(environment.getDockerExecutable()));

        // Add the environment variables from the Docker environment
        environment.getEnvironmentVariables().forEach(options::add);

        // Set the arguments to the environment arguments plus this commands arguments
        options.add(arguments);

        // set the timeout if not already set
        Timeout timeout = getTimeout();

        if (timeout != null)
        {
            options.addIfAbsent(timeout);
        }
    }


    @Override
    public void onLaunched(Platform platform, Application application, Options options)
    {
        // there is nothing to do here
    }


    /**
     * Obtain a copy of this {@link AbstractDockerCommand} with the
     * addition of the specified command arguments {@link Argument}s.
     * <p>
     * Command arguments in Docker come after the command name on the
     * command line, e.g. docker [options...] COMMAND_NAME [args...]
     * Parts of the Docker documentation that refer to these parameters
     * as both arguments and options. In our case we refer to them as
     * arguments and the are applied after the command name on the
     * command line.
     *
     * @param args  the additional {@link Argument}s
     *
     * @return  a copy of this {@link AbstractDockerCommand} with
     *          the addition of the specified {@link Argument}s
     */
    public abstract C withCommandArguments(Argument... args);


    /**
     * Obtain a copy of this {@link AbstractDockerCommand} without the
     * specified command arguments {@link Argument}s.
     * <p>
     * Command arguments in Docker come after the command name on the
     * command line, e.g. docker [options...] COMMAND_NAME [args...]
     * Parts of the Docker documentation that refer to these parameters
     * as both arguments and options. In our case we refer to them as
     * arguments and the are applied after the command name on the
     * command line.
     *
     * @param args  the additional {@link Argument}s
     *
     * @return  a copy of this {@link AbstractDockerCommand} without
     *          the specified {@link Argument}s
     */
    public abstract C withoutCommandArguments(Argument... args);


    /**
     * Obtain the {@link Arguments} for this command.
     *
     * @return  the {@link Arguments} for this command
     */
    public Arguments getCommandArguments()
    {
        return commandArguments;
    }


    /**
     * Set the {@link Timeout} to use when running the command.
     *
     * @param duration  the duration for the {@link Timeout}
     * @param units     the {@link TimeUnit}s for the duration of the {@link Timeout}
     *
     * @return  a copy of this {@link AbstractDockerCommand} with
     *          the addition of the specified timeout
     */
    public C timeoutAfter(long duration, TimeUnit units)
    {
        return timeoutAfter(Timeout.after(duration, units));
    }


    /**
     * Set the {@link Timeout} to use when running the command.
     *
     * @param timeout  the {@link Timeout} to use when executing the command
     *
     * @return  a copy of this {@link AbstractDockerCommand} with
     *          the addition of the specified timeout
     */
    public C timeoutAfter(Timeout timeout)
    {
        this.timeout = Objects.requireNonNull(timeout, "The timeout cannot be null");

        return (C) this;
    }


    /**
     * Obtain the {@link Timeout} to use when executing the command.
     *
     * @return  the {@link Timeout} to use when executing the command
     */
    public Timeout getTimeout()
    {
        return timeout;
    }
}
