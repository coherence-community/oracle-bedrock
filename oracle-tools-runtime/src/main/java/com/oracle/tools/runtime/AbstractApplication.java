/*
 * File: AbstractApplication.java
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

package com.oracle.tools.runtime;

import com.oracle.tools.runtime.console.SystemApplicationConsole;
import com.oracle.tools.runtime.java.process.VirtualProcess;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * An {@link AbstractApplication} is a base implementation for an
 * {@link Application} that interally uses a {@link Process} as a means of
 * representing and controlling the said {@link Application}, typically at the
 * operating system level.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Harvey Raja
 */
public abstract class AbstractApplication<A> implements Application<A>
{
    /**
     * The default timeout for the {@link Application}.
     */
    public static final long DEFAULT_TIMEOUT = 60;

    /**
     * The default timeout {@link TimeUnit} for the {@link Application}.
     */
    public static final TimeUnit DEFAULT_TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * The {@link Process} representing the runtime {@link Application}.
     */
    private final Process m_process;

    /**
     * The name of the {@link Application}.
     */
    private String m_name;

    /**
     * The {@link ApplicationConsole} that will be used for the {@link Application} I/O.
     */
    private final ApplicationConsole m_console;

    /**
     * The environment variables used when establishing the {@link Application}.
     */
    private Properties m_environmentVariables;

    /**
     * The {@link Thread} that is used to capture standard output from the underlying {@link Process}.
     */
    private Thread m_outThread;

    /**
     * The {@link Thread} that is used to capture standard error from the underlying {@link Process}.
     */
    private Thread m_errThread;

    /**
     * The os process id this abstract application represents
     */
    private long m_pid = -1;

    /**
     * The default timeout duration.
     */
    private long m_defaultTimeout;

    /**
     * The default timeout duration {@link TimeUnit}.
     */
    private TimeUnit m_defaultTimeoutUnits;

    /**
     * The {@link LifecycleEventInterceptor}s that must be executed for
     * {@link LifecycleEvent}s on the {@link Application}.
     */
    private List<LifecycleEventInterceptor<A>> m_interceptors;


    /**
     * Construct an {@link AbstractApplication}.
     *
     * @param process               the {@link Process} representing the {@link Application}
     * @param name                  the name of the application
     * @param console               the {@link ApplicationConsole} that will be used for I/O by the {@link Application}
     * @param environmentVariables  the environment variables used when establishing the {@link Application}
     */
    public AbstractApplication(Process            process,
                               String             name,
                               ApplicationConsole console,
                               Properties         environmentVariables)
    {
        this(process, name, console, environmentVariables, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT, null);
    }


