/*
 * File: DiagnosticsRecording.java
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

package com.oracle.bedrock.diagnostics;

import com.oracle.bedrock.Bedrock;
import com.oracle.bedrock.Option;
import com.oracle.bedrock.table.Cell;
import com.oracle.bedrock.table.Row;
import com.oracle.bedrock.table.Table;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link DiagnosticsRecording} provides a mechanism to record diagnostics
 * information concerning an algorithm at runtime for a single thread.
 * <p>
 * {@link DiagnosticsRecording}s are intended to be used as try-with-resources
 * resources as they are auto-closed and thus cleaned up when exceptions occur.
 * While it is possible to use them outside of a try-with-resources block,
 * care must be taken to close them correctly to prevent memory-leaks.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class DiagnosticsRecording implements AutoCloseable
{
    /**
     * The {@link Logger} for this class.
     */
    private static final Logger LOGGER = Logger.getLogger(DiagnosticsRecording.class.getName());

    /**
     * The {@link Stack} of nested {@link DiagnosticsRecording}s for the
     * current thread.
     */
    private static ThreadLocal<Stack<DiagnosticsRecording>> recordings = new ThreadLocal<Stack<DiagnosticsRecording>>()
    {
        @Override
        protected Stack<DiagnosticsRecording> initialValue()
        {
            return new Stack<>();
        }
    };

    /**
     * The parent {@link DiagnosticsRecording} in which this nested {@link DiagnosticsRecording} was created.
     * <code>null</code> if this is the root {@link DiagnosticsRecording}.
     */
    private DiagnosticsRecording parent;

    /**
     * The name of the {@link DiagnosticsRecording}.
     */
    private String name;

    /**
     * The {@link Table} into which diagnostics information will be recorded.
     */
    private Table table;

    /**
     * The {@link Logger} to use for logging the {@link DiagnosticsRecording}.
     */
    private Logger logger;

    /**
     * The {@link Level} to use for logging the {@link DiagnosticsRecording}.
     * (null when not specified)
     */
    private Level level;


    /**
     * Constructs a {@link DiagnosticsRecording} with the specified name.
     *
     * @param name  the name of the recording
     */
    public DiagnosticsRecording(String name)
    {
        this.parent = null;
        this.name   = name;
        this.table  = new Table();

        this.logger = null;
        this.level  = null;

        // add this recording to the stack on the current thread
        recordings.get().push(this);
    }


    /**
     * Constructs a nested {@link DiagnosticsRecording} that part of a previously
     * defined parent {@link DiagnosticsRecording}.
     *
     * @param parent  the parent {@link DiagnosticsRecording}
     * @param name    the optional name for the {@link DiagnosticsRecording}
     */
    public DiagnosticsRecording(DiagnosticsRecording parent,
                                String               name)
    {
        this.parent = parent;
        this.name   = name;
        this.table  = name == null ? parent.table : new Table();
        this.logger = null;
        this.level  = null;

        // add this recording to the stack on the current thread
        recordings.get().push(this);
    }


    /**
     * Adds a {@link Row} to the {@link DiagnosticsRecording}.
     *
     * @param row  the {@link Row} to add to the {@link DiagnosticsRecording}
     *
     * @return the {@link DiagnosticsRecording} to permit fluent-style method calls
     */
    public DiagnosticsRecording add(Row row)
    {
        if (row != null)
        {
            table.addRow(row);
        }

        return this;
    }


    /**
     * Adds a {@link Row} consisting of a collection of {@link Cell}s to
     * the {@link DiagnosticsRecording}.
     *
     * @param cells  the {@link Cell}s in the {@link Row}
     *
     * @return the {@link DiagnosticsRecording} to permit fluent-style method calls
     */
    public DiagnosticsRecording add(Cell... cells)
    {
        if (cells != null)
        {
            table.addRow(cells);
        }

        return this;
    }


    /**
     * Adds a {@link Row} consisting of a collection of {@link Cell} content
     * to the {@link DiagnosticsRecording}.
     *
     * @param cells  the {@link Cell} content in the {@link Row}
     *
     * @return the {@link DiagnosticsRecording} to permit fluent-style method calls
     */
    public DiagnosticsRecording add(String... cells)
    {
        if (cells != null)
        {
            table.addRow(cells);
        }

        return this;
    }


    /**
     * Adds the {@link Option}s to the {@link DiagnosticsRecording}.
     *
     * @param options  the {@link Option}s
     *
     * @return the {@link DiagnosticsRecording} to permit fluent-style method calls
     */
    public DiagnosticsRecording with(Option... options)
    {
        if (options != null)
        {
            // add all of the options to the table
            this.table.getOptions().addAll(options);
        }

        return this;
    }


    /**
     * Sets the {@link Logger} and {@link Level} for logging the {@link DiagnosticsRecording}.
     * (Ignored if the {@link DiagnosticsRecording} is a nested {@link DiagnosticsRecording}).
     *
     * @param logger  the {@link Logger}
     * @param level   the {@link Level}
     *
     * @return the {@link DiagnosticsRecording} to permit fluent-style method calls
     */
    public DiagnosticsRecording using(Logger logger,
                                      Level  level)
    {
        this.logger = logger;
        this.level  = level;

        return this;
    }


    /**
     * Creates a new {@link DiagnosticsRecording} with the specified name.
     *
     * @param name the name of the {@link DiagnosticsRecording}
     *
     * @return a new {@link DiagnosticsRecording}
     */
    public static DiagnosticsRecording create(String name)
    {
        return new DiagnosticsRecording(name);
    }


    /**
     * Creates a nested {@link DiagnosticsRecording}.  If there's no previously defined
     * {@link DiagnosticsRecording}, an "(undefined)" {@link DiagnosticsRecording} will be created.
     *
     * @return a new {@link DiagnosticsRecording}, that continues a previously defined {@link DiagnosticsRecording}
     */
    public static DiagnosticsRecording continued()
    {
        if (recordings.get().isEmpty())
        {
            return new DiagnosticsRecording("(undefined)");
        }
        else
        {
            return new DiagnosticsRecording(recordings.get().peek(), null);
        }
    }


    /**
     * Creates a nested {@link DiagnosticsRecording} section with a specified name.  If there's no previously defined
     * {@link DiagnosticsRecording}, a new {@link DiagnosticsRecording} with the specified name will be created.
     *
     * @param name the name of the {@link DiagnosticsRecording} section
     *
     * @return a new {@link DiagnosticsRecording}
     */
    public static DiagnosticsRecording section(String name)
    {
        if (recordings.get().isEmpty())
        {
            return new DiagnosticsRecording(name);
        }
        else
        {
            return new DiagnosticsRecording(recordings.get().peek(), name);
        }
    }


    @Override
    public void close()
    {
        // grab the top recording
        DiagnosticsRecording recording = recordings.get().pop();

        // ensure this recording is at the top of the stack
        // (if it isn't we're attempting to close some other recording)
        if (this != recording)
        {
            throw new IllegalStateException("Attempted to close a DiagnosticRecording that was previously closed");
        }
        else
        {
            if (parent == null)
            {
                // log the root recording to the specified logger
                Logger logger = this.logger == null ? LOGGER : this.logger;
                Level  level  = this.level == null ? Level.INFO : this.level;

                if (level != Level.OFF && logger.isLoggable(level))
                {
                    logger.log(level,
                               "Oracle Bedrock " + Bedrock.getVersion() + ": " + name + " ...\n"
                               + "------------------------------------------------------------------------\n"
                               + table.toString() + "\n"
                               + "------------------------------------------------------------------------\n");
                }
            }
            else
            {
                if (name == null)
                {
                    // when no name has been provided, we assume that we've added the diagnostics
                    // directly to the parent
                }
                else
                {
                    // add the table to the parent when a name was provided
                    parent.table.addRow(name, table.toString());
                }
            }
        }
    }
}
