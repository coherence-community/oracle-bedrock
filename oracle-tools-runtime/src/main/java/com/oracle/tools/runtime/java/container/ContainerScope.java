/*
 * File: ContainerScope.java
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

package com.oracle.tools.runtime.java.container;

import com.oracle.tools.runtime.LocalPlatform;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import java.util.Properties;

/**
 * A {@link Scope} specifically designed for use by Container-based applications
 * that provides the ability to pipe Standard Input, Output and Error to other streams.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class ContainerScope extends AbstractContainerScope
{
    /**
     * The {@link java.io.PipedOutputStream} to which Standard Output will be written when
     * an application uses this {@link ContainerScope}.
     * <p>
     * NOTE: This will be <code>null</code> when the Scope represents the
     * underlying Java Virtual Machine resources.
     */
    private PipedOutputStream stdoutPipedOutputStream;

    /**
     * The {@link java.io.PipedInputStream} from which Standard Output written by an
     * application using this {@link ContainerScope}, may be read.
     * <p>
     * NOTE: This will be <code>null</code> when the Scope represents the
     * underlying Java Virtual Machine resources.
     */
    private PipedInputStream stdoutPipedInputStream;

    /**
     * Is the Standard Error Stream redirected to the Standard Output?
     */
    private boolean redirectErrorStream;

    /**
     * The {@link java.io.PipedOutputStream} to which Standard Error will be written when
     * an application uses this {@link ContainerScope}.
     * <p>
     * NOTE: This will be <code>null</code> when the Scope represents the
     * underlying Java Virtual Machine resources.
     */
    private PipedOutputStream stderrPipedOutputStream;

    /**
     * The {@link java.io.PipedInputStream} from which Standard Error written by an
     * application using this {@link ContainerScope}, may be read.
     * <p>
     * NOTE: This will be <code>null</code> when the Scope represents the
     * underlying Java Virtual Machine resources.
     */
    private PipedInputStream stderrPipedInputStream;

    /**
     * The {@link java.io.PipedOutputStream} to which Standard Input to an application
     * using this {@link ContainerScope}, may be written.
     * <p>
     * NOTE: This will be <code>null</code> when the Scope represents the
     * underlying Java Virtual Machine resources.
     */
    private PipedOutputStream stdinPipedOutputStream;

    /**
     * The {@link java.io.PipedInputStream} from which Standard Input for an
     * application using this {@link ContainerScope}, may be read.
     * <p>
     * NOTE: This will be <code>null</code> when the Scope represents the
     * underlying Java Virtual Machine resources.
     */
    private PipedInputStream stdinPipedInputStream;

    /**
     * The {@link ContainerMBeanServerBuilder} to be used when an application
     * is in this {@link ContainerScope}.
     */
    private ContainerMBeanServerBuilder mBeanServerBuilder;


    /**
     * Constructs a {@link ContainerScope}.
     *
     * @param name  the name of the scope
     */
    public ContainerScope(String name)
    {
        this(name,
             new Properties(),
             LocalPlatform.getInstance().getAvailablePorts(),
             null,
             false,
             Container.PIPE_BUFFER_SIZE_BYTES);
    }


    /**
     * Constructs a {@link ContainerScope}.
     *
     * @param name                 the name of the scope
     * @param properties           the System {@link java.util.Properties} for the scope
     */
    public ContainerScope(String     name,
                          Properties properties)
    {
        this(name,
             properties,
             LocalPlatform.getInstance().getAvailablePorts(),
             null,
             false,
             Container.PIPE_BUFFER_SIZE_BYTES);
    }


    /**
     * Constructs a {@link ContainerScope}.
     *
     * @param name                 the name of the scope
     * @param properties           the System {@link java.util.Properties} for the scope
     * @param availablePorts       the {@link com.oracle.tools.runtime.network.AvailablePortIterator} for the scope
     * @param mBeanServerBuilder   the {@link ContainerMBeanServerBuilder}
     *                             (if null a default will be created)
     * @param redirectErrorStream  should the stderr stream be redirected to stdout
     * @param pipeBufferSizeBytes  the number of bytes to reserve for i/o buffers
     */
    public ContainerScope(String                      name,
                          Properties                  properties,
                          AvailablePortIterator       availablePorts,
                          ContainerMBeanServerBuilder mBeanServerBuilder,
                          boolean                     redirectErrorStream,
                          int                         pipeBufferSizeBytes)
    {
        super(name, properties, availablePorts, mBeanServerBuilder);

        this.redirectErrorStream = redirectErrorStream;

        try
        {
            stdoutPipedOutputStream = new PipedOutputStream();
            stdoutPipedInputStream  = new PipedInputStream(stdoutPipedOutputStream, pipeBufferSizeBytes);
            stdout                  = new PrintStream(stdoutPipedOutputStream);

            if (redirectErrorStream)
            {
                stderrPipedOutputStream = null;
                stderrPipedInputStream  = null;
                stderr                  = stdout;
            }
            else
            {
                stderrPipedOutputStream = new PipedOutputStream();
                stderrPipedInputStream  = new PipedInputStream(stderrPipedOutputStream, pipeBufferSizeBytes);
                stderr                  = new PrintStream(stderrPipedOutputStream);
            }

            stdinPipedOutputStream = new PipedOutputStream();
            stdinPipedInputStream  = new PipedInputStream(stdinPipedOutputStream, pipeBufferSizeBytes);
            stdin                  = stdinPipedInputStream;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not establish i/o pipes for the ContainerScope [" + getName() + "]", e);
        }

        this.mBeanServerBuilder = mBeanServerBuilder == null
                                  ? new ContainerMBeanServerBuilder(this.availablePorts) : mBeanServerBuilder;
    }


    /**
     * Obtains the {@link java.io.InputStream} that can be used to read the contents
     * of the Standard Output that has been written to this {@link ContainerScope}.
     *
     * @return the Standard Output {@link java.io.InputStream}
     */
    public InputStream getStandardOutputInputStream()
    {
        return stdoutPipedInputStream;
    }


    /**
     * Obtains the {@link java.io.InputStream} that can be used to read the contents
     * of the Standard Error that has been written to this {@link ContainerScope}.
     *
     * @return the Standard Error {@link java.io.InputStream}
     */
    public InputStream getStandardErrorInputStream()
    {
        if (redirectErrorStream)
        {
            throw new UnsupportedOperationException("The Standard Error Stream has been redirected to the Standard Output Stream");
        }
        else
        {
            return stderrPipedInputStream;
        }
    }


    /**
     * Obtains the {@link java.io.OutputStream} that can be used to write content
     * of the Standard Input that can been read in this {@link ContainerScope}.
     *
     * @return the Standard Error {@link java.io.InputStream}
     */
    public OutputStream getStandardInputOutputStream()
    {
        return stdinPipedOutputStream;
    }


    @Override
    public boolean close()
    {
        if (super.close())
        {
            try
            {
                stdoutPipedOutputStream.close();
            }
            catch (Exception e)
            {
                // SKIP: we ignore exceptions
            }

            try
            {
                stdoutPipedInputStream.close();
            }
            catch (Exception e)
            {
                // SKIP: we ignore exceptions
            }

            try
            {
                stdout.close();
            }
            catch (Exception e)
            {
                // SKIP: we ignore exceptions
            }

            if (!redirectErrorStream)
            {
                try
                {
                    stderrPipedOutputStream.close();
                }
                catch (Exception e)
                {
                    // SKIP: we ignore exceptions
                }

                try
                {
                    stderrPipedInputStream.close();
                }
                catch (Exception e)
                {
                    // SKIP: we ignore exceptions
                }

                try
                {
                    stderr.close();
                }
                catch (Exception e)
                {
                    // SKIP: we ignore exceptions
                }
            }

            try
            {
                stdinPipedOutputStream.close();
            }
            catch (Exception e)
            {
                // SKIP: we ignore exceptions
            }

            try
            {
                stdinPipedInputStream.close();
            }
            catch (Exception e)
            {
                // SKIP: we ignore exceptions
            }

            try
            {
                stdin.close();
            }
            catch (Exception e)
            {
                // SKIP: we ignore exceptions
            }

            return true;
        }
        else
        {
            return false;
        }
    }
}
