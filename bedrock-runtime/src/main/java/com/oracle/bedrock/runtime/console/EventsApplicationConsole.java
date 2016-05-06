/*
 * File: EventsApplicationConsole.java
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

package com.oracle.bedrock.runtime.console;

import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

/**
 * An implementation of an {@link ApplicationConsole} that
 * treats line of output to StdOut and StdErr as events and fires them to registered {@link Listener}s.
 * This console also allows StdIn to be piped to the application.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class EventsApplicationConsole extends AbstractPipedApplicationConsole
{
    /**
     * The {@link Listener}s listening to StdOut
     */
    private final ConcurrentLinkedQueue<Pair<Predicate<String>, Listener>> stdoutListeners;

    /**
     * The {@link Listener}s listening to StdErr
     */
    private final ConcurrentLinkedQueue<Pair<Predicate<String>, Listener>> stderrListeners;

    /**
     * The {@link Thread} capturing StdOut lines
     */
    private final Thread stdoutThread;

    /**
     * The {@link Thread} capturing StdErr lines
     */
    private final Thread stderrThread;


    /**
     * Constructs {@link EventsApplicationConsole}.
     * <p>
     * This constructor will set the maximum number of lines to capture to {@link Integer#MAX_VALUE}.
     */
    public EventsApplicationConsole()
    {
        super(DEFAULT_PIPE_SIZE, false);

        this.stdoutListeners = new ConcurrentLinkedQueue<>();
        this.stderrListeners = new ConcurrentLinkedQueue<>();

        this.stdoutThread    = new Thread(new OutputCaptor(stdoutReader, stdoutListeners));
        this.stderrThread    = new Thread(new OutputCaptor(stderrReader, stderrListeners));

        this.stdoutThread.start();
        this.stderrThread.start();
    }


    @Override
    public void close()
    {
        super.close();

        try
        {
            stdoutThread.join();
            stderrThread.join();
        }
        catch (InterruptedException e)
        {
            // Ignored
        }
    }


    /**
     * Add a listener to receive stdout console lines as events.
     *
     * @param listener  the {@link Listener}
     *
     * @return  this {@link EventsApplicationConsole}
     */
    public EventsApplicationConsole withStdOutListener(Listener listener)
    {
        return withStdOutListener((line) -> true, listener);
    }


    /**
     * Add a listener to receive stdout console lines as events
     * that match the specified {@link Predicate}.
     *
     * @param predicate  the {@link Predicate} to use to match console output lines
     * @param listener   the {@link Listener}
     *
     * @return  this {@link EventsApplicationConsole}
     */
    public EventsApplicationConsole withStdOutListener(Predicate<String> predicate,
                                                       Listener          listener)
    {
        if (predicate == null)
        {
            stdoutListeners.add(new Pair<>((line) -> true, listener));
        }
        else
        {
            stdoutListeners.add(new Pair<>(predicate, listener));
        }

        return this;
    }


    /**
     * Add a listener to receive stderr console lines as events.
     *
     * @param listener  the {@link Listener}
     *
     * @return  this {@link EventsApplicationConsole}
     */
    public EventsApplicationConsole withStdErrListener(Listener listener)
    {
        return withStdErrListener((line) -> true, listener);
    }


    /**
     * Add a listener to receive stdout console lines as events
     * that match the specified {@link Predicate}.
     *
     * @param predicate  the {@link Predicate} to use to match console output lines
     * @param listener   the {@link Listener}
     *
     * @return  this {@link EventsApplicationConsole}
     */
    public EventsApplicationConsole withStdErrListener(Predicate<String> predicate,
                                                       Listener          listener)
    {
        if (predicate == null)
        {
            stderrListeners.add(new Pair<>((line) -> true, listener));
        }
        else
        {
            stderrListeners.add(new Pair<>(predicate, listener));
        }

        return this;
    }


    /**
     * Obtains a {@link PrintWriter} that can be used to write to the stdin
     * of an {@link ApplicationConsole}.
     *
     * @return a {@link PrintWriter}
     */
    public PrintWriter getInputWriter()
    {
        return stdinWriter;
    }


    /**
     * An interface implemented by classes that wish to
     * respond to console output as though they were events.
     */
    @FunctionalInterface
    public interface Listener
    {
        /**
         * Called when a line of output has been written to the console.
         *
         * @param line  the line of output
         */
        void onOutput(String line);
    }


    /**
     * A special {@link Listener} implementation that is also a
     * {@link CountDownLatch} that counts down each time the
     * {@link #onOutput(String)} method is called.
     * <p>
     * This listener would typically be added with a {@link Predicate}
     * to await for a specific matching output line.
     */
    public static class CountDownListener extends CountDownLatch implements Listener
    {
        /**
         * Create a {@link CountDownListener} that will wait for the
         * specified number of lines of output.
         *
         * @param count  the nuber of lines of output to wait for
         */
        public CountDownListener(int count)
        {
            super(count);
        }


        @Override
        public void onOutput(String line)
        {
            countDown();
        }
    }


    /**
     * The {@link Runnable} used to capture lines of output.
     */
    class OutputCaptor implements Runnable
    {
        /**
         * The {@link BufferedReader} to capture output from.
         */
        BufferedReader reader;

        /**
         * The {@link Listener}a to send output lines to.
         */
        ConcurrentLinkedQueue<Pair<Predicate<String>, Listener>> listeners;


        /**
         * Create an {@link OutputCaptor}.
         *
         * @param reader      The {@link BufferedReader} to capture output from
         * @param listeners   The {@link Listener}s to send output lines to
         */
        OutputCaptor(BufferedReader                                           reader,
                     ConcurrentLinkedQueue<Pair<Predicate<String>, Listener>> listeners)
        {
            this.reader    = reader;
            this.listeners = listeners;
        }


        /**
         * The {@link Runnable#run()} method for this {@link OutputCaptor}
         * that will capture output.
         */
        @Override
        public void run()
        {
            try
            {
                String line = reader.readLine();

                while (line != null)
                {
                    for (Pair<Predicate<String>, Listener> pair : listeners)
                    {
                        try
                        {
                            if (pair.getX().test(line))
                            {
                                pair.getY().onOutput(line);
                            }
                        }
                        catch (Throwable t)
                        {
                            t.printStackTrace();
                        }
                    }

                    line = reader.readLine();
                }
            }
            catch (IOException e)
            {
                // Skip: Likely caused by application termination
            }
        }
    }
}
