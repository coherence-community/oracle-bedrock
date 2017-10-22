/*
 * File: JUnitTestRun.java
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

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.options.DisplayName;

/**
 * An instance of a {@link JavaApplication} that runs a suite of JUnit
 * tests.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public interface JUnitTestRun extends JavaApplication
{
    /**
     * Start execution of the JUnit test suite.
     *
     * @param optionsByType  the {@link OptionsByType} controlling the test execution
     */
    void startTests(OptionsByType optionsByType);


    /**
     * The {@link MetaClass} for {@link JUnitTestRun}s.
     */
    class MetaClass implements com.oracle.bedrock.runtime.MetaClass<JavaApplication>
    {
        /**
         * Constructs a {@link MetaClass} for a {@link JavaApplication}.
         */
        @OptionsByType.Default
        public MetaClass()
        {
        }


        @Override
        public Class<? extends JavaApplication> getImplementationClass(Platform      platform,
                                                                       OptionsByType optionsByType)
        {
            return SimpleJUnitTestRun.class;
        }


        @Override
        public void onLaunching(Platform      platform,
                                OptionsByType optionsByType)
        {
            optionsByType.add(ClassName.of(JUnitTestRunner.class));
            optionsByType.addIfAbsent(DisplayName.of("JUnit"));
        }


        @Override
        public void onLaunch(Platform      platform,
                             OptionsByType optionsByType)
        {
            // there's nothing to do before launching the application
        }


        @Override
        public void onLaunched(Platform        platform,
                               JavaApplication application,
                               OptionsByType   optionsByType)
        {
            JUnitTestRun jUnitTestRun = application.get(JUnitTestRun.class);

            if (jUnitTestRun != null)
            {
                jUnitTestRun.startTests(optionsByType);
            }
        }
    }
}
