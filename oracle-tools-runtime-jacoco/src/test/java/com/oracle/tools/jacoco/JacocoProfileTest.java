/*
 * File: JacocoProfileTest.java
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

package com.oracle.tools.jacoco;

import applications.SleepingApplication;
import com.oracle.tools.Option;
import com.oracle.tools.lang.ExpressionEvaluator;
import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.console.SystemApplicationConsole;
import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the {@link JacocoProfile}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JacocoProfileTest
{
    /**
     * Ensure we can configure and establish a {@link JacocoProfile} as an {@link Option}.
     */
    @Test
    public void shouldEstablishJaCoCoProfileUsingAnOption() throws Exception
    {
        // define the JaCoCo destination file pattern
        String jacocoDestinationFileName = "jacoco-${oracletools.runtime.id}.exec";

        // create a temp file name for JaCoCo to output the code coverage report to
        File destinationFile = new File(System.getProperty("java.io.tmpdir"), jacocoDestinationFileName);

        // define the JaCoCo profile
        JacocoProfile profile = new JacocoProfile("destfile=" + destinationFile);

        // define the SleepingApplication
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).setPreferIPv4(true).addOption(profile);

        // build and start the SleepingApplication
        LocalPlatform      platform = LocalPlatform.getInstance();

        ApplicationConsole console  = new SystemApplicationConsole();

        File               telemetricsFile;

        try (SimpleJavaApplication application = platform.realize("sleeping", schema, console))
        {
            ExpressionEvaluator evaluator = new ExpressionEvaluator(application.getOptions());

            // create a File representing the JaCoCo telemetrics file
            telemetricsFile = new File(System.getProperty("java.io.tmpdir"),
                                       evaluator.evaluate(jacocoDestinationFileName, String.class));
        }

        // assert that JaCoCo created the telemetrics file (and thus the agent ran)
        assertThat(telemetricsFile.exists(), is(true));

        // assert that the telemetrics file contains something
        assertThat(telemetricsFile.length(), is(greaterThan(0L)));
    }


    /**
     * Ensure we can configure and establish a {@link JacocoProfile} using a System Property
     */
    @Test
    public void shouldEstablishJaCoCoProfileUsingASystemProperty() throws Exception
    {
        // define the JaCoCo destination file pattern
        String jacocoDestinationFileName = "jacoco-${oracletools.runtime.id}.exec";

        // create a temp file name for JaCoCo to output the code coverage report to
        File destinationFile = new File(System.getProperty("java.io.tmpdir"), jacocoDestinationFileName);

        // define the JaCoCo profile as a System Property
        System.getProperties().setProperty("oracletools.profile.jacoco", "destfile=" + destinationFile);

        // define the SleepingApplication
        SimpleJavaApplicationSchema schema =
            new SimpleJavaApplicationSchema(SleepingApplication.class.getName()).setPreferIPv4(true);

        // build and start the SleepingApplication
        LocalPlatform      platform = LocalPlatform.getInstance();

        ApplicationConsole console  = new SystemApplicationConsole();

        File               telemetricsFile;

        try (SimpleJavaApplication application = platform.realize("sleeping", schema, console))
        {
            ExpressionEvaluator evaluator = new ExpressionEvaluator(application.getOptions());

            // create a File representing the JaCoCo telemetrics file
            telemetricsFile = new File(System.getProperty("java.io.tmpdir"),
                                       evaluator.evaluate(jacocoDestinationFileName, String.class));
        }
        finally
        {
            System.getProperties().remove("oracletools.profile.jacoco");
        }

        // assert that JaCoCo created the telemetrics file (and thus the agent ran)
        assertThat(telemetricsFile.exists(), is(true));

        // assert that the telemetrics file contains something
        assertThat(telemetricsFile.length(), is(greaterThan(0L)));
    }
}
