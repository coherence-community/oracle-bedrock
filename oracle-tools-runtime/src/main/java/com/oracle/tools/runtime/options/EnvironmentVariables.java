/*
 * File: EnvironmentVariables.java
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

package com.oracle.tools.runtime.options;

import com.oracle.tools.ComposableOption;
import com.oracle.tools.Option;

import com.oracle.tools.Options;
import com.oracle.tools.runtime.PropertiesBuilder;

import java.util.Iterator;

/**
 * An {@link Option} to define operating system environment variables, sourced
 * from a specific location.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class EnvironmentVariables implements Option, ComposableOption<EnvironmentVariables>
{
    /**
     * The initial source of environment variables, upon which
     * customizations may be made.
     */
    public enum Source
    {
        /**
         * The initial environment variables will be sourced and based upon
         * those defined by this application, namely those defined by
         * {@link System#getenv()}.
         */
        ThisApplication,

        /**
         * The initial environment variables will be sourced from and based upon
         * those defined by the target platform.
         */
        TargetPlatform,

        /**
         * The initial environment variables provided to an application will
         * be custom (thus starting with none).
         */
        Custom
    }


    /**
     * The location from which initial environment variables will be extracted.
     */
    private Source source;

    /**
     * A {@link PropertiesBuilder} for the custom environment variables, in
     * addition and/or replacing those defined by the {@link #source}.
     */
    private PropertiesBuilder variables;


    /**
     * Privately construct an {@link EnvironmentVariables} {@link Option}
     *
     * @param source     the {@link Source} of the environment variables
     * @param variables  a {@link PropertiesBuilder} for custom environment variables
     */
    private EnvironmentVariables(Source            source,
                                 PropertiesBuilder variables)
    {
        this.source    = source;
        this.variables = variables;
    }


    /**
     * Obtains the {@link Source} of the environment variables
     *
     * @return  the {@link Source}
     */
    public Source getSource()
    {
        return source;
    }


    /**
     * Obtains the {@link PropertiesBuilder} to use for realizing the custom
     * environment variables.
     *
     * @return  the {@link PropertiesBuilder}
     */
    public PropertiesBuilder getBuilder()
    {
        return variables;
    }


    /**
     * Constructs an {@link Option} to create {@link EnvironmentVariables} based
     * on a specific {@link Source}.
     *
     * @param source  the {@link Source} of the environment variables
     *
     * @return  an {@link EnvironmentVariables}
     */
    public static EnvironmentVariables of(Source source)
    {
        return new EnvironmentVariables(source, new PropertiesBuilder());
    }


    /**
     * Constructs a custom set of {@link EnvironmentVariables}, starting
     * initially with a cleared environment.
     *
     * @return  an {@link EnvironmentVariables}
     */
    public static EnvironmentVariables custom()
    {
        return new EnvironmentVariables(Source.Custom, new PropertiesBuilder());
    }


    /**
     * Constructs a custom set of {@link EnvironmentVariables}, starting
     * initially with the environment variables defined by this application.
     *
     * @return  an {@link EnvironmentVariables}
     */
    @Options.Default
    public static EnvironmentVariables inherited()
    {
        return new EnvironmentVariables(Source.ThisApplication, new PropertiesBuilder());
    }


    /**
     * Defines a custom environment variable, overriding any previously
     * defined variable of the same name.
     *
     * @param name   the name of the environment variable
     * @param value  the value of the environment variable
     *
     * @return  the {@link EnvironmentVariables} {@link Option} to permit fluent-method calls
     */
    public EnvironmentVariables set(String name,
                                    Object value)
    {
        variables.setProperty(name, value);

        return this;
    }


    /**
     * Defines a custom environment variable, if-and-only-if it's not already defined.
     *
     * @param name   the name of the environment variable
     * @param value  the value of the environment variable
     *
     * @return  the {@link EnvironmentVariables} {@link Option} to permit fluent-method calls
     */
    public EnvironmentVariables setIfAbsent(String name,
                                            Object value)
    {
        variables.setPropertyIfAbsent(name, value);

        return this;
    }


    /**
     * Defines a custom environment variable, overriding any previously
     * defined variable of the same name, the value to be used to be taken
     * from the specified iterator when the {@link #getBuilder()} is realized.
     *
     * @param name      the name of the environment variable
     * @param iterator  the {@link Iterator} providing values for the variable
     *
     * @return  the {@link EnvironmentVariables} {@link Option} to permit fluent-method calls
     */
    public EnvironmentVariables set(String      name,
                                    Iterator<?> iterator)
    {
        variables.setProperty(name, iterator);

        return this;
    }


    /**
     * Defines a custom environment variable, if-and-only-if it's not already defined,
     * the value to be used to be taken from the specified iterator when the
     * {@link #getBuilder()} is realized.
     *
     * @param name      the name of the environment variable
     * @param iterator  the {@link Iterator} providing values for the variable
     *
     * @return  the {@link EnvironmentVariables} {@link Option} to permit fluent-method calls
     */
    public EnvironmentVariables setIfAbsent(String      name,
                                            Iterator<?> iterator)
    {
        variables.setPropertyIfAbsent(name, iterator);

        return this;
    }


    @Override
    public EnvironmentVariables compose(EnvironmentVariables other)
    {
        // make a copy of the environment variables
        EnvironmentVariables environmentVariables = new EnvironmentVariables(source, new PropertiesBuilder(variables));

        // add all of the other environment variables
        environmentVariables.getBuilder().addProperties(other.getBuilder());

        return environmentVariables;
    }
}
