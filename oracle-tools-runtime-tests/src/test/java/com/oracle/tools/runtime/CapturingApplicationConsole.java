/*
 * File: CapturingApplicationConsole.java
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

package com.oracle.tools.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of an {@link ApplicationConsole} that captures output to a list
 * so that it can be asserted in tests.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class CapturingApplicationConsole implements ApplicationConsole
{
    private Map<String, List<String>> lines   = new HashMap<String, List<String>>();
    private final Object              monitor = new Object();


    /**
     * A convenience method to write a formatted string to a console using
     * the specified format string and arguments.
     *
     * @param format A format string as described in {@link java.util.Formatter} string syntax.
     * @param args   Arguments referenced by the format specifiers in the format string. If there are more
     *               arguments than format specifiers, the extra arguments are ignored.
     *               The number of arguments is variable and may be zero.
     */
    @Override
    public synchronized void printf(String format,
                                    Object... args)
    {
        String key = String.valueOf(args[1]);

        if (!lines.containsKey(key))
        {
            lines.put(key, new ArrayList<String>());
        }

        lines.get(key).add(String.format(format, args));
        notifyAll();
    }


    /**
     * @return the output that has been written to this ApplicationConsole.
     */
    public String getConsoleOutput()
    {
        return lines.toString();
    }


    /**
     * Method description
     *
     * @param key
     * @param lineNum
     *
     * @return
     */
    public String getConsoleOutputLine(String key,
                                       int lineNum)
    {
        String line = "";

        if (lines.containsKey(key))
        {
            List<String> lineList = lines.get(key);

            line = (lineList.size() > lineNum) ? lineList.get(lineNum) : "";
        }

        return line;
    }


    /**
     * Method description
     *
     * @param key
     *
     * @return
     */
    public List<String> getConsoleOutputLines(String key)
    {
        return lines.get(key);
    }


    /**
     * Clear the text stored in the buffer.
     */
    public synchronized void clear()
    {
        lines.clear();
    }


    /**
     * Method description
     *
     * @param timeout
     *
     * @throws InterruptedException
     */
    public synchronized void waitForLine(long timeout) throws InterruptedException
    {
        if (lines.isEmpty())
        {
            wait(timeout);
        }
    }
}
