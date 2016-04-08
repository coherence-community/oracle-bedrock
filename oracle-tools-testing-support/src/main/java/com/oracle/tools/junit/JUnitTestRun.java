/*
 * File: JUnitApplication.java
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

import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.annotations.PreferredMetaClass;

import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.options.ClassName;

import com.oracle.tools.runtime.options.DisplayName;


/**
 * An instance of a {@link JavaApplication} that runs a suite of JUnit
 * tests.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
@PreferredMetaClass(JUnitTestRun.MetaClass.class)
public interface JUnitTestRun extends JavaApplication
{
    /**
     * Start execution of the JUnit test suite.
     *
     * @param options  the {@link Options} controlling the test execution
     */
    void startTests(Options options);


    /**
     * The {@link MetaClass} for {@link JUnitTestRun}s.
     */
    class MetaClass implements com.oracle.tools.runtime.options.MetaClass<JavaApplication>
    {
        /**
         * Constructs a {@link MetaClass} for a {@link JavaApplication}.
         */
        @Options.Default
        public MetaClass()
        {
        }


        @Override
        public Class<? extends JavaApplication> getImplementationClass(Platform platform, Options options)
        {
            return SimpleJUnitTestRun.class;
        }

        @Override
        public void onLaunching(Platform platform, Options options)
        {
            options.add(ClassName.of(JUnitTestRunner.class));
            options.addIfAbsent(DisplayName.of("JUnit"));
        }

        @Override
        public void onLaunched(Platform platform, Application application, Options options)
        {
            JUnitTestRun jUnitTestRun = application.get(JUnitTestRun.class);

            if (jUnitTestRun != null)
            {
                jUnitTestRun.startTests(options);
            }
        }
    }
}
