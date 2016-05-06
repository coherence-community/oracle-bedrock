/*
 * File: Kill.java
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

import java.util.Arrays;
import java.util.List;

/**
 * A representation of the Docker kill command.
 * <p>
 * The main process inside the container will be sent SIGKILL, or any signal
 * specified with option --signal.
 * <p>
 * Instances of {@link Kill} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Kill} command return a
 * new instance of a {@link Kill} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Kill extends CommandWithArgumentList<Kill>
{
    /**
     * Create a {@link Kill} command to kill the specified list of containers.
     *
     * @param containers  the containers to kill
     */
    private Kill(List<?> containers)
    {
        super("kill", containers);
    }


    /**
     * Create a {@link Kill} command with the specified {@link Arguments}
     * and containers to kill.
     *
     * @param arguments   the command {@link Arguments}
     * @param containers  the containers to kill
     */
    private Kill(Arguments arguments,
                 List<?>   containers)
    {
        super(arguments, containers);
    }


    /**
     * Set the signal to send to the container process.
     * <p>
     * Equates to the <code>--signal</code> argument.
     *
     * @param signal  the signal to send container process
     *
     * @return  a copy of this {@link Kill} command with the <code>--time</code>
     *          argument applied
     */
    public Kill signal(String signal)
    {
        return withCommandArguments(Argument.of("--signal", '=', signal));
    }


    @Override
    public Kill withCommandArguments(List<Argument> names,
                                     Argument...    args)
    {
        return new Kill(getCommandArguments().with(args), names);
    }


    @Override
    protected Kill withoutCommandArguments(List<Argument> names,
                                           Argument...    args)
    {
        return new Kill(getCommandArguments().without(args), names);
    }


    /**
     * Create a {@link Kill} command to kill the containers with the specified names.
     *
     * @param names  the values that will resolve to the names of the containers to kill
     *
     * @return  a {@link Kill} command to kill the containers with the specified names
     */
    public static Kill containers(Object... names)
    {
        return containers(Arrays.asList(names));
    }


    /**
     * Create a {@link Kill} command to kill the containers with the specified
     * {@link List} names.
     *
     * @param names  the {@link List} of values that will resolve to the names
     *               of the containers to kill
     *
     * @return  a {@link Kill} command to kill the containers with the specified names
     */
    public static Kill containers(List<?> names)
    {
        return new Kill(names);
    }
}
