/*
 * File: Run.java
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

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.docker.DockerContainer;
import com.oracle.bedrock.runtime.docker.DockerImage;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.EnvironmentVariable;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

/**
 * A representation of the Docker run command.
 * <p>
 * Instances of {@link Run} are <strong>immutable</strong>, methods that
 * add options and configuration to this {@link Run} command return a
 * new instance of a {@link Run} command with the modifications applied.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Run extends AbstractDockerCommand<Run>
{
    /**
     * The {@link Argument} holding the name of the image to run
     */
    private final Argument imageArgument;

    /**
     * The environment variables to add to the run command.
     * <p>
     * Each variable will be mapped to a <code>--env</code>
     * command line argument.
     */
    private List<EnvironmentVariable> environmentVariables;


    /**
     * Create a {@link Run} command.
     *
     * @param imageName      the name of the image to run
     * @param containerName  the name to give the container
     */
    private Run(String imageName,
                Object containerName)
    {
        this(Arguments.of(Argument.of("run"), Argument.of("--name", '=', containerName)), imageName, new ArrayList<>());
    }


    /**
     * Create a {@link Run} command.
     *
     * @param commandOptions        the run command's arguments
     * @param image                 the name of the image to run
     * @param environmentVariables  the environment variables to add to the command
     */
    private Run(Arguments                 commandOptions,
                Object                    image,
                List<EnvironmentVariable> environmentVariables)
    {
        super(commandOptions);

        this.imageArgument        = Argument.of(image);
        this.environmentVariables = environmentVariables;
    }


    @Override
    public Run withCommandArguments(Argument... args)
    {
        return new Run(getCommandArguments().with(args), imageArgument, environmentVariables);
    }


    @Override
    public Run withoutCommandArguments(Argument... args)
    {
        return new Run(getCommandArguments().without(args), imageArgument, environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--detach</code> option applied
     */
    public Run detached()
    {
        Arguments args = getCommandArguments().with(Argument.of("--detach")).without(Argument.of("--interactive"));

        return new Run(args, imageArgument, environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--interactive</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--interactive</code> option
     *          applied.
     */
    public Run interactive()
    {
        Arguments args = getCommandArguments().with(Argument.of("--interactive")).without(Argument.of("--detach"));

        return new Run(args, imageArgument, environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--tty</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--tty</code> option
     *          applied.
     */
    public Run tty()
    {
        return withCommandArguments(Argument.of("--tty"));
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--env</code> option applied using the specified
     * environment variable name.
     *
     * @param name the name
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--env</code> option
     *          applied.
     */
    public Run env(String name)
    {
        List<EnvironmentVariable> variables = new ArrayList<>(this.environmentVariables);

        variables.add(EnvironmentVariable.of(name));

        return new Run(this.getCommandArguments(), this.imageArgument, variables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--env</code> option applied using the specified
     * environment variable name and value.
     *
     * @param name   the name
     * @param value  the value
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--env</code> option
     *          applied.
     */
    public Run env(String name,
                   Object value)
    {
        List<EnvironmentVariable> variables = new ArrayList<>(this.environmentVariables);

        variables.add(EnvironmentVariable.of(name, value));

        return new Run(this.getCommandArguments(), this.imageArgument, variables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--env</code> option applied using the specified
     * environment variable names and values from the specified {@link Properties}.
     *
     * @param variables   the variables
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--env</code> option
     *          applied.
     */
    public Run env(Properties variables)
    {
        // There are no variables to add so return ourselves
        if (variables == null || variables.isEmpty())
        {
            return this;
        }

        List<EnvironmentVariable> copy = new ArrayList<>(this.environmentVariables);

        variables.entrySet().stream().map((entry) -> EnvironmentVariable.of(entry.getKey().toString(),
                                                                            entry.getValue())).forEach(copy::add);

        return new Run(this.getCommandArguments(), this.imageArgument, copy);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--expose</code> option applied.
     *
     * @param ports  one or more values that resolve to a port or port range to expose
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--expose</code> option
     *          applied.
     */
    public Run expose(Object... ports)
    {
        Argument arg = Argument.of("--expose", '=', new Argument.Multiple(ports));

        return new Run(this.getCommandArguments().replace(arg), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--publish-all</code> option applied.
     * <p>
     * This will publish (NAT) all of the exposed ports in the image to random
     * ports on the Docker host.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--publish-all</code> option
     *          applied.
     */
    public Run publishAll()
    {
        Arguments arguments = this.getCommandArguments().with(Argument.of("--publish-all"));

        return new Run(arguments, this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--publish</code> option applied.
     * <p>
     * This will publish (NAT) the specified ports to ports on the Docker host
     * depending on the format of the specific port mappings.
     *
     * @param portMappings  one or more values that resolve to valid Docker port mappings.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--publish</code> option
     *          applied.
     */
    public Run publish(Object... portMappings)
    {
        if (portMappings.length == 0)
        {
            return this;
        }

        return publish(Arrays.asList(portMappings));
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--publish</code> option applied.
     * <p>
     * This will publish (NAT) the specified ports to ports on the Docker host
     * depending on the format of the specific port mappings.
     *
     * @param portMappings  one or more values that resolve to valid Docker port mappings.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--publish</code> option
     *          applied.
     */
    public Run publish(Collection<?> portMappings)
    {
        if (portMappings == null || portMappings.isEmpty())
        {
            return this;
        }

        Arguments arguments = this.getCommandArguments().with(Argument.of("--publish",
                                                                          '=',
                                                                          new Argument.Multiple(portMappings)));

        return new Run(arguments, this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--rm</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--rm</code> option
     *          applied.
     */
    public Run autoRemove()
    {
        return withCommandArguments(Argument.of("--rm"));
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--attach</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--attach</code> option
     *          applied.
     */
    public Run attach()
    {
        Argument argument = Argument.of("--attach");

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--add-host</code> option applied.
     *
     * @param hostMappings  one or more values that resolve to host mappings of the form "host:ip"
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--add-host</code> option
     *          applied.
     */
    public Run addHost(Object... hostMappings)
    {
        Argument argument = Argument.of("--add-host", '=', new Argument.Multiple(hostMappings));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--blkio-weight</code> option applied.
     *
     * @param weight  The Block IO Weight (relative)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--blkio-weight</code> option
     *          applied.
     */
    public Run blockIOWeight(int weight)
    {
        Argument argument = Argument.of("--blkio-weight", '=', weight);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--blkio-weight-device</code> option applied.
     *
     * @param weight  one or more values that resolve to block IO weights
     *                (relative device weight, format: "DEVICE_NAME:WEIGHT")
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--blkio-weight-device</code>
     *          option applied.
     */
    public Run blockIOWeightDevice(int weight)
    {
        Argument argument = Argument.of("--blkio-weight-device", '=', weight);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cpu-shares</code> option applied.
     *
     * @param weight  the CPU shares (relative weight)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cpu-shares</code>
     *          option applied.
     */
    public Run cpuShares(int weight)
    {
        return new Run(getCommandArguments().replace(Argument.of("--cpu-shares", '=', weight)),
                       this.imageArgument,
                       this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cap-add</code> option applied.
     *
     * @param capabilities  one or more values resolving to Linux capabilities to add
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cap-add</code>
     *          option applied.
     */
    public Run addCapability(Object... capabilities)
    {
        Argument argument = Argument.of("--cap-add", '=', new Argument.Multiple(capabilities));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cap-drop</code> option applied.
     *
     * @param capabilities  one or more values resolving to Linux capabilities to drop
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cap-drop</code>
     *          option applied.
     */
    public Run dropCapability(Object... capabilities)
    {
        Argument argument = Argument.of("--cap-drop", '=', new Argument.Multiple(capabilities));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cgroup-parent</code> option applied.
     *
     * @param parent  the parent cgroup for the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cgroup-parent</code>
     *          option applied.
     */
    public Run cgroupParent(String parent)
    {
        Argument argument = Argument.of("--cgroup-parent", '=', parent);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cidfile</code> option applied.
     *
     * @param file  the file to write the container ID to
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cidfile</code>
     *          option applied.
     */
    public Run cidFile(File file)
    {
        Argument argument = Argument.of("--cidfile", '=', file);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cpu-period</code> option applied.
     *
     * @param value  Limit CPU CFS (Completely Fair Scheduler) period
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cpu-period</code>
     *          option applied.
     */
    public Run cpuPeriod(int value)
    {
        Argument argument = Argument.of("--cpu-period", '=', value);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cpu-quota</code> option applied.
     *
     * @param value  Limit CPU CFS (Completely Fair Scheduler) quota
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cpu-quota</code>
     *          option applied.
     */
    public Run cpuQuota(int value)
    {
        Argument argument = Argument.of("--cpu-quota", '=', value);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cpuset-cpus</code> option applied.
     *
     * @param value  the CPUs in which to allow execution (0-3, 0,1, etc)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cpuset-cpus</code>
     *          option applied.
     */
    public Run cpuSetCPUs(String value)
    {
        Argument argument = Argument.of("--cpuset-cpus", '=', value);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--cpuset-mems</code> option applied.
     *
     * @param value  the memory nodes (MEMs) in which to allow execution (0-3, 0,1, etc)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--cpuset-mems</code>
     *          option applied.
     */
    public Run cpuSetMems(String value)
    {
        Argument argument = Argument.of("--cpuset-mems", '=', value);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--detach</code> option applied.
     *
     * @param name  the name
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--detach</code>
     *          option applied.
     */
    public Run detach(String name)
    {
        Argument argument = Argument.of("--detach", '=', name);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--detach-keys</code> option applied.
     *
     * @param keys  the escape key sequence used to detach a container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--keys</code>
     *          option applied.
     */
    public Run detachKeys(String keys)
    {
        Argument argument = Argument.of("--detach-keys", '=', keys);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--device</code> option applied.
     *
     * @param names  one or more values that resolve to host device to add to
     *               the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--device</code>
     *          option applied.
     */
    public Run device(Object... names)
    {
        Argument argument = Argument.of("--device", '=', new Argument.Multiple(names));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--device-read-bps</code> option applied.
     *
     * @param values  the read rate (bytes per second) from a
     *                device (e.g., --device-read-bps=/dev/sda:1mb)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--device-read-bps</code>
     *          option applied.
     */
    public Run deviceReadBytesPerSec(String... values)
    {
        Argument argument = Argument.of("--device-read-bps", '=', new Argument.Multiple(values));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--device-read-iops</code> option applied.
     *
     * @param values  the read rate (IO per second) from a device
     *                (e.g., --device-read-iops=/dev/sda:1000)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--device-read-iops</code>
     *          option applied.
     */
    public Run deviceReadIOPS(String... values)
    {
        Argument argument = Argument.of("--device-read-iops", '=', new Argument.Multiple(values));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--device-write-bps</code> option applied.
     *
     * @param values  the write rate (bytes per second) to a device
     *                (e.g., --device-write-bps=/dev/sda:1mb)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--device-write-bps</code>
     *          option applied.
     */
    public Run deviceWriteBytesPerSec(String... values)
    {
        Argument argument = Argument.of("--device-write-bps", '=', new Argument.Multiple(values));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--device-write-iops</code> option applied.
     *
     * @param values  the write rate (IO per second) to a device
     *                (e.g., --device-write-bps=/dev/sda:1000)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--device-write-iops</code>
     *          option applied.
     */
    public Run deviceWriteIOPS(String... values)
    {
        Argument argument = Argument.of("--device-write-iops", '=', new Argument.Multiple(values));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--disable-content-trust</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--disable-content-trust</code>
     *          option applied.
     */
    public Run disableContentTrust()
    {
        Argument argument = Argument.of("--disable-content-trust", '=', true);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--disable-content-trust</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--disable-content-trust</code>
     *          option applied.
     */
    public Run enableContentTrust()
    {
        Argument argument = Argument.of("--disable-content-trust", '=', false);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--dns</code> option applied.
     *
     * @param servers  the custome DNS servers to use
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--dns</code>
     *          option applied.
     */
    public Run dns(String... servers)
    {
        Argument argument = Argument.of("--dns", '=', new Argument.Multiple(servers));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--dns-opt</code> option applied.
     *
     * @param options  the custom DNS options
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--dns-opt</code>
     *          option applied.
     */
    public Run dnsOptions(String... options)
    {
        Argument argument = Argument.of("--dns-opt", '=', new Argument.Multiple(options));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--dns-search</code> option applied.
     *
     * @param domains  the custom DNS search domains
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--dns-search</code>
     *          option applied.
     */
    public Run dnsSearch(String... domains)
    {
        Argument argument = Argument.of("--dns-search", '=', new Argument.Multiple(domains));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--entrypoint</code> option applied.
     *
     * @param entrypoint  the value to use for the entry point
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--</code>
     *          option applied.
     */
    public Run entryPoint(String entrypoint)
    {
        Argument argument = Argument.of("--entrypoint", '=', entrypoint);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--env-file</code> option applied.
     *
     * @param file  the file to use to read environment variables
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--env-file</code>
     *          option applied.
     */
    public Run envFile(File file)
    {
        Argument argument = Argument.of("--env-file", '=', file);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--group-add</code> option applied.
     *
     * @param groups  the names of additional groups to run as
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--group-add</code>
     *          option applied.
     */
    public Run groupAdd(String... groups)
    {
        Argument argument = Argument.of("--group-add", '=', new Argument.Multiple(groups));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--hostname</code> option applied.
     *
     * @param name  the host name to give the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--hostname</code>
     *          option applied.
     */
    public Run hostName(String name)
    {
        Argument argument = Argument.of("--hostname", '=', name);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--ip</code> option applied.
     *
     * @param address  the IP4 address to give to the container (e.g. 172.30.100.104)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--ip</code>
     *          option applied.
     */
    public Run ip(String address)
    {
        Argument argument = Argument.of("--ip", '=', address);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--ip6</code> option applied.
     *
     * @param address  the IP6 address to give to the container (e.g. 2001:db8::33)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--ip6</code>
     *          option applied.
     */
    public Run ip6(String address)
    {
        Argument argument = Argument.of("--ip6", '=', address);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--ipc</code> option applied.
     *
     * @param namespace  the IPC namespace to use
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--ipc</code>
     *          option applied.
     */
    public Run ipc(String namespace)
    {
        Argument argument = Argument.of("--ipc", '=', namespace);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--isolation</code> option applied.
     *
     * @param value  the container isolation technology to use
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--isolation</code>
     *          option applied.
     */
    public Run isolation(String value)
    {
        Argument argument = Argument.of("--isolation", '=', value);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--kernel-memory</code> option applied.
     *
     * @param limit  the kernel memory limit
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--kernel-memory</code>
     *          option applied.
     */
    public Run kernalMemory(String limit)
    {
        Argument argument = Argument.of("--kernel-memory", '=', limit);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--label</code> option applied.
     *
     * @param values  one or more values resolving to labels to assign
     *                to the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--label</code>
     *          option applied.
     */
    public Run labels(Object... values)
    {
        Argument argument = Argument.of("--label", '=', new Argument.Multiple(values));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--label-file</code> option applied.
     *
     * @param files  one or more files of label values to read
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--label-file</code>
     *          option applied.
     */
    public Run labelFile(File... files)
    {
        Argument argument = Argument.of("--label-file", '=', new Argument.Multiple(files));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--link</code> option applied.
     *
     * @param containers  one or more values resolving to the names of
     *                    other containers to link to
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--link</code>
     *          option applied.
     */
    public Run link(Object... containers)
    {
        return link(Arrays.asList(containers));
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--link</code> option applied.
     *
     * @param containers  one or more values resolving to the names of
     *                    other containers to link to
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--link</code>
     *          option applied.
     */
    public Run link(List<?> containers)
    {
        if (containers == null || containers.isEmpty())
        {
            return this;
        }

        Argument argument = Argument.of("--link", '=', new Argument.Multiple(containers));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--log-driver</code> option applied.
     *
     * @param name  the name of the logging driver to use
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--log-driver</code>
     *          option applied.
     */
    public Run logDriver(String name)
    {
        Argument argument = Argument.of("--log-driver", '=', name);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--log-opt</code> option applied.
     *
     * @param options  one or more value resolving to options to supply to the log driver
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--log-opt</code>
     *          option applied.
     */
    public Run logOptions(Object... options)
    {
        Argument argument = Argument.of("--log-opt", '=', new Argument.Multiple(options));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--mac-address</code> option applied.
     *
     * @param address  the MAC address to apply to the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--mac-address</code>
     *          option applied.
     */
    public Run macAddress(String address)
    {
        Argument argument = Argument.of("--mac-address", '=', address);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--memory</code> option applied.
     *
     * @param limit  the memory limit to apply to the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--memory</code>
     *          option applied.
     */
    public Run memory(String limit)
    {
        Argument argument = Argument.of("--memory", '=', limit);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--memory-reservation</code> option applied.
     *
     * @param limit  the memory soft limit limit to apply to the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--memory-reservation</code>
     *          option applied.
     */
    public Run memoryReservation(String limit)
    {
        Argument argument = Argument.of("--memory-reservation", '=', limit);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--memory-swap</code> option applied.
     *
     * @param swap  a positive integer equal to memory plus swap
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--memory-swap</code>
     *          option applied.
     */
    public Run memorySwap(int swap)
    {
        Argument argument = Argument.of("--memory-swap", '=', swap);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--memory-swappiness</code> option applied.
     *
     * @param swap  tue a containers swappiness behaviour
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--memory-swappiness</code>
     *          option applied.
     */
    public Run memorySwappiness(int swap)
    {
        Argument argument = Argument.of("--memory-swappiness", '=', swap);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--net=none</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--net=none</code>
     *          option applied.
     */
    public Run netNone()
    {
        return net("none");
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--net=bridge</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--net=bridge</code>
     *          option applied.
     */
    public Run netBridge()
    {
        return net("bridge");
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--net=host</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--net=host</code>
     *          option applied.
     */
    public Run netHost()
    {
        return net("host");
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--net=container:id</code> option applied.
     *
     * @param container  the name of the container to reuse the network
     *                   stack from
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--new=container:id</code>
     *          option applied.
     */
    public Run netContainer(String container)
    {
        return net("container:" + container);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--net</code> option applied.
     *
     * @param name  the name of a network to connect to
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--</code>
     *          option applied.
     */
    public Run net(String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            return this;
        }

        Argument argument = Argument.of("--net", '=', name);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--net</code> option applied.
     *
     * @param aliases  the network scoped aliases to use
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--</code>
     *          option applied.
     */
    public Run netAlias(String... aliases)
    {
        Argument argument = Argument.of("--net-alias", '=', new Argument.Multiple(aliases));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--oom-kill-disabled</code> option applied.
     *
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--oom-kill-disabled</code>
     *          option applied.
     */
    public Run oomKillDisabled()
    {
        Argument argument = Argument.of("--oom-kill-disabled");

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--oom-score-adj</code> option applied.
     *
     * @param score  the host's OOM preferences for containers (accepts -1000 to 1000)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--oom-score-adj</code>
     *          option applied.
     */
    public Run oomScoreAdjust(int score)
    {
        Argument argument = Argument.of("--oom-score-adj", '=', score);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--pid</code> option applied.
     *
     * @param namespace  the PID namespace to use
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--pid</code>
     *          option applied.
     */
    public Run pid(String namespace)
    {
        Argument argument = Argument.of("--pid", '=', namespace);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--pids-limit</code> option applied.
     *
     * @param limit  the container pids limit (set -1 for unlimited)
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--pids-limit</code>
     *          option applied.
     */
    public Run pidsLimit(int limit)
    {
        Argument argument = Argument.of("--pids-limit", '=', limit);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--privileged</code> option applied.
     *
     * @param limit  the limit
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--privileged</code>
     *          option applied.
     */
    public Run privileged(int limit)
    {
        Argument argument = Argument.of("--privileged");

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--read-only</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--read-only</code>
     *          option applied.
     */
    public Run readOnly()
    {
        Argument argument = Argument.of("--read-only");

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--restart=always</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--restart=always</code>
     *          option applied.
     */
    public Run restartAlways()
    {
        return restart("always");
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--restart=no</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--restart=no</code>
     *          option applied.
     */
    public Run restartNo()
    {
        return restart("no");
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--restart=on-failure</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--restart=on-failure</code>
     *          option applied.
     */
    public Run restartOnFailure()
    {
        return restart("on-failure");
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--restart=on-failure:max-retries</code> option applied.
     *
     * @param maxRetries  the maximum number of attempts to restart the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--restart=on-failure:max-retries</code>
     *          option applied.
     */
    public Run restartOnFailure(int maxRetries)
    {
        return restart("on-failure");
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--restart=unless-stopped</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--restart=unless-stopped</code>
     *          option applied.
     */
    public Run restartUnlessStopped()
    {
        return restart("unless-stopped");
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--restart</code> option applied.
     *
     * @param policy  the container restart policy
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--restart</code>
     *          option applied.
     */
    public Run restart(String policy)
    {
        Argument argument = Argument.of("--restart", '=', policy);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--rm</code> option applied.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--rm</code>
     *          option applied.
     */
    public Run rm()
    {
        Argument argument = Argument.of("--rm");

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--shm-size</code> option applied.
     * <p>
     * The format is `&lt;number&gt;&lt;unit&gt;`. `number` must be greater than `0`.
     * Unit is optional and can be `b` (bytes), `k` (kilobytes), `m` (megabytes), or
     * `g` (gigabytes). If you omit the unit, the system uses bytes. If you omit the
     * size entirely, the system uses `64m`.
     *
     * @param values  one or more values resolving to sizes of of `/dev/shm`.
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--shm-size</code>
     *          option applied.
     */
    public Run shmSize(Object... values)
    {
        Argument argument = Argument.of("--shm-size", '=', new Argument.Multiple(values));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--security-opt</code> option applied.
     *
     * @param options  one or more values that resolves to a security option
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--security-opt</code>
     *          option applied.
     */
    public Run securityOptions(Object... options)
    {
        Argument argument = Argument.of("--security-opt", '=', new Argument.Multiple(options));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--sig-proxy</code> option applied.
     *
     * @param enabled  true to proxy received signals to the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--sig-proxy</code>
     *          option applied.
     */
    public Run signalProxy(boolean enabled)
    {
        Argument argument = Argument.of("--sig-proxy", '=', enabled);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--stop-signal</code> option applied.
     *
     * @param signal  the signal to use to stop the container
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--stop-signal</code>
     *          option applied.
     */
    public Run stopSignal(String signal)
    {
        Argument argument = Argument.of("--stop-signal", '=', signal);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--user</code> option applied.
     *
     * @param username  the username or UID to use (format: &lt;name|uid&gt;[:&lt;group|gid&gt;])
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--user</code>
     *          option applied.
     */
    public Run user(String username)
    {
        Argument argument = Argument.of("--user", '=', username);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--userns</code> option applied.
     *
     * @param namespace  the container user namesapce
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--userns</code>
     *          option applied.
     */
    public Run userNamespace(String namespace)
    {
        Argument argument = Argument.of("--userns", '=', namespace);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--ulimit</code> option applied.
     *
     * @param options  the Ulimit options
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--ulimit</code>
     *          option applied.
     */
    public Run ulimit(Object... options)
    {
        Argument argument = Argument.of("--ulimit", '=', new Argument.Multiple(options));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--ulimit</code> option applied.
     *
     * @param options  the Ulimit options
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--ulimit</code>
     *          option applied.
     */
    public Run utsNamespace(Object... options)
    {
        Argument argument = Argument.of("--ulimit", '=', new Argument.Multiple(options));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--volume</code> option applied.
     * <p>
     * Bind mount a volume. The comma-delimited "options" are
     * [rw|ro], [z|Z], [[r]shared|[r]slave|[r]private], and [nocopy].
     * The 'host-src' is an absolute path or a name value.
     *
     * @param volumes  one or more values resolving to volumes to mount in
     *                 the format [host-src:]container-dest[:options]
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--volume</code>
     *          option applied.
     */
    public Run volume(Object... volumes)
    {
        Argument argument = Argument.of("--volume", '=', new Argument.Multiple(volumes));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--volume-driver</code> option applied.
     *
     * @param name  the name of the volume driver to use
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--volume-driver</code>
     *          option applied.
     */
    public Run volumeDriver(String name)
    {
        Argument argument = Argument.of("--volume-driver", '=', name);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--volumes-from</code> option applied.
     *
     * @param containers  the containers to mount volumes from
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--volumes-from</code>
     *          option applied.
     */
    public Run volumesFrom(String... containers)
    {
        Argument argument = Argument.of("--volumes-from", '=', new Argument.Multiple(containers));

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    /**
     * Create a new {@link Run} command that is a copy of this {@link Run}
     * command with the <code>--workdir</code> option applied.
     *
     * @param file  the container working directory
     *
     * @return  a new {@link Run} command that is a copy of this
     *          {@link Run} command with the <code>--workdir</code>
     *          option applied.
     */
    public Run workinDirectory(File file)
    {
        Argument argument = Argument.of("--workdir", '=', file);

        return new Run(getCommandArguments().replace(argument), this.imageArgument, this.environmentVariables);
    }


    @Override
    public void onLaunch(Platform      platform,
                         OptionsByType optionsByType)
    {
        // Build up the command in the form "run [options...] image-name [args...]"

        // 1. Capture any Arguments already set in the options as these will become
        // container argument (the [args...] part)
        Arguments currentArguments = optionsByType.get(Arguments.class);

        // 2. call super to add all of the command arguments, the "run [options...]" part
        super.onLaunch(platform, optionsByType);

        // 3. add any environment variable arguments (--env=name=value) part of the [options...] arguments
        EnvironmentVariables envVars   = EnvironmentVariables.custom().with(this.environmentVariables);
        Properties           variables = envVars.realize(platform, optionsByType.asArray());

        variables.entrySet().stream().map(
            (entry) -> {
                if (entry.getValue() == null)
                {
                    return entry.getKey().toString();
                }

                return entry.getKey() + "=" + entry.getValue();
            }).map((value) -> Argument.of("--env", value)).forEach(optionsByType::add);

        // 4. The next parameter is the image name
        optionsByType.add(imageArgument);

        // 5. finally add back any command line args to form the command line for the container the [args...] part
        currentArguments.forEach(optionsByType::add);
    }


    @Override
    public void onLaunched(Platform      platform,
                           Application   application,
                           OptionsByType optionsByType)
    {
        // Pull the container name out of the command line arguments by looking for the --name argument
        String name = getCommandArguments().stream().filter((arg) -> arg.getName() != null
                                                                     && arg.getName().equals("--name"))
                                                                     .map((arg) -> String.valueOf(arg.getValue()))
                                                                     .findFirst().orElse(null);

        DockerContainer container = new DockerContainer(name, optionsByType);

        application.add(container);
    }


    /**
     * Create a {@link Run} command to run a container from the
     * specified image name.
     * <p>
     * The resulting container will be given a random name.
     *
     * @param image  the name of the image
     *
     * @return  a {@link Run} command to run a container from
     *          the specified image name
     */
    public static Run image(String image)
    {
        return image(image, UUID.randomUUID().toString());
    }


    /**
     * Create a {@link Run} command to run a container from the
     * specified image name.
     *
     * @param image          the image name
     * @param containerName  a value that will resolve to a unique
     *                       container name
     *
     * @return  a {@link Run} command to run a container from
     *          the specified image name
     */
    public static Run image(String image,
                            Object containerName)
    {
        return new Run(image, containerName);
    }


    /**
     * Create a {@link Run} command to run a container from the
     * specified {@link DockerImage} name.
     * <p>
     * The resulting container will be given a random name.
     *
     * @param image  the {@link DockerImage} representing the image to use
     *
     * @return  a {@link Run} command to run a container from
     *          the specified image name
     */
    public static Run image(DockerImage image)
    {
        return image(image.getFirstTag(), UUID.randomUUID().toString());
    }


    /**
     * Create a {@link Run} command to run a container from the
     * specified image name.
     *
     * @param image          the name of the image
     * @param containerName  a value that will resolve to a unique
     *                       container name
     *
     * @return  a {@link Run} command to run a container from
     *          the specified image
     */
    public static Run image(DockerImage image,
                            Object      containerName)
    {
        return new Run(image.getFirstTag(), containerName);
    }
}
