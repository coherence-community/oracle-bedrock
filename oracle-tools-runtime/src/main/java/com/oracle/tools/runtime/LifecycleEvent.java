/*
 * File: LifecycleEvent.java
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * The contents of this file are subject to the terms and conditions of 
 * the Common Development and Distribution License 1.0 (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License by consulting the LICENSE.txt file
 * distributed with this file, or by consulting
 * or https://oss.oracle.com/licenses/CDDL
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

/**
 * A {@link LifecycleEvent} captures the information pertaining to an event
 * in the life-cycle of a subject (ie: object).
 *
 * @param <T> the type of object on which the event occurred
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface LifecycleEvent<T>
{
    /**
     * Obtains the type of the {@link LifecycleEvent}.
     *
     * @return the type of the {@link LifecycleEvent}
     */
    public Enum<?> getType();


    /**
     * Obtains the object of the event
     *
     * @return
     */
    public T getObject();
}
