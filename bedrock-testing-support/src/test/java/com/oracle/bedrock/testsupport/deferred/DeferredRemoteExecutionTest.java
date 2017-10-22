/*
 * File: DeferredRemoteExecutionTest.java
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

package com.oracle.bedrock.testsupport.deferred;

import com.oracle.bedrock.deferred.Deferred;
import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.testsupport.deferred.DeferredRemoteExecution;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link DeferredRemoteExecution}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DeferredRemoteExecutionTest
{
    /**
     * Ensure that we can correctly determine the type of a Callable.
     */
    @Test
    public void shouldReturnCorrectCallableType()
    {
        Deferred<String> deferred = new DeferredRemoteExecution<String>(null,
                                                                        new RemoteCallable<String>()
                                                                        {
                                                                            @Override
                                                                            public String call() throws Exception
                                                                            {
                                                                                return "hello world";
                                                                            }
                                                                        });

        Assert.assertEquals(String.class, deferred.getDeferredClass());
    }
}
