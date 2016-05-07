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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.extensible.AbstractExtensible;
import com.oracle.bedrock.options.Diagnostics;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.console.SystemApplicationConsole;
import com.oracle.bedrock.runtime.java.container.Container;
import com.oracle.bedrock.runtime.options.ApplicationClosingBehavior;
import com.oracle.bedrock.runtime.options.DisplayName;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;

/**
 * A base implementation of an {@link Application}.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Harvey Raja
 *
 * @param <P>  the type of {@link ApplicationProcess} used to internally represent
 *             the underlying {@link Application} at runtime
 */
@Internal
public abstract class AbstractApplication<P extends ApplicationProcess> extends AbstractExtensible
    implements Application
{
    /**
     * The {@link Platform} on which the {@link Application} was launched.
     */
    protected final Platform platform;

    /**
     * The resolved display name for the  {@link Application}, based on the {@link Options} used
     * to launch the {@link Application}.
     */
    protected final String displayName;

    /**
     * The underlying {@link ApplicationProcess} used to internally represent and
     * manage the {@link Application}.
     */
    protected final P process;

    /**
     * The {@link Options} used to launch the {@link Application}.
     */
    protected final Options options;

    /**
     * The {@link ApplicationConsole} for interacting with the {@link Application}.
     */
    protected final ApplicationConsole console;

    /**
     * The {@link Thread} that is used to capture standard output from the underlying {@link Process}.
     */
    private final Thread stdoutThread;

    /**
     * The {@link Thread} that is used to capture standard error from the underlying {@link Process}.
     */
    private final Thread stderrThread;

    /**
     * The {@link Thread} that is used to pipe standard in into the underlying {@link Process}.
     */
    private final Thread stdinThread;

    /**
     * The default {@link Timeout} to use for the {@link Application}.
     */
    private Timeout defaultTimeout;


    /**
     * Construct an {@link AbstractApplication}.
     *
     * @param platform  the {@link Platform} on which the {@link Application} was launched
     * @param process   the underlying {@link ApplicationProcess} representing the {@link Application}
     * @param options   the {@link Options} used to launch the {@link Application}
     */
    public AbstractApplication(Platform platform,
                               P        process,
                               Options  options)
    {
        this.platform = platform;
        this.process  = process;
        this.options  = options;

        // establish the default Timeout for the application
        this.defaultTimeout = options.get(Timeout.class);

        // determine if diagnostics is enabled
        boolean diagnosticsEnabled = options.get(Diagnostics.class).isEnabled();

        // resolve the application name, including the discriminator (if one is defined)
        this.displayName = options.get(DisplayName.class).resolve(options);

        // establish the application console
        console = options.getOrDefault(ApplicationConsoleBuilder.class,
                                       SystemApplicationConsole.builder()).build(displayName);

        // establish the standard input, output and error redirection threads for the application console

        // start a thread to redirect standard out to the console
        stdoutThread = new Thread(new OutputRedirector(displayName,
                                                       "out",
                                                       process.getInputStream(),
                                                       console.getOutputWriter(),
                                                       process.getId(),
                                                       diagnosticsEnabled
                                                       &&!(console instanceof SystemApplicationConsole),
                                                       console.isDiagnosticsEnabled()));
        stdoutThread.setDaemon(true);
        stdoutThread.setName(displayName + " StdOut Thread");
        stdoutThread.start();

        // start a thread to redirect standard err to the console
        stderrThread = new Thread(new OutputRedirector(displayName,
                                                       "err",
                                                       process.getErrorStream(),
                                                       console.getErrorWriter(),
                                                       process.getId(),
                                                       diagnosticsEnabled
                                                       &&!(console instanceof SystemApplicationConsole),
                                                       console.isDiagnosticsEnabled()));
        stderrThread.setDaemon(true);
        stderrThread.setName(displayName + " StdErr Thread");
        stderrThread.start();

        stdinThread = new Thread(new InputRedirector(console.getInputReader(), process.getOutputStream()));
        stdinThread.setDaemon(true);
        stdinThread.setName(displayName + " StdIn Thread");
        stdinThread.start();
    }


    @Override
    public Timeout getDefaultTimeout()
    {
        return defaultTimeout;
    }


    @Override
    public String getName()
    {
        return displayName;
    }


    @Override
    public Platform getPlatform()
    {
        return platform;
    }


    @Override
    public Options getOptions()
    {
        return options;
    }


    @Override
    public void close()
    {
        // delegate the close() to close(Option...)
        close(new Option[]
        {
        });
    }


    @Override
    public void close(Option... options)
    {
        // determine the custom closing behavior for the application
        Options closingOptions = new Options(options);

        // ------ notify any ApplicationListener-based Features (about closing) ------

        for (ApplicationListener listener : getInstancesOf(ApplicationListener.class))
        {
            listener.onClosing(this, closingOptions);
        }

        // ----- notify the Profiles that the application is closing -----

        for (Profile profile : getOptions().getInstancesOf(Profile.class))
        {
            profile.onClosing(platform, this, getOptions());
        }

        // ------ notify ApplicationListeners-based Options (about closing) ------

        for (ApplicationListener listener : getOptions().getInstancesOf(ApplicationListener.class))
        {
            listener.onClosing(this, closingOptions);
        }

        // ------ perform any necessary ApplicationClosingBehaviors ------

        // determine the default closing behavior (defined for the application options)
        ApplicationClosingBehavior defaultClosingBehavior = getOptions().get(ApplicationClosingBehavior.class);

        // determine the required closing behavior
        ApplicationClosingBehavior closingBehavior = closingOptions.getOrDefault(ApplicationClosingBehavior.class,
                                                                                 defaultClosingBehavior);

        if (closingBehavior != null)
        {
            try
            {
                closingBehavior.onBeforeClosing(this, options);
            }
            catch (Exception e)
            {
                // we ignore any issues that occurred due to closing behaviors

                // TODO: if diagnostics are enabled we should output the exception
            }
        }

        // ------ close the process ------

        // close the process
        process.close();

        // ------ clean up ------

        // terminate the thread that is writing to the process standard in
        try
        {
            stdinThread.interrupt();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        // terminate the thread that is reading from the process standard out
        try
        {
            stdoutThread.interrupt();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            stdoutThread.join();
        }
        catch (InterruptedException e)
        {
            // nothing to do here as we don't care
        }

        // terminate the thread that is reading from the process standard err
        try
        {
            stderrThread.interrupt();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            stderrThread.join();
        }
        catch (InterruptedException e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            console.close();
        }
        catch (Exception e)
        {
            // nothing to do here as we don't care
        }

        try
        {
            // wait for the application to terminate
            waitFor(options);
        }
        catch (RuntimeException e)
        {
            // nothing to do here as we don't care
        }

        // ------ notify ApplicationListeners-based Options (about being closed) ------

        for (ApplicationListener listener : getOptions().getInstancesOf(ApplicationListener.class))
        {
            listener.onClosed(this, closingOptions);
        }

        // ------ notify ApplicationListener-based Features (about being closed) ------

        for (ApplicationListener listener : getInstancesOf(ApplicationListener.class))
        {
            listener.onClosed(this, closingOptions);
        }

        // ----- remove all of the features -----

        removeAllFeatures();
    }


    @Override
    public long getId()
    {
        return process.getId();
    }


    @Override
    public int waitFor(Option... options)
    {
        // include the application specific options for waiting
        Options waitForOptions = new Options(getOptions().asArray()).addAll(options);

        return process.waitFor(waitForOptions.asArray());
    }


    @Override
    public int exitValue()
    {
        return process.exitValue();
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
        private Reader reader;

        /**
         * The {@link OutputStream} to which the content read from the
         * {@link Reader} will be written.
         */
        private OutputStream outputStream;


        /**
         * Constructs an {@link OutputRedirector}.
         *
         * @param reader        the {@link Reader} from which to read content
         * @param outputStream  the {@link OutputStream} to which to write content
         */
        private InputRedirector(Reader       reader,
                                OutputStream outputStream)
        {
            this.reader       = reader;
            this.outputStream = outputStream;
        }


        @Override
        public void run()
        {
            try
            {
                BufferedReader bufferedReader = new BufferedReader(reader);
                PrintWriter    printWriter    = new PrintWriter(outputStream);

                while (true)
                {
                    String line = bufferedReader.readLine();

                    if (line == null)
                    {
                        break;
                    }

                    printWriter.println(line);
                    printWriter.flush();
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
     * typically of some {@link ApplicationProcess} to an {@link ApplicationConsole}.
     */
    static class OutputRedirector implements Runnable
    {
        private String applicationName;

        /**
         * The prefix to write in-front of lines sent to the {@link ApplicationConsole}.
         */
        private String prefix;

        /**
         * The {@link ApplicationProcess} identifier.
         */
        private long processId;

        /**
         * Should diagnostic information be logged/output.
         */
        private boolean diagnosticsEnabled;

        /**
         * The {@link InputStream} from which context will be read.
         */
        private InputStream inputStream;

        /**
         * The {@link PrintWriter} to which the content read from the
         * {@link InputStream} will be written.
         */
        private PrintWriter printWriter;

        /**
         * Flag indicating whether output to the {@link ApplicationConsole}
         * should be prefixed with details of the application.
         */
        private boolean consoleDiagnosticsEnabled;


        /**
         * Constructs an {@link OutputRedirector}.
         *
         * @param applicationName            the name of the application
         * @param prefix                     the prefix to output on each console line
         *                                   (typically this is the abbreviation of the stream
         *                                   like "stderr" or "stdout")
         * @param inputStream                the {@link InputStream} from which to read content
         * @param printWriter                the {@link PrintWriter} to which to write content
         * @param processId                  the {@link ApplicationProcess} identifier
         * @param diagnosticsEnabled         should diagnostic information be logged/output
         * @param consoleDiagnosticsEnabled  if false then the process output is redirected
         *                                   without prefixing with application information
         */
        OutputRedirector(String      applicationName,
                         String      prefix,
                         InputStream inputStream,
                         PrintWriter printWriter,
                         long        processId,
                         boolean     diagnosticsEnabled,
                         boolean     consoleDiagnosticsEnabled)
        {
            this.applicationName           = applicationName;
            this.prefix                    = prefix;
            this.inputStream               = inputStream;
            this.printWriter               = printWriter;
            this.processId                 = processId;
            this.diagnosticsEnabled        = diagnosticsEnabled;
            this.consoleDiagnosticsEnabled = consoleDiagnosticsEnabled;
        }


        @Override
        public void run()
        {
            long lineNumber = 1;

            try
            {
                BufferedReader reader  =
                    new BufferedReader(new InputStreamReader(new BufferedInputStream(inputStream)));

                boolean        running = true;

                while (running || reader.ready())
                {
                    try
                    {
                        String line = reader.readLine();

                        if (line == null)
                        {
                            break;
                        }

                        String diagnosticOutput = (diagnosticsEnabled
                                                   || consoleDiagnosticsEnabled) ? String.format("[%s:%s%s] %4d: %s",
                                                                                                 applicationName,
                                                                                                 prefix,
                                                                                                 processId < 0
                                                                                                 ? "" : ":" + processId,
                                                                                                 lineNumber++,
                                                                                                 line) : null;

                        String output = consoleDiagnosticsEnabled ? diagnosticOutput : line;

                        if (diagnosticsEnabled)
                        {
                            Container.getPlatformScope().getStandardOutput().println(output);
                        }

                        printWriter.println(output);
                        printWriter.flush();
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
                String diagnosticOutput = (diagnosticsEnabled
                                           || consoleDiagnosticsEnabled) ? String.format("[%s:%s%s] %4d: (terminated)",
                                                                                         applicationName,
                                                                                         prefix,
                                                                                         processId < 0
                                                                                         ? "" : ":" + processId,
                                                                                         lineNumber) : null;

                String output = consoleDiagnosticsEnabled ? diagnosticOutput : "(terminated)";

                if (diagnosticsEnabled)
                {
                    Container.getPlatformScope().getStandardOutput().println(output);
                }

                printWriter.println(output);
                printWriter.flush();
            }
            catch (Exception e)
            {
                // SKIP: deliberately empty as we safely assume exceptions
                // are always due to process termination.
            }
        }
    }
}
