/*
 * File: DockerPlatform.java
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

package com.oracle.bedrock.runtime.docker;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.AbstractPlatform;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.ApplicationLauncher;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.MetaClass;
import com.oracle.bedrock.runtime.OperatingSystem;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.docker.commands.AbstractDockerCommand;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.remote.RemoteTerminalBuilder;
import com.oracle.bedrock.runtime.remote.SimpleRemoteApplicationLauncher;
import com.oracle.bedrock.runtime.remote.java.RemoteJavaApplicationLauncher;
import com.oracle.bedrock.util.Version;

import java.net.InetAddress;

/**
 * A {@link Platform} that can execute Docker commands using a
 * specific {@link Docker} environment.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerPlatform extends AbstractPlatform
{
    /**
     * The {@link Platform} on which {@link Docker} is running.
     */
    private final Platform clientPlatform;

    /**
     * The {@link Docker} environment.
     */
    private final Docker docker;


    /**
     * Constructs a {@link DockerPlatform}.
     *
     * @param clientPlatform  the client {@link Platform}
     * @param docker          the {@link Docker}
     * @param options         the {@link Option}s
     */
    public DockerPlatform(Platform  clientPlatform,
                          Docker    docker,
                          Option... options)
    {
        this(clientPlatform.getName(), clientPlatform, docker, options);
    }


    /**
     * Constructs a {@link DockerPlatform} with a specific name.
     *
     * @param name            the name
     * @param clientPlatform  the client {@link Platform}
     * @param docker          the {@link Docker}
     * @param options         the {@link Option}s
     */
    public DockerPlatform(String    name,
                          Platform  clientPlatform,
                          Docker    docker,
                          Option... options)
    {
        super(name, options);

        this.clientPlatform = clientPlatform;
        this.docker         = docker;

        getOptions().add(docker);
    }


    /**
     * Obtain the {@link Platform} that is used to run Docker client commands.
     *
     * @return  the {@link Platform} that is used to run Docker client commands
     */
    public Platform getClientPlatform()
    {
        return clientPlatform;
    }


    /**
     * Obtain the {@link Docker} environment being used by
     * this {@link DockerPlatform}.
     *
     * @return  the {@link Docker} environment being used
     *          by this {@link DockerPlatform}
     */
    public Docker getDocker()
    {
        return docker;
    }


    @Override
    public OperatingSystem getOperatingSystem()
    {
        return OperatingSystem.custom("Generic", "", OperatingSystem.Type.LINUX, Version.of("1.0"));
    }


    /**
     * Obtain the {@link InetAddress} of the Docker daemon.                                         
     *
     * @return  the {@link InetAddress} of the Docker daemon.
     */
    @Override
    public InetAddress getAddress()
    {
        return docker.getDaemonInetAddress(clientPlatform);
    }


    @Override
    public <A extends Application> A launch(MetaClass<A> metaClass,
                                            Option...    options)
    {
        // establish the initial launch options based on those defined by the platform
        OptionsByType launchOptions = OptionsByType.of(getOptions());

        // include the options specified when this method was called
        launchOptions.addAll(options);

        launchOptions.addIfAbsent(docker);

        if (metaClass instanceof AbstractDockerCommand)
        {
            // The options contain a Docker command so just run it on the client platform
            return clientPlatform.launch(metaClass, launchOptions.asArray());
        }
        else
        {
            // This is a normal launch command so we will build and image and run
            // it in a container

            DockerRemoteTerminal  terminal = new DockerRemoteTerminal(clientPlatform);
            RemoteTerminalBuilder builder  = (platform) -> terminal;

            launchOptions.add(builder);
            launchOptions.add(terminal);

            return super.launch(metaClass, launchOptions.asArray());
        }
    }


    @Override
    protected <A extends Application, B extends ApplicationLauncher<A>> B getApplicationLauncher(MetaClass<A>  metaClass,
                                                                                                 OptionsByType optionsByType)
                                                                                                 throws UnsupportedOperationException
    {
        Class<? extends A> applicationClass = metaClass.getImplementationClass(this, optionsByType);

        if (JavaApplication.class.isAssignableFrom(applicationClass))
        {
            return (B) new RemoteJavaApplicationLauncher();
        }
        else
        {
            return (B) new SimpleRemoteApplicationLauncher();
        }
    }


    /**
     * Obtain a {@link DockerPlatform} using the {@link LocalPlatform} as the client
     * {@link Platform} and automatically configuring the Docker environment.
     *
     * @return  a {@link DockerPlatform} using the {@link LocalPlatform} as the client
     *          {@link Platform}
     */
    public static DockerPlatform localClient()
    {
        return clientAt(LocalPlatform.get(), Docker.auto());
    }


    /**
     * Obtain a {@link DockerPlatform} using the {@link LocalPlatform} as the client
     * {@link Platform} and using the specified {@link Docker} environment.
     *
     * @param environment  the {@link Docker} environment to use when executing Docker commands
     *
     * @return  a {@link DockerPlatform} using the {@link LocalPlatform} as the client
     *          {@link Platform} and the specified {@link Docker} environment
     */
    public static DockerPlatform localClient(Docker environment)
    {
        return clientAt(LocalPlatform.get(), environment);
    }


    /**
     * Obtain a {@link DockerPlatform} using the specified client {@link Platform}
     * and {@link Docker} environment.
     *
     * @param platform     the {@link Platform} to execute Docker client commands on
     * @param environment  the {@link Docker} environment to use when executing Docker commands
     *
     * @return  a {@link DockerPlatform} using the specified client {@link Platform}
     *          and {@link Docker} environment
     */
    public static DockerPlatform clientAt(Platform platform,
                                          Docker   environment)
    {
        return new DockerPlatform(platform, environment);
    }
}
