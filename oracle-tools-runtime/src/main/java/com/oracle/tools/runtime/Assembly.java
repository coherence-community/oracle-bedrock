/*
 * File: Assembly.java
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

package com.oracle.tools.runtime;

import java.io.Closeable;

/**
 * Represents a collection of related {@link Application}s at runtime.
 * <p>
 * {@link Assembly}s are created using {@link AssemblyBuilder}s.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of the {@link Application} in the {@link Assembly}
 */
public interface Assembly<A extends Application> extends Iterable<A>, Closeable
{
    /**
     * Closes the {@link Assembly} including all of the {@link Application}s
     * that are part of the {@link Assembly}.
     * <p>
     * Upon returning it is safe to assume that all previously running
     * {@link Application}s have been closed.
     * <p>
     * If the {@link Assembly} is already closed, calling this method has no effect.
     * </p>
     */
    @Override
    public void close();


    /**
     * Obtains the number of {@link Application} instances in the {@link Assembly}.
     *
     * @return  the number of {@link Application}s in the {@link Assembly}
     */
    public int size();
}
