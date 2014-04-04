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
    private String[] args;


    /**
     * Constructs an RemoteCallableStaticMethod.
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

        ArrayList<String> arguments = new ArrayList<String>();

        for (String arg : args)
        {
            arguments.add(arg);
        }

        this.args = new String[arguments.size()];
        arguments.toArray(this.args);
    }


    /**
     * Constructs an RemoteCallableStaticMethod.
     *
     * @param className   the name of the class
     * @param methodName  the name of the static method
     * @param args        the arguments for the method
     */
    public RemoteCallableStaticMethod(String    className,
                                      String    methodName,
                                      String... args)
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

        Method      method;
        boolean     hasArgs;

        try
        {
            // FUTURE: we should enhance this to support any type of argument, not just a list of strings.
            method  = clazz.getMethod(methodName, new String[0].getClass());
            hasArgs = true;
        }
        catch (NoSuchMethodException e)
        {
            method  = clazz.getMethod(methodName);
            hasArgs = false;
        }

        // ensure that the method is declared as a static
        if (Modifier.isStatic(method.getModifiers()))
        {
            return hasArgs ? (T) method.invoke(null, (Object) args) : (T) method.invoke(null);
        }
        else
        {
            throw new IllegalArgumentException("The specified method [" + className + "." + methodName
                                               + "] is not static");
        }
    }


    @Override
    public String toString()
    {
        return "RemoteCallableStaticMethod{ClassName=\'" + className + '\'' + ", Method='" + methodName + '\''
               + ", Arguments=" + Arrays.toString(args) + '}';
    }
}
