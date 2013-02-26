/*
 * File: VirtualizationClassLoader.java
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

package com.oracle.tools.runtime.java.virtualization;

import org.junit.Test;

import java.net.URL;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link VirtualizationClassLoader}
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VirtualizationClassLoaderTest {

    @Test
    public void shouldSetCorrectClassPathWhenPathEntriesAreWindowsStyle() throws Exception {
        Properties systemProperties = new Properties();
        systemProperties.setProperty(VirtualizationClassLoader.PROPERTY_JAVA_CLASS_PATH, "c:/lib/test1.jar;c:/lib/test2.jar");
        systemProperties.setProperty(VirtualizationClassLoader.PROPERTY_PATH_SEPARATOR, ";");

        VirtualizationClassLoader classLoader = VirtualizationClassLoader.newInstance("Test", null, new Properties(), systemProperties);
        URL[] path = classLoader.getClassPath();

        assertThat(path.length, is(2));
        assertThat(path[0].toExternalForm(), is("file:/c:/lib/test1.jar"));
        assertThat(path[1].toExternalForm(), is("file:/c:/lib/test2.jar"));
    }

    @Test
    public void shouldSetCorrectClassPathWhenPathEntriesNonWindowsStyle() throws Exception {
        Properties systemProperties = new Properties();
        systemProperties.setProperty(VirtualizationClassLoader.PROPERTY_JAVA_CLASS_PATH, "/lib/test1.jar:/lib/test2.jar");
        systemProperties.setProperty(VirtualizationClassLoader.PROPERTY_PATH_SEPARATOR, ":");

        VirtualizationClassLoader classLoader = VirtualizationClassLoader.newInstance("Test", null, new Properties(), systemProperties);
        URL[] path = classLoader.getClassPath();

        assertThat(path.length, is(2));
        assertThat(path[0].toExternalForm(), is("file:/lib/test1.jar"));
        assertThat(path[1].toExternalForm(), is("file:/lib/test2.jar"));
    }
}
