/*
 * File: RemoteExecutor.java
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

package com.oracle.tools.runtime.concurrent;

import com.oracle.tools.util.CompletionListener;

import java.util.concurrent.Callable;

/**
 * A means of submitting {@link Callable}s and/or {@link Runnable}s
 * for asynchronous remote execution.
 * <p>
 * The submitted {@link Callable}s/{@link Runnable}s are not required to be
 * {@link java.io.Serializable}.
 * If they are, they will be serialized/deserialized.  If they are not, their
 * class-names will be serialized (and then later deserialized for execution).
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see ControllableRemoteExecutor
 */
public interface RemoteExecutor
{
    /**
     * Submits the {@link Callable} to be asynchronously executed remotely.
     *
     * @param callable  the {@link Callable} to be executed remotely
     * @param listener  a {@link CompletionListener} to be notified upon
     *                  completed execution of the {@link Callable}
     * @param <T>       the return type of the {@link Callable}
     *
     * @throws IllegalStateException  if the {@link RemoteExecutor} is closed
     */
    public <T> void submit(Callable<T>           callable,
                           CompletionListener<T> listener) throws IllegalStateException;


    /**
     * Submits the {@link Runnable} for asynchronous remote execution.
     *
     * @param runnable  the {@link Runnable} to asynchronously execute remotely
     *
     * @throws IllegalStateException  if the {@link RemoteExecutor} is closed
     */
    public void submit(Runnable runnable) throws IllegalStateException;
}