    /**
     * Construct an {@link AbstractApplication}.
     *
     * @param process               the {@link Process} representing the {@link Application}
     * @param name                  the name of the application
     * @param console               the {@link ApplicationConsole} that will be used for I/O by the {@link Application}
     * @param environmentVariables  the environment variables used when establishing the {@link Application}
     * @param defaultTimeout        the default timeout duration
     * @param defaultTimeoutUnits   the default timeout duration {@link TimeUnit}
     * @param interceptors          the {@link LifecycleEventInterceptor}s
     */
    public AbstractApplication(Process                                process,
                               String                                 name,
                               ApplicationConsole                     console,
                               Properties                             environmentVariables,
                               long                                   defaultTimeout,
                               TimeUnit                               defaultTimeoutUnits,
                               Iterable<LifecycleEventInterceptor<A>> interceptors)
    {
        m_process              = process;
        m_name                 = name;
        m_console              = console == null ? new SystemApplicationConsole() : console;
        m_environmentVariables = environmentVariables;
        m_pid                  = determinePID(process);
        m_defaultTimeout       = defaultTimeout;
        m_defaultTimeoutUnits  = defaultTimeoutUnits;

        // make a copy of the interceptors
        m_interceptors = new ArrayList<LifecycleEventInterceptor<A>>();

        if (interceptors != null)
        {
            for (LifecycleEventInterceptor<A> interceptor : interceptors)
            {
                m_interceptors.add(interceptor);
            }
        }

        // start a thread to redirect standard out to the console
        m_outThread = new Thread(new OutputRedirector("out", m_process.getInputStream()));
        m_outThread.setDaemon(true);
        m_outThread.setName(name + " StdOut Thread");
        m_outThread.start();

        // start a thread to redirect standard err to the console
        m_errThread = new Thread(new OutputRedirector("err", m_process.getErrorStream()));
        m_errThread.setDaemon(true);
        m_outThread.setName(name + " StdErr Thread");
        m_errThread.start();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Properties getEnvironmentVariables()
    {
        return m_environmentVariables;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return m_name;
    }


    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public int destroy()
    {
        // terminate the thread that is reading from the process standard out
        try
        {
            m_outThread.interrupt();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        // terminate the thread that is reading from the process standard err
        try
        {
            m_errThread.interrupt();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        // close the io streams being used by the process
        try
        {
            m_process.getInputStream().close();
        }
        catch (IOException e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            m_process.getOutputStream().close();
        }
        catch (IOException e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            m_process.getErrorStream().close();
        }
        catch (IOException e)
        {
            // nothing to do here as we don't care
        }

        // terminate the process
        try
        {
            m_process.destroy();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        // wait for it to actually terminate (because the above line may not finish for a while)
        // (if we don't wait the process may be left hanging/orphaned)
        int result;

        try
        {
            result = m_process.waitFor();
        }
        catch (InterruptedException e)
        {
            // nothing to do here as we don't care
            result = 0;
        }

        // raise the starting / realized event for the application
        @SuppressWarnings("rawtypes") LifecycleEvent event = new LifecycleEvent<Application>()
        {
            @Override
            public Enum<?> getType()
            {
                return Application.EventKind.DESTROYED;
            }

            @Override
            public Application getObject()
            {
                return AbstractApplication.this;
            }
        };

        for (LifecycleEventInterceptor<A> interceptor : this.getLifecycleInterceptors())
        {
            interceptor.onEvent(event);
        }

        return result;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long getPid()
    {
        return m_pid;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public long getDefaultTimeout()
    {
        return m_defaultTimeout;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public TimeUnit getDefaultTimeoutUnits()
    {
        return m_defaultTimeoutUnits;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterable<LifecycleEventInterceptor<A>> getLifecycleInterceptors()
    {
        return m_interceptors;
    }


    /**
     * Obtains the underlying {@link Process} that controls the {@link Application}.
     *
     * @return the {@link Process} for the {@link Application}
     */
    protected Process getProcess()
    {
        return m_process;
    }


    /**
     * Obtains the default timeout duration in milliseconds.
     *
     * @return  the default timeout in milliseconds
     */
    protected long getDefaultTimeoutMS()
    {
        return m_defaultTimeoutUnits.toMillis(m_defaultTimeout);
    }


    /**
     * {@inheritDoc}
     */
    public int waitFor() throws InterruptedException
    {
        return m_process.waitFor();
    }


    /**
     * {@inheritDoc}
     */
    public int exitValue()
    {
        return m_process.exitValue();
    }


    /**
     * Determine the {@link Process} id for the {@link Application}
     *
     * @param p the {@link Process} to attain the process id from
     * @return The {@link Process} id or -1 if it can't be determined.
     */
    private long determinePID(final Process p)
    {
        long pid = -1;

        try
        {
            // Internal process
            if (p instanceof VirtualProcess)
            {
                pid = -1;
            }

            // Unix variants incl. OSX
            else if (p.getClass().getSimpleName().equals("UNIXProcess"))
            {
                final Class<?> clazz = p.getClass();
                final Field    pidF  = clazz.getDeclaredField("pid");

                pidF.setAccessible(true);

                Object oPid = pidF.get(p);

                if (oPid instanceof Number)
                {
                    pid = ((Number) oPid).longValue();
                }
                else if (oPid instanceof String)
                {
                    pid = Long.parseLong((String) oPid);
                }
            }

            // Windows processes, i.e. Win32Process or ProcessImpl
            else
            {
                RuntimeMXBean rtb      = ManagementFactory.getRuntimeMXBean();
                final String  sProcess = rtb.getName();
                final int     iPID     = sProcess.indexOf('@');

                if (iPID > 0)
                {
                    String sPID = sProcess.substring(0, iPID);

                    pid = Long.parseLong(sPID);
                }
            }
        }
        catch (SecurityException e)
        {
        }
        catch (NoSuchFieldException e)
        {
        }
        catch (IllegalArgumentException e)
        {
        }
        catch (IllegalAccessException e)
        {
        }

        return pid;
    }


    /**
     * An {@link OutputRedirector} pipes output from an {@link InputStream},
     * typically of some {@link Process} to an {@link ApplicationConsole}.
     */
    private class OutputRedirector implements Runnable
    {
        /**
         * The prefix to write in-front of lines sent to the {@link ApplicationConsole}.
         */
        private String m_prefix;

        /**
         * The source {@link InputStream} from which the {@link OutputRedirector}
         * will read.
         */
        private InputStream m_stream;


        /**
         * Constructs an {@link OutputRedirector}.
         *
         * @param prefix   the prefix to output on each console line
         * @param stream   the {@link InputStream} from which to read the content
         * @param console  the {@link ApplicationConsole} to output the content
         */
        private OutputRedirector(String      prefix,
                                 InputStream stream)
        {
            m_prefix = prefix;
            m_stream = stream;
        }


        /**
         * {@inheritDoc}
         */
        public void run()
        {
            long lineNumber = 1;

            try
            {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new BufferedInputStream(m_stream)));

                while (true)
                {
                    String line = reader.readLine();

                    if (line == null)
                    {
                        break;
                    }

                    AbstractApplication.this.m_console.printf("[%s:%s%s] %4d: %s\n",
                                                              AbstractApplication.this.m_name,
                                                              m_prefix,
                                                              m_pid < 0 ? "" : ":" + m_pid,
                                                              lineNumber++,
                                                              line);
                }
            }
            catch (Exception exception)
            {
                // deliberately empty as we safely assume exceptions
                // are always due to process termination.
            }

            AbstractApplication.this.m_console.printf("[%s:%s%s] %4d: (terminated)\n",
                                                      AbstractApplication.this.m_name,
                                                      m_prefix,
                                                      m_pid < 0 ? "" : ":" + m_pid,
                                                      lineNumber);
        }
    }
}
