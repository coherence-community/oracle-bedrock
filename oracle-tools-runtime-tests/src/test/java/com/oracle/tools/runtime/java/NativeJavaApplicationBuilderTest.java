/*
 * File: NativeJavaApplicationBuilderTest.java
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

package com.oracle.tools.runtime.java;

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.deferred.listener.DeferredCompletionListener;

import com.oracle.tools.runtime.ApplicationConsole;

import com.oracle.tools.runtime.concurrent.RemoteExecutor;
import com.oracle.tools.runtime.concurrent.RemoteExecutorListener;
import com.oracle.tools.runtime.concurrent.socket.RemoteExecutorServer;
import com.oracle.tools.runtime.concurrent.socket.SocketBasedRemoteExecutorTests;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.applications.ParentApplication;
import com.oracle.tools.runtime.java.applications.SleepingApplication;
import com.oracle.tools.runtime.java.container.Container;

import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.eventually;
import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.Serializable;

import java.net.InetAddress;

import java.util.UUID;

import java.util.concurrent.Callable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Functional Tests for {@link com.oracle.tools.runtime.java.NativeJavaApplicationBuilder}s.
 * <p>
 * Copyright (c) 2013. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public class NativeJavaApplicationBuilderTest extends AbstractJavaApplicationBuilderTest
{
    /**
     * {@inheritDoc}
     */
    public JavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> newJavaApplicationBuilder()
    {
        return new NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();
    }


    /**
     * Ensure that the {@link #newJavaApplicationBuilder()} method is producing
     * the expected type of builder.
     */
    @Test
    public void shouldBeCorrectJavaApplicationBuilder()
    {
        assertThat(newJavaApplicationBuilder(), is(instanceOf(NativeJavaApplicationBuilder.class)));
    }


    /**
     * Ensure that {@link NativeJavaApplicationBuilder}s in orphan mode
     * will leave orphan application processes running.
     */
    @Test
    public void shouldCreateOrphans()
    {
        RemoteExecutorServer  server            = new RemoteExecutorServer(Container.getAvailablePorts().next());
        SimpleJavaApplication parentApplication = null;

        try
        {
            // determine the address
            InetAddress localAddress = InetAddress.getLocalHost();

            // start a server that the child can connect too.
            server.open();

            // add a listener for the ClientApplication
            ClientApplicationListener listener = new ClientApplicationListener();

            server.addListener(listener);

            // define and start the ParentApplication (this will start a ChildApplication)
            SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(ParentApplication.class.getName());

            schema.setSystemProperty("server.address", localAddress.getHostAddress());
            schema.setSystemProperty("server.port", server.getPort());
            schema.setSystemProperty("orphan.children", true);

            NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
                new NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

            builder.setOrphansPermitted(false);

            ApplicationConsole console = new SystemApplicationConsole();

            parentApplication = builder.realize(schema, "parent", console);

            // wait for the ChildApplication to connect back to the ServerChannel
            Eventually.assertThat(invoking(listener).isOpened(), is(true));

            // close the ParentApplication
            parentApplication.close();

            // submit the child a request to prove that it's orphaned
            RemoteExecutor                     child    = listener.getExecutor();
            DeferredCompletionListener<String> response = new DeferredCompletionListener<String>(String.class);

            child.submit(new SocketBasedRemoteExecutorTests.PingPong(), response);

            Eventually.assertThat(response, is("PONG"));

            // shutdown the client
            child.submit(new SystemExitRequest(), null);

            // wait for the ChildApplication to disconnect from the ServerChannel
            Eventually.assertThat(invoking(listener).isClosed(), is(true));
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (parentApplication != null)
            {
                parentApplication.close();
            }

            if (server != null)
            {
                server.close();
            }
        }
    }


    /**
     * Ensure that {@link NativeJavaApplicationBuilder}s in orphan-less mode don't leave
     * orphan application processes running.
     */
    @Test
    public void shouldNotCreateOrphans()
    {
        RemoteExecutorServer  server            = new RemoteExecutorServer(Container.getAvailablePorts().next());
        SimpleJavaApplication parentApplication = null;

        try
        {
            // determine the address
            InetAddress localAddress = InetAddress.getLocalHost();

            // start a server that the child can connect too.
            server.open();

            // add a listener for the ClientApplication
            ClientApplicationListener listener = new ClientApplicationListener();

            server.addListener(listener);

            // define and start the ParentApplication (this will start a ChildApplication)
            SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(ParentApplication.class.getName());

            schema.setSystemProperty("server.address", localAddress.getHostAddress());
            schema.setSystemProperty("server.port", server.getPort());
            schema.setSystemProperty("orphan.children", false);

            NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
                new NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

            builder.setOrphansPermitted(false);

            ApplicationConsole console = new SystemApplicationConsole();

            parentApplication = builder.realize(schema, "parent", console);

            // wait for the ChildApplication to connect back to the ServerChannel
            Eventually.assertThat(invoking(listener).isOpened(), is(true));

            // close the ParentApplication
            parentApplication.close();

            // wait for the ChildApplication to disconnect from the ServerChannel
            Eventually.assertThat(invoking(listener).isClosed(), is(true));
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (parentApplication != null)
            {
                parentApplication.close();
            }

            if (server != null)
            {
                server.close();
            }
        }
    }


    /**
     * Ensure that {@link NativeJavaApplicationBuilder}s create applications that
     * can have {@link java.util.concurrent.Callable}s submitted to them and executed.
     */
    @Test
    public void shouldExecuteCallable() throws InterruptedException
    {
        SimpleJavaApplication application = null;

        try
        {
            // define the SleepingApplication
            SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName());

            // set a System-Property for the SleepingApplication (we'll request it back)
            String uuid = UUID.randomUUID().toString();

            schema.setSystemProperty("uuid", uuid);

            // build and start the SleepingApplication
            NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema> builder =
                new NativeJavaApplicationBuilder<SimpleJavaApplication, SimpleJavaApplicationSchema>();

            ApplicationConsole console = new SystemApplicationConsole();

            application = builder.realize(schema, "sleeping", console);

            // request the system property from the SleepingApplication
            DeferredCompletionListener<String> deferred = new DeferredCompletionListener<String>(String.class);

            application.submit(new GetSystemProperty("uuid"), deferred);

            Eventually.assertThat(deferred, is(uuid));
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (application != null)
            {
                application.close();
            }
        }
    }


    /**
     * A {@link com.oracle.tools.runtime.concurrent.RemoteExecutorListener} to track when it's been opened and closed.
     */
    public static class ClientApplicationListener implements RemoteExecutorListener
    {
        /**
         * The {@link com.oracle.tools.runtime.concurrent.RemoteExecutor} that was opened / closed.
         */
        private RemoteExecutor executor;

        /**
         * Was the {@link com.oracle.tools.runtime.concurrent.RemoteExecutor} open?
         */
        private AtomicBoolean isOpened;

        /**
         * Was the {@link com.oracle.tools.runtime.concurrent.RemoteExecutor} closed?
         */
        private AtomicBoolean isClosed;


        /**
         * Constructs a {@link ClientApplicationListener}.
         */
        public ClientApplicationListener()
        {
            executor = null;
            isOpened = new AtomicBoolean(false);
            isClosed = new AtomicBoolean(false);
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void onOpened(RemoteExecutor executor)
        {
            isOpened.compareAndSet(false, true);
            this.executor = executor;
        }


        /**
         * Determine if the {@link ClientApplicationListener} has opened.
         *
         * @return if opened
         */
        public boolean isOpened()
        {
            return isOpened.get();
        }


        /**
         * {@inheritDoc}
         */
        @Override
        public void onClosed(RemoteExecutor executor)
        {
            isClosed.compareAndSet(false, true);
        }


        /**
         * Determine if the {@link ClientApplicationListener} has closed.
         *
         * @return if closed
         */
        public boolean isClosed()
        {
            return isClosed.get();
        }


        /**
         * Obtains the {@link com.oracle.tools.runtime.concurrent.RemoteExecutor} provided to the {@link com.oracle.tools.runtime.concurrent.RemoteExecutorListener}.
         *
         * @return a {@link com.oracle.tools.runtime.concurrent.RemoteExecutor}
         */
        public RemoteExecutor getExecutor()
        {
            return executor;
        }
    }


    /**
     * A {@link Callable} to perform a {@link System#exit(int)}.
     */
    public static class SystemExitRequest implements Callable<Void>
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public Void call() throws Exception
        {
            System.out.println("Terminating Application (due to SystemExitRequest)");

            System.exit(-1);

            return null;
        }
    }
}
