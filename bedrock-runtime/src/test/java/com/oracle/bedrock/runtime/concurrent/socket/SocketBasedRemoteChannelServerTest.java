package com.oracle.bedrock.runtime.concurrent.socket;

import com.oracle.bedrock.runtime.concurrent.RemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteChannelListener;
import org.junit.Ignore;
import org.junit.Test;

import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author Jonathan Knight  2020.07.22
 */
public class SocketBasedRemoteChannelServerTest
{
    /**
     * This test is to replicate an issue seen in CI builds where the server was
     * assigned an ephemeral port that was previously used for a JMX server and
     * on starting a previous JMX client attempts to connect to the socket
     * consequently breaking the channel.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void shouldIgnoreInvalidConnection() throws Exception
    {
        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer("Test"))
        {
            ChannelListener serverListener = new ChannelListener(1);
            ChannelListener clientListener = new ChannelListener(1);
            server.addListener(serverListener);

            InetAddress   address = server.open();
            int           port    = server.getPort();
            JMXServiceURL url     = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");

            // Connect the the server with a JMX client - this should fail to connect
            try
            {
                JMXConnectorFactory.connect(url, null);
                fail("Should have failed to connect");
            }
            catch (IOException e)
            {
                System.err.println(e.getMessage());
                // expected
            }

            // Connect to the server with a real client - this should succeed.
            try (SocketBasedRemoteChannelClient client = new SocketBasedRemoteChannelClient(address, port, null))
            {
                client.addListener(clientListener);
                client.open();
                assertThat(client.isOpen(), is(true));

                assertThat(serverListener.await(5, TimeUnit.SECONDS), is(true));
                assertThat(clientListener.await(5, TimeUnit.SECONDS), is(true));

                // submit a simple callable that returns a boolean to verify the channel works
                CompletableFuture<Boolean> future = client.submit(() -> true);
                assertThat(future.get(1, TimeUnit.MINUTES), is(true));
            }
        }
    }

    /**
     * This test is to replicate an issue seen in CI builds where the server was
     * assigned an ephemeral port that was previously used for a JMX server and
     * on starting a previous JMX client attempts to connect to the socket
     * consequently breaking the channel.
     *
     * @throws Exception if the test fails
     */
    @Test
    public void shouldIgnoreInvalidConnectionAfterTestCOnnection() throws Exception
    {
        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer("Test"))
        {
            ChannelListener serverListener = new ChannelListener(1);
            ChannelListener clientListener = new ChannelListener(1);
            server.addListener(serverListener);

            InetAddress   address = server.open();
            int           port    = server.getPort();
            JMXServiceURL url     = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");

            // Connect to the server with a real client - this should succeed.
            try (SocketBasedRemoteChannelClient client = new SocketBasedRemoteChannelClient(address, port, null))
            {
                client.addListener(clientListener);
                client.open();
                assertThat(client.isOpen(), is(true));

                assertThat(serverListener.await(5, TimeUnit.SECONDS), is(true));
                assertThat(clientListener.await(5, TimeUnit.SECONDS), is(true));

                // submit a simple callable that returns a boolean to verify the channel works
                CompletableFuture<Boolean> future = client.submit(() -> true);
                assertThat(future.get(1, TimeUnit.MINUTES), is(true));

                // Connect the the server with a JMX client - this should fail to connect
                // but may just get stuck so we need to do it async
                try
                {
                    JMXConnectorFactory.connect(url, null);
                    fail("Should have failed to connect");
                }
                catch (IOException e)
                {
                    // expected
                }

                // submit a simple callable that returns a boolean to verify the channel works
                future = client.submit(() -> true);
                assertThat(future.get(1, TimeUnit.MINUTES), is(true));
            }
        }
    }

    @Test
    @Ignore
    public void shouldWorkWithultipleInvalidConnections() throws Exception
    {
        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer("Test"))
        {
            InetAddress   address = server.open();
            int           port    = server.getPort();
            JMXServiceURL url     = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");
            AtomicBoolean run     = new AtomicBoolean(true);

            Thread thread = new Thread(() -> {
                while(run.get())
                {
                    try
                    {
                        System.out.println("Connecting JMX...");
                        JMXConnectorFactory.connect(url, null);
                    }
                    catch (Throwable thrown)
                    {
                        // expected
                    }
                }
            }, "JMXConnector");

            try
            {
                thread.setDaemon(true);
                thread.start();

                for (int i=0; i<20; i++)
                {
                    System.out.println("Starting test " + i);
                    ChannelListener serverListener = new ChannelListener(1);
                    ChannelListener clientListener = new ChannelListener(1);
                    server.addListener(serverListener);

                    // Connect to the server with a real client - this should succeed.
                    try (SocketBasedRemoteChannelClient client = new SocketBasedRemoteChannelClient(address, port, null))
                    {
                        client.addListener(clientListener);
                        client.open();
                        assertThat(client.isOpen(), is(true));

                        assertThat(serverListener.await(5, TimeUnit.SECONDS), is(true));
                        assertThat(clientListener.await(5, TimeUnit.SECONDS), is(true));

                        // submit a simple callable that returns a boolean to verify the channel works
                        CompletableFuture<Boolean> future = client.submit(() -> true);
                        assertThat(future.get(1, TimeUnit.MINUTES), is(true));

                        // Connect the the server with a JMX client - this should fail to connect
                        // but may just get stuck so we need to do it async

                        // submit a simple callable that returns a boolean to verify the channel works
                        future = client.submit(() -> true);
                        assertThat(future.get(1, TimeUnit.MINUTES), is(true));
                        System.out.println("Finished test " + i);
                    }
                    finally
                    {
                        server.removeListener(serverListener);
                    }
                }
            }
            finally
            {
                run.set(false);
            }
            thread.interrupt();
            thread.join();
        }
    }

    public static class ChannelListener
            extends CountDownLatch
            implements RemoteChannelListener
    {
        public ChannelListener(int count)
        {
            super(count);
        }

        @Override
        public void onOpened(RemoteChannel channel)
        {
            countDown();
        }

        @Override
        public void onClosed(RemoteChannel channel)
        {
        }
    }
}
