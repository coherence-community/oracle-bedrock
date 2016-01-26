/*
 * File: Variable.java
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

package com.oracle.tools.options;

import com.oracle.tools.Option;

import com.oracle.tools.lang.ExpressionEvaluator;

/**
 * A {@link Collectable} {@link Option} representing a variable, consisting
 * of a name and value, for an {@link ExpressionEvaluator}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @see Variables
 */
public class Variable implements Option.Collectable
{
    /**
     * The name of the {@link Variable}.
     */
    private String name;

    /**
     * The value of the {@link Variable}.
     */
    private Object value;


    /**
     * Constructs a {@link Variable} with no value.
     *
     * @param name the name of the {@link Variable}
     */
    private Variable(String name)
    {
        this.name  = name;
        this.value = "";
    }


    /**
     * Constructs a {@link Variable} based on another {@link Variable}.
     *
     * @param property the {@link Variable} from which to construct
     *                 (copy) the new {@link Variable}
     */
    private Variable(Variable property)
    {
        this.name  = property.getName();
        this.value = property.getValue();
    }


    /**
     * Constructs a {@link Variable}.
     *
     * @param name  the name of the {@link Variable}
     * @param value the value of the {@link Variable}
     */
    private Variable(String name,
                     Object value)
    {
        this.name  = name;
        this.value = value;
    }


    /**
     * Constructs a {@link Variable}.
     *
     * @param name   the name of the {@link Variable}
     * @param object the values for the {@link Variable}
     */
    public static Variable with(String name,
                                Object object)
    {
        return new Variable(name, object);
    }


    /**
     * Obtains the name of the {@link Variable}.
     *
     * @return the name of the {@link Variable}
     */
    public String getName()
    {
        return name;
    }


    /**
     * Obtains the value of the {@link Variable}.
     *
     * @return the value of the {@link Variable}
     */
    public Object getValue()
    {
        return value;
    }


    @Override
    public String toString()
    {
        return String.format("{name=%s, value=%s}", name, value);
    }


    @Override
    public Class<Variables> getCollectorClass()
    {
        return Variables.class;
    }
}
