/*
 * File: WindowsShellOptions.java
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

package com.oracle.tools.runtime.remote.winrm;

import com.oracle.tools.ComposableOption;
import com.oracle.tools.Option;

import com.oracle.tools.runtime.PropertiesBuilder;

import java.util.Iterator;

import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;

/**
 * An {@link Option} to define options to set on a WinRM remote shell.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class WindowsShellOptions implements Option, ComposableOption<WindowsShellOptions>
{
    /** 
     * The default lifetime of a WinRM shell
     */
    public static final Duration DEFAULT_SHELL_LIFETIME = ObjectFactories.DATATYPE.newDuration(TimeUnit.MINUTES.toMillis(30));

    /**
     * A {@link PropertiesBuilder} to hold the WinRM options.
     */
    private final PropertiesBuilder options;

    /**
     * The value to use to set the lifetime of a WinRM Shell
     */
    private Duration shellLifetime;

    /**
     * Create a {@link WindowsShellOptions}.
     */
    private WindowsShellOptions()
    {
        this(new PropertiesBuilder(), DEFAULT_SHELL_LIFETIME);
    }


    /**
     * Create a {@link WindowsShellOptions}.
     */
    private WindowsShellOptions(PropertiesBuilder variables, Duration shellLifetime)
    {
        this.options       = variables;
        this.shellLifetime = shellLifetime;
    }


    /**
     * Create a {@link WindowsShellOptions} containing the basic set of
     * WinRM options on top of which the the specified set of WinRM
     * options will be added.
     *
     * @param builder the {@link PropertiesBuilder} containing the
     *                set of WinRM options to add to the basic options
     *
     * @return  a {@link WindowsShellOptions} containing the basic set
     *          of WinRM options on top of which the the specified set
     *          of WinRM options will be added.
     */
    public static WindowsShellOptions with(PropertiesBuilder builder)
    {
        WindowsShellOptions options = basic();

        options.options.addProperties(builder);

        return new WindowsShellOptions(new PropertiesBuilder(builder), DEFAULT_SHELL_LIFETIME);
    }


    /**
     * Create a {@link WindowsShellOptions} containing the basic set of
     * WinRM options.
     *
     * @return a {@link WindowsShellOptions} containing the basic set of
     *         WinRM options
     */
    public static WindowsShellOptions basic()
    {
        WindowsShellOptions shellOptions = new WindowsShellOptions();

        shellOptions.options.setProperty("WINRS_CONSOLEMODE_STDIN", true);
        shellOptions.options.setProperty("WINRS_NOPROFILE", false);
        shellOptions.options.setProperty("WINRS_CODEPAGE", 437);

        return shellOptions;
    }


    /**
     * Obtains the {@link PropertiesBuilder} to use for realizing the
     * WinRM options.
     *
     * @return  the {@link PropertiesBuilder}
     */
    public PropertiesBuilder getBuilder()
    {
        return options;
    }


    /**
     * Defines a custom environment variable, overriding any previously
     * defined variable of the same name.
     *
     * @param name   the name of the environment variable
     * @param value  the value of the environment variable
     *
     * @return  the {@link WindowsShellOptions} {@link Option} to permit fluent-method calls
     */
    public WindowsShellOptions set(String name,
                                   Object value)
    {
        options.setProperty(name, value);

        return this;
    }


    /**
     * Defines a custom environment variable, if-and-only-if it's not already defined.
     *
     * @param name   the name of the environment variable
     * @param value  the value of the environment variable
     *
     * @return  the {@link WindowsShellOptions} {@link Option} to permit fluent-method calls
     */
    public WindowsShellOptions setIfAbsent(String name,
                                           Object value)
    {
        options.setPropertyIfAbsent(name, value);

        return this;
    }


    /**
     * Defines a custom environment variable, overriding any previously
     * defined variable of the same name, the value to be used to be taken
     * from the specified iterator when the {@link #getBuilder()} is realized.
     *
     * @param name      the name of the environment variable
     * @param iterator  the {@link java.util.Iterator} providing values for the variable
     *
     * @return  the {@link WindowsShellOptions} {@link Option} to permit fluent-method calls
     */
    public WindowsShellOptions set(String      name,
                                   Iterator<?> iterator)
    {
        options.setProperty(name, iterator);

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
     * @return  the {@link WindowsShellOptions} {@link Option} to permit fluent-method calls
     */
    public WindowsShellOptions setIfAbsent(String      name,
                                           Iterator<?> iterator)
    {
        options.setPropertyIfAbsent(name, iterator);

        return this;
    }


    /**
     * Set the lifetime of the WinRM shell
     *
     * @param lifetime the lifetime of the WinRM shell
     * @param units    the units to apply to the lifetime value
     *
     * @return this {@link WindowsShellOptions}
     */
    public WindowsShellOptions withLifetime(long lifetime, TimeUnit units)
    {
        shellLifetime = ObjectFactories.DATATYPE.newDuration(units.toMillis(lifetime));

        return this;
    }


    /**
     * Obtain the lifetime of the WinRM shell.
     *
     * @return the lifetime of the WinRM shell
     */
    public Duration getShellLifetime()
    {
        return shellLifetime;
    }

    @Override
    public WindowsShellOptions compose(WindowsShellOptions other)
    {
        // make a copy of the existing options
        WindowsShellOptions shellOptions = new WindowsShellOptions(new PropertiesBuilder(this.options),
                                                                   DEFAULT_SHELL_LIFETIME);

        // add all of the other options
        shellOptions.options.addProperties(other.options);

        shellOptions.shellLifetime = other.shellLifetime != null ? other.shellLifetime : this.shellLifetime;

        return shellOptions;
    }
}
