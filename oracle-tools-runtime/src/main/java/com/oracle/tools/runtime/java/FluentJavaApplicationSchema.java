/*
 * File: FluentJavaApplicationSchema.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.FluentApplicationSchema;

import java.util.Iterator;
import java.util.List;

/**
 * A {@link FluentJavaApplicationSchema} is a Java specific {@link ApplicationSchema}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link JavaApplication} that can be configured by the {@link FluentJavaApplicationSchema}
 * @param <S>  the type of {@link FluentJavaApplicationSchema} that will be returned from fluent methods
 */
public interface FluentJavaApplicationSchema<A extends JavaApplication, S extends FluentJavaApplicationSchema<A, S>>
    extends FluentApplicationSchema<A, S>,
            JavaApplicationSchema<A>
{
    /**
     * Sets the fully-qualified class name of the class containing the main method
     * for the desired application.
     *
     * @param className  the fully-qualified class name of the "main" class
     */
    public S setApplicationClassName(String className);


    /**
     * Adds an additional JVM option to use when starting the Java application.
     *
     * @param option  the JVM option
     * @return  the {@link JavaApplicationSchema}
     */
    public S addJvmOption(String option);


    /**
     * Adds multiple JVM options to use when starting the Java application.
     *
     * @param options  the JVM options
     * @return  the {@link JavaApplicationSchema}
     */
    public S addJvmOptions(String... options);


    /**
     * Adds multiple JVM options to use when starting the Java application.
     *
     * @param options  the JVM options
     * @return  the {@link JavaApplicationSchema}
     */
    public S addJvmOptions(List<String> options);


    /**
     * Sets (replaces) the JVM options to use when starting the Java application.
     *
     * @param options  the JVM options
     * @return  the {@link JavaApplicationSchema}
     */
    public S setJvmOptions(String... options);


    /**
     * Sets (replaces) the JVM options to use when starting the Java application.
     *
     * @param options  the JVM options
     * @return  the {@link JavaApplicationSchema}
     */
    public S setJvmOptions(List<String> options);

    /**
     * Set whether {@link JavaApplication}s realized from this schema should have
     * remote debugging enabled.
     * </p>
     * This will override any default value set if the controlling JVM, which defaults
     * this value to true if running in debug mode and false if running normally.
     *
     * @param remoteDebuggingEnabled  true if remote debugging should be enabled
     *
     * @return the {@link FluentJavaApplicationSchema}
     */
    public S setRemoteDebuggingEnabled(boolean remoteDebuggingEnabled);

    /**
     * Set the flag that determines whether {@link JavaApplication}s realized from this schema
     * should start suspended if remote debugging is also enabled.
     *
     * @param startSuspended true if the application should start suspended if remote
     *                       debugging is also enabled
     *
     * @return the {@link FluentJavaApplicationSchema}
     */
    public S setRemoteDebuggingStartSuspended(boolean startSuspended);

    /**
     * Set the port that will be used for remote debugging if enabled for {@link JavaApplication}s
     * realized from this schema.
     * </p>
     * If the remoteDebugPorts parameter is null, or the iterator is has no more elements then
     * rather than failing to start the application a random port will be assigned for the debug port.
     * </p>
     * If the {@link #getRemoteDebuggingMode()} value is {@link RemoteDebuggingMode#LISTEN_FOR_DEBUGGER} then
     * a different port will be taken from the iterator for each {@link JavaApplication} realized from this schema,
     * and hence each application will listen for debugger connections on a different port.
     * </p>
     * If the {@link #getRemoteDebuggingMode()} value is {@link RemoteDebuggingMode#ATTACH_TO_DEBUGGER} then
     * the first port will be taken from the iterator and used for all {@link JavaApplication}s realized from this
     * schema, and hence each {@link JavaApplication} will attempt to connect back to the same debugger.
     *
     * @param remoteDebugPorts  the {@link java.util.Iterator} providing the ports to assign to the remote debug port
     *
     * @return the {@link FluentJavaApplicationSchema}
     */
    public S setRemoteDebugPorts(Iterator<Integer> remoteDebugPorts);

    /**
     * Set the port that will be used for remote debugging if enabled for {@link JavaApplication}s
     * realized from this schema.
     * </p>
     * If the {@link #getRemoteDebuggingMode()} value is {@link RemoteDebuggingMode#LISTEN_FOR_DEBUGGER} then
     * this port will be used for the first {@link JavaApplication} realized from this schema. If more
     * {@link JavaApplication}s are subsequently realized from this schema then random ports will be assigned
     * and hence each application will listen for debugger connections on a different port.
     * </p>
     * If the {@link #getRemoteDebuggingMode()} value is {@link RemoteDebuggingMode#ATTACH_TO_DEBUGGER} then
     * the this port will be used for all {@link JavaApplication}s realized from this schema, and hence each
     * {@link JavaApplication} will attempt to connect back to the same debugger.
     *
     * @param remoteDebugPort  the the port to assign to the remote debug port
     *
     * @return the {@link FluentJavaApplicationSchema}
     */
    public S setRemoteDebugPort(int remoteDebugPort);

    /**
     * Set the mode that the {@link JavaApplication} will run in if remote
     * debugging is enabled.
     *
     * @param remoteDebuggingMode  the remote debugging mode
     *
     * @return the {@link FluentJavaApplicationSchema}
     */
    public S setRemoteDebuggingMode(RemoteDebuggingMode remoteDebuggingMode);
}
