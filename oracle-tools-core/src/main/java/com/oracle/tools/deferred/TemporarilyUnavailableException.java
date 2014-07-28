/*
 * File: TemporarilyUnavailableException.java
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

/**
 * An {@link TemporarilyUnavailableException} is thrown by a {@link Deferred}
 * when an attempt to resolve and acquire an underlying object was unsuccessful
 * however it may be retried at some point in the future.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
@SuppressWarnings("serial")
public class TemporarilyUnavailableException extends UnavailableException
{
    /**
     * Constructs a {@link TemporarilyUnavailableException} for the specified
     * {@link Deferred}.
     *
     * @param deferred  the {@link Deferred}
     */
    public TemporarilyUnavailableException(Deferred<?> deferred)
    {
        super(deferred);
    }


    /**
     * Constructs a {@link TemporarilyUnavailableException} for the specified
     * {@link Deferred}, with the specified causing {@link Throwable}.
     *
     * @param deferred  the {@link Deferred}
     * @param cause     the {@link Throwable} that may have caused the
     *                  object to be unavailable
     */
    public TemporarilyUnavailableException(Deferred<?> deferred,
                                           Throwable   cause)
    {
        super(deferred, cause);
    }


    @Override
    public String toString()
    {
        return String.format("TemporarilyUnavailableException: %s\n%s", getMessage(), super.toString());
    }
}
