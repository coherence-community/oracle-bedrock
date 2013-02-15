/*
 * File: JavaProcessBuilder.java
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

package com.oracle.tools.runtime.java.process;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * A {@link JavaProcessBuilder} is a builder of Java {@link Process}es.
 * <p>
 * This interface is designed to replaced the class-based
 * Java {@link ProcessBuilder} implementation.  It allows for developers
 * to provide different {@link ProcessBuilder} implementations, yet provide
 * a common interface for configuring them.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public interface JavaProcessBuilder
{
    /**
     * Obtains the name of the application(s) produced by this {@link JavaProcessBuilder}.
     * This is typically used only for display purposes.
     *
     * @return the application name
     */
    String getApplicationName();


    /**
     * Obtains the name of the application class that will be
     * executed by the Java {@link Process}.
     *
     * @return  the name of the application class.
     */
    String getApplicationClassName();


    /**
     * Sets operating system program and arguments for Java {@link Process}es
     * realized by this {@link JavaProcessBuilder}.
     *
     * @param commands  the list containing the program and its arguments
     *
     * @return this {@link JavaProcessBuilder} to enable fluent-style method calls
     */
    public JavaProcessBuilder setCommands(List<String> commands);


    /**
     * Sets operating system program and arguments for Java {@link Process}es
     * realized by this {@link JavaProcessBuilder}.
     *
     * @param commands  the list containing the program and its arguments
     *
     * @return this {@link JavaProcessBuilder} to enable fluent-style method calls
     */
    public JavaProcessBuilder setCommands(String... commands);


    /**
     * Obtains the current list of commands for the {@link JavaProcessBuilder}.
     *
     * @return a list of commands
     */
    public List<String> getCommands();


    /**
     * Obtains a {@link Map} representation of the {@link JavaProcessBuilder}
     * operating system environment variables to be used when {@link #realize()}ing
     * a Java {@link Process}.
     * <p>
     * This {@link Map} is initialized to the currently values of the
     * operating system environment variables in which this {@link JavaProcessBuilder}
     * is running.
     * <p>
     * Any modifications to the returned {@link Map} will be used for
     * any subsequent Java {@link Process} {@link #realize()}d using this
     * {@link JavaProcessBuilder}. However any modifications to the {@link Map}
     * won't affect or be reflected in Java {@link Process}es previously
     * {@link #realize()}d with this {@link JavaProcessBuilder}.
     * <p>
     * If the underlying operating system does not support environment variables,
     * an empty map is returned.
     * <p>
     * Since the external format of environment variable names and
     * values is system-dependent, there may not be a one-to-one
     * mapping between them and Java's Unicode strings.  Nevertheless,
     * the map is implemented in such a way that environment variables
     * which are not modified by Java code will have an unmodified
     * native representation in the subprocess.
     * <p>
     * The returned map and its collection views may not obey the
     * general contract of the {@link Object#equals} and
     * {@link Object#hashCode} methods.
     * <p>
     * The returned map is typically case-sensitive on all platforms.
     * <p>
     * If a security manager exists, its {@link SecurityManager#checkPermission checkPermission}
     * method is called with a <code>{@link RuntimePermission}("getenv.*")</code>
     * permission.  This may result in a {@link SecurityException} being thrown.
     *
     * @return this {@link JavaProcessBuilder} to enable fluent-style method calls
     *
     * @throws SecurityException  when a security manager exists and its
     *                            {@link SecurityManager#checkPermission checkPermission}
     *                            method doesn't allow access to the process environment
     *
     * @see Runtime#exec(String[], String[], java.io.File)
     * @see System#getenv()
     */
    public Map<String, String> getEnvironment();


    /**
     * Obtains the {@link JavaProcessBuilder} working directory, that of which
     * Java {@link Process}es {@link #realize()}d with the said {@link JavaProcessBuilder}
     * will use as their working directory.
     *
     * @return the working directory of the {@link JavaProcessBuilder}.
     *         <code>null</code> indicates to use the current Java {@link Process}
     *         working directory, that of which is typically defined by the
     *         system property <code>user.dir</code>
     */
    public File getWorkingDirectory();


    /**
     * Set the {@link JavaProcessBuilder} working directory, that of which
     * Java {@link Process}es {@link #realize()}d with the said {@link JavaProcessBuilder}
     * will use as their working directory.
     *
     * @param directory  the working directory of the {@link JavaProcessBuilder}.
     *                   <code>null</code> indicates to use the current Java {@link Process}
     *                   working directory, that of which is typically defined by the
     *                   system property <code>user.dir</code>
     *
     * @return this {@link JavaProcessBuilder} to enable fluent-style method calls
     */
    public JavaProcessBuilder setWorkingDirectory(File directory);


    /**
     * Obtains the system properties to be used for the Java {@link Process}es
     * {@link #realize()}d with this {@link JavaProcessBuilder}.
     *
     * @return  the system properties
     */
    public Properties getSystemProperties();


    /**
     * Set the a system property to be used for the Java {@link Process}es
     * {@link #realize()}d with this {@link JavaProcessBuilder}.
     *
     * @param name   the system property name
     * @param value  the system property value
     *
     * @return this {@link JavaProcessBuilder} to enable fluent-style method calls
     */
    public JavaProcessBuilder setSystemProperty(String name,
                                                String value);


    /**
     * Obtains the current {@link List} of arguments for the next
     * Java {@link Process} to be {@link #realize()}d by this
     * {@link JavaProcessBuilder}.
     *
     * @return the {@link List} of arguments
     */
    public List<String> getArguments();


    /**
     * Adds an argument to the current {@link List} of arguments for the next
     * Java {@link Process} to be {@link #realize()}d by this
     * {@link JavaProcessBuilder}.
     *
     * @param arg  the argument
     *
     * @return this {@link JavaProcessBuilder} to enable fluent-style method calls
     */
    public JavaProcessBuilder addArgument(String argument);


    /**
     * Adds the arguments to the current {@link List} of arguments for the next
     * Java {@link Process} to be {@link #realize()}d by this
     * {@link JavaProcessBuilder}.
     *
     * @param arguments the arguments
     *
     * @return this {@link JavaProcessBuilder} to enable fluent-style method calls
     */
    public JavaProcessBuilder addArguments(Iterable<String> arguments);


    /**
     * Adds the arguments to the current {@link List} of arguments for the next
     * Java {@link Process} to be {@link #realize()}d by this
     * {@link JavaProcessBuilder}.
     *
     * @param arguments the arguments
     *
     * @return this {@link JavaProcessBuilder} to enable fluent-style method calls
     */
    public JavaProcessBuilder addArguments(String... arguments);


    /**
     * Realizes a new Java {@link Process} using the current attributes of
     * this {@link JavaProcessBuilder}.
     * <p>
     * If there is a security manager, its {@link SecurityManager#checkExec checkExec}
     * method is called with the first component of this object's
     * <code>command</code> array as its argument. This may result in
     * a {@link SecurityException} being thrown.
     * <p>
     * Starting an operating system process is highly system-dependent.
     * Among the many things that can go wrong are:
     * <ul>
     *   <li>The operating system program file was not found.
     *   <li>Access to the program file was denied.
     *   <li>The working directory does not exist.
     * </ul>
     * <p>
     * In such cases an exception will be thrown.  The exact nature
     * of the exception is system-dependent, but it will always be a
     * subclass of {@link java.io.IOException}.
     * <p>
     * Subsequent modifications to this process builder will not
     * affect the returned {@link Process}.
     *
     * @return a new Java {@link Process} representing the Java process
     *
     * @throws NullPointerException       when an element of the command list is null
     * @throws IndexOutOfBoundsException  when the command is an empty list (has size <code>0</code>)
     * @throws SecurityException          when a security manager exists and its
     *                                    {@link SecurityManager#checkExec checkExec}
     *                                    method doesn't allow creation of the subprocess
     * @throws java.io.IOException        when an I/O error occurs
     *
     * @see Runtime#exec(String[], String[], java.io.File)
     * @see SecurityManager#checkExec(String)
     */
    public Process realize() throws IOException;
}
