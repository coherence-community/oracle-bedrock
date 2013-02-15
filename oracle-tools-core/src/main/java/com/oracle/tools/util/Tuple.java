/*
 * File: Tuple.java
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

package com.oracle.tools.util;

/**
 * An immutable sequence of values, each potentially of a different type.
 * <p>
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Tuple
{
    /**
     * Obtains the number of values in the {@link Tuple}.
     *
     * @return  the number of values in the {@link Tuple}
     */
    public int size();


    /**
     * Obtains the value at the specified index.  The first value is at index 0.
     *
     * @param index  the position of the value to obtain
     *
     * @return  the value at the specified position in the {@link Tuple}
     *
     * @throws IndexOutOfBoundsException when 0 < index <= size()
     */
    public Object get(int index) throws IndexOutOfBoundsException;
}
