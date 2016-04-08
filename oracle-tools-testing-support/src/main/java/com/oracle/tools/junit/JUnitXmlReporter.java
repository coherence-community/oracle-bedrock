/*
 * File: JUnitXmlReporter.java
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

package com.oracle.tools.junit;

import com.oracle.tools.Option;

import java.io.File;
import java.io.PrintStream;

import java.util.Properties;
import java.util.Queue;
import java.util.TreeSet;

/**
 * An implementation of a {@link JUnitReporter} that produces an XML based report
 * in the same format as that produced by the Maven Surefire plugin.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class JUnitXmlReporter extends JUnitReporter
{
    public JUnitXmlReporter()
    {
        super(null);
    }


    public JUnitXmlReporter(File reportFolder)
    {
        super(reportFolder);
    }

    @Override
    public void writeReport(PrintStream out, TestResults test)
    {
        Queue<Event> results = test.getEvents();

        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");

        out.printf("<testsuite name=\"%s\" time=\"%.3f\" tests=\"%d\" errors=\"%d\" skipped=\"%d\" failures=\"%d\">\n",
                   test.getClassName(),
                   test.getTestTime(),
                   results.size(),
                   test.getErrorCount(),
                   test.getSkipCount(),
                   test.getFailureCount());

        Properties properties = getTestProperties();

        if (properties != null)
        {
            out.println("  <properties>");
            for (String name : new TreeSet<>(properties.stringPropertyNames()))
            {
                out.printf("    <property name=\"%s\" value=\"%s\"/>\n", name, properties.getProperty(name));
            }
            out.println("  </properties>");
        }

        for (Event result : results)
        {
            float       testTime    = ((float) result.getTime()) / 1000.0f;

            out.printf("  <testcase name=\"%s\" classname=\"%s\" time=\"%.3f\"",
                              result.getName(), result.getClassName(), testTime);

            if (result.isSkipped())
            {
                String message = result.getMessage();

                if (message == null)
                {
                    out.println(">\n    <skipped/>");
                }
                else
                {
                    out.printf(">\n    <skipped message=\"%s\"/>\n", message);
                }

                out.println("  </testcase>");
            }
            else if (result.isFailure() || result.isError())
            {
                String exception = result.getException();
                String message   = result.getMessage();

                out.printf(">\n    <failure message=\"%s\" type=\"%s\"", message, exception);

                StackTraceElement[] stackTrace = result.getStackTrace();
                if (stackTrace != null)
                {
                    out.printf("><![CDATA[%s: %s\n", exception, message);
                    for (StackTraceElement element : stackTrace)
                    {
                        out.println("\tat " + element.toString());
                    }
                    out.println("]]></failure>");
                }
                else
                {
                    out.println("/>");
                }

                out.println("  </testcase>");
            }
            else
            {
                out.println("/>");
            }
        }

        out.println("</testsuite>");
    }


    public String getReportFileName(Class testClass)
    {
        return getReportFileName(testClass.getCanonicalName());
    }


    public String getReportFileName(String className)
    {
        return "TEST-" + className + ".xml";
    }


    public static Option at(File folder)
    {
        if (folder.exists() && !folder.isDirectory())
        {
            throw new IllegalStateException("Report folder exists but is not a directory " + folder);
        }

        return new JUnitXmlReporter(folder).asOption();
    }


    public static Option toConsole()
    {
        return new JUnitXmlReporter().asOption();
    }
}
