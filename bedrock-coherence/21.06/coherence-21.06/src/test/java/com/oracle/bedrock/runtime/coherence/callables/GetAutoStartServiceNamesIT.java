package com.oracle.bedrock.runtime.coherence.callables;

import com.oracle.bedrock.runtime.coherence.options.CacheConfig;
import com.oracle.coherence.common.base.Classes;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.Coherence;
import com.tangosol.net.CoherenceConfiguration;
import com.tangosol.net.ConfigurableCacheFactory;
import com.tangosol.net.DefaultCacheServer;
import com.tangosol.net.SessionConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.hamcrest.MatcherAssert.assertThat;

public class GetAutoStartServiceNamesIT
{
    @AfterEach
    void cleanup()
    {
        Coherence.closeAll();
        CacheFactory.shutdown();
        System.clearProperty(CacheConfig.PROPERTY);
    }

    @Test
    public void shouldGetAllCoherenceServiceNames() throws Exception
    {
        CoherenceConfiguration configuration = CoherenceConfiguration.builder()
                .withSession(SessionConfiguration.builder().withConfigUri("test-cache-config.xml").named("foo").withScopeName("Foo").build())
                .withSession(SessionConfiguration.builder().withConfigUri("test-cache-config.xml").named("bar").withScopeName("Bar").build())
                .build();

        try (Coherence coherence = Coherence.clusterMember(configuration).start().join())
        {
            Set<String> names = new GetAutoStartServiceNames().call();
            assertThat(names, containsInAnyOrder("Foo:distributed-service", "Bar:distributed-service"));
        }
    }


    @Test
    public void shouldGetAllDefaultCacheServerServiceNames() throws Exception
    {
        System.setProperty(CacheConfig.PROPERTY, "test-cache-config.xml");
        Set<String> names = new GetAutoStartServiceNames().call();
        assertThat(names, containsInAnyOrder("distributed-service"));
    }
}
