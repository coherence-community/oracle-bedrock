/*
 * File: Logs.java
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

import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;

import java.util.Collections;
import java.util.List;

/**
 * A representation of the Docker logs command.
 * <p>
 * Instances of {@link Logs} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Logs} command return a
 * new instance of a {@link Logs} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Logs extends CommandWithArgumentList<Logs>
{
    /**
     * Create a {@link Logs} command to retrieve the
     * logs for the specified container.
     *
     * @param name  a value that will resolve to a Docker
     *              container name
     */
    private Logs(Object name)
    {
        super("logs", Collections.singletonList(name));
    }


    /**
     * Create a {@link Logs} command with the specified {@link Arguments}
     * and container names.
     *
     * @param arguments  the command {@link Arguments}
     * @param names      the names of the containers
     */
    private Logs(Arguments arguments, List<?> names)
    {
        super(arguments, names);
    }


    /**
     * Follow log output (equates to the --follow argument).
     *
     * @return  a new {@link Logs} instance that is the same as this
     *          instance with the --follow option applied
     */
    public Logs follow(boolean enabled)
    {
        if (enabled)
        {
            return withCommandArguments(Argument.of("--follow"));
        }

        return withoutCommandArguments(Argument.of("--follow"));
    }


    /**
     * Show logs since timestamp (equates to the --since argument).
     *
     * @param   timestamp  a value that resolves to the timestamp
     *                     to use to determine the logs to show
     *
     * @return  a new {@link Logs} instance that is the same as this
     *          instance with the --since option applied
     */
    public Logs since(Object timestamp)
    {
        return withCommandArguments(Argument.of("--since", '=', timestamp));
    }


    /**
     * Show the specified number of lines to from the end of the
     * logs (equates to the --tail argument).
     *
     * @param   numberOfLines  a value that resolves to the number
     *                         of log lines to show, or equates to
     *                         "all" to show all lines
     *
     * @return  a new {@link Logs} instance that is the same as this
     *          instance with the --tail option applied
     */
    public Logs tail(Object numberOfLines)
    {
        return withCommandArguments(Argument.of("--tail", '=', numberOfLines));
    }


    /**
     * Create a {@link Logs} command to view the logs
     * from a specific container.
     *
     * @param containerName  the containe to view the logs for
     *
     * @return  a {@link Logs} command to view the logs
     *          from a specific container
     */
    public static Logs from(Object containerName)
    {
        return new Logs(containerName);
    }


    @Override
    protected Logs withCommandArguments(List<Argument> endArgs, Argument... args)
    {
        return new Logs(getCommandArguments().with(args), endArgs);
    }


    @Override
    protected Logs withoutCommandArguments(List<Argument> names, Argument... args)
    {
        return new Logs(getCommandArguments().without(args), names);
    }
}
