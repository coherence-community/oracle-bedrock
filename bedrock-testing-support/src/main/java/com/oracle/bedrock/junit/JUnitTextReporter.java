/*
 * File: JUnitTextReporter.java
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

package com.oracle.bedrock.junit;

import com.oracle.bedrock.Option;

import java.io.File;
import java.io.PrintStream;

/**
 * An implementation of a {@link JUnitReporter} that produces a text based report
 * in the same format as that produced by the Maven Surefire plugin.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JUnitTextReporter extends JUnitReporter
{
    /**
     * A line of dashes used on reports.
     */
    public static final String DASHES = "-------------------------------------------------------------------------------";


    /**
     * Create a {@link JUnitTextReporter} that prints the
     * report to {@link System#out}.
     */
    public JUnitTextReporter()
    {
        super(null);
    }


    /**
     * Create a {@link JUnitTextReporter} that prints the
     * report to a file in the specified folder.
     *
     * @param  reportFolder  the folder to print the report file to
     */
    public JUnitTextReporter(File reportFolder)
    {
        super(reportFolder);
    }


    @Override
    public void writeReport(PrintStream out, TestResults test)
    {
        String className = test.getClassName();
        int    failures  = test.getFailureCount();
        int    errors    = test.getErrorCount();

        out.println(DASHES);
        out.println("Test set: " + className);
        out.println(DASHES);

        out.printf("Tests run: %d, Failures: %d, Errors: %d, Skipped: %d, Time elapsed: %.3f sec %s - in %s\n",
                   test.getTestCount(),
                   failures,
                   errors,
                   test.getSkipCount(),
                   test.getTestTime(),
                   (errors > 0 || failures > 0) ? "<<< FAILURE!" : "",
                   className);

        for (Event event : test.getEvents())
        {
            if (event.isError() || event.isFailure())
            {
                float               testTime   = ((float) event.getTime()) / 1000.0f;
                StackTraceElement[] stackTrace = event.getStackTrace();

                out.printf("%s Time elapsed: %.3f sec  <<< FAILURE!\n", event.getName(), testTime);

                out.printf("%s : %s\n", event.getException(), event.getMessage());

                for (StackTraceElement element : stackTrace)
                {
                    out.println("\t at " + element);
                }
            }
        }

    }


    @Override
    public String getReportFileName(String className)
    {
        return className + ".txt";
    }


    /**
     * Obtain the file name that will be used to print a test
     * report for the specified {@link Class}.
     *
     * @param testClass  the {@link Class} to obtain the file name for
     *
     * @return  the file name that will be used to print a test report
     */
    public String getReportFileName(Class testClass)
    {
        return getReportFileName(testClass.getCanonicalName());
    }


    /**
     * Obtain a {@link JUnitTextReporter} as an {@link Option}
     * that will print test reports to the specified folder.
     *
     * @param folder  the folder to print test reports to
     *
     * @return  a {@link JUnitTextReporter} as an {@link Option}
     *          that will print test reports to the specified folder
     */
    public static Option at(File folder)
    {
        if (folder.exists() && !folder.isDirectory())
        {
            throw new IllegalStateException("Report folder exists but is not a directory " + folder);
        }

        return new JUnitTextReporter(folder).asOption();
    }


    /**
     * Obtain a {@link JUnitTextReporter} as an {@link Option}
     * that will print test reports to {@link System#out}.
     *
     * @return  a {@link JUnitTextReporter} as an {@link Option}
     *          that will print test reports to {@link System#out}
     */
    public static Option toConsole()
    {
        return new JUnitTextReporter().asOption();
    }
}
