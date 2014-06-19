/*
 * File: RemoteCallableStaticMethod.java
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

package com.oracle.tools.runtime.java.util;

import com.oracle.tools.runtime.concurrent.RemoteCallable;

import com.oracle.tools.util.ReflectionHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A {@link RemoteCallable} for a static {@link Method}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class RemoteCallableStaticMethod<T> implements RemoteCallable<T>
{
    /**
     * The name of the {@link Class} on which the static method is defined.
     */
    private final String className;

    /**
     * The name of the static method.
     */
    private final String methodName;

    /**
     * The arguments for the method.
     */
    private Object[] args;


    /**
     * Constructs an {@link RemoteCallableStaticMethod} for a String-based var-arg
     * static methods (like <code>public static void main(String[] args)</code> methods).
     * <p>
     * ie: This constructor assumes the method being represented is declared as
     * <code>methodName(String[] args)</code> or <code>methodName(String... args)</code>.
     *
     * @param className   the name of the class
     * @param methodName  the name of the static method
     * @param args        the arguments for the method
     */
    public RemoteCallableStaticMethod(String           className,
                                      String           methodName,
                                      Iterable<String> args)
    {
        this.className  = className;
        this.methodName = methodName;

        ArrayList<String> argumentList = new ArrayList<String>();

        for (String arg : args)
        {
            argumentList.add(arg);
        }

        String[] argumentArray = new String[argumentList.size()];

        argumentList.toArray(argumentArray);

        if (argumentArray.length == 0)
        {
            this.args = new Object[0];
        }
        else
        {
            this.args    = new Object[1];
            this.args[0] = argumentArray;
        }
    }


    /**
     * Constructs an {@link RemoteCallableStaticMethod}.
     * <p>
     * If the method is defined using a var-arg, the last parameter to this constructor
     * must be an array.  eg: the method "methodName(Foo... args)" must be
     * represented as a call this this constructor as:
     * RemoteCallableStaticMethod(className, "methodName", new Foo[]{...});
     *
     * @param className   the name of the class
     * @param methodName  the name of the static method
     * @param args        the arguments for the method
     */
    public RemoteCallableStaticMethod(String    className,
                                      String    methodName,
                                      Object... args)
    {
        this.className  = className;
        this.methodName = methodName;
        this.args       = args;
    }



    @Override
    public T call() throws Exception
    {
        // use the Thread's context ClassLoader to resolve the Class.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>    clazz       = classLoader.loadClass(className);

        // resolve the static method to call
        Method method = ReflectionHelper.getCompatibleMethod(clazz, methodName, args);

        // was the method found?
        if (method == null)
        {
            throw new NoSuchMethodException("The specified method [" + className + "." + methodName
                                            + "] for the arguments [" + Arrays.toString(args)
                                            + "] could not be located");
        }
        else
        {
            // ensure that the method is static
            if (Modifier.isStatic(method.getModifiers()))
            {
                return args.length > 0 ? (T) method.invoke(null, args) : (T) method.invoke(null);
            }
            else
            {
                throw new IllegalArgumentException("The specified method [" + className + "." + methodName
                                                   + "] is not static");
            }
        }
    }


    @Override
    public String toString()
    {
        return "RemoteCallableStaticMethod{ClassName=\'" + className + '\'' + ", Method='" + methodName + '\''
               + ", Arguments=" + Arrays.toString(args) + '}';
    }
}
