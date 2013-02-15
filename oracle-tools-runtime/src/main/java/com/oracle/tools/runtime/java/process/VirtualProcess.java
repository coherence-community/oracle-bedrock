/*
 * File: VirtualProcess.java
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

package com.oracle.tools.runtime.java.process;

import com.oracle.tools.runtime.java.virtualization.VirtualizedSystemClassLoader;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@link Process} that represents a pseudo-process
 * running in an isolated {@link ClassLoader} within a JVM.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 * @author Brian Oliver
 */
public class VirtualProcess extends Process
{
    private String                       m_applicationClassName;
    private VirtualizedSystemClassLoader m_classLoader;
    private String                       m_startMethodName;
    private String                       m_stopMethodName;
    private List<String>                 m_arguments;
    private InputStream                  m_inputStream;
    private InputStream                  m_errorStream;
    private OutputStream                 m_outputStream;
    private VirtualProcessMethodInvoker  m_startInvoker;


    /**
     * Constructs an {@link VirtualProcess}.
     *
     * @param applicationClassName  the application class to run
     * @param classLoader           the {@link ClassLoader} in which to run the application
     * @param startMethodName       the method to start the application
     * @param stopMethodName        the method to stop the application
     * @param arguments             the command-line arguments for the application
     */
    public VirtualProcess(String                       applicationClassName,
                          VirtualizedSystemClassLoader classLoader,
                          String                       startMethodName,
                          String                       stopMethodName,
                          List<String>                 arguments)
    {
        m_applicationClassName = applicationClassName;
        m_startMethodName      = startMethodName;
        m_stopMethodName       = stopMethodName;
        m_arguments            = arguments == null ? new ArrayList<String>() : new ArrayList<String>(arguments);

        m_classLoader          = classLoader;
        m_inputStream          = new NullInputStream();
        m_errorStream          = new NullInputStream();
        m_outputStream         = new NullOutputStream();
    }


    /**
     * Obtains the {@link ClassLoader} used by the {@link VirtualProcess}
     * to load and run the application.
     *
     * @return the application {@link ClassLoader}
     */
    public ClassLoader getClassLoader()
    {
        return m_classLoader;
    }


    /**
     * Obtains the name of {@link Class} of the application being run by
     * the {@link VirtualProcess}.
     *
     * @return the name of the application {@link Class}
     */
    public String getApplicationClassName()
    {
        return m_applicationClassName;
    }


    /**
     * Obtains the name of the method to start the application.
     *
     * @return the name of the start method
     */
    public String getStartMethodName()
    {
        return m_startMethodName;
    }


    /**
     * Obtains the name of the method to stop the application.
     *
     * @return the name of the stop method
     */
    public String getStopMethodName()
    {
        return m_stopMethodName;
    }


    /**
     * Obtains the command-line arguments for the application.
     *
     * @return the command-line arguments
     */
    public List<String> getArguments()
    {
        return m_arguments;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public OutputStream getOutputStream()
    {
        return m_outputStream;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getInputStream()
    {
        return m_inputStream;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getErrorStream()
    {
        return m_errorStream;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int waitFor() throws InterruptedException
    {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int exitValue()
    {
        return 0;
    }


    /**
     * Starts the application.
     */
    public void start()
    {
        if (m_startInvoker == null)
        {
            m_startInvoker = createRunner(m_applicationClassName, m_startMethodName, null);
            invoke(m_startInvoker);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy()
    {
        Object instance = (m_startInvoker != null) ? m_startInvoker.getInstance() : null;

        invoke(createRunner(m_applicationClassName, m_stopMethodName, instance));
        m_startInvoker = null;
    }


    protected VirtualProcessMethodInvoker getStartRunnable()
    {
        return m_startInvoker;
    }


    protected void setStartRunnable(VirtualProcessMethodInvoker startRunnable)
    {
        this.m_startInvoker = startRunnable;
    }


    protected VirtualProcessMethodInvoker createRunner(String className,
                                                       String methodName,
                                                       Object instance)
    {
        return new VirtualProcessMethodInvoker(m_classLoader, className, methodName, m_arguments, instance);
    }


    /**
     * Invoke the specified method on the specified class
     * within the current pseudo-process isolation.
     * </p>
     * If the method is not static the class must have a default empty
     * constructor to allow an instance of it to be created.
     * </p>
     * This allows methods to be called that will execute as though they
     * were part of the current pseudo-process' isolation. Any return value
     * from the method is returned to the caller. As the method will execute
     * within another ClassLoader it is best to only use Java primitives or
     * classes that are part of the JRE as return types.
     *
     * @param className  - the name of the class to run the specified method on
     * @param methodName - the method to run
     * @return any return value from invoking the method
     */
    @SuppressWarnings({"unchecked"})
    public <T> T invoke(String className,
                        String methodName)
    {
        VirtualProcessMethodInvoker runner = createRunner(className, methodName, null);

        invoke(runner);

        return (T) runner.getResult();
    }


    private void invoke(final VirtualProcessMethodInvoker runner)
    {
        Thread t = new Thread(runner);

        t.start();

        synchronized (runner)
        {
            while (!runner.isFinished())
            {
                try
                {
                    runner.wait(1000);
                }
                catch (InterruptedException e)
                {
                    // ignored
                }
            }
        }

        Throwable error = runner.getError();

        if (error != null)
        {
            throw new RuntimeException(error);
        }
    }
}
