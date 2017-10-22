/*
 * File: JUnitTest.java
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

package com.oracle.bedrock.testsupport.junit;

import com.oracle.bedrock.testsupport.junit.AbstractJUnit4Test;
import com.oracle.bedrock.testsupport.junit.JUnit4Test;
import com.oracle.bedrock.testsupport.junit.MyBadTest;
import com.oracle.bedrock.testsupport.junit.MyOtherTest;
import com.oracle.bedrock.testsupport.junit.options.TestClasses;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.java.ClassPath;
import com.oracle.bedrock.runtime.java.JavaVirtualMachine;
import com.oracle.bedrock.testsupport.junit.JUnitTestRun;
import com.oracle.bedrock.testsupport.junit.JUnitTextReporter;
import com.oracle.bedrock.testsupport.junit.JUnitXmlReporter;
import com.oracle.bedrock.testsupport.junit.SimpleJUnitTestListener;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Functional tests for JUnit application support
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
@RunWith(Parameterized.class)
public class JUnitTest
{
    /**
     * This test class runs with the {@link Parameterized} runner.
     * This is used to run the tests on both the {@link LocalPlatform}
     * and on the containerised {@link JavaVirtualMachine} platform.
     *
     * @return  the test parameters.
     */
    @Parameterized.Parameters(name = "platform={0}")
    public static Collection<Object[]> getTestParameters()
    {
        List<Object[]> parameters = new ArrayList<>();

        parameters.add(new Object[]{LocalPlatform.get().getName(), LocalPlatform.get()});
        parameters.add(new Object[]{JavaVirtualMachine.get().getName(), JavaVirtualMachine.get()});

        return parameters;
    }

    /**
     * Create temporary folders for test reports.
     */
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The {@link Platform} to run tests on
     */
    private Platform platform;


    /**
     * Test class constructor.
     *
     * @param name      the {@link Platform} name
     * @param platform  the {@link Platform} to run tests on
     */
    public JUnitTest(String name, Platform platform)
    {
        this.platform = platform;
    }


    @Test
    public void shouldRunJUnitTests() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();
        TestClasses             tests    = TestClasses.of(JUnit4Test.class, MyOtherTest.class, MyBadTest.class);

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  tests,
                                                  listener.asOption()))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        assertThat(listener.hasTestFailures(), is(true));
        assertThat(listener.getTestCount(), is(7));
        assertThat(listener.getErrorCount(), is(1));
        assertThat(listener.getFailureCount(), is(2));
        assertThat(listener.getSkipCount(), is(4));
    }


    @Test
    public void shouldRunJUnitTestsAndPrintXmlReport() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();
        File                    folder   = temporaryFolder.newFolder();
        TestClasses             tests    = TestClasses.of(JUnit4Test.class);
        JUnitXmlReporter        reporter = new JUnitXmlReporter(folder);

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  tests,
                                                  reporter.asOption(),
                                                  listener.asOption()))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        dumpReports(folder);

        assertThat(new File(folder, reporter.getReportFileName(JUnit4Test.class)).exists(), is(true));
    }


    @Test
    public void shouldRunJUnitTestsAndPrintTextReport() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();
        File                    folder   = temporaryFolder.newFolder();
        TestClasses             tests    = TestClasses.of(JUnit4Test.class);
        JUnitTextReporter       reporter = new JUnitTextReporter(folder);

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  tests,
                                                  reporter.asOption(),
                                                  listener.asOption()))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        dumpReports(folder);

        assertThat(new File(folder, reporter.getReportFileName(JUnit4Test.class)).exists(), is(true));
    }


    @Test
    public void shouldRunJUnitTestsWithClassIncludes() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  listener.asOption(),
                                                  TestClasses.of(JUnit4Test.class, MyOtherTest.class)
                                                          .include("(.)*JUnit4Test")
                                                 ))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        assertThat(listener.hasTestFailures(), is(true));
        assertThat(listener.getTestCount(), is(3));
        assertThat(listener.getErrorCount(), is(0));
        assertThat(listener.getFailureCount(), is(1));
        assertThat(listener.getSkipCount(), is(2));
    }


    @Test
    public void shouldRunJUnitTestsWithClassExcludes() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  listener.asOption(),
                                                  TestClasses.of(JUnit4Test.class, MyOtherTest.class)
                                                          .exclude("(.)*MyOtherTest(.)*")
                                                 ))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        assertThat(listener.hasTestFailures(), is(true));
        assertThat(listener.getTestCount(), is(3));
        assertThat(listener.getErrorCount(), is(0));
        assertThat(listener.getFailureCount(), is(1));
        assertThat(listener.getSkipCount(), is(2));
    }


    @Test
    public void shouldRunJUnitTestsWithMethodIncludes() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  listener.asOption(),
                                                  TestClasses.of(JUnit4Test.class, MyOtherTest.class)
                                                          .include("(.)*JUnit4(.)*#(.)*Pass(.*)")
                                                 ))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        assertThat(listener.hasTestFailures(), is(false));
        assertThat(listener.getTestCount(), is(1));
        assertThat(listener.getErrorCount(), is(0));
        assertThat(listener.getFailureCount(), is(0));
        assertThat(listener.getSkipCount(), is(0));
    }

    @Test
    public void shouldRunJUnitTestsFromClassPathWithMethodIncludes() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  listener.asOption(),
                                                  TestClasses.from(ClassPath.ofClass(AbstractJUnit4Test.class))
                                                          .include("(.)*JUnit4Test#(.)*Pass(.*)")))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        assertThat(listener.hasTestFailures(), is(false));
        assertThat(listener.getTestCount(), is(1));
        assertThat(listener.getErrorCount(), is(0));
        assertThat(listener.getFailureCount(), is(0));
        assertThat(listener.getSkipCount(), is(0));
    }


    @Test
    public void shouldRunJUnitTestsWithMethodExcludes() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  listener.asOption(),
                                                  TestClasses.of(JUnit4Test.class)
                                                          .exclude("#shouldFail")
                                                 ))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        assertThat(listener.hasTestFailures(), is(false));
        assertThat(listener.getTestCount(), is(2));
        assertThat(listener.getErrorCount(), is(0));
        assertThat(listener.getFailureCount(), is(0));
        assertThat(listener.getSkipCount(), is(2));
    }


    @Test
    public void shouldRunJUnitTestsWithMethodIncludesAndExcludes() throws Exception
    {
        SimpleJUnitTestListener listener = new SimpleJUnitTestListener();

        try (JUnitTestRun jUnit = platform.launch(JUnitTestRun.class,
                                                  listener.asOption(),
                                                  TestClasses.of(JUnit4Test.class, MyOtherTest.class)
                                                          .include("(.)*JUnit4(.)*")
                                                          .exclude("#shouldFail")
                                                 ))
        {
            jUnit.waitFor();

            assertThat(listener.awaitCompletion(2, TimeUnit.MINUTES), is(true));
        }

        assertThat(listener.hasTestFailures(), is(false));
        assertThat(listener.getTestCount(), is(2));
        assertThat(listener.getErrorCount(), is(0));
        assertThat(listener.getFailureCount(), is(0));
        assertThat(listener.getSkipCount(), is(2));
    }



    private void dumpReports(File folder) throws Exception
    {

        for (Path report : Files.newDirectoryStream(folder.toPath()))
        {
            System.out.println("===========================================================================");
            System.out.println(report);

            try (BufferedReader reader = new BufferedReader(new FileReader(report.toFile())))
            {
                String line = reader.readLine();

                while (line != null)
                {
                    System.out.println(line);
                    line = reader.readLine();
                }
            }
        }
    }
}
