/*
 * File: CallableStaticMethod.java
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

import java.io.Serializable;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.concurrent.Callable;

/**
 * A {@link Callable} for a static {@link Method}.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class CallableStaticMethod<T> implements Callable<T>, Serializable
{
    /**
     * The name of the {@link Class} on which the static method is defined.
     */
    private final String m_className;

    /**
     * The name of the static method.
     */
    private final String m_methodName;

    /**
     * The arguments for the method.
     */
    private String[] m_args;


    /**
     * Constructs an CallableStaticMethod.
     *
     * @param className   the name of the class
     * @param methodName  the name of the static method
     * @param args        the arguments for the method
     */
    public CallableStaticMethod(String           className,
                                String           methodName,
                                Iterable<String> args)
    {
        m_className  = className;
        m_methodName = methodName;

        ArrayList<String> arguments = new ArrayList<String>();

        for (String arg : args)
        {
            arguments.add(arg);
        }

        m_args = new String[arguments.size()];
        arguments.toArray(m_args);
    }


    /**
     * Constructs an CallableStaticMethod.
     *
     * @param className   the name of the class
     * @param methodName  the name of the static method
     * @param args        the arguments for the method
     */
    public CallableStaticMethod(String    className,
                                String    methodName,
                                String... args)
    {
        m_className  = className;
        m_methodName = methodName;
        m_args       = args;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public T call() throws Exception
    {
        // use the Thread's context ClassLoader to resolve the Class.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        Class<?>    clazz       = classLoader.loadClass(m_className);

        Method      method;
        boolean     hasArgs;

        try
        {
            // FUTURE: we should enhance this to support any type of argument, not just a list of strings.
            method  = clazz.getMethod(m_methodName, new String[0].getClass());
            hasArgs = true;
        }
        catch (NoSuchMethodException e)
        {
            method  = clazz.getMethod(m_methodName);
            hasArgs = false;
        }

        // ensure that the method is declared as a static
        if (Modifier.isStatic(method.getModifiers()))
        {
            return hasArgs ? (T) method.invoke(null, (Object) m_args) : (T) method.invoke(null);
        }
        else
        {
            throw new IllegalArgumentException("The specified method [" + m_className + "." + m_methodName
                                               + "] is not static");
        }
    }


    /**
     * Method description
     *
     * @return
     */
    @Override
    public String toString()
    {
        return "CallableStaticMethod{ClassName=\'" + m_className + '\'' + ", Method='" + m_methodName + '\''
               + ", Arguments=" + Arrays.toString(m_args) + '}';
    }
}
