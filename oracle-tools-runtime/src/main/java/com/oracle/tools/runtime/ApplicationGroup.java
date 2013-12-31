/*
 * File: ApplicationGroup.java
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
 * An {@link ApplicationGroup} represents a collection of related {@link Application}s
 * at runtime.
 * <p>
 * {@link ApplicationGroup}s are created using {@link ApplicationGroupBuilder}s.
 *
 * @param <A>  The type of the {@link Application}
 * <p>
 * Copyright (c) 2011-2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface ApplicationGroup<A extends Application<A>> extends Iterable<A>, Closeable
{
    /**
     * <b>WARNING:</b>This method is now deprecated.  It is replaced by {@link #close()}.
     * <p>
     * Destroys all of the {@link Application}s in the {@link ApplicationGroup}.
     * Upon returning from this method you can safely assume all
     * {@link Application}s in the {@link ApplicationGroup} are no longer running.
     *
     * @deprecated This method has been replaced by {@link #close()}
     */
    @Deprecated
    public void destroy();


    /**
     * Closes the {@link ApplicationGroup} including all of the {@link Application}s
     * that are part of the {@link ApplicationGroup}.
     * <p>
     * Upon returning it is safe to assume that all previously running
     * {@link Application}s have been closed.
     * <p>
     * If the {@link ApplicationGroup} is already closed, calling this method has no effect.
     * </p>
     */
    @Override
    public void close();
}
