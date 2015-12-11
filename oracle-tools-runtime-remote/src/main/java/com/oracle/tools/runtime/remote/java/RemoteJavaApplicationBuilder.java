/*
 * File: RemoteJavaApplicationBuilder.java
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

package com.oracle.tools.runtime.remote.java;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.deferred.AbstractDeferred;
import com.oracle.tools.deferred.PermanentlyUnavailableException;
import com.oracle.tools.deferred.TemporarilyUnavailableException;

import com.oracle.tools.options.Timeout;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;

import com.oracle.tools.runtime.concurrent.ControllableRemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorServer;

import com.oracle.tools.runtime.java.JavaApplication;
import com.oracle.tools.runtime.java.JavaApplicationBuilder;
import com.oracle.tools.runtime.java.JavaApplicationProcess;
import com.oracle.tools.runtime.java.JavaApplicationSchema;
import com.oracle.tools.runtime.java.options.RemoteDebugging;

import com.oracle.tools.runtime.remote.AbstractRemoteApplicationBuilder;
import com.oracle.tools.runtime.remote.RemoteApplicationProcess;
import com.oracle.tools.runtime.remote.RemotePlatform;

import com.oracle.tools.util.CompletionListener;

import static com.oracle.tools.deferred.DeferredHelper.ensure;
import static com.oracle.tools.deferred.DeferredHelper.within;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A {@link JavaApplicationBuilder} that realizes {@link JavaApplication}s on
 * a {@link RemotePlatform}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>
 */
public class RemoteJavaApplicationBuilder<A extends JavaApplication>
    extends AbstractRemoteApplicationBuilder<A, RemoteJavaApplicationEnvironment<A>, RemoteJavaApplicationBuilder<A>>
    implements JavaApplicationBuilder<A, RemotePlatform>
{
    /**
     * Constructs a {@link RemoteJavaApplicationBuilder} for the specified
     * {@link RemotePlatform}.
     *
     * @param platform  the {@link RemotePlatform}
     */
    public RemoteJavaApplicationBuilder(RemotePlatform platform)
    {
        super(platform);
    }


    /**
     * Method description
     *
     * @param applicationSchema
     * @param options
     * @param <T>
     * @param <S>
     *
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    protected <T extends A,
        S extends ApplicationSchema<T>> RemoteJavaApplicationEnvironment<A> getRemoteApplicationEnvironment(S applicationSchema,
        Options                                                                                               options)
    {
        JavaApplicationSchema<A> schema = (JavaApplicationSchema) applicationSchema;

        try
        {
            return new RemoteJavaApplicationEnvironment(schema, platform, options);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to establish the remote application environment", e);
        }
    }


    /**
     * Method description
     *
     * @param options
     * @param schema
     * @param environment
     * @param applicationName
     * @param process
     * @param console
     * @param <T>
     * @param <S>
     *
     * @return
     */
    @Override
    protected <T extends A, S extends ApplicationSchema<T>> T createApplication(Options                             options,
                                                                                S                                   schema,
                                                                                RemoteJavaApplicationEnvironment<A> environment,
                                                                                String                              applicationName,
                                                                                RemoteApplicationProcess            process,
                                                                                ApplicationConsole                  console)
    {
        JavaApplicationSchema<T> javaSchema = (JavaApplicationSchema) schema;

        // create a JavaProcess representation of the RemoteApplicationProcess
        RemoteJavaApplicationProcess remoteJavaProcess = new RemoteJavaApplicationProcess(process,
                                                                                          environment
                                                                                              .getRemoteExecutor());

        // ensure that the launcher process connects back to the server to
        // know that the application has started
        RemoteDebugging remoteDebugging = options.get(RemoteDebugging.class);

        if (!(remoteDebugging.isEnabled() && remoteDebugging.isStartSuspended()))
        {
            Timeout                    timeout = options.get(Timeout.class);

            final RemoteExecutorServer server  = environment.getRemoteExecutor();

            ensure(new AbstractDeferred<Boolean>()
            {
                @Override
                public Boolean get() throws TemporarilyUnavailableException, PermanentlyUnavailableException
                {
                    if (!server.getRemoteExecutors().iterator().hasNext())
                    {
                        throw new TemporarilyUnavailableException(this);
                    }
                    else
                    {
                        return true;
                    }
                }

            }, within(timeout));
        }

        return javaSchema.createJavaApplication(remoteJavaProcess,
                                                applicationName,
                                                platform,
                                                options,
                                                console,
                                                environment.getRemoteEnvironmentVariables(),
                                                environment.getRemoteSystemProperties(),
                                                environment.getRemoteDebugPort());
    }


    /**
     * A {@link RemoteJavaApplicationProcess} is an adapter for a {@link RemoteApplicationProcess},
     * specifically for Java-based applications.
     */
    public static class RemoteJavaApplicationProcess implements JavaApplicationProcess
    {
        /**
         * The {@link RemoteApplicationProcess} being adapted.
         */
        private RemoteApplicationProcess process;

        /**
         * The {@link RemoteExecutor} for the {@link RemoteJavaApplicationProcess}.
         */
        private ControllableRemoteExecutor remoteExecutor;


        /**
         * Constructs a {@link RemoteJavaApplicationProcess}.
         *
         * @param process         the underlying {@link RemoteApplicationProcess}
         * @param remoteExecutor  the {@link RemoteExecutor} for executing remote requests
         */
        public RemoteJavaApplicationProcess(RemoteApplicationProcess   process,
                                            ControllableRemoteExecutor remoteExecutor)
        {
            this.process        = process;
            this.remoteExecutor = remoteExecutor;
        }


        @Override
        public long getId()
        {
            return process.getId();
        }


        @Override
        public void destroy()
        {
            process.destroy();
        }


        @Override
        public void close()
        {
            process.close();
            remoteExecutor.close();
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
            return process.waitFor(options);
        }


        @Override
        public <T> void submit(RemoteCallable<T>     callable,
                               CompletionListener<T> listener) throws IllegalStateException
        {
            remoteExecutor.submit(callable, listener);
        }


        @Override
        public void submit(RemoteRunnable runnable) throws IllegalStateException
        {
            remoteExecutor.submit(runnable);
        }
    }
}
