/*
 * File: DeferredMatchTest.java
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

package com.oracle.bedrock.deferred;

import com.oracle.bedrock.deferred.Deferred;
import com.oracle.bedrock.deferred.Existing;
import com.oracle.bedrock.deferred.TemporarilyUnavailableException;
import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.core.Is.is;

import static org.junit.Assert.fail;

/**
 * Unit tests for {@link DeferredMatch}.
 * <p>
 * Copyright (c) 2012. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredMatchTest
{
    /**
     * Ensure that the last evaluated value in a {@link DeferredMatch} is
     * retained.
     */
    @Test
    public void shouldRetainLastEvaluatedValue()
    {
        Deferred<String>      deferred      = new Existing<String>("Hello World");
        DeferredMatch<String> deferredMatch = new DeferredMatch<String>(deferred, is("Gudday"));

        try
        {
            deferredMatch.get();
            fail("Match should have thrown an TemporarilyUnavailableException");
        }
        catch (TemporarilyUnavailableException e)
        {
            Assert.assertEquals("Hello World", deferredMatch.getLastUsedMatchValue());
        }
    }
}
