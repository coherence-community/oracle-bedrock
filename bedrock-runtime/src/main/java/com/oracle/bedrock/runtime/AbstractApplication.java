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
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.extensible.AbstractExtensible;
import com.oracle.bedrock.options.Diagnostics;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.console.InputRedirector;
import com.oracle.bedrock.runtime.console.OutputRedirector;
import com.oracle.bedrock.runtime.options.ApplicationClosingBehavior;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.ConsoleErrorRedirector;
import com.oracle.bedrock.runtime.options.ConsoleInputRedirector;
import com.oracle.bedrock.runtime.options.ConsoleOutputRedirector;
import com.oracle.bedrock.runtime.options.DisplayName;

import java.util.concurrent.atomic.AtomicBoolean;

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
     * The resolved display name for the  {@link Application},
     * based on the {@link OptionsByType} used to launch the {@link Application}.
     */
    protected final String displayName;

    /**
     * The underlying {@link ApplicationProcess} used to internally represent and
     * manage the {@link Application}.
     */
    protected final P process;

    /**
     * The {@link OptionsByType} used to launch the {@link Application}.
     */
    protected final OptionsByType optionsByType;

    /**
     * The {@link ApplicationConsole} for interacting with the {@link Application}.
     */
    protected final ApplicationConsole console;

    /**
     * The {@link OutputRedirector} that is used to capture standard output from the underlying {@link Process}.
     */
    private final OutputRedirector stdoutThread;

    /**
     * The {@link OutputRedirector} that is used to capture standard error from the underlying {@link Process}.
     */
    private final OutputRedirector stderrThread;

    /**
     * The {@link InputRedirector} that is used to pipe standard in into the underlying {@link Process}.
     */
    private final InputRedirector stdinThread;

    /**
     * The default {@link Timeout} to use for the {@link Application}.
     */
    private Timeout defaultTimeout;

    /**
     * Is the {@link Application} considered closed and no longer usable?
     */
    private AtomicBoolean closed;


    /**
     * Construct an {@link AbstractApplication}.
     *
     * @param platform       the {@link Platform} on which the {@link Application} was launched
     * @param process        the underlying {@link ApplicationProcess} representing the {@link Application}
     * @param optionsByType  the {@link OptionsByType} used to launch the {@link Application}
     */
    public AbstractApplication(Platform      platform,
                               P             process,
                               OptionsByType optionsByType)
    {
        this.platform      = platform;
        this.process       = process;
        this.optionsByType = optionsByType;

        this.closed        = new AtomicBoolean(false);

        // establish the default Timeout for the application
        this.defaultTimeout = optionsByType.get(Timeout.class);

        // determine if diagnostics is enabled
        boolean diagnosticsEnabled = optionsByType.get(Diagnostics.class).isEnabled();

        // resolve the application name, including the discriminator (if one is defined)
        this.displayName = optionsByType.get(DisplayName.class).resolve(optionsByType);

        // establish the application console
        console = optionsByType.getOrSetDefault(ApplicationConsoleBuilder.class, Console.system()).build(displayName);

        // establish the standard input, output and error redirection threads for the application console

        // start a thread to redirect standard out to the console
        ConsoleOutputRedirector outputRedirector = optionsByType.getOrDefault(ConsoleOutputRedirector.class,
                                                                              ConsoleOutputRedirector.defaultRedirector());

        stdoutThread = outputRedirector.getRedirector();
        stdoutThread.setName(displayName + " StdOut Thread");
        stdoutThread.start(displayName,
                           "out",
                           process.getInputStream(),
                           console,
                           process.getId(),
                           diagnosticsEnabled);


        // start a thread to redirect standard err to the console
        ConsoleErrorRedirector errorRedirector = optionsByType.getOrDefault(ConsoleErrorRedirector.class,
                                                                            ConsoleErrorRedirector.defaultRedirector());

        stderrThread = errorRedirector.getRedirector();
        stderrThread.setName(displayName + " StdErr Thread");
        stderrThread.start(displayName,
                           "err",
                           process.getErrorStream(),
                           console,
                           process.getId(),
                           diagnosticsEnabled);

        // start a thread to redirect standard in from the console
        ConsoleInputRedirector inRedirector = optionsByType.getOrDefault(ConsoleInputRedirector.class,
                                                                         ConsoleInputRedirector.defaultRedirector());

        stdinThread = inRedirector.getRedirector();
        stdinThread.setName(displayName + " StdIn Thread");
        stdinThread.start(process.getOutputStream(), console);
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
    public OptionsByType getOptions()
    {
        return optionsByType;
    }


    @Override
    public boolean isOperational()
    {
        return !closed.get();
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
        if (closed.compareAndSet(false, true))
        {
            // determine the custom closing behavior for the application
            OptionsByType closingOptions = OptionsByType.of(options);

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
        OptionsByType waitForOptions = OptionsByType.of(getOptions().asArray()).addAll(options);

        return process.waitFor(waitForOptions.asArray());
    }


    @Override
    public int exitValue()
    {
        return process.exitValue();
    }
}
