/*
 * File: DockerMachinePlatform.java
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

package com.oracle.bedrock.runtime.docker.machine;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.docker.Docker;
import com.oracle.bedrock.runtime.docker.DockerPlatform;
import com.oracle.bedrock.runtime.remote.Password;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import com.oracle.bedrock.runtime.remote.options.StrictHostChecking;

import java.io.Closeable;
import java.net.InetAddress;

/**
 * An implementation of a {@link RemotePlatform} that is associated with a
 * Docker Machine VM.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerMachinePlatform extends DockerPlatform implements Closeable
{
    /**
     * The {@link DockerMachine} instance to use to obtain
     * the Docker Machine environment.
     */
    private final DockerMachine machine;


    /**
     * Create a {@link DockerMachinePlatform}.
     *
     * @param machine  the {@link DockerMachine} instance to use to obtain
     *                 the Docker Machine environment
     * @param name     the name of the Docker Machine VM instance
     * @param options  any {@link Option}s to apply to the platform
     */
    public DockerMachinePlatform(DockerMachine machine,
                                 String        name,
                                 Option...     options)
    {
        super(name, machine.getClientPlatform(), Docker.machine(name, machine, options), options);

        this.machine = machine;
    }


    /**
     * Obtain the {@link DockerMachine} environment used by this platform.
     *
     * @return  the {@link DockerMachine} environment used by this platform
     */
    public DockerMachine getDockerMachine()
    {
        return machine;
    }


    /**
     * Obtain a {@link RemotePlatform} on the Docker machine instance.
     *
     * @return a {@link RemotePlatform} on the Docker machine instance
     */
    public RemotePlatform getRemotePlatform()
    {
        return getRemotePlatform(new Option[0]);
    }


    /**
     * Obtain a {@link RemotePlatform} on the Docker machine instance.
     *
     * @param options  the {@link Option}s
     *
     * @return a {@link RemotePlatform} on the Docker machine instance
     */
    public RemotePlatform getRemotePlatform(Option... options)
    {
        String      name    = getName();
        InetAddress address = machine.getAddress(name);
        OptionsByType platformOptions =
            OptionsByType.of(this.getOptions()).add(StrictHostChecking.disabled()).addAll(options);

        return new RemotePlatform(name, address, "docker", new Password("tcuser"), platformOptions.asArray());
    }


    @Override
    public void close()
    {
        close(new Option[0]);
    }


    /**
     * Close this {@link DockerMachinePlatform} performing any
     * {@link MachineCloseBehaviour} present in the {@link Option}s.
     * <p>
     * If no {@link MachineCloseBehaviour} is present in the options the
     * default {@link MachineCloseBehaviour} will be used.
     *
     * @param options  the {@link Option}s to use
     *
     * @see MachineCloseBehaviour
     */
    public void close(Option... options)
    {
        OptionsByType         closeOptions = OptionsByType.of(getOptions()).addAll(options);
        MachineCloseBehaviour behaviour    = closeOptions.get(MachineCloseBehaviour.class);

        behaviour.accept(this);
    }


    /**
     * Obtain the status of this Docker Machine instance.
     *
     * @return  the status of this Docker Machine instance
     */
    public String status()
    {
        return machine.status(this.getName());
    }
}
