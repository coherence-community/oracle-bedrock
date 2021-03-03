/*
 * File: NonStaticInnerClasses.java
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

package com.oracle.bedrock.runtime.java;

import com.oracle.bedrock.runtime.concurrent.RemoteCallable;
import com.oracle.bedrock.runtime.concurrent.RemoteRunnable;

/**
 * Supporting class for {@link ContainerBasedJavaApplicationTest#shouldNotAllowNonStaticInnerClassRemoteCallable()}
 * tests asserting the result of a non static inner class, which cannot be serialized, fails correctly.
 *
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Tim Middleton
 */
public class NonStaticInnerClasses
{
    /**
     * A non-static {@link RemoteCallable} inner class that will return a result of "OK".
     */
    public class InvalidCallable implements RemoteCallable<String>
    {
        @Override
        public String call() throws Exception
        {
            return "OK";
        }
    }


    /**
     * A non-static {@link RemoteRunnable} inner class.
     */
    public class InvalidRunnable implements RemoteRunnable
    {
        @Override
        public void run()
        {
            // do nothing
        }
    }
}
