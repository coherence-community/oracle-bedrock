import com.oracle.bedrock.runtime.docker.PortMapping;
import com.oracle.bedrock.runtime.options.Ports;
import com.oracle.bedrock.util.Capture;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author jk  2018.10.17
 */
public class PortMappingTest
{
    @Test
    public void shouldMapToSamePort()
    {
        PortMapping mapping = new PortMapping("foo", 1234);
        Ports.Port  port    = mapping.getPort();

        assertThat(port.getName(), is("foo"));
        assertThat(port.getActualPort(), is(1234));
        assertThat(port.getMappedPort(), is(1234));
        assertThat(mapping.toString(), is("1234:1234"));
    }

    @Test
    public void shouldMapToPort()
    {
        PortMapping mapping = new PortMapping("foo", 1234, 9876);
        Ports.Port  port    = mapping.getPort();

        assertThat(port.getName(), is("foo"));
        assertThat(port.getActualPort(), is(1234));
        assertThat(port.getMappedPort(), is(9876));
        assertThat(mapping.toString(), is("1234:9876"));
    }

    @Test
    public void shouldMapToPortFromIterator()
    {
        Iterator<Integer> iterator = Arrays.asList(1234, 5678).iterator();
        PortMapping       mapping = new PortMapping("foo", iterator, 9876);
        Ports.Port        port    = mapping.getPort();

        assertThat(port.getName(), is("foo"));
        assertThat(port.getActualPort(), is(1234));
        assertThat(port.getMappedPort(), is(9876));
        assertThat(mapping.toString(), is("1234:9876"));
    }

    @Test
    public void shouldMapToPortFromCapture()
    {
        Iterator<Integer> iterator = Arrays.asList(1234, 5678).iterator();
        Capture<Integer>  capture  = Capture.of(iterator);
        PortMapping       mapping  = new PortMapping("foo", capture, 9876);
        Ports.Port        port     = mapping.getPort();

        assertThat(port.getName(), is("foo"));
        assertThat(port.getActualPort(), is(1234));
        assertThat(port.getMappedPort(), is(9876));
        assertThat(mapping.toString(), is("1234:9876"));
    }
}
