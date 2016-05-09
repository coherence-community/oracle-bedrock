/*
 * File: Network.java
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

import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.docker.Docker;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * A representation of the Docker Network commands.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class Network<C extends CommandWithArgumentList> extends CommandWithArgumentList<C>
{
    /**
     * Create a {@link Network}.
     *
     * @param arguments  the command {@link Arguments}
     * @param argList    {@link List} of {@link Argument}s
     *                   that will be appended to the end of
     *                   the command  line
     */
    private Network(Arguments arguments,
                    List<?>   argList)
    {
        super(arguments, argList);
    }


    /**
     * Create a Docker network using the bridge driver.
     *
     * @param name  the name of the network to create
     *
     * @return  a new {@link Create} command.
     */
    public static Create createBridge(String name)
    {
        return create(name, "bridge");
    }


    /**
     * Create a Docker network using the overlay driver.
     *
     * @param name  the name of the network to create
     *
     * @return  a new {@link Create} command.
     */
    public static Create createOverlay(String name)
    {
        return create(name, "overlay");
    }


    /**
     * Create a Docker network using the specified driver.
     *
     * @param name    the name of the network to create
     * @param driver  the name of the network driver to use
     *
     * @return  a new {@link Create} command.
     */
    public static Create create(String name,
                                String driver)
    {
        return new Create(name, driver);
    }


    /**
     * Connect a container to a Docker network.
     *
     * @param network    the name of the network to connect to
     * @param container  the name of the container to connect to the network
     *
     * @return  a new {@link Connect} command.
     */
    public static Connect connect(String network,
                                  String container)
    {
        return new Connect(network, container);
    }


    /**
     * Connect a container to a Docker network.
     *
     * @param network    the name of the network to connect to
     * @param container  the name of the container to connect to the network
     *
     * @return  a new {@link Connect} command.
     */
    public static Disconnect disconnect(String network,
                                        String container)
    {
        return new Disconnect(network, container);
    }


    /**
     * Inspect the specified networks.
     *
     * @param names  the names of the networks to inspect
     *
     * @return a new {@link Inspect}
     */
    public static Inspect inspect(String... names)
    {
        return inspect(Arrays.asList(names));
    }


    /**
     * Inspect the specified networks.
     *
     * @param names  the names of the networks to inspect
     *
     * @return a new {@link Inspect}
     */
    public static Inspect inspect(List<String> names)
    {
        return new Inspect(names);
    }


    /**
     * Execute an {@link Inspect} command on the specified {@link Platform}
     * using the specified {@link Docker} environment.
     *
     * @param platform     the {@link Platform} to use to execute the command
     * @param environment  the {@link Docker} environment to use
     * @param networks     the names of the networks to inspect
     *
     * @return  a {@link JsonArray} containing the results of the inspect command
     */
    public static JsonValue inspect(Platform  platform,
                                    Docker    environment,
                                    String... networks)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        Inspect                     inspect = inspect(networks);

        try (Application app = platform.launch(inspect, environment, Console.of(console)))
        {
            if (app.waitFor() != 0)
            {
                console.getCapturedOutputLines().forEach(System.out::println);
                console.getCapturedErrorLines().forEach(System.err::println);

                return null;
            }
        }

        Queue<String> lines = console.getCapturedOutputLines();
        String json = lines.stream().filter((line) -> line != null
                                                      &&!line.equals("(terminated)")).collect(Collectors.joining("\n"))
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


    /**
     * List the available Docker networks,
     *
     * @return  a new {@link LS} command.
     */
    public static LS list()
    {
        return new LS();
    }


    /**
     * Remove one or more Docker networks.
     *
     * @param names  the names of the network to remove
     *
     * @return  a new {@link Remove} command.
     */
    public static Remove remove(String... names)
    {
        return remove(Arrays.asList(names));
    }


    /**
     * Remove one or more Docker networks.
     *
     * @param names  the names of the network to remove
     *
     * @return  a new {@link Remove} command.
     */
    public static Remove remove(List<String> names)
    {
        return new Remove(names);
    }


    /**
     * A representation of the Docker Network Connect command.
     */
    public static class Connect extends Network<Connect>
    {
        /**
         * Create a {@link Connect}.
         *
         * @param arguments  the command {@link Arguments}
         * @param argList    {@link List} of {@link Argument}s
         *                   that will be appended to the end of
         *                   the command  line
         */
        private Connect(Arguments arguments,
                        List<?>   argList)
        {
            super(arguments, argList);
        }


        /**
         * Create a {@link Connect} command.
         *
         * @param networkName    the name of the network to connect to
         * @param containerName  the name of the container to
         *                       connect to the network
         */
        private Connect(String networkName,
                        String containerName)
        {
            super(Arguments.of("network", "connect"), Arrays.asList(networkName, containerName));
        }


        /**
         * Add network-scoped aliases for the container (equates to the --alias argument).
         *
         * @param alias  values that resolve to one or more valid
         *               aliases
         *
         * @return  a new {@link Connect} instance that is the same as this
         *          instance with the --alias option applied
         */
        public Connect alias(Object... alias)
        {
            if (alias == null)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--alias", '=', new Argument.Multiple(alias)));
        }


        /**
         * Set the IPv4 Address (equates to the --ip argument).
         *
         * @param address  a value that resolves to a valid IPv4 addresses
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --ip option applied
         */
        public Connect ip(Object address)
        {
            if (address == null)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--ip", '=', address));
        }


        /**
         * Set the IPv6 Address (equates to the --ip6 argument).
         *
         * @param address  a value that resolves to a valid IPv6 addresses
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --ip6 option applied
         */
        public Connect ip6(Object address)
        {
            if (address == null)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--ip6", '=', address));
        }


        /**
         * Add a link to another container (equates to the --link argument).
         *
         * @param containers  values that resolve to one or more valid
         *                    container names
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --link option applied
         */
        public Connect link(Object... containers)
        {
            if (containers == null || containers.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--link", '=', new Argument.Multiple(containers)));
        }


        @Override
        protected Connect withCommandArguments(List<Argument> endArgs,
                                               Argument...    args)
        {
            return new Connect(getCommandArguments().with(args), endArgs);
        }


        @Override
        protected Connect withoutCommandArguments(List<Argument> endArgs,
                                                  Argument...    args)
        {
            return new Connect(getCommandArguments().without(args), endArgs);
        }
    }


    /**
     * A representation of the Docker Network Create command.
     */
    public static class Create extends Network<Create>
    {
        /**
         * Create a {@link Create}.
         *
         * @param arguments  the command {@link Arguments}
         * @param argList    {@link List} of {@link Argument}s
         *                   that will be appended to the end of
         *                   the command  line
         */
        private Create(Arguments arguments,
                       List<?>   argList)
        {
            super(arguments, argList);
        }


        /**
         * Create a new {@link Create} command.
         *
         * @param name    the name of the network to create
         * @param driver  the name of the network driver
         */
        private Create(String name,
                       String driver)
        {
            super(Arguments.of("network", "create", Argument.of("--driver", '=', driver)),
                  Collections.singletonList(name));
        }


        /**
         * Set the auxiliary ipv4 or ipv6 addresses used by network
         * driver (equates to the --aux-address argument).
         *
         * @param address  values that resolve to one or more valid
         *                 auxiliary ipv4 or ipv6 addresses
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --aux-address option applied
         */
        public Create auxAddress(Object... address)
        {
            if (address == null || address.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--aux-address", '=', new Argument.Multiple(address)));
        }


        /**
         * Set the ipv4 or ipv6 Gateway for the master subnet (equates to the --gateway argument).
         *
         * @param address  values that resolve to one or more valid
         *                 IP ranges, for example 192.168.0.100
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --gateway option applied
         */
        public Create gateway(Object... address)
        {
            if (address == null || address.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--gateway", '=', new Argument.Multiple(address)));
        }


        /**
         * Restrict external access to the network (equates to the --internal argument).
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --internal option applied
         */
        public Create internal()
        {
            return withCommandArguments(Argument.of("--internal"));
        }


        /**
         * Allocate container ip from a sub-range (equates to the --ip-range argument).
         *
         * @param range  values that resolve to one or more valid
         *               IP ranges, for example 172.28.5.0/24
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --ip-range option applied
         */
        public Create ipRange(Object... range)
        {
            if (range == null || range.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--ip-range", '=', new Argument.Multiple(range)));
        }


        /**
         * Set the IP Address Management Driver (equates to the --ipam-driver argument).
         *
         * @param driver  the name of the IP Address Management Driver
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --ipam-driver option applied
         */
        public Create ipamDriver(String driver)
        {
            if (driver == null || driver.trim().isEmpty())
            {
                return this;
            }

            return withCommandArguments(Argument.of("--ipam-driver", '=', driver));
        }


        /**
         * Set custom IPAM driver specific options (equates to the --ipam-opt argument).
         *
         * @param options  values that resolve to one or more valid
         *                 IPAM driver options
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --ipam-opt option applied
         */
        public Create ipamOpts(Object... options)
        {
            if (options == null || options.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--ipam-opt", '=', new Argument.Multiple(options)));
        }


        /**
         * Set metadata on a network (equates to the --ipv6 argument).
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --ipv6 option applied
         */
        public Create ipv6()
        {
            return withCommandArguments(Argument.of("--ipv6"));
        }


        /**
         * Set metadata on a network (equates to the --label argument).
         *
         * @param labels  values that resolve to one or more valid
         *                 meta-data values
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --label option applied
         */
        public Create labels(Object... labels)
        {
            if (labels == null || labels.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--label", '=', new Argument.Multiple(labels)));
        }


        /**
         * Set custom driver specific options (equates to the --opt argument).
         *
         * @param options  values that resolve to one or more valid
         *                 custom driver options
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --opt option applied
         */
        public Create driverOpts(Object... options)
        {
            if (options == null || options.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--opt", '=', new Argument.Multiple(options)));
        }


        /**
         * Set the subnet in CIDR format that represents a
         * network segment (equates to the --subnet argument).
         *
         * @param cidr  values that resolve to one or more valid
         *              CIDR arguments, for example 172.28.0.0/16
         *
         * @return  a new {@link Create} instance that is the same as this
         *          instance with the --subnet option applied
         */
        public Create subnet(Object... cidr)
        {
            if (cidr == null || cidr.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--subnet", '=', new Argument.Multiple(cidr)));
        }


        @Override
        protected Create withCommandArguments(List<Argument> endArgs,
                                              Argument...    args)
        {
            return new Create(getCommandArguments().with(args), endArgs);
        }


        @Override
        protected Create withoutCommandArguments(List<Argument> endArgs,
                                                 Argument...    args)
        {
            return new Create(getCommandArguments().without(args), endArgs);
        }
    }


    /**
     * A representation of the Docker Network Disconnect command.
     */
    public static class Disconnect extends Network<Disconnect>
    {
        /**
         * Create a {@link Disconnect}.
         *
         * @param arguments  the command {@link Arguments}
         * @param argList    {@link List} of {@link Argument}s
         *                   that will be appended to the end of
         *                   the command  line
         */
        private Disconnect(Arguments arguments,
                           List<?>   argList)
        {
            super(arguments, argList);
        }


        /**
         * Create a {@link Disconnect} command.
         *
         * @param networkName    the name of the network to connect to
         * @param containerName  the name of the container to
         *                       connect to the network
         */
        private Disconnect(String networkName,
                           String containerName)
        {
            super(Arguments.of("network", "disconnect"), Arrays.asList(networkName, containerName));
        }


        /**
         * Force the container to disconnect from a network (equates to the --force argument).
         *
         * @return  a new {@link Disconnect} instance that is the same as this
         *          instance with the --force option applied
         */
        public Disconnect force()
        {
            return withCommandArguments(Argument.of("--force"));
        }


        @Override
        protected Disconnect withCommandArguments(List<Argument> endArgs,
                                                  Argument...    args)
        {
            return new Disconnect(getCommandArguments().with(args), endArgs);
        }


        @Override
        protected Disconnect withoutCommandArguments(List<Argument> endArgs,
                                                     Argument...    args)
        {
            return new Disconnect(getCommandArguments().without(args), endArgs);
        }
    }


    /**
     * A representation of the Docker Network Inspect command.
     */
    public static class Inspect extends Network<Inspect>
    {
        /**
         * Create an {@link Inspect} command.
         *
         * @param names  the names of the networks to inspect
         */
        private Inspect(List<String> names)
        {
            super(Arguments.of("network", "inspect"), names);
        }


        /**
         * Create a {@link Inspect}.
         *
         * @param arguments  the command {@link Arguments}
         * @param argList    {@link List} of {@link Argument}s
         *                   that will be appended to the end of
         *                   the command  line
         */
        private Inspect(Arguments arguments,
                        List<?>   argList)
        {
            super(arguments, argList);
        }


        /**
         * Format the output using the given go template (equates to the --format option).
         *
         * @param format  the template to use to format the output.
         *
         * @return  a copy of this {@link Inspect} command with the
         *          --format option applied
         */
        public Inspect format(String format)
        {
            if (format == null || format.trim().isEmpty())
            {
                return this;
            }

            return withCommandArguments(Argument.of("--format", '=', format));
        }


        @Override
        protected Inspect withCommandArguments(List<Argument> endArgs,
                                               Argument...    args)
        {
            return new Inspect(getCommandArguments().with(args), endArgs);
        }


        @Override
        protected Inspect withoutCommandArguments(List<Argument> endArgs,
                                                  Argument...    args)
        {
            return new Inspect(getCommandArguments().without(args), endArgs);
        }
    }


    /**
     * A representation of the Docker Network LS command.
     */
    public static class LS extends Network<LS>
    {
        /**
         * Create a new {@link LS} command.
         */
        private LS()
        {
            super(Arguments.of("network", "ls"), Collections.emptyList());
        }


        /**
         * Create a {@link LS}.
         *
         * @param arguments  the command {@link Arguments}
         * @param argList    {@link List} of {@link Argument}s
         *                   that will be appended to the end of
         *                   the command  line
         */
        private LS(Arguments arguments,
                   List<?>   argList)
        {
            super(arguments, argList);
        }


        /**
         * Filter the network list (equates to the --filter argument).
         *
         * @param filters  values that resolve to one or more valid filter arguments
         *
         * @return  a new {@link LS} instance that is the same as this
         *          instance with the --filter option applied
         */
        public LS filter(Object... filters)
        {
            if (filters == null || filters.length == 0)
            {
                return this;
            }

            return withCommandArguments(Argument.of("--filter", '=', new Argument.Multiple(filters)));
        }


        /**
         * Do not truncate output, equates to the --no-trunc argument.
         *
         * @return  a new {@link LS} instance that is the same as this
         *          instance with the --no-trunc option applied
         */
        public LS noTruncate()
        {
            return withCommandArguments(Argument.of("--no-trunc"));
        }


        /**
         * Only show numeric IDs, equates to the --quiet argument.
         *
         * @return  a new {@link LS} instance that is the same as this
         *          instance with the --quiet option applied
         */
        public LS quiet()
        {
            return withCommandArguments(Argument.of("--quiet"));
        }


        @Override
        protected LS withCommandArguments(List<Argument> endArgs,
                                          Argument...    args)
        {
            return new LS(getCommandArguments().with(args), endArgs);
        }


        @Override
        protected LS withoutCommandArguments(List<Argument> endArgs,
                                             Argument...    args)
        {
            return new LS(getCommandArguments().without(args), endArgs);
        }
    }


    /**
     * A representation of the Docker Network rm command.
     */
    public static class Remove extends Network<Remove>
    {
        /**
         * Create a new {@link Remove} command.
         *
         * @param names  the names of the network to remove
         */
        private Remove(List<String> names)
        {
            super(Arguments.of("network", "rm"), names);
        }


        private Remove(Arguments arguments,
                       List<?>   argList)
        {
            super(arguments, argList);
        }


        @Override
        protected Remove withCommandArguments(List<Argument> endArgs,
                                              Argument...    args)
        {
            return new Remove(getCommandArguments().with(args), endArgs);
        }


        @Override
        protected Remove withoutCommandArguments(List<Argument> endArgs,
                                                 Argument...    args)
        {
            return new Remove(getCommandArguments().without(args), endArgs);
        }
    }
}
