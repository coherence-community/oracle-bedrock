/*
 * File: Tests.java
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

package com.oracle.bedrock.junit.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An option representing an immutable collection of {@link TestClasses}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Tests implements Option.Collector<TestClasses, Tests>
{
    /**
     * The {@link List} of {@link TestClasses} to use to resolve the {@link Class}es
     * to use for a test run.
     */
    private List<TestClasses> listOfTestClasses;


    /**
     * Create a {@link Tests} instance with the specified
     * {@link List} of {@link TestClasses}.
     *
     * @param list  the {@link List} of {@link TestClasses} that will be used
     *              to resolve test classes to execute
     */
    private Tests(List<TestClasses> list)
    {
        this.listOfTestClasses = list;
    }


    /**
     * Determine whether this {@link Tests} instance contains any
     * {@link TestClasses} to execute.
     *
     * @return  {@linkplain true} if there are tests to execute
     */
    public boolean isEmpty()
    {
        return listOfTestClasses.isEmpty();
    }


    @Override
    public Iterator<TestClasses> iterator()
    {
        return listOfTestClasses.iterator();
    }


    @Override
    @SuppressWarnings("unchecked")
    public <O> Iterable<O> getInstancesOf(Class<O> requiredClass)
    {
        if (requiredClass.isAssignableFrom(TestClasses.class))
        {
            return (Iterable<O>) listOfTestClasses;
        }
        else
        {
            return Collections.emptyList();
        }
    }


    @Override
    public Tests with(TestClasses testClasses)
    {
        List<TestClasses> list = new ArrayList<>(this.listOfTestClasses);

        list.add(testClasses);

        return new Tests(list);
    }


    public Tests with(Tests tests)
    {
        List<TestClasses> list = new ArrayList<>(this.listOfTestClasses);

        list.addAll(tests.listOfTestClasses);

        return new Tests(list);
    }


    @Override
    public Tests without(TestClasses testClasses)
    {
        List<TestClasses> list = new ArrayList<>(this.listOfTestClasses);

        list.remove(testClasses);

        return new Tests(list);
    }


    /**
     * Create an empty set of tests.
     *
     * @return  an empty {@link Tests} implementation
     */
    @Options.Default
    public static Tests none()
    {
        return new Tests(Collections.emptyList());
    }
}
