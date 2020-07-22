package com.oracle.bedrock.runtime.concurrent.socket;

import com.oracle.bedrock.runtime.concurrent.RemoteChannel;
import com.oracle.bedrock.runtime.concurrent.RemoteChannelListener;
import org.junit.Test;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        try (SocketBasedRemoteChannelServer server = new SocketBasedRemoteChannelServer())
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
                // expected
            }

            // Connect to the server with a real client - this should succeed.
            try (SocketBasedRemoteChannelClient client = new SocketBasedRemoteChannelClient(address, port))
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
