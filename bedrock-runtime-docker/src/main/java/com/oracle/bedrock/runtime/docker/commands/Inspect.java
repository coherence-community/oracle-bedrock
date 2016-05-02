/*
 * File: Inspect.java
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
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.console.Console;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.docker.Docker;
import com.oracle.bedrock.runtime.options.Arguments;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * A representation of the Docker inspect command.
 * <p>
 * Instances of {@link Inspect} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Inspect} command return a
 * new instance of a {@link Inspect} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Inspect extends CommandWithArgumentList<Inspect>
{
    /**
     * Create an {@link Inspect} command with the specified {@link Arguments}
     * and containers or images to inspect.
     *
     * @param names      the containers or images to inspect
     */
    private Inspect(List<?> names)
    {
        super("inspect", names);
    }


    /**
     * Create a {@link Remove} command with the specified {@link Arguments}
     * and items to be removed.
     *
     * @param arguments  the command {@link Arguments}
     * @param names      the names of the items to remove
     */
    private Inspect(Arguments arguments,
                    List<?>   names)
    {
        super(arguments, names);
    }


    /**
     * Format the output using the given go template.
     *
     * @param format  the template to use to format the output.
     *
     * @return  a copy of this {@link Inspect} command with the specified format
     */
    public Inspect format(String format)
    {
        if (format == null || format.trim().isEmpty())
        {
            return this;
        }

        return withCommandArguments(Argument.of("--format", '=', format));
    }


    /**
     * Display total file sizes if the type is container.
     *
     * @param include  <code>true</code> to include sizes
     *
     * @return  a copy of this {@link Inspect} command with the
     *          specified size argument
     */
    public Inspect includeSizes(boolean include)
    {
        if (include)
        {
            return withCommandArguments(Argument.of("--size"));
        }

        return withoutCommandArguments(Argument.of("--size"));
    }


    @Override
    protected Inspect withCommandArguments(List<Argument> names,
                                           Argument...    args)
    {
        return new Inspect(getCommandArguments().with(args), names);
    }


    @Override
    protected Inspect withoutCommandArguments(List<Argument> names,
                                              Argument...    args)
    {
        return new Inspect(getCommandArguments().without(args), names);
    }


    /**
     * Create an {@link Inspect} command to inspect the image with the specified name.
     *
     * @param names  the name of the image to inspect
     *
     * @return  an {@link Inspect} command to inspect the image with the specified name
     */
    public static Inspect image(List<?> names)
    {
        return new Inspect(names).withCommandArguments(Argument.of("--type", '=', "image"));
    }


    /**
     * Create an {@link Inspect} command to inspect the images with the specified names.
     *
     * @param names  the names of the image to inspect
     *
     * @return  an {@link Inspect} command to inspect the images with the specified name
     */
    public static Inspect image(Object... names)
    {
        return image(Arrays.asList(names));
    }


    /**
     * Create an {@link Inspect} command to inspect the container with the specified name.
     *
     * @param names  the name of the container to inspect
     *
     * @return  an {@link Inspect} command to inspect the container with the specified name
     */
    public static Inspect container(List<?> names)
    {
        return new Inspect(names).withCommandArguments(Argument.of("--type", '=', "container"));
    }


    /**
     * Create an {@link Inspect} command to inspect the images with the specified names.
     *
     * @param names  the names of the image to inspect
     *
     * @return  an {@link Inspect} command to inspect the images with the specified name
     */
    public static Inspect container(Object... names)
    {
        return container(Arrays.asList(names));
    }


    /**
     * Execute this {@link Inspect} command on the specified {@link Platform}
     * using the specified {@link Docker} environment.
     *
     * @param platform     the {@link Platform} to use to execute the command
     * @param environment  the {@link Docker} environment to use
     *
     * @return  a {@link JsonArray} containing the results of the inspect command
     */
    public JsonValue run(Platform platform,
                         Docker   environment)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application app = platform.launch(this, environment, Console.of(console)))
        {
            if (app.waitFor() != 0)
            {
                console.getCapturedOutputLines().forEach(System.out::println);
                console.getCapturedErrorLines().forEach(System.err::println);
                return null;
            }
        }

        Queue<String> lines = console.getCapturedOutputLines();
        String        json  = lines.stream()
                                   .filter((line) -> line != null && !line.equals("(terminated)"))
                                   .collect(Collectors.joining("\n"))
                                                      .trim();

        if (!json.startsWith("[") &&!json.startsWith("{"))
        {
            json = "[" + json + "]";
        }

        JsonReader reader = Json.createReader(new StringReader(json));

        try
        {
            return reader.read();
        }
        catch (Exception e)
        {
            System.err.println("Error parsing JSON");
            System.err.println(json);
            e.printStackTrace();

            return null;
        }
    }
}
