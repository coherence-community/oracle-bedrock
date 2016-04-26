/*
 * File: CommandWithList.java
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

package com.oracle.tools.runtime.containers.docker.commands;

import com.oracle.tools.Options;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An immutable {@link SimpleDockerCommand} with a list of {@link Argument}s
 * that will be appended to the end of the command line. For example
 * the command line <code>"docker command [args...] [end-args...]"</code>
 * the {@link Argument}s are applied to the end-args position. A
 * concrete example would be the Docker rm command to remove containers,
 * this command takes the form <code>docker rm [args...] [containers...]</code>
 * where there are some optional args, such as --force, but the container names
 * are always appended to the end of the command line.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class CommandWithArgumentList<C extends CommandWithArgumentList> extends AbstractDockerCommand<C>
{
    /**
     * The {@link List} of {@link Argument}s that will be appended
     * to the end of the command  line.
     */
    private List<Argument> argumentList;


    /**
     * Create a {@link CommandWithArgumentList}.
     *
     * @param command  the Docker command
     * @param argList  {@link List} of {@link Argument}s
     *                 that will be appended to the end of
     *                 the command  line
     */
    protected CommandWithArgumentList(String command, List<?> argList)
    {
        super(command);

        setArgList(argList);
    }

    /**
     * Create a {@link CommandWithArgumentList}.
     *
     * @param arguments  the command {@link Arguments}
     * @param argList    {@link List} of {@link Argument}s
     *                   that will be appended to the end of
     *                   the command  line
     */
    protected CommandWithArgumentList(Arguments arguments, List<?> argList)
    {
        super(arguments);

        setArgList(argList);
    }


    /**
     * Set the {@link List} of {@link Argument} using
     * the specified values.
     *
     * @param values  the {@link Argument}s or values that will
     *                become {@link Argument}s
     */
    private void setArgList(List<?> values)
    {
        this.argumentList = values.stream()
                          .filter((value) -> value != null)
                          .map(Argument::of)
                          .collect(Collectors.toList());
    }

    /**
     * Obtain a copy of this {@link CommandWithArgumentList} with the
     * addition of the specified command arguments {@link Argument}s.
     *
     * @param args  the additional {@link Argument}s
     *
     * @return  a copy of this {@link CommandWithArgumentList} with
     *          the addition of the specified {@link Argument}s
     */
    @Override
    public C withCommandArguments(Argument... args)
    {
        return withCommandArguments(argumentList, args);
    }


    @Override
    public C withoutCommandArguments(Argument... args)
    {
        return withoutCommandArguments(argumentList, args);
    }


    /**
     * Obtain a copy of this {@link CommandWithArgumentList} with the
     * addition of the specified command arguments {@link Argument}s.
     *
     * @param endArgs  the {@link List} of {@link Arguments} that appear at
     *                 the end of the command line
     * @param args     the new command to add
     *
     * @return  a copy of this {@link CommandWithArgumentList} with the
     *          addition of the specified command arguments {@link Argument}s
     */
    protected abstract C withCommandArguments(List<Argument> endArgs, Argument... args);


    /**
     * Obtain a copy of this {@link CommandWithArgumentList} without the
     * the specified command arguments {@link Argument}s.
     *
     * @param endArgs  the {@link List} of {@link Arguments} that appear at
     *                 the end of the command line
     * @param args     the new command to add
     *
     * @return  a copy of this {@link CommandWithArgumentList} without the
     *          specified command arguments {@link Argument}s
     */
    protected abstract C withoutCommandArguments(List<Argument> endArgs, Argument... args);


    /**
     * On launching add the {@link Argument}s to the
     * end of the command line arguments.
     *
     * @param platform   the {@link Platform} launching the command
     * @param options    the {@link Options}  for the command
     */
    @Override
    public void onFinalize(Platform platform, Options options)
    {
        // call super to add all of the command's arguments
        super.onFinalize(platform, options);

        // add this class's arguments to the end of the arguments list
        argumentList.forEach(options::add);
    }
}
