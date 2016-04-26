/*
 * File: LocalApplicationProcess.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.deferred.DeferredHelper;
import com.oracle.tools.options.Timeout;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;
import static com.oracle.tools.deferred.DeferredHelper.within;

/**
 * An {@link ApplicationProcess} that represents a locally executing
 * operating system process.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LocalApplicationProcess implements ApplicationProcess
{
    /**
     * The underlying representation of the {@link ApplicationProcess}.
     */
    protected Process process;


    /**
     * Construct a {@link LocalApplicationProcess} based on a Java {@link Process}.
     *
     * @param process  the Java {@link Process}
     */
    public LocalApplicationProcess(Process process)
    {
        if (process == null)
        {
            throw new NullPointerException("The provided process can't be null");
        }
        else
        {
            this.process = process;
        }
    }


    @Override
    @Deprecated
    public void destroy()
    {
        close();
    }


    @Override
    public void close()
    {
        process.destroy();
    }


    @Override
    public long getId()
    {
        long id = -1;

        try
        {
            if (process.getClass().getSimpleName().equals("UNIXProcess"))
            {
                final Class<?> clazz = process.getClass();
                final Field    field = clazz.getDeclaredField("pid");

                field.setAccessible(true);

                Object oPid = field.get(process);

                if (oPid instanceof Number)
                {
                    id = ((Number) oPid).longValue();
                }
                else if (oPid instanceof String)
                {
                    id = Long.parseLong((String) oPid);
                }
            }

            // Windows processes, i.e. Win32Process or ProcessImpl
            else
            {
                RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
                final String  sProcess      = runtimeMXBean.getName();
                final int     iPID          = sProcess.indexOf('@');

                if (iPID > 0)
                {
                    String sPID = sProcess.substring(0, iPID);

                    id = Long.parseLong(sPID);
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

        return id;
    }


    @Override
    public int exitValue()
    {
        return process.exitValue();
    }


    @Override
    public InputStream getErrorStream()
    {
        return process.getErrorStream();
    }


    @Override
    public InputStream getInputStream()
    {
        return process.getInputStream();
    }


    @Override
    public OutputStream getOutputStream()
    {
        return process.getOutputStream();
    }


    @Override
    public int waitFor(Option... options)
    {
        Options optionsMap = new Options(options);

        Timeout timeout    = optionsMap.get(Timeout.class);

        DeferredHelper.ensure(eventually(invoking(this).exitValue()), within(timeout));

        return exitValue();
    }
}
