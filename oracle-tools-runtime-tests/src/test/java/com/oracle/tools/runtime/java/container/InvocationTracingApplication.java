/*
 * File: InvocationTracingApplication.java
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

package com.oracle.tools.runtime.java.container;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple application that is designed to run in a container so that we can
 * test and trace container infrastructure calls and application lifecycle.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class InvocationTracingApplication
{
    /**
     * The name of the standard application main method.
     */
    public static final String METHOD_STATIC_MAIN = "main";

    /**
     * The name of a custom application start method.
     */
    public static final String METHOD_STATIC_START = "staticStart";

    /**
     * The name of a custom application start method (without arguments).
     */
    public static final String METHOD_STATIC_START_NO_ARGS = "staticStartNoArgs";

    /**
     * The name of a custom application stop method (without arguments).
     */
    public static final String METHOD_STATIC_STOP_NO_ARGS = "staticStopNoArgs";

    /**
     * The list of {@link MethodInvocation}s against this class.
     */
    private static ArrayList<MethodInvocation> m_methodInvocations;

    /**
     * The next value to be returned when calling a static method on this
     * class that requires a return value.
     */
    private static Object m_nextReturnValue;


    /**
     * Initialize the InvocationTracingApplication.
     */
    public static void initialize()
    {
        m_methodInvocations = new ArrayList<MethodInvocation>();
        m_nextReturnValue   = null;
    }


    /**
     * Sets the value to return when a static method is next called on this
     * class that returns a value
     *
     * @param nextReturnValue  the next value to return
     */
    public static void setNextReturnValue(Object nextReturnValue)
    {
        m_nextReturnValue = nextReturnValue;
    }


    /**
     * Obtains the {@link MethodInvocation}s that have occurred against the class.
     *
     * @return an {@link Iterable} over the {@link MethodInvocation}
     */
    public static Iterable<MethodInvocation> getMethodInvocations()
    {
        return m_methodInvocations;
    }


    /**
     * The standard application main method.
     *
     * @param args  application arguments
     */
    public static void main(String[] args)
    {
        m_methodInvocations.add(new MethodInvocation(METHOD_STATIC_MAIN, args, Void.class));
    }


    /**
     * A custom application start method.
     *
     * @param args  application arguments
     *
     * @return the pre-defined result
     */
    public static Object staticStart(String[] args)
    {
        m_methodInvocations.add(new MethodInvocation(METHOD_STATIC_START, args, m_nextReturnValue));

        return m_nextReturnValue;
    }


    /**
     * A custom application start method.
     *
     * @return the pre-defined result
     */
    public static Object staticStartNoArgs()
    {
        m_methodInvocations.add(new MethodInvocation(METHOD_STATIC_START_NO_ARGS, null, m_nextReturnValue));

        return m_nextReturnValue;
    }


    /**
     * A custom application stop method.
     *
     * @return the pre-defined result
     */
    public static Object staticStopNoArgs()
    {
        m_methodInvocations.add(new MethodInvocation(METHOD_STATIC_STOP_NO_ARGS, null, m_nextReturnValue));

        return m_nextReturnValue;
    }


    /**
     * Records a Method invocation.
     */
    public static class MethodInvocation
    {
        /**
         * The name of the method that was called.
         */
        private String m_methodName;

        /**
         * The arguments that where passed to the method.
         */
        private Object[] m_arguments;

        /**
         * The result of the method or a Void.class if void.
         */
        private Object m_result;


        /**
         * Constructs a {@link MethodInvocation}.
         *
         * @param methodName  the name of the method invoked
         * @param arguments   the arguments used for the invocation
         * @param result      the result of the invocation (a Void.class if void)
         */
        public MethodInvocation(String   methodName,
                                Object[] arguments,
                                Object   result)
        {
            m_methodName = methodName;
            m_arguments  = arguments;
            m_result     = result;
        }


        /**
         * Obtains the name of the method that was invoked.
         *
         * @return the name of the method
         */
        public String getMethodName()
        {
            return m_methodName;
        }


        /**
         * Obtains the arguments passed to the method invocation
         * (null if no arguments passed).
         *
         * @return  the arguments
         */
        public Object[] getArguments()
        {
            return m_arguments;
        }


        /**
         * Obtains the result of the method invocation
         * (Void.class if the invocation was of a void method).
         *
         * @return  the result
         */
        public Object getResult()
        {
            return m_result;
        }
    }
}
