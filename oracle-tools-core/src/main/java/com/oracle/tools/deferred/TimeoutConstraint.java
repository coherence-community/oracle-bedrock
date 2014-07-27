/*
 * File: TimeoutConstraint.java
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

package com.oracle.tools.deferred;

import java.util.Iterator;

/**
 * Defines the timeout constraints to use when attempting to ensure
 * a {@link Deferred} object using an {@link Ensured}.
 * <p>
 * NOTE: Instances of this class may safely be re-used across {@link Ensured}
 * instances.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface TimeoutConstraint
{
    /**
     * Obtains the minimum number of milliseconds to initially wait before
     * attempting to acquire an {@link Object} from a {@link Deferred}.
     *
     * @return  the minimum milliseconds to wait
     */
    public long getInitialDelayMilliseconds();


    /**
     * Obtains the maximum number of milliseconds to wait before
     * giving up on attempting to acquire an {@link Object} from a
     * {@link Deferred}, including the {@link #getInitialDelayMilliseconds()})
     *
     * @return  the maximum milliseconds to retry
     */
    public long getMaximumRetryMilliseconds();


    /**
     * Obtains an {@link Iterable} that can be used to produce an
     * {@link Iterator} consisting of suitable milliseconds to delay
     * between attempts to acquire an {@link Object} from a {@link Deferred}.
     *
     * @return  an {@link Iterator} of milliseconds to delay
     */
    public Iterable<Long> getRetryDelayMillisecondsIterable();
}
