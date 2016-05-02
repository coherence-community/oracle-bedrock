/*
 * File: DockerCommand.java
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
 * A representation of a generic Docker command.
 * <p>
 * Instances of {@link SimpleDockerCommand} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link SimpleDockerCommand} command return a
 * new instance of a {@link SimpleDockerCommand} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class SimpleDockerCommand extends AbstractDockerCommand<SimpleDockerCommand>
{
    /**
     * Create a {@link SimpleDockerCommand} command with the
     * specified {@link Arguments}.
     *
     * @param arguments  the command {@link Arguments}
     */
    private SimpleDockerCommand(Arguments arguments)
    {
        super(arguments);
    }


    @Override
    public SimpleDockerCommand withCommandArguments(Argument... args)
    {
        return new SimpleDockerCommand(getCommandArguments().with(args));
    }


    @Override
    public SimpleDockerCommand withoutCommandArguments(Argument... args)
    {
        return new SimpleDockerCommand(getCommandArguments().without(args));
    }


    /**
     * Create a {@link SimpleDockerCommand} with the specified command name.
     *
     * @param command  the name of the command
     *
     * @return  a {@link SimpleDockerCommand} with the specified command name
     */
    public static SimpleDockerCommand of(String command)
    {
        return new SimpleDockerCommand(Arguments.of(Argument.of(command)));
    }
}
