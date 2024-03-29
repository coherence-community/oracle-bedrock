/*
 * File: SessionBuilders.java
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

/**
 * A helper class for creating various types of {@link SessionBuilder}s.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class SessionBuilders
{
    /**
     * Constructs a {@link SessionBuilder} for a Storage Disabled Member.
     *
     * @return a {@link SessionBuilder}
     */
    public static SessionBuilder storageDisabledMember()
    {
        return new StorageDisabledMember();
    }


    /**
     * Constructs a {@link SessionBuilder} for a *Extend Client.
     *
     * @param cacheConfigURI  the Cache Configuration URI
     *
     * @return a {@link SessionBuilder}
     */
    public static SessionBuilder extendClient(String cacheConfigURI)
    {
        return new ExtendClient(cacheConfigURI);
    }


    /**
     * Constructs a {@link SessionBuilder} for a *Extend Client.
     *
     * @param cacheConfigURI  the Cache Configuration URI
     * @param options         additional options to configure the client
     *
     * @return a {@link SessionBuilder}
     */
    public static SessionBuilder extendClient(String cacheConfigURI, Option... options)
    {
        return new ExtendClient(cacheConfigURI, options);
    }
}
