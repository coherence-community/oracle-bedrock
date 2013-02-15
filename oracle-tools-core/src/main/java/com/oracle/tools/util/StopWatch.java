/*
 * File: StopWatch.java
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

package com.oracle.tools.util;

import java.util.concurrent.TimeUnit;

/**
 * A simple class that provides the functionality of a stop watch with
 * millisecond accuracy (though this is based on the
 * level of accuracy provided by the underlying operating system).
 * <p>
 * Copyright (c) 2008. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class StopWatch
{
    /**
     * The instant in time when the stop watch was started (in milliseconds).
     * (-1 means not started).
     */
    private long m_startInstant;

    /**
     * The time the stop watch was stopped (in milliseconds).
     * (-1 means not stopped).
     */
    private long m_stopInstant;


    /**
     * Constructs a {@link StopWatch}
     * (in the not started state)
     */
    public StopWatch()
    {
        m_startInstant = -1;
        m_stopInstant  = -1;
    }


    /**
     * Starts the {@link StopWatch}. If the {@link StopWatch} was previously
     * started and not stopped, the {@link StopWatch} is restarted at the
     * current time.
     */
    public void start()
    {
        m_startInstant = System.currentTimeMillis();
        m_stopInstant  = -1;
    }


    /**
     * Stops the {@link StopWatch} so that the duration can be measured.  If
     * the {@link StopWatch} was not previously started, an {@link IllegalStateException}
     * is thrown.
     *
     * @throws IllegalStateException  if the {@link StopWatch} was not previously
     *                                started prior to this call
     */
    public void stop()
    {
        if (m_startInstant != -1)
        {
            m_stopInstant = System.currentTimeMillis();
        }
        else
        {
            throw new IllegalStateException("An attempt was made to stop a StopWatch that was not previously started.");
        }
    }


    /**
     * Obtains the amount of elapsed time in the specified units for the {@link StopWatch}.
     * If the {@link StopWatch} has been stopped, the stop time is used.
     * If the {@link StopWatch} has not be stopped, the current time is used.
     * If the {@link StopWatch} hasn't been started, an {@link IllegalStateException} is thrown.
     *
     * @param units  the {@link TimeUnit} for the elapsed time
     *
     * @return  the number of units of the elapsed time since the {@link StopWatch}
     *          started
     *
     * @throws IllegalStateException  if the {@link StopWatch} was not previously
     *                                started prior to this call
     */
    public long getElapsedTimeIn(TimeUnit units)
    {
        if (m_startInstant != -1)
        {
            long endTime    = m_stopInstant == -1 ? System.currentTimeMillis() : m_stopInstant;

            long durationMS = endTime - m_startInstant;

            return units.convert(durationMS, TimeUnit.MILLISECONDS);
        }
        else
        {
            throw new IllegalStateException("An attempt was made to get the duration elapse of a StopWatch that was not previously started.");
        }
    }
}
