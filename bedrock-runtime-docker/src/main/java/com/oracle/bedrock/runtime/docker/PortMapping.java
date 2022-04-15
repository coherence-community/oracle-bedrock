package com.oracle.bedrock.runtime.docker;

import com.oracle.bedrock.runtime.options.Ports;
import com.oracle.bedrock.util.Capture;

import java.util.Collections;
import java.util.Iterator;

/**
 * A representation of a container port mapping.
 *
 * @author jk  2018.10.17
 */
public class PortMapping
{
    /**
     * The name of this {@link PortMapping}
     */
    private String m_sName;

    /**
     * The local port.
     */
    private Capture<Integer> m_capture;

    /**
     * The container port.
     */
    private int m_portContainer;


    /**
     * Create a {@link PortMapping}.
     *
     * @param sName          the name of the mapping
     * @param nPortContainer the container port to map
     */
    public PortMapping(String sName, int nPortContainer)
    {
        this(sName, nPortContainer, nPortContainer);
    }


    /**
     * Create a {@link PortMapping}.
     *
     * @param sName          the name of the mapping
     * @param nPortLocal     the local port to map to the container port
     * @param nPortContainer the container port to map
     */
    public PortMapping(String sName, int nPortLocal, int nPortContainer)
    {
        this(sName, Collections.singleton(nPortLocal).iterator(), nPortContainer);
    }


    /**
     * Create a {@link PortMapping}.
     *
     * @param sName          the name of the mapping
     * @param itPortLocal    the local port to map to the container port
     * @param nPortContainer the container port to map
     */
    public PortMapping(String sName, Iterator<Integer> itPortLocal, int nPortContainer)
    {
        this(sName, new Capture<>(itPortLocal), nPortContainer);
    }


    /**
     * Create a {@link PortMapping}.
     *
     * @param sName          the name of the mapping
     * @param localPort      the local port to map to the container port
     * @param nPortContainer the container port to map
     */
    public PortMapping(String sName, Capture<Integer> localPort, int nPortContainer)
    {
        m_sName         = sName;
        m_capture       = localPort;
        m_portContainer = nPortContainer;
    }


    @Override
    public String toString()
    {
        return m_capture.get() + ":" + m_portContainer;
    }

    /**
     * Obtain the local port value.
     *
     * @return the local port value
     */
    public Ports.Port getPort()
    {
        return new Ports.Port(m_sName, m_capture.get(), m_portContainer);
    }
}