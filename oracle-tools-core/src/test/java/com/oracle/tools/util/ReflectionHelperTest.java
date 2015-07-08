/*
 * File: ReflectionHelperTest.java
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

import org.junit.Assert;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.lang.reflect.Method;

import java.util.HashMap;

/**
 * Unit Tests for the {@link ReflectionHelper} class.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ReflectionHelperTest
{
    /**
     * Ensure that we can find an interface method that is implemented by an abstract parent class
     */
    @Test
    public void shouldLocateMethodImplementedByAbstractParentClass()
    {
        HashMap map    = new HashMap();
        Method  method = ReflectionHelper.getCompatibleMethod(map.getClass(), "put", "key", "value");

        Assert.assertThat(method, is(notNullValue()));
    }


    /**
     * Ensure that we can find a static main method.
     */
    @Test
    public void shouldLocateStaticMainMethod()
    {
        Method method = ReflectionHelper.getCompatibleMethod(ReflectionHelperMain.class,
                                                             "main",
                                                             (Object) new String[] {"hello"});

        Assert.assertThat(method, is(notNullValue()));
    }


    /**
     * Ensure that we can find a void method with zero arguments.
     */
    @Test
    public void shouldLocateVoidMethodWithZeroArguments()
    {
        // find it on an interface
        Method interfaceMethod = ReflectionHelper.getCompatibleMethod(ReflectionHelperInterface.class,
                                                                      "voidMethodWithZeroArguments");

        Assert.assertThat(interfaceMethod, is(notNullValue()));

        // find it on a class
        Method classMethod = ReflectionHelper.getCompatibleMethod(ReflectionHelperClass.class,
                                                                  "voidMethodWithZeroArguments");

        Assert.assertThat(classMethod, is(notNullValue()));
    }


    /**
     * Ensure that we can find a void method with one primitive argument.
     */
    @Test
    public void shouldLocateVoidMethodWithOnePrimitiveArgument()
    {
        // find it on an interface
        Method interfaceMethod = ReflectionHelper.getCompatibleMethod(ReflectionHelperInterface.class,
                                                                      "voidMethodWithOnePrimitiveArgument",
                                                                      42);

        Assert.assertThat(interfaceMethod, is(notNullValue()));

        // find it on a class
        Method classMethod = ReflectionHelper.getCompatibleMethod(ReflectionHelperClass.class,
                                                                  "voidMethodWithOnePrimitiveArgument",
                                                                  42);

        Assert.assertThat(classMethod, is(notNullValue()));
    }


    /**
     * Ensure that we can find a void method with one wrapper argument.
     */
    @Test
    public void shouldLocateVoidMethodWithOneWrapperArgument()
    {
        // find it on an interface
        Method interfaceMethod = ReflectionHelper.getCompatibleMethod(ReflectionHelperInterface.class,
                                                                      "voidMethodWithOneWrapperArgument",
                                                                      42L);

        Assert.assertThat(interfaceMethod, is(notNullValue()));

        // find it on a class
        Method classMethod = ReflectionHelper.getCompatibleMethod(ReflectionHelperClass.class,
                                                                  "voidMethodWithOneWrapperArgument",
                                                                  42L);

        Assert.assertThat(classMethod, is(notNullValue()));
    }


    /**
     * Ensure that we can find a void method with one {@link String} argument.
     */
    @Test
    public void shouldLocateVoidMethodWithOneStringArgument()
    {
        // find it on an interface
        Method interfaceMethod = ReflectionHelper.getCompatibleMethod(ReflectionHelperInterface.class,
                                                                      "voidMethodWithOneStringArgument",
                                                                      "42");

        Assert.assertThat(interfaceMethod, is(notNullValue()));

        // find it on a class
        Method classMethod = ReflectionHelper.getCompatibleMethod(ReflectionHelperClass.class,
                                                                  "voidMethodWithOneStringArgument",
                                                                  "42");

        Assert.assertThat(classMethod, is(notNullValue()));
    }
}
