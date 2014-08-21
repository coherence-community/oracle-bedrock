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

import com.oracle.tools.Option;

import com.oracle.tools.runtime.PropertiesBuilder;

import java.util.Iterator;

/**
 * An {@link Option} to define operating system environment variables
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class EnvironmentVariables implements Option
{
    /**
     * Should environment variables be inherited from the current process?
     * <p>
     * When <code>true</code> environment variables from the current process
     * should be used in addition to those defined by this instance.
     * <p>
     * When <code>false</code> a clean environment should be established,
     * only containing those environment variables defined by this instance.
     */
    private boolean inheritVariables;

    /**
     * A {@link PropertiesBuilder} for the custom environment variables.
     */
    private PropertiesBuilder variables;


    /**
     * Privately construct an {@link EnvironmentVariables} {@link Option}
     *
     * @param inheritVariables  are the current process environment variables to be inherited
     * @param variables         a {@link PropertiesBuilder} to produce the environment variables
     */
    private EnvironmentVariables(boolean           inheritVariables,
                                 PropertiesBuilder variables)
    {
        this.inheritVariables = inheritVariables;
        this.variables        = variables;
    }


    /**
     * Determines if the {@link EnvironmentVariables} are to be inherited
     * from the current process.
     *
     * @return  <code>true</code> if environment variables should be inherited
     */
    public boolean areInherited()
    {
        return inheritVariables;
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
     * Obtain an {@link EnvironmentVariables} {@link Option} indicating that
     * environment variables should be inherited from the current process.
     *
     * @return  an {@link EnvironmentVariables} {@link Option}
     */
    public static EnvironmentVariables inherited()
    {
        return new EnvironmentVariables(true, new PropertiesBuilder());
    }


    /**
     * Obtain an {@link EnvironmentVariables} {@link Option} indicating that
     * environment variables should be cleared / empty.
     *
     * @return  an {@link EnvironmentVariables} {@link Option}
     */
    public static EnvironmentVariables areCleared()
    {
        return new EnvironmentVariables(false, new PropertiesBuilder());
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
}
