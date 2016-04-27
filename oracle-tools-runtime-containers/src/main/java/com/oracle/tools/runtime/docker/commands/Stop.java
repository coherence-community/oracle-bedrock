/*
 * File: Stop.java
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

package com.oracle.tools.runtime.docker.commands;

import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;

import java.util.Arrays;
import java.util.List;

/**
 * A representation of the Docker stop command.
 * <p>
 * Instances of {@link Stop} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Stop} command return a
 * new instance of a {@link Stop} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Stop extends CommandWithArgumentList<Stop>
{
    /**
     * Create a {@link Stop} command to stop the specified list of containers.
     *
     * @param containers  the containers to stop
     */
    private Stop(List<?> containers)
    {
        super("stop", containers);
    }


    /**
     * Create a {@link Stop} command with the specified {@link Arguments}
     * and containers to stop.
     *
     * @param arguments   the command {@link Arguments}
     * @param containers  the containers to stop
     */
    private Stop(Arguments arguments, List<?> containers)
    {
        super(arguments, containers);
    }


    /**
     * Set the number of seconds for Docker to wait for the container to
     * stop before killing the container process.
     * <p>
     * Equates to the <code>--time</code> argument.
     *
     * @param time  the number of seconds for Docker to wait for
     *              the container to stop before killing the
     *              container process
     *
     * @return  a copy of this {@link Stop} command with the <code>--time</code>
     *          argument applied
     */
    public Stop timeUntilKill(int time)
    {
        return withCommandArguments(Argument.of("--time", '=', time));
    }


    @Override
    public Stop withCommandArguments(List<Argument> names, Argument... args)
    {
        return new Stop(getCommandArguments().with(args), names);
    }


    @Override
    protected Stop withoutCommandArguments(List<Argument> names, Argument... args)
    {
        return new Stop(getCommandArguments().without(args), names);
    }


    /**
     * Create a {@link Stop} command to stop the containers with the specified names.
     *
     * @param names  the values that will resolve to the names of the containers to stop
     *
     * @return  a {@link Stop} command to stop the containers with the specified names
     */
    public static Stop containers(Object... names)
    {
        return containers(Arrays.asList(names));
    }


    /**
     * Create a {@link Stop} command to stop the containers with the specified
     * {@link List} names.
     *
     * @param names  the {@link List} of values that will resolve to the names
     *               of the containers to stop
     *
     * @return  a {@link Stop} command to stop the containers with the specified names
     */
    public static Stop containers(List<?> names)
    {
        return new Stop(names);
    }
}
