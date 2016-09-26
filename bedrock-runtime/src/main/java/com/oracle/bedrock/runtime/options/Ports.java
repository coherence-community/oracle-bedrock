/*
 * File: Ports.java
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

package com.oracle.bedrock.runtime.options;

import com.oracle.bedrock.ComposableOption;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.options.Decoration;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An {@link Option} used to hold and capture ports.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Ports implements ComposableOption<Ports>
{
    /**
     * The {@link List} of ports.
     */
    private final Map<String, Port> ports;


    private Ports(Map<String, Port> ports)
    {
        this.ports = ports;
    }


    /**
     * Obtain the ports.
     *
     * @return  an immutable {@link List} of ports
     */
    public Collection<Port> getPorts()
    {
        return Collections.unmodifiableCollection(ports.values());
    }


    /**
     * Obtain a {@link SystemProperties} instance that is a copy of the specified
     * {@link SystemProperties} with all of the properties contained within this
     * {@link Ports} altered to be the value of the mapped ports.
     *
     * @param properties  the {@link SystemProperties} to copy
     *
     * @return  a {@link SystemProperties} with the mapped port values
     */
    public SystemProperties asMappedProperties(SystemProperties properties)
    {
        SystemProperties    copy             = new SystemProperties(properties);
        Map<String, Object> mappedProperties = new HashMap<>();

        for (SystemProperty property : properties)
        {
            String name = property.getName();

            if (ports.containsKey(name))
            {
                Port port = ports.get(name);

                mappedProperties.put(name, port.getMappedPort());
            }
        }

        return copy.addAll(mappedProperties);
    }


    @Override
    public synchronized Ports compose(Ports other)
    {
        Map<String, Port> set = new HashMap<>(this.ports);

        set.putAll(other.ports);

        return new Ports(set);
    }


    /**
     * Create a {@link Ports} holding the
     * specified port values.
     *
     * @param ports  the port values
     *
     * @return  a {@link Ports} holding the specified
     *          port values
     */
    public static Ports of(Port... ports)
    {
        return of(Arrays.asList(ports));
    }


    /**
     * Create a {@link Ports} holding the
     * specified port values.
     *
     * @param ports  the port values
     *
     * @return  a {@link Ports} holding the specified
     *          port values
     */
    public static Ports of(Collection<Port> ports)
    {
        Map<String, Port> map = new HashMap<>();

        ports.forEach((port) -> map.put(port.getName(), port));

        return new Ports(map);
    }


    /**
     * Determine whether there is a port mapping of the specific name.
     *
     * @param name  the name of the port mapping
     *
     * @return  true if a port mapping exists, otherwise false
     */
    public boolean hasPort(String name)
    {
        return ports.containsKey(name);
    }


    /**
     * Create a default empty {@link Ports}.
     *
     * @return  a default empty {@link Ports}
     */
    @OptionsByType.Default
    public static Ports empty()
    {
        return new Ports(Collections.emptyMap());
    }


    /**
     * Return an {@link Option} wrapping a {@link ResolveHandler}
     * capable of capturing values from{@link Argument}s
     * and {@link SystemProperty}s
     *
     * @return a {@link ResolveHandler}
     */
    public static Option capture()
    {
        return Decoration.of(ResolveHandler.INSTANCE);
    }


    /**
     * An individual port.
     */
    public static class Port
    {
        private final String name;
        private final int    actualPort;
        private final int    mappedPort;


        /**
         * Constructs a {@link Port}.
         *
         * @param name        the name of the {@link Port}
         * @param actualPort  the port
         */
        public Port(String name,
                    int    actualPort)
        {
            this(name, actualPort, actualPort);
        }


        /**
         * Constructs a {@link Port}.
         *
         * @param name        the name of the {@link Port}
         * @param actualPort  the port
         * @param mappedPort  the mapped port
         */
        public Port(String name,
                    int    actualPort,
                    int    mappedPort)
        {
            this.name       = name;
            this.actualPort = actualPort;
            this.mappedPort = mappedPort;
        }


        public String getName()
        {
            return name;
        }


        public int getActualPort()
        {
            return actualPort;
        }


        public int getMappedPort()
        {
            return mappedPort;
        }


        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }

            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            Port port = (Port) o;

            if (actualPort != port.actualPort)
            {
                return false;
            }

            if (mappedPort != port.mappedPort)
            {
                return false;
            }

            return name != null ? name.equals(port.name) : port.name == null;

        }


        @Override
        public int hashCode()
        {
            int result = name != null ? name.hashCode() : 0;

            result = 31 * result + actualPort;
            result = 31 * result + mappedPort;

            return result;
        }


        @Override
        public String toString()
        {
            return "Port(name='" + name + "' " + actualPort + "=" + mappedPort + ')';
        }
    }


    /**
     * A {@link Argument.ResolveHandler} and {@link SystemProperty.ResolveHandler}
     * used to capture port values.
     */
    private static class ResolveHandler implements Argument.ResolveHandler, SystemProperty.ResolveHandler
    {
        /**
         * The singleton instance of a {@link ResolveHandler}.
         */
        private static ResolveHandler INSTANCE = new ResolveHandler();


        @Override
        public void onResolve(String        name,
                              String        value,
                              OptionsByType optionsByType)
        {
            try
            {
                optionsByType.add(Ports.of(new Port(name, Integer.parseInt(value))));
            }
            catch (NumberFormatException e)
            {
                // ignored - we just don't capture non-numeric values
            }
        }


        @Override
        public void onResolve(String        name,
                              List<String>  values,
                              OptionsByType optionsByType)
        {
            for (String value : values)
            {
                try
                {
                    optionsByType.add(Ports.of(new Port(name, Integer.parseInt(value))));
                }
                catch (NumberFormatException e)
                {
                    // ignored - we just don't capture non-numeric values
                }
            }
        }
    }
}
