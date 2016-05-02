/*
 * File: AbstractTest.java
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

import com.oracle.bedrock.runtime.java.container.Container;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * An {@link AbstractTest} defined base test functionality for both unit
 * and functional tests.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public abstract class AbstractTest
{
    /**
     * Before running any tests we ask the {@link Container} framework
     * for the physical {@link System}.  This ensures that we can restore
     * the physical System after the tests have run thus preventing any
     * System-level side-effects occurring between or after the tests.
     */
    @BeforeClass
    public static void onBeforeTestsInClass()
    {
        Container.getPlatformScope();
    }


    /**
     * After running tests we ensure that the {@link Container} has been
     * stopped.  This ensures that physical system resources are returned
     * to normal, thus preventing any System-level side-effects from the tests
     * in the class.
     */
    @AfterClass
    public static void onAfterTestsInClass()
    {
        Container.stop();
    }


    /**
     * Before each test in the class we start the {@link Container} to
     * protect System-level resources.
     */
    @Before
    public void onBeforeEachTest()
    {
        Container.start();
    }


    /**
     * After each test in the class we restore the {@link Container} to
     * return System-level resources back to normal.
     */
    @After
    public void onAfterEachTest()
    {
        Container.stop();
    }
}
