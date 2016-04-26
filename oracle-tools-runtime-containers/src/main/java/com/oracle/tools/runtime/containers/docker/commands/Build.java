/*
 * File: Build.java
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
import com.oracle.tools.options.Timeout;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.containers.docker.DockerImage;
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * A representation of the Docker build command.
 * <p>
 * Instances of {@link Build} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Build} command return a
 * new instance of a {@link Build} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Build extends AbstractDockerCommand<Build>
{
    /**
     * The default timeout for build commands.
     */
    public static final Timeout DEFAULT_TIMEOUT = Timeout.after(5, TimeUnit.MINUTES);

    /**
     * A value that will resolve to the location of the build context folder.
     */
    private Object contextLocation;


    /**
     * Create an empty {@link Build} command with the
     * build context location set to the working directory
     * that the command is run in.
     */
    private Build()
    {
        super("build");
        
        this.contextLocation = ".";
    }
    
    
    /**
     * Create a {@link Build} command with the specified {@link Arguments}
     * and build context folder.
     *
     * @param arguments        the command {@link Arguments}
     * @param contextLocation  a value that will resolve to the location of
     *                         the build context folder
     */
    private Build(Arguments arguments, Object contextLocation)
    {
        super(arguments);
        this.contextLocation = contextLocation;
    }
    
    
    @Override
    public Build withCommandArguments(Argument... args)
    {
        return new Build(getCommandArguments().with(args), contextLocation);
    }


    @Override
    public Build withoutCommandArguments(Argument... args)
    {
        return new Build(getCommandArguments().without(args), contextLocation);
    }


    /**
     * Obtain a {@link Build} command that is the same as this command with
     * the specified --tag build argument values.
     * The value equates to one or more name and optionally a tag in
     * the 'name:tag' format
     *
     * @param tags  the values to use for the --tag argument
     *
     * @return   a {@link Build} command that is the same as this command with
     *           the specified --tag argument values
     */
    public Build withTags(Object... tags)
    {
        Argument argument = Argument.of("--tag", '=', new Argument.Multiple(tags));

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Obtain a {@link Build} command that is a copy of this {@link Build}
     * command with the specified build context location.
     *
     * @param location  the location of the build context
     *
     * @return  a {@link Build} command that is a copy of this {@link Build}
     *          command with the specified build context location
     */
    public Build buildContextAt(File location)
    {
        return new Build(getCommandArguments(), location);
    }


    /**
     * Obtain a {@link Build} command that is a copy of this {@link Build}
     * command with the specified build context location.
     *
     * @param location  the location of the build context
     *
     * @return  a {@link Build} command that is a copy of this {@link Build}
     *          command with the specified build context location
     */
    public Build buildContextAt(URL location)
    {
        return new Build(getCommandArguments(), location);
    }


    /**
     * Obtain a {@link Build} command that is a copy of this {@link Build}
     * command with the specified <code>--build-args</code> option.
     *
     * @param args  one or more values that resolve to build time arguments
     *
     * @return  a {@link Build} command that is a copy of this {@link Build}
     *          command with the specified <code>--build-args</code> option
     */
    public Build buildArgs(Object... args)
    {
        Argument argument = Argument.of("--build-arg", '=', new Argument.Multiple(args));

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Obtain a {@link Build} command that is a copy of this {@link Build}
     * command with the specified <code>--cpu-shares=true</code> option.
     *
     * @return  a {@link Build} command that is a copy of this {@link Build}
     *          command with the specified <code>--cpu-shares=true</code> option
     */
    public Build cpuShares()
    {
        return cpuShares(true);
    }


    /**
     * Obtain a {@link Build} command that is a copy of this {@link Build}
     * command with the specified <code>--cpu-shares</code> option.
     *
     * @param enabled  whether to enable ot disable cpu-shares
     *
     * @return  a {@link Build} command that is a copy of this {@link Build}
     *          command with the specified <code>--cpu-shares</code> option
     */
    public Build cpuShares(boolean enabled)
    {
        if (enabled)
        {
            return new Build(getCommandArguments().with(Argument.of("--cpu-shares")), contextLocation);
        }

        return new Build(getCommandArguments().without(Argument.of("--cpu-shares")), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--cgroup-parent</code> option applied.
     *
     * @param parent  the parent cgroup for the container
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--cgroup-parent</code>
     *          option applied.
     */
    public Build cgroupParent(String parent)
    {
        Argument argument = Argument.of("--cgroup-parent", '=', parent);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--cpu-period</code> option applied.
     *
     * @param value  Limit CPU CFS (Completely Fair Scheduler) period
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--cpu-period</code>
     *          option applied.
     */
    public Build cpuPeriod(int value)
    {
        Argument argument = Argument.of("--cpu-period", '=', value);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--cpu-quota</code> option applied.
     *
     * @param value  Limit CPU CFS (Completely Fair Scheduler) quota
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--cpu-quota</code>
     *          option applied.
     */
    public Build cpuQuota(int value)
    {
        Argument argument = Argument.of("--cpu-quota", '=', value);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--cpuset-cpus</code> option applied.
     *
     * @param value  the CPUs in which to allow execution (0-3, 0,1, etc)
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--cpuset-cpus</code>
     *          option applied.
     */
    public Build cpuSetCPUs(String value)
    {
        Argument argument = Argument.of("--cpuset-cpus", '=', value);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--cpuset-mems</code> option applied.
     *
     * @param value  the memory nodes (MEMs) in which to allow execution (0-3, 0,1, etc)
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--cpuset-mems</code>
     *          option applied.
     */
    public Build cpuSetMems(String value)
    {
        Argument argument = Argument.of("--cpuset-mems", '=', value);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--disable-content-trust</code> option applied.
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--disable-content-trust</code>
     *          option applied.
     */
    public Build disableContentTrust()
    {
        Argument argument = Argument.of("--disable-content-trust", '=', true);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--disable-content-trust</code> option applied.
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--disable-content-trust</code>
     *          option applied.
     */
    public Build enableContentTrust()
    {
        Argument argument = Argument.of("--disable-content-trust", '=', false);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--file</code> option applied.
     *
     * @param name  the name of the Dockerfile to use for the build
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--file</code>
     *          option applied.
     */
    public Build dockerFileName(String name)
    {
        Argument argument = Argument.of("--file", '=', name);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--force-rm</code> option applied.
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--force-rm</code>
     *          option applied.
     */
    public Build forceRM()
    {
        return forceRM(true);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--force-rm</code> option applied.
     *
     * @param force  true to alway remove intermediate containers
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--force-rm</code>
     *          option applied.
     */
    public Build forceRM(boolean force)
    {
        if (force)
        {
            return new Build(getCommandArguments().with(Argument.of("--force-rm")), contextLocation);
        }

        return new Build(getCommandArguments().without(Argument.of("--force-rm")), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--isolation</code> option applied.
     *
     * @param value  the container isolation technology to use
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--isolation</code>
     *          option applied.
     */
    public Build isolation(String value)
    {
        Argument argument = Argument.of("--isolation", '=', value);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--label</code> option applied.
     *
     * @param values  one or more values resolving to labels to assign
     *                to the image
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--label</code>
     *          option applied.
     */
    public Build labels(Object... values)
    {
        Argument argument = Argument.of("--label", '=', new Argument.Multiple(values));

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--memory</code> option applied.
     *
     * @param limit  the memory limit to apply to the container
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--memory</code>
     *          option applied.
     */
    public Build memory(String limit)
    {
        Argument argument = Argument.of("--memory", '=', limit);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--memory-swap</code> option applied.
     *
     * @param swap  a positive integer equal to memory plus swap
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--memory-swap</code>
     *          option applied.
     */
    public Build memorySwap(String swap)
    {
        Argument argument = Argument.of("--memory-swap", '=', swap);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--no-cache</code> option applied.
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--no-cache</code>
     *          option applied.
     */
    public Build noCache()
    {
        return noCache(true);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--no-cache</code> option applied.
     *
     * @param noCache  true to not use cache when building the image
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--no-cache</code>
     *          option applied.
     */
    public Build noCache(boolean noCache)
    {
        if (noCache)
        {
            return new Build(getCommandArguments().with(Argument.of("--no-cache")), contextLocation);
        }

        return new Build(getCommandArguments().without(Argument.of("--no-cache")), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--pull</code> option applied.
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--pull</code>
     *          option applied.
     */
    public Build alwaysPull()
    {
        return new Build(getCommandArguments().with(Argument.of("--pull")), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--quiet</code> option applied.
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--quiet</code>
     *          option applied.
     */
    public Build quiet()
    {
        return quiet(true);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--quiet</code> option applied.
     *
     * @param quiet  true to enable quiet mode
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--quiet</code>
     *          option applied.
     */
    public Build quiet(boolean quiet)
    {
        if (quiet)
        {
            return new Build(getCommandArguments().with(Argument.of("--quiet")), contextLocation);
        }

        return new Build(getCommandArguments().without(Argument.of("--quiet")), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--rm</code> option applied.
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--rm</code>
     *          option applied.
     */
    public Build removeIntermidiateContainers()
    {
        return removeIntermidiateContainers(true);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--rm</code> option applied.
     *
     * @param rm  true to remove intermediate containers after successful build
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--rm</code>
     *          option applied.
     */
    public Build removeIntermidiateContainers(boolean rm)
    {
        Argument argument = Argument.of("--rm", '=', rm);

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--shm-size</code> option applied.
     * <p>
     * The format is `<number><unit>`. `number` must be greater than `0`.
     * Unit is optional and can be `b` (bytes), `k` (kilobytes), `m` (megabytes), or
     * `g` (gigabytes). If you omit the unit, the system uses bytes. If you omit the
     * size entirely, the system uses `64m`.
     *
     * @param values  one or more values resolving to sizes of of `/dev/shm`.
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--shm-size</code>
     *          option applied.
     */
    public Build shmSize(Object... values)
    {
        Argument argument = Argument.of("--shm-size", '=', new Argument.Multiple(values));

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    /**
     * Create a new {@link Build} command that is a copy of this {@link Build}
     * command with the <code>--ulimit</code> option applied.
     *
     * @param options  the Ulimit options
     *
     * @return  a new {@link Build} command that is a copy of this
     *          {@link Build} command with the <code>--ulimit</code>
     *          option applied.
     */
    public Build ulimit(Object... options)
    {
        Argument argument = Argument.of("--ulimit", '=', new Argument.Multiple(options));

        return new Build(getCommandArguments().replace(argument), contextLocation);
    }


    @Override
    public void onFinalize(Platform platform, Options options)
    {
        // set the Image build timeout
        Timeout timeout = getTimeout();

        if (timeout == null)
        {
            // default the timeout to 5 minutes as image builds can typically take a while
            timeout = DEFAULT_TIMEOUT;
        }

        options.addIfAbsent(timeout);


        // call super to add all of the command arguments
        super.onFinalize(platform, options);

        // add the context location, which is the last argument in the chain
        options.add(Argument.of(contextLocation));
    }


    @Override
    public void onLaunched(Platform platform, Application application, Options options)
    {
        Arguments    arguments = options.get(Arguments.class);
        String       tagPrefix = "--tag=";
        int          prefixLen = tagPrefix.length();

        List<String> tags      = arguments.stream()
                                          .map((arg) ->  String.valueOf(arg.getValue()))
                                          .filter((tagArg) -> tagArg.startsWith(tagPrefix))
                                          .map((tagArg) -> tagArg.substring(prefixLen))
                                          .collect(Collectors.toList());

        DockerImage image = new DockerImage(tags, options);

        application.add(image);
    }


    /**
     * Create a {@link Build} command that will build an image
     * using the Dockerfile with the default Dockerfile name
     * located in the working directory that the command is
     * launched in.
     *
     * @return  a {@link Build} command
     */
    public static Build fromDockerFile()
    {
        // We must have a tag to identify the image so create a random one.
        // The user can override it if they wish

        return new Build()
                .dockerFileName("Dockerfile")
                .withTags(UUID.randomUUID().toString());
    }


    /**
     * Create a {@link Build} command that will build an image
     * using the Dockerfile with the specified name located in
     * the working directory that the command is launched in.
     *
     * @return  a {@link Build} command
     */
    public static Build fromDockerFile(String dockerFileName)
    {
        return fromDockerFile().dockerFileName(dockerFileName);
    }
}
