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

package com.oracle.tools.junit;

import com.oracle.tools.runtime.java.virtualization.Virtualization;
import org.junit.After;
import org.junit.Before;

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
     * Before tests we ask the {@link Virtualization} framework for
     * the physical {@link System}.  This ensure we can reset it after the
     * tests.
     */
    @Before
    public void prepareForVirtualization()
    {
        Virtualization.getPhysicalSystem();
    }


    /**
     * After tests we stop ensure that the {@link Virtualization} has been
     * stopped.  This ensures that physical system resources are returned
     * to normal.
     */
    @After
    public void cleanupAfterVirtualization()
    {
        Virtualization.stop();
    }
}
