/*
 * File: SocketBasedRemoteChannelTests.java
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

package com.oracle.tools.runtime.concurrent.socket;

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.deferred.listener.DeferredCompletionListener;

import com.oracle.tools.runtime.concurrent.RemoteCallable;
import com.oracle.tools.runtime.concurrent.RemoteEvent;
import com.oracle.tools.runtime.concurrent.RemoteEventListener;
import com.oracle.tools.runtime.concurrent.RemoteRunnable;
import com.oracle.tools.runtime.concurrent.options.StreamName;

import org.junit.Assert;
import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.valueOf;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;

import java.io.IOException;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Functional Tests for {@link SocketBasedRemoteChannel}s.
 */
public class SocketBasedRemoteChannelTests
{
    /**
     * Ensure a {@link SocketBasedRemoteChannelServer} can submit and receive a
     * {@link Callable}.
     */
    @Test
    public void shouldSubmitStaticPingPongRequest() throws InterruptedException
    {
        SocketBasedRemoteChannelServer server = null;
        SocketBasedRemoteChannelClient client = null;

        try
        {
            server = new SocketBasedRemoteChannelServer();

            InetAddress address = server.open();

            client = new SocketBasedRemoteChannelClient(address, server.getPort());

            client.open();

            DeferredCompletionListener<String> serverResponse = new DeferredCompletionListener<String>(String.class);

            client.submit(new PingPong(), serverResponse);

            Eventually.assertThat(valueOf(serverResponse), is("PONG"));

            DeferredCompletionListener<String> clientResponse = new DeferredCompletionListener<String>(String.class);

            server.submit(new PingPong(), clientResponse);

            Eventually.assertThat(valueOf(clientResponse), is("PONG"));
        }
        catch (IOException e)
        {
            Assert.fail("Failed to process the request due to:\n" + e);
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }

            if (server != null)
            {
                server.close();
            }
        }
    }


    /**
     * Ensure a {@link SocketBasedRemoteChannelServer} won't accept
     * inner class submissions.
     */
    @Test
    public void shouldNotPermitInnerClassSubmissions() throws InterruptedException
    {
        SocketBasedRemoteChannelServer server = null;
        SocketBasedRemoteChannelClient client = null;

        try
        {
            server = new SocketBasedRemoteChannelServer();

            InetAddress address = server.open();

            client = new SocketBasedRemoteChannelClient(address, server.getPort());

            client.open();

            client.submit(new RemoteCallable<String>()
                          {
                              @Override
                              public String call() throws Exception
                              {
                                  return "PONG";
                              }
                          });

            Assert.fail("Anonymous Inner-Classes Can't be used");
        }
        catch (IOException e)
        {
            Assert.fail("Failed to process the request due to:\n" + e);
        }
        catch (IllegalArgumentException e)
        {
            // success!
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }

            if (server != null)
            {
                server.close();
            }
        }
    }


    /**
     * Ensure a {@link SocketBasedRemoteChannelServer} won't accept submissions without connected clients.
     */
    @Test
    public void shouldNotSubmitWithoutClients() throws InterruptedException
    {
        SocketBasedRemoteChannelServer server = null;
        SocketBasedRemoteChannelClient client = null;

        try
        {
            server = new SocketBasedRemoteChannelServer();

            try
            {
                server.submit(new PingPong());

                Assert.fail("Should not be able to submit a RemoteCallable without a connected client");
            }
            catch (IllegalStateException e)
            {
                // success!
            }

            try
            {
                server.submit(new Ping());

                Assert.fail("Should not be able to submit a RemoteRunnable without a connected client");
            }
            catch (IllegalStateException e)
            {
                // success!
            }
        }
        finally
        {
            if (client != null)
            {
                client.close();
            }

            if (server != null)
            {
                server.close();
            }
        }
    }


    @Test
    public void shouldRaiseAndProcessEvents() throws Exception
    {
        final CountDownLatch latch    = new CountDownLatch(1);
        RemoteEventListener  listener = new RemoteEventListener()
        {
            @Override
            public void onEvent(RemoteEvent event)
            {
                latch.countDown();
            }
        };

        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer())
        {
            StreamName  streamName = StreamName.of("Foo");

            InetAddress address    = server.open();

            server.addListener(listener, streamName);

            try (SocketBasedRemoteChannelClient client = new SocketBasedRemoteChannelClient(address, server.getPort()))
            {
                client.open();

                client.raise(new Event(1), streamName);

                assertThat(latch.await(1, TimeUnit.MINUTES), is(true));
            }
        }
    }


    @Test
    public void shouldReceiveEventsInOrder() throws Exception
    {
        int                  count    = 100;
        final CountDownLatch latch    = new CountDownLatch(count);
        final List<Integer>  list     = new ArrayList<>();
        RemoteEventListener  listener = new RemoteEventListener()
        {
            @Override
            public void onEvent(RemoteEvent event)
            {
                list.add(((Event) event).getId());
                latch.countDown();
            }
        };

        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer())
        {
            StreamName  streamName = StreamName.of("Foo");

            InetAddress address    = server.open();

            server.addListener(listener, streamName);

            try (SocketBasedRemoteChannelClient client = new SocketBasedRemoteChannelClient(address, server.getPort()))
            {
                client.open();

                for (int i = 0; i < count; i++)
                {
                    client.raise(new Event(i), streamName);
                }

                assertThat(latch.await(1, TimeUnit.MINUTES), is(true));
                assertThat(list.size(), is(count));

                for (int i = 0; i < count; i++)
                {
                    assertThat(list.get(i), is(i));
                }
            }
        }
    }


    /**
     * A simple {@link RemoteEvent} for testing.
     */
    public static class Event implements RemoteEvent
    {
        /**
         * The identity of the {@link Event}.
         */
        private int id;


        /**
         * Constructs the {@link Event}.
         *
         * @param id  the {@link Event} identity
         */
        public Event(int id)
        {
            this.id = id;
        }


        public int getId()
        {
            return id;
        }
    }


    /**
     * A simple ping {@link RemoteRunnable}.
     */
    public static class Ping implements RemoteRunnable
    {
        @Override
        public void run()
        {
            System.out.println("PING");
        }
    }


    /**
     * A simple ping-pong {@link RemoteCallable}.
     */
    public static class PingPong implements RemoteCallable<String>
    {
        @Override
        public String call() throws Exception
        {
            return "PONG";
        }
    }
}
