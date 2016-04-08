/*
 * File: ThreadDump.java
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

package com.oracle.tools.runtime.concurrent.runnable;

import com.oracle.tools.runtime.concurrent.RemoteRunnable;

import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * A {@link RemoteRunnable} to perform a Thread dump to either the
 * process's stdout or stderr.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ThreadDump implements RemoteRunnable
{
    /**
     * A flag indicating whether to write the thread dump to
     * stdout ({@linkplain true}, or to stderr {@linkplain false}
     */
    private boolean useStdOut;


    /**
     * Create a {@link ThreadDump} that will write a thread dump to
     * the specified location.
     *
     * @param useStdOut  write the thread dump to stdout
     *                   ({@linkplain true}, or to stderr
     *                   ({@linkplain false})
     */
    private ThreadDump(boolean useStdOut)
    {
        this.useStdOut = useStdOut;
    }

    
    @Override
    public void run()
    {
        if (useStdOut)
        {
            generateThreadDump(System.out);
        }
        else
        {
            generateThreadDump(System.err);
        }
    }


    /**
     * Generate a thread dump and write it to the specified {@link PrintStream}.
     *
     * @param out  the {@link PrintStream} to write the thread dump to
     */
    public void generateThreadDump(PrintStream out)
    {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 100);

        for (ThreadInfo threadInfo : threadInfos)
        {
            out.print('"');
            out.print(threadInfo.getThreadName());
            out.print("\" ");

            Thread.State state = threadInfo.getThreadState();

            out.print("\n   java.lang.Thread.State: ");
            out.print(state);

            StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();

            for (final StackTraceElement stackTraceElement : stackTraceElements)
            {
                out.print("\n        at ");
                out.print(stackTraceElement);
            }

            out.print("\n");
        }
    }


    /**
     * Obtain a {@link ThreadDump} instance that will write a thread dump to
     * the process's {@link System#out}.
     *
     * @return  a {@link ThreadDump} instance that will write a thread dump
     *          to the process's {@link System#out}
     */
    public static ThreadDump toStdOut()
    {
        return new ThreadDump(true);
    }


    /**
     * Obtain a {@link ThreadDump} instance that will write a thread dump to
     * the process's {@link System#err}.
     *
     * @return  a {@link ThreadDump} instance that will write a thread dump
     *          to the process's {@link System#err}
     */
    public static ThreadDump toStdErr()
    {
        return new ThreadDump(false);
    }
}
