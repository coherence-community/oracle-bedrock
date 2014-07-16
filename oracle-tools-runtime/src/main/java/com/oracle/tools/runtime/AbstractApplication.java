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
import com.oracle.tools.runtime.java.container.Container;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A base implementation of an {@link Application} that internally uses an
 * {@link ApplicationProcess} as a means of representing and controlling the said
 * {@link Application}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Harvey Raja
 *
 * @param <A>  the type of {@link AbstractApplication} to permit fluent method calls
 * @param <P>  the type of {@link ApplicationProcess} used to internally represent
 *             the underlying {@link Application} at runtime
 */
public abstract class AbstractApplication<A extends AbstractApplication<A, P>, P extends ApplicationProcess>
    implements FluentApplication<A>
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
     * The {@link ApplicationProcess} of the executing {@link Application}.
     */
    private final P m_process;

    /**
     * The name of the {@link Application}.
     */
    private String m_name;

    /**
     * The {@link Platform} that this {@link Application} is running on.
     */
    private Platform m_platform;

    /**
     * The {@link ApplicationConsole} that will be used for the {@link Application} I/O.
     */
    private final ApplicationConsole m_console;

    /**
     * Should diagnostic information be enabled for the {@link Application}.
     */
    private boolean m_isDiagnosticsEnabled;

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
     * The {@link Thread} that is used to pipe standard in into the underlying {@link Process}.
     */
    private Thread m_inThread;

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
    private List<LifecycleEventInterceptor<? super A>> m_interceptors;


    /**
     * Construct an {@link AbstractApplication}.
     *
     * @param process               the {@link ApplicationProcess} representing the {@link Application}
     * @param name                  the name of the application
     * @param platform              the {@link Platform} that this {@link Application} is running on
     * @param console               the {@link ApplicationConsole} that will be used for I/O by the {@link Application}
     * @param environmentVariables  the environment variables used when establishing the {@link Application}
     */
    public AbstractApplication(P                  process,
                               String             name,
                               Platform           platform,
                               ApplicationConsole console,
                               Properties         environmentVariables)
    {
        this(process, name, platform, console, environmentVariables, false, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT_UNIT, null);
    }


    /**
     * Construct an {@link AbstractApplication}.
     *
     * @param process               the {@link ApplicationProcess} representing the {@link Application}
     * @param name                  the name of the application
     * @param platform              the {@link Platform} that this {@link Application} is running on
     * @param console               the {@link ApplicationConsole} that will be used for I/O by the {@link Application}
     * @param environmentVariables  the environment variables used when establishing the {@link Application}
     * @param isDiagnosticsEnabled  should diagnostic information be logged/output
     * @param defaultTimeout        the default timeout duration
     * @param defaultTimeoutUnits   the default timeout duration {@link TimeUnit}
     * @param interceptors          the {@link LifecycleEventInterceptor}s
     */
    public AbstractApplication(P                                              process,
                               String                                         name,
                               Platform                                       platform,
                               ApplicationConsole                             console,
                               Properties                                     environmentVariables,
                               boolean                                        isDiagnosticsEnabled,
                               long                                           defaultTimeout,
                               TimeUnit                                       defaultTimeoutUnits,
                               Iterable<LifecycleEventInterceptor<? super A>> interceptors)
    {
        m_process              = process;
        m_name                 = name;
        m_platform             = platform;
        m_console              = console == null ? new SystemApplicationConsole() : console;
        m_environmentVariables = environmentVariables;
        m_isDiagnosticsEnabled = Settings.isDiagnosticsEnabled(isDiagnosticsEnabled);
        m_defaultTimeout       = defaultTimeout;
        m_defaultTimeoutUnits  = defaultTimeoutUnits;

        // make a copy of the interceptors
        m_interceptors = new ArrayList<LifecycleEventInterceptor<? super A>>();

        if (interceptors != null)
        {
            for (LifecycleEventInterceptor<? super A> interceptor : interceptors)
            {
                m_interceptors.add(interceptor);
            }
        }

        // start a thread to redirect standard out to the console
        m_outThread = new Thread(new OutputRedirector(m_name,
                                                      "out",
                                                      m_process.getInputStream(),
                                                      m_console.getOutputWriter(),
                                                      m_process.getId(),
                                                      m_isDiagnosticsEnabled,
                                                      m_console.isDiagnosticsEnabled()));
        m_outThread.setDaemon(true);
        m_outThread.setName(name + " StdOut Thread");
        m_outThread.start();

        // start a thread to redirect standard err to the console
        m_errThread = new Thread(new OutputRedirector(m_name,
                                                      "err",
                                                      m_process.getErrorStream(),
                                                      m_console.getErrorWriter(),
                                                      m_process.getId(),
                                                      m_isDiagnosticsEnabled,
                                                      m_console.isDiagnosticsEnabled()));
        m_errThread.setDaemon(true);
        m_errThread.setName(name + " StdErr Thread");
        m_errThread.start();

        m_inThread = new Thread(new InputRedirector(m_console.getInputReader(), m_process.getOutputStream()));
        m_inThread.setDaemon(true);
        m_inThread.setName(name + " StdIn Thread");
        m_inThread.start();
    }


    @Override
    public Properties getEnvironmentVariables()
    {
        return m_environmentVariables;
    }


    @Override
    public String getName()
    {
        return m_name;
    }

    @Override
    public Platform getPlatform()
    {
        return m_platform;
    }

    @Override
    public void close()
    {
        // close the process
        m_process.close();

        // terminate the thread that is writing to the process standard in
        try
        {
            m_inThread.interrupt();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        // terminate the thread that is reading from the process standard out
        try
        {
            m_outThread.interrupt();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            m_outThread.join();
        }
        catch (InterruptedException e)
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

        try
        {
            m_errThread.join();
        }
        catch (InterruptedException e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            m_console.close();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            // wait for the process to actually terminate (because the above statements may not finish for a while)
            // (if we don't wait the process may be left hanging/orphaned)
            m_process.waitFor();
        }
        catch (InterruptedException e)
        {
            // nothing to do here as we don't care
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

        for (LifecycleEventInterceptor<? super A> interceptor : this.getLifecycleInterceptors())
        {
            interceptor.onEvent(event);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    @Deprecated
    public int destroy()
    {
        close();

        return exitValue();
    }


    @Override
    public long getId()
    {
        return m_process.getId();
    }


    @Override
    public long getDefaultTimeout()
    {
        return m_defaultTimeout;
    }


    @Override
    public TimeUnit getDefaultTimeoutUnits()
    {
        return m_defaultTimeoutUnits;
    }


    @Override
    public Iterable<LifecycleEventInterceptor<? super A>> getLifecycleInterceptors()
    {
        return m_interceptors;
    }


    /**
     * Obtains the underlying {@link ApplicationProcess} that controls the
     * {@link Application}.
     *
     * @return the {@link ApplicationProcess} for the {@link Application}
     */
    protected P getApplicationProcess()
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


    @Override
    public int waitFor() throws InterruptedException
    {
        return m_process.waitFor();
    }


    @Override
    public int exitValue()
    {
        return m_process.exitValue();
    }


    /**
     * An {@link InputRedirector} pipes input to an {@link OutputStream},
     * typically from an {@link ApplicationConsole} to a {@link Process}.
     */
    private static class InputRedirector implements Runnable
    {
        /**
         * The {@link Reader} from which content will be read.
         */
        private Reader m_inputReader;

        /**
         * The {@link OutputStream} to which the content read from the
         * {@link Reader} will be written.
         */
        private OutputStream m_outputStream;


        /**
         * Constructs an {@link OutputRedirector}.
         *
         * @param inputStream      the {@link InputStream} from which to read content
         * @param outputStream     the {@link PrintWriter} to which to write content
         */
        private InputRedirector(Reader       inputStream,
                                OutputStream outputStream)
        {
            m_inputReader  = inputStream;
            m_outputStream = outputStream;
        }


        @Override
        public void run()
        {
            try
            {
                BufferedReader reader = new BufferedReader(m_inputReader);
                PrintWriter    writer = new PrintWriter(m_outputStream);

                while (true)
                {
                    String line = reader.readLine();

                    if (line == null)
                    {
                        break;
                    }

                    writer.println(line);
                    writer.flush();
                }
            }
            catch (Exception exception)
            {
                // SKIP: deliberately empty as we safely assume exceptions
                // are always due to process termination.
            }
        }
    }


    /**
     * An {@link OutputRedirector} pipes output from an {@link InputStream},
     * typically of some {@link Process} to an {@link ApplicationConsole}.
     */
    static class OutputRedirector implements Runnable
    {
        private String m_ApplicationName;

        /**
         * The prefix to write in-front of lines sent to the {@link ApplicationConsole}.
         */
        private String m_prefix;

        /**
         * The {@link ApplicationProcess} identifier.
         */
        private long m_processId;

        /**
         * Should diagnostic information be logged/output.
         */
        private boolean m_isDiagnosticsEnabled;

        /**
         * The {@link InputStream} from which context will be read.
         */
        private InputStream m_inputStream;

        /**
         * The {@link PrintWriter} to which the content read from the
         * {@link InputStream} will be written.
         */
        private PrintWriter m_outputWriter;

        /**
         * Flag indicating whether output to the {@link ApplicationConsole}
         * should be prefixed with details of the application.
         */
        private boolean m_isConsoleDiagnosticsEnabled;

        /**
         * Constructs an {@link OutputRedirector}.
         *
         * @param applicationName              the name of the application
         * @param prefix                       the prefix to output on each console line
         *                                     (typically this is the abbreviation of the stream
         *                                     like "stderr" or "stdout")
         * @param inputStream                  the {@link InputStream} from which to read content
         * @param outputWriter                 the {@link PrintWriter} to which to write content
         * @param processId                    the {@link ApplicationProcess} identifier
         * @param isDiagnosticsEnabled         should diagnostic information be logged/output
         * @param isConsoleDiagnosticsEnabled  if false then the process output is redirected
         *                                     without prefixing with application information
         */
        OutputRedirector(String      applicationName,
                         String      prefix,
                         InputStream inputStream,
                         PrintWriter outputWriter,
                         long        processId,
                         boolean     isDiagnosticsEnabled,
                         boolean     isConsoleDiagnosticsEnabled)
        {
            m_ApplicationName            = applicationName;
            m_prefix                     = prefix;
            m_inputStream                = inputStream;
            m_outputWriter               = outputWriter;
            m_processId                  = processId;
            m_isDiagnosticsEnabled       = isDiagnosticsEnabled;
            m_isConsoleDiagnosticsEnabled = isConsoleDiagnosticsEnabled;
        }


        @Override
        public void run()
        {
            long lineNumber = 1;

            try
            {
                BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new BufferedInputStream(m_inputStream)));

                boolean running = true;

                while (running || reader.ready())
                {
                    try
                    {
                        String line = reader.readLine();

                        if (line == null)
                        {
                            break;
                        }

                        String diagnosticOutput = (m_isDiagnosticsEnabled || m_isConsoleDiagnosticsEnabled)
                                    ? String.format("[%s:%s%s] %4d: %s",
                                                    m_ApplicationName,
                                                    m_prefix,
                                                    m_processId < 0 ? "" : ":" + m_processId,
                                                    lineNumber++,
                                                    line)
                                    : null;

                        String output = m_isConsoleDiagnosticsEnabled ? diagnosticOutput : line;

                        if (m_isDiagnosticsEnabled)
                        {
                            Container.getPlatformScope().getStandardOutput().println(output);
                        }

                        m_outputWriter.println(output);
                        m_outputWriter.flush();
                    }
                    catch (InterruptedIOException e)
                    {
                        running = false;
                    }
                }
            }
            catch (Exception exception)
            {
                // SKIP: deliberately empty as we safely assume exceptions
                // are always due to process termination.
            }

            try
            {
                String diagnosticOutput = (m_isDiagnosticsEnabled || m_isConsoleDiagnosticsEnabled)
                                    ? String.format("[%s:%s%s] %4d: (terminated)",
                                                    m_ApplicationName,
                                                    m_prefix,
                                                    m_processId < 0 ? "" : ":" + m_processId,
                                                    lineNumber)
                                    : null;

                String output = m_isConsoleDiagnosticsEnabled ? diagnosticOutput : "(terminated)";

                if (m_isDiagnosticsEnabled)
                {
                    Container.getPlatformScope().getStandardOutput().println(output);
                }

                m_outputWriter.println(output);
                m_outputWriter.flush();
            }
            catch (Exception e)
            {
                // SKIP: deliberately empty as we safely assume exceptions
                // are always due to process termination.
            }
        }
    }
}
