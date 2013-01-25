/*
 * File: VirtualProcessMethodInvoker.java
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

package com.oracle.tools.runtime.java.process;

import com.oracle.tools.runtime.java.virtualization.Virtualization;
import com.oracle.tools.runtime.java.virtualization.VirtualizedSystem;
import com.oracle.tools.runtime.java.virtualization.VirtualizedSystemClassLoader;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.util.List;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An {@link VirtualProcessMethodInvoker} is a {@link Runnable} that will call a
 * method through reflection on a class isolated with a specific {@link ClassLoader}
 * and return any return value from invoking the method.
 * <p>
 * If the method is not static an instance of the class will be created.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class VirtualProcessMethodInvoker implements Runnable
{
    private final VirtualizedSystemClassLoader m_classLoader;
    private final String                       m_className;
    private final String                       m_methodName;
    private String[]                           m_args;
    private Object                             m_instance;
    private AtomicReference<Throwable>         m_throwable = new AtomicReference<Throwable>(null);
    private Object                             m_result;
    private AtomicBoolean                      m_finished = new AtomicBoolean(false);


    /**
     * Constructs a {@link VirtualProcessMethodInvoker}.
     * <p>
     * If the method is not static then an instance of the class will be created by calling
     * the default no-arg constructor.
     *
     * @param name        - the name of the process
     * @param classLoader - the ClassLoader to use for isolation of the method invocation
     * @param className   - the name of the class to call the method on
     * @param methodName  - the name of the method
     * @param arguments        - the arguments to pass to the start method
     */
    public VirtualProcessMethodInvoker(VirtualizedSystemClassLoader classLoader,
                                       String className,
                                       String methodName,
                                       List<String> arguments)
    {
        this(classLoader, className, methodName, arguments, null);
    }


    /**
     * Create an instance of InProcessRunner.
     * <p>
     * If the method is not static then the instance specified will be used to invoke the method.
     * If instance is null an instance of the class will be created by calling
     * the default no-arg constructor.
     *
     * @param classLoader - the ClassLoader to use for isolation of the method invocation
     * @param className   - the name of the class to call the method on
     * @param methodName  - the name of the method
     * @param args        - the arguments to pass to the start method
     * @param instance    - the instance of the class to use to invoke the method if the method is not static
     */
    public VirtualProcessMethodInvoker(VirtualizedSystemClassLoader classLoader,
                                       String className,
                                       String methodName,
                                       List<String> args,
                                       Object instance)
    {
        m_classLoader = classLoader;
        m_className   = className;
        m_methodName  = methodName;
        m_args        = args.toArray(new String[args.size()]);
        m_instance    = instance;
    }


    /**
     * Method description
     *
     * @return
     */
    public VirtualizedSystemClassLoader getClassLoader()
    {
        return m_classLoader;
    }


    /**
     * Method description
     *
     * @return
     */
    public String getClassName()
    {
        return m_className;
    }


    /**
     * Method description
     *
     * @return
     */
    public String getMethodName()
    {
        return m_methodName;
    }


    /**
     * @return any error that was thrown as a result of invoking the method or null if no error occurred
     */
    public Throwable getError()
    {
        return m_throwable.get();
    }


    /**
     * @return the instance of the class used to invoke the method if the method was not static
     */
    public Object getInstance()
    {
        return m_instance;
    }


    /**
     * @return any return value from invoking the method
     */
    @SuppressWarnings({"unchecked"})
    public <T> T getResult()
    {
        return (T) m_result;
    }


    /**
     * Method description
     *
     * @return
     */
    public boolean isFinished()
    {
        return m_finished.get();
    }


    /**
     * Method description
     */
    public void run()
    {
        ClassLoader       originalClassLoader = Thread.currentThread().getContextClassLoader();

        VirtualizedSystem virtualSystem       = m_classLoader.getVirtualizedSystem();

        try
        {
            Thread.currentThread().setContextClassLoader(m_classLoader);
            Virtualization.associateThreadWith(virtualSystem);

            Class<?> aClass;

            try
            {
                aClass = m_classLoader.loadClass(m_className);
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException("Cannot find application class " + m_className + " in classpath", e);
            }

            Method  method;
            boolean hasArgs;

            try
            {
                method  = aClass.getMethod(m_methodName, new String[0].getClass());
                hasArgs = true;
            }
            catch (NoSuchMethodException e)
            {
                method  = aClass.getMethod(m_methodName);
                hasArgs = false;
            }

            if (!Modifier.isStatic(method.getModifiers()))
            {
                if (m_instance == null)
                {
                    m_instance = aClass.newInstance();
                }
            }

            if (hasArgs)
            {
                m_result = method.invoke(m_instance, (Object) m_args);
            }
            else
            {
                m_result = method.invoke(m_instance);
            }

        }
        catch (Throwable t)
        {
            m_throwable.set(t);
        }
        finally
        {
            Virtualization.dissociateThread();

            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }

        synchronized (this)
        {
            m_finished.set(true);
            notifyAll();
        }
    }
}
