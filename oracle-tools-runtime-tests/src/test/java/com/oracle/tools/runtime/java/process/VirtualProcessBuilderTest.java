/*
 * File: VirtualProcessBuilderTest.java
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

import com.oracle.tools.junit.AbstractTest;
import com.oracle.tools.runtime.java.virtualization.VirtualizationClassLoader;
import com.oracle.tools.runtime.java.virtualization.VirtualizedSystem;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the {@link VirtualProcessBuilder}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VirtualProcessBuilderTest extends AbstractTest
{
    private String       className   = VirtualProcessRunnableStub.class.getCanonicalName();
    private String       startMethod = "start";
    private String       stopMethod  = "stop";
    private List<String> arguments   = Arrays.asList("A", "B", "C");


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateInternalProcessWithCorrectClass() throws Exception
    {
        VirtualProcessBuilder builder = new VirtualProcessBuilder("Test", className, startMethod, stopMethod);
        VirtualProcess        process = (VirtualProcess) builder.realize();

        assertThat(process.getApplicationClassName(), is(className));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateInternalProcessWithCorrectStartMethod() throws Exception
    {
        VirtualProcessBuilder builder = new VirtualProcessBuilder("Test", className, startMethod, stopMethod);
        VirtualProcess        process = (VirtualProcess) builder.realize();

        assertThat(process.getStartMethodName(), is(startMethod));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateInternalProcessWithCorrectStopMethod() throws Exception
    {
        VirtualProcessBuilder builder = new VirtualProcessBuilder("Test", className, startMethod, stopMethod);
        VirtualProcess        process = (VirtualProcess) builder.realize();

        assertThat(process.getStopMethodName(), is(stopMethod));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateInternalProcessWithCorrectArgs() throws Exception
    {
        VirtualProcessBuilder builder = new VirtualProcessBuilder("Test", className, startMethod, stopMethod);

        builder.addArguments(arguments);

        VirtualProcess process = (VirtualProcess) builder.realize();

        assertThat(process.getArguments(), containsInAnyOrder("A", "B", "C"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldUseCreateClassLoaderWithCorrectClassPath() throws Exception
    {
        String                coherenceJar = "/coherence.jar";
        URL                   coherenceURL = new File(coherenceJar).toURI().toURL();
        String                incubatorJar = "/coherence-common.jar";
        URL                   incubatorURL = new File(incubatorJar).toURI().toURL();
        String                classpath    = coherenceJar + File.pathSeparatorChar + incubatorJar;

        VirtualProcessBuilder builder      = new VirtualProcessBuilder("Test", className, startMethod, stopMethod);

        builder.getEnvironment().put("CLASSPATH", classpath);

        VirtualizationClassLoader classLoader = (VirtualizationClassLoader) builder.createClassLoader();

        assertThat(classLoader.getURLs(),
                   arrayContainingInAnyOrder(coherenceURL, incubatorURL));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassLoaderWithBulkAddedSystemProperties() throws Exception
    {
        Properties properties = new Properties();

        properties.setProperty("test.1", "value.1");
        properties.setProperty("test.2", "value.2");

        VirtualProcessBuilder builder = new VirtualProcessBuilder("Test", className, startMethod, stopMethod);

        builder.getSystemProperties().putAll(properties);

        VirtualizationClassLoader classLoader = (VirtualizationClassLoader) builder.createClassLoader();

        VirtualizedSystem         system      = classLoader.getVirtualizedSystem();

        assertThat(system.getProperties().getProperty("test.1"), is("value.1"));
        assertThat(system.getProperties().getProperty("test.2"), is("value.2"));
    }


    /**
     * Method description
     *
     * @throws Exception
     */
    @Test
    public void shouldCreateClassLoaderWithIndividualAddedSystemProperties() throws Exception
    {
        VirtualProcessBuilder builder = new VirtualProcessBuilder("Test", className, startMethod, stopMethod);

        builder.setSystemProperty("test.1", "value.1");
        builder.setSystemProperty("test.2", "value.2");

        VirtualizationClassLoader classLoader = (VirtualizationClassLoader) builder.createClassLoader();

        VirtualizedSystem         system      = classLoader.getVirtualizedSystem();

        assertThat(system.getProperties().getProperty("test.1"), is("value.1"));
        assertThat(system.getProperties().getProperty("test.2"), is("value.2"));
    }
}
