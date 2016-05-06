/*
 * File: Events.java
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

/**
 * A representation of the Docker events command.
 * <p>
 * Instances of {@link Events} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Events} command return a
 * new instance of a {@link Events} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Events extends AbstractDockerCommand<Events>
{
    /**
     * Create a {@link Events} command to view Docker events.
     */
    private Events()
    {
        super("events");
    }


    /**
     * Create an {@link Events} command with the specified {@link Arguments}
     *
     * @param arguments  the command {@link Arguments}
     */
    private Events(Arguments arguments)
    {
        super(arguments);
    }


    /**
     * Filter the event output (equates to the --filter argument).
     *
     * @param filters  values that resolve to one or more valid filter arguments
     *
     * @return  a new {@link Events} instance that is the same as this
     *          instance with the --filter option applied
     */
    public Events filter(Object... filters)
    {
        return withCommandArguments(Argument.of("--filter", '=', new Argument.Multiple(filters)));
    }


    /**
     * Show events since timestamp (equates to the --since argument).
     *
     * @param   timestamp  a value that resolves to the timestamp
     *                     to use to determine the events to show
     *
     * @return  a new {@link Events} instance that is the same as this
     *          instance with the --since option applied
     */
    public Events since(Object timestamp)
    {
        return withCommandArguments(Argument.of("--since", '=', timestamp));
    }


    /**
     * Show events until timestamp (equates to the --until argument).
     *
     * @param   timestamp  a value that resolves to the timestamp
     *                     to use to determine the events to show
     *
     * @return  a new {@link Events} instance that is the same as this
     *          instance with the --until option applied
     */
    public Events until(Object timestamp)
    {
        return withCommandArguments(Argument.of("--until", '=', timestamp));
    }


    /**
     * Create a {@link Events} command to view the events
     * from a specific container.
     *
     * @param containerName  the container to view the events for
     *
     * @return  a {@link Events} command to view the events
     *          from a specific container
     */
    public static Events fromContainer(Object containerName)
    {
        return new Events().filter("container=" + containerName);
    }


    /**
     * Create a {@link Events} command to view Docker events.
     *
     * @return  a {@link Events} command to view Docker events
     */
    public static Events all()
    {
        return new Events();
    }


    @Override
    public Events withCommandArguments(Argument... args)
    {
        return new Events(getCommandArguments().with(args));
    }


    @Override
    public Events withoutCommandArguments(Argument... args)
    {
        return new Events(getCommandArguments().without(args));
    }
}
