/*
 * File: AbstractCoherenceTest.java
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

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.network.Constants;
import com.tangosol.net.CacheFactory;
import org.junit.After;
import org.junit.Before;

/**
 * A base class for JUnit-based Test Classes that make direct use of
 * Oracle Coherence APIs.
 * <p>
 * This class is designed to isolate Coherence-based JUnit tests to ensure
 * that such tests can run side-by-side in all environments, especially on
 * those platforms that require specialized/non-standard network or environmental
 * settings.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractCoherenceTest extends AbstractTest
{
    /**
     * {@inheritDoc}
     */
    @Before
    @Override
    public void onBeforeEachTest()
    {
        super.onBeforeEachTest();

        // we only want to run locally
        System.setProperty("tangosol.coherence.clusterport", Integer.toString(LocalPlatform.getInstance().getAvailablePorts().next()));
        System.setProperty("tangosol.coherence.localhost", Constants.getLocalHost());
        System.setProperty("tangosol.coherence.ttl", Integer.toString(0));
    }


    /**
     * {@inheritDoc}
     */
    @After
    @Override
    public void onAfterEachTest()
    {
        // shutdown the cluster after each test
        CacheFactory.shutdown();

        super.onAfterEachTest();
    }
}
