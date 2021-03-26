/*
 * File: Docker.java
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
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.docker.machine.DockerMachine;
import com.oracle.bedrock.runtime.docker.options.DockerDefaultBaseImages;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.EnvironmentVariable;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An encapsulation of the various settings required to run Docker commands.
 * <p>
 * <strong>A {@link Docker} instance is immutable</strong>, methods that
 *  add options and configuration to this {@link Docker} environment return a
 *  new instance of a {@link Docker} environment with the modifications applied
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Docker implements Option
{
    /**
     * The API version to use (e.g. 1.19)
     */
    public static final String ENV_DOCKER_API_VERSION = "DOCKER_API_VERSION";

    /**
     * The location of your client configuration files.
     */
    public static final String ENV_DOCKER_CONFIG = "DOCKER_CONFIG";

    /**
     * The location of your authentication keys.
     */
    public static final String ENV_DOCKER_CERT_PATH = "DOCKER_CERT_PATH";

    /**
     * The graph driver to use.
     */
    public static final String ENV_DOCKER_DRIVER = "DOCKER_DRIVER";

    /**
     * Daemon socket to connect to.
     */
    public static final String ENV_DOCKER_HOST = "DOCKER_HOST";

    /**
     * Prevent warnings that your Linux kernel is unsuitable for Docker.
     */
    public static final String ENV_DOCKER_NOWARN_KERNEL_VERSION = "DOCKER_NOWARN_KERNEL_VERSION";

    /**
     * If set this will disable ‘pivot_root’.
     */
    public static final String ENV_DOCKER_RAMDISK = "DOCKER_RAMDISK";

    /**
     * When set Docker uses TLS and verifies the remote.
     */
    public static final String ENV_DOCKER_TLS_VERIFY = "DOCKER_TLS_VERIFY";

    /**
     * When set Docker uses notary to sign and verify images.
     * Equates to --disable-content-trust=false
     * for build, create, pull, push, run.
     */
    public static final String ENV_DOCKER_CONTENT_TRUST = "DOCKER_CONTENT_TRUST";

    /**
     * The URL of the Notary server to use. This defaults to
     * the same URL as the registry.
     */
    public static final String ENV_DOCKER_CONTENT_TRUST_SERVER = "DOCKER_CONTENT_TRUST_SERVER";

    /**
     * Location for temporary Docker files.
     */
    public static final String ENV_DOCKER_TMPDIR = "DOCKER_TMPDIR";

    /**
     * Location of client config files
     * E.G. --config=~/.docker
     */
    public static final String ARG_CONFIG = "--config";

    /**
     * Enable debug mode
     * E.G. --debug=false
     */
    public static final String ARG_DEBUG = "--debug";

    /**
     * Daemon socket(s) to connect to
     */
    public static final String ARG_HOST = "--host";

    /**
     * Print usage
     * E.G. --help=false
     */
    public static final String ARG_HELP = "--help";

    /**
     * Set the logging level
     * E.G. --log-level=info
     */
    public static final String ARG_LOG_LEVEL = "--log-level";

    /**
     * Use TLS; implied by --tlsverify
     * E.G. --tls=false
     */
    public static final String ARG_TLS = "--tls";

    /**
     * Trust certs signed only by this CA
     * E.G. --tlscacert=~/.docker/ca.pem
     */
    public static final String ARG_TLS_CA_CERT = "--tlscacert";

    /**
     * Path to TLS certificate file
     * E.G. --tlscert=~/.docker/cert.pem
     */
    public static final String ARG_TLS_CERT = "--tlscert";

    /**
     * Path to TLS key file
     * E.G. --tlskey=~/.docker/key.pem
     */
    public static final String ARG_TLS_KEY = "--tlskey";

    /**
     * Use TLS and verify the remote
     * E.G. --tlsverify=false
     */
    public static final String ARG_TLS_VERIFY = "--tlsverify";

    /**
     * Print version information and quit
     * E.G. --version=false
     */
    public static final String ARG_VERSION = "--version";

    /**
     * The default name of the executable to use to execute Docker commands.
     */
    public static final String DEFAULT_EXECUTABLE = "docker";

    /**
     * An immutable list of {@link EnvironmentVariable}s to use to configure a Docker environment.
     */
    private final List<EnvironmentVariable> environmentVariables;

    /**
     * An immutable list of command options to use when running a Docker command.
     */
    private final List<Argument> arguments;

    /**
     * The name of the executable to use to execute Docker commands.
     */
    private final String dockerExecutable;

    /**
     * The tree of default base images to use for given {@link Application}
     * classes.
     */
    private final DockerDefaultBaseImages baseImages;

    /**
     * The name of the network to connect automatically created containers to.
     */
    private String defaultNetwork;

    /**
     * The address of the Docker daemon.
     */
    private final String daemonAddress;


    /**
     * Create a {@link Docker} environment.
     *
     * @param environmentVariables  the {@link EnvironmentVariable}s to use to configure
     *                              a Docker environment
     * @param arguments             the command options to use when running a Docker command
     */
    private Docker(String                    daemonAddress,
                   String                    executable,
                   List<EnvironmentVariable> environmentVariables,
                   List<Argument>            arguments,
                   DockerDefaultBaseImages   baseImages)
    {
        this.daemonAddress = daemonAddress;
        this.baseImages    = baseImages;

        // We are immutable so make sure we cannot change these lists
        this.environmentVariables = Collections.unmodifiableList(environmentVariables);
        this.arguments            = Collections.unmodifiableList(arguments);

        if (executable == null || executable.isEmpty())
        {
            executable = DEFAULT_EXECUTABLE;
        }

        this.dockerExecutable = executable;
    }


    /**
     * Obtain a {@link Docker} environment that is the same as this
     * {@link Docker} environment with the specified Docker daemon
     * address.
     * <p>
     * Equates to the Docker <code>--host</code> command option.
     *
     * @param address  the address of the Docker daemon
     *
     * @return  a {@link Docker} environment that is the same as this
     *          {@link Docker} environment with the specified Docker
     *          daemon address
     */
    public Docker withDaemonAddress(String address)
    {
        return new Docker(address,
                          this.dockerExecutable,
                          this.environmentVariables,
                          replaceArgument(this.arguments, Argument.of(ARG_HOST, '=', address)),
                          this.baseImages);
    }


    /**
     * Obtain the Docker address that this {@link Docker}
     * environment will use to communicate with the Docker daemon.
     * <p>
     *
     * @return  the Docker address that this {@link Docker}
     *          environment will use to communicate with the
     *          Docker daemon
     */
    public String getDaemonAddress()
    {
        return daemonAddress;
    }


    /**
     * Set the executable name to use to run Docker client commands.
     * <p>
     * This would typically be "docker" but some third-party add-ons
     * provide other clients.
     *
     * @param executable  the name of the executable or null to use the default of "docker"
     *
     * @return  a new instance of {@link Docker} that is a copy of this
     *          instance with the addition of the executable name change
     */
    public Docker dockerExecutableOf(String executable)
    {
        if (this.dockerExecutable.equals(executable))
        {
            return this;
        }

        return new Docker(daemonAddress, executable, environmentVariables, arguments, baseImages);
    }


    /**
     * Obtain the executable name to use to run Docker client commands.
     *
     * @return  the executable name to use to run Docker client commands
     */
    public String getDockerExecutable()
    {
        return dockerExecutable;
    }


    /**
     * Obtain an immutable copy of the command line {@link Argument}s
     * for this {@link Docker} environment.
     *
     * @return  an immutable copy of the command line {@link Argument}s
     *          for this {@link Docker} environment
     */
    public List<Argument> getArguments()
    {
        return Collections.unmodifiableList(arguments);
    }


    /**
     * Obtain an immutable copy of the {@link EnvironmentVariable}s
     * for this {@link Docker} environment.
     *
     * @return  an immutable copy of the {@link EnvironmentVariable}s
     *          for this {@link Docker} environment
     */
    public List<EnvironmentVariable> getEnvironmentVariables()
    {
        return Collections.unmodifiableList(environmentVariables);
    }


    /**
     * Add a default base image to use for a specific type of
     * application class.
     *
     * @param applicationClass  the {@link Class} of the application
     * @param baseImageName     the base image name to use
     *
     * @return  a new instance of {@link Docker} that is a copy of this
     *          instance with the addition of the base image default
     *
     * @see #getBaseImage(Class)
     *
     * @throws IllegalArgumentException if the application class is null or
     *                                  if the base image name is null or blank
     */
    public Docker withBaseImage(Class<? extends Application> applicationClass,
                                String                       baseImageName)
    {
        if (applicationClass == null)
        {
            throw new IllegalArgumentException("The application Class cannot be null");
        }

        if (baseImageName == null || baseImageName.trim().isEmpty())
        {
            throw new IllegalArgumentException("The base image name cannot be null or blank");
        }

        // If the proposed change will not make a difference then return ourselves
        if (this.getBaseImage(applicationClass).equals(baseImageName))
        {
            return this;
        }

        // DockerDefaultBaseImages is immutable so create a new on from ours with the additional class
        DockerDefaultBaseImages images = this.baseImages.with(applicationClass, baseImageName);

        return new Docker(this.daemonAddress, this.dockerExecutable, this.environmentVariables, this.arguments, images);
    }


    /**
     * Obtain the default base image name to use
     * for applications of a given {@link Class}.
     *
     * @param applicationClass  the application {@link Class}
     *
     * @return  the name of the base image to use
     *
     * @see #withBaseImage(Class, String)
     */
    public String getBaseImage(Class<? extends Application> applicationClass)
    {
        return baseImages.getBaseImage(applicationClass);
    }


    /**
     * Obtain a new {@link Docker} environment that is a copy of
     * this environment with the addition of the specified
     * default network name.
     *
     * @param networkName  the name of the default network to connect
     *                     automatically created containers to
     *
     * @return  a new {@link Docker} environment that is a copy of
     *          this environment with the addition of the specified
     *          default network name
     */
    public Docker withDefaultNetwork(String networkName)
    {
        Docker docker = new Docker(this.daemonAddress,
                                   this.dockerExecutable,
                                   this.environmentVariables,
                                   this.arguments,
                                   this.baseImages);

        docker.defaultNetwork = networkName;

        return docker;
    }


    /**
     * Obtain the name of the default network to connect
     * automatically run containers to.
     *
     * @return  the name of the default network to connect
     *          automatically run containers to
     */
    public String getDefaultNetworkName()
    {
        return defaultNetwork;
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified location of client config files.
     * <p>
     * Equates to the Docker <code>--config</code> command option.
     *
     * @param config the config
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified location of client config files
     */
    public Docker configAt(Object config)
    {
        return withCommandOptions(Argument.of(ARG_CONFIG, '=', config));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified  debug setting.
     * <p>
     * Equates to the Docker <code>--debug</code> command option.
     *
     * @param enabled  true to enable debug, false to disable debug
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified debug setting
     */
    public Docker debug(boolean enabled)
    {
        return withCommandOptions(Argument.of(ARG_DEBUG, '=', enabled));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified log level.
     * <p>
     * Equates to the Docker <code>--log-level</code> command option.
     *
     * @param level  the logging level to use, for example "info"
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified log level
     */
    public Docker logLevel(String level)
    {
        return withCommandOptions(Argument.of(ARG_LOG_LEVEL, '=', level));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified tls setting.
     * <p>
     * Equates to the Docker <code>--tls</code> command option.
     *
     * @param enabled  true to enables TLS, false to disable TLS
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified tls setting
     */
    public Docker tls(boolean enabled)
    {
        return withCommandOptions(Argument.of(ARG_TLS, '=', enabled));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified Trust certs signed only by this CA.
     * <p>
     * Equates to the Docker <code>--tlscacert</code> command option.
     *
     * @param location  the location of the certificates
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified Trust certs signed only
     *          by this CA
     */
    public Docker tlsCACert(File location)
    {
        return withCommandOptions(Argument.of(ARG_TLS_CA_CERT, '=', location));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified path to TLS certificate file.
     * <p>
     * Equates to the Docker <code>--tlscert</code> command option.
     *
     * @param location  the location of the certificate file
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified path to TLS certificate file
     */
    public Docker tlsCert(File location)
    {
        return withCommandOptions(Argument.of(ARG_TLS_CERT, '=', location));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified path to TLS key file.
     * <p>
     * Equates to the Docker <code>--tlskey</code> command option.
     *
     * @param location  the location of the key file
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified path to TLS key file
     */
    public Docker tlsKey(File location)
    {
        return withCommandOptions(Argument.of(ARG_TLS_KEY, '=', location));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified TLS verification setting.
     * <p>
     * Equates to the Docker <code>--tlsverify</code> command option.
     *
     * @param verify  true to enable TLS verification, false to disable TLS verification
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified  TLS verification setting
     */
    public Docker tlsVerify(boolean verify)
    {
        return withCommandOptions(Argument.of(ARG_TLS_VERIFY, '=', verify));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified <code>DOCKER_API_VERSION</code>
     * environment variable.
     *
     * @param version  The API version to use (e.g. 1.19)
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified <code>DOCKER_API_VERSION</code>
     *          environment variable
     */
    public Docker apiVersion(String version)
    {
        return withEnvironmentVariables(EnvironmentVariable.of(ENV_DOCKER_API_VERSION, version));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified <code>DOCKER_DRIVER</code>
     * environment variable.
     *
     * @param driver  The graph driver to use
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified <code>DOCKER_DRIVER</code>
     *          environment variable
     */
    public Docker driver(String driver)
    {
        return withEnvironmentVariables(EnvironmentVariable.of(ENV_DOCKER_DRIVER, driver));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified <code>DOCKER_NOWARN_KERNEL_VERSION</code>
     * environment variable.
     *
     * @param noWarn  true to prevent warnings that your Linux kernel is
     *                unsuitable for Docker
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified <code>DOCKER_NOWARN_KERNEL_VERSION</code>
     *          environment variable
     */
    public Docker noWarnKernelVersion(boolean noWarn)
    {
        return withEnvironmentVariables(EnvironmentVariable.of(ENV_DOCKER_NOWARN_KERNEL_VERSION, noWarn));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified <code>DOCKER_RAMDISK</code>
     * environment variable.
     *
     * @param use  true to disable ‘pivot_root’
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified <code>DOCKER_RAMDISK</code>
     *          environment variable
     */
    public Docker ramDisk(boolean use)
    {
        return withEnvironmentVariables(EnvironmentVariable.of(ENV_DOCKER_RAMDISK, use));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified <code>DOCKER_CONTENT_TRUST</code>
     * environment variable.
     * <p>
     * Equates to --disable-content-trust=false for build, create, pull,
     * push, run
     *
     * @param enabled  true to set Docker to use notary to sign and verify images
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified <code>DOCKER_CONTENT_TRUST</code>
     *          environment variable
     */
    public Docker contentTrustEnabled(boolean enabled)
    {
        return withEnvironmentVariables(EnvironmentVariable.of(ENV_DOCKER_CONTENT_TRUST, enabled));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified <code>DOCKER_CONTENT_TRUST_SERVER</code>
     * environment variable.
     * <p>
     * This defaults to the same URL as the registry.
     *
     * @param url   the URL of the Notary server to use.
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified <code>DOCKER_CONTENT_TRUST_SERVER</code>
     *          environment variable
     */
    public Docker contentTrustAt(String url)
    {
        return withEnvironmentVariables(EnvironmentVariable.of(ENV_DOCKER_CONTENT_TRUST_SERVER, url));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified <code>DOCKER_TMPDIR</code>
     * environment variable.
     *
     * @param temp  the location for temporary Docker files
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified <code>DOCKER_TMPDIR</code>
     *          environment variable
     */
    public Docker tempFilesAt(File temp)
    {
        return withEnvironmentVariables(EnvironmentVariable.of(ENV_DOCKER_TMPDIR, temp));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified command line options.
     * <p>
     * Docker command line options are placed on the command line before
     * the name of the command to be executed. For example if the command
     * being executed is the <code>build</code> command the command line would
     * be: <code>docker command-options build command-args</code>
     *
     * @param opts  the command line options
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified command line options
     */
    public Docker withCommandOptions(Argument... opts)
    {
        return new Docker(this.daemonAddress,
                          this.dockerExecutable,
                          this.environmentVariables,
                          replaceArgument(this.arguments, opts),
                          this.baseImages);
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified {@link EnvironmentVariable}s.
     * <p>
     * The {@link EnvironmentVariable}s will be applied to any process
     * running Docker commands using this {@link Docker} environment.
     *
     * @param environmentVariables  the {@link EnvironmentVariable}s to use
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified {@link EnvironmentVariable}s
     */
    public Docker withEnvironmentVariables(EnvironmentVariable... environmentVariables)
    {
        return withEnvironmentVariables(Arrays.asList(environmentVariables));
    }


    /**
     * Obtain a copy of this {@link Docker} environment with the
     * addition of the specified {@link EnvironmentVariable}s.
     * <p>
     * The {@link EnvironmentVariable}s will be applied to any process
     * running Docker commands using this {@link Docker} environment.
     *
     * @param environmentVariables  the {@link EnvironmentVariable}s to use
     *
     * @return  a copy of this {@link Docker} environment with the
     *          addition of the specified {@link EnvironmentVariable}s
     */
    public Docker withEnvironmentVariables(List<EnvironmentVariable> environmentVariables)
    {
        return new Docker(this.daemonAddress,
                          this.dockerExecutable,
                          replaceEnvVariable(this.environmentVariables, environmentVariables),
                          this.arguments,
                          this.baseImages);
    }


    /**
     * Obtain a {@link List} of {@link Argument}s that is the same
     * as the specified list with the addition (or replacement) of
     * the specified argument.
     *
     * @param toCopy     the {@link List} of {@link Argument}s to copy
     * @param arguments  the {@link Argument}s to replace
     *
     * @return  the copied {@link List} of {@link Argument}s
     */
    private List<Argument> replaceArgument(List<Argument> toCopy,
                                           Argument...    arguments)
    {
        List<Argument> copy = new ArrayList<>(toCopy);

        for (Argument argument : arguments)
        {
            String name = argument.getName();
            List<Argument> existing =
                toCopy.stream().filter((arg) -> arg.getName().equals(name)).collect(Collectors.toList());

            // remove any arguments with the same name from the copy
            if (!existing.isEmpty())
            {
                copy.removeAll(existing);
            }

            // add the new argument to the copy
            copy.add(argument);
        }

        return copy;
    }


    /**
     * Obtain a {@link List} of {@link EnvironmentVariable}s that is the same
     * as the specified list with the addition (or replacement) of
     * the specified environment variable.
     *
     * @param toCopy                the {@link List} of {@link EnvironmentVariable}s to copy
     * @param environmentVariables  the {@link EnvironmentVariable}s to replace or add
     *
     * @return  the copied {@link List} of {@link EnvironmentVariable}s
     */
    private List<EnvironmentVariable> replaceEnvVariable(List<EnvironmentVariable> toCopy,
                                                         List<EnvironmentVariable> environmentVariables)
    {
        List<EnvironmentVariable> copy = new ArrayList<>(toCopy);

        for (EnvironmentVariable environmentVariable : environmentVariables)
        {
            String name = environmentVariable.getName();
            List<EnvironmentVariable> existing =
                copy.stream().filter((env) -> env.getName().equals(name)).collect(Collectors.toList());

            // remove any environment variables with the same name from the copy
            if (!existing.isEmpty())
            {
                copy.removeAll(existing);
            }

            // add the new environment variable to the copy
            copy.add(environmentVariable);
        }

        return copy;
    }


    /**
     * Try to find a valid local {@link InetAddress} that is
     * routable to the Docker daemon.
     * <p>
     * If the daemon address does not have the tcp:// prefix
     * then it is probably local, i.e. unix:// or fd:// so
     * the address returned will be the address of
     * the {@link LocalPlatform}.
     * <p>
     * For a tcp:// address the host and port are parsed out
     * and a socket opened to the address. The local address
     * of the socket is returned.
     * <p>
     * If any errors occur the address of the
     * {@link LocalPlatform} is returned.
     *
     * @return  an {@link InetAddress} that should be visible from
     *          the daemon
     */
    public InetAddress getValidLocalAddress()
    {
        InetAddress localAddress = LocalPlatform.get().getAddress();

        // We can only work with "tcp://" prefixed hosts,
        // anything else such as unix:// or fd:// is probably
        // local anyway
        if (daemonAddress.startsWith("tcp://"))
        {
            // Split out the host and daemon port
            String[] parts = daemonAddress.substring(6).split(":");

            // Make sure we have two parts otherwise don't bother
            if (parts.length == 2)
            {
                try
                {
                    int               port          = Integer.parseInt(parts[1]);
                    InetSocketAddress socketAddress = new InetSocketAddress(parts[0], port);

                    try (Socket socket = new Socket())
                    {
                        socket.connect(socketAddress);
                        localAddress = socket.getLocalAddress();
                    }
                }
                catch (Exception e)
                {
                    // nothing to do here
                }
            }
        }

        return localAddress;
    }


    public InetAddress getDaemonInetAddress(Platform clientPlatform)
    {
        InetAddress address = clientPlatform.getAddress();

        if (daemonAddress.startsWith("tcp://"))
        {
            // Split out the host and daemon port
            String[] parts = daemonAddress.substring(6).split(":");

            // Make sure we have two parts otherwise don't bother
            if (parts.length == 2)
            {
                try
                {
                    address = InetAddress.getByName(parts[0]);
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();

                    // do nothing, if an error occurs use the client address
                }
            }
        }

        return address;
    }


    /**
     * Configure Docker from the current environment.
     * <p>
     * Using auto configuration assumes that Docker is already
     * configured on the client platform with environment variables
     * pointing to the Docker daemon or the Docker daemon is local
     * to the client platform.
     *
     * @return a new {@link Docker}
     */
    @OptionsByType.Default
    public static Docker auto()
    {
        List<EnvironmentVariable> environmentVariables = new ArrayList<>();
        List<Argument>            arguments            = new ArrayList<>();

        return new Docker(null,
                          DEFAULT_EXECUTABLE,
                          environmentVariables,
                          arguments,
                          DockerDefaultBaseImages.defaultImages());
    }


    /**
     * Configure Docker using the specified Docker Machine machine name.
     * <p>
     * Docker client commands will be executed with the correct settings
     * to connect to the Docker daemon on the specified Docker Machine
     * instance.
     *
     * @param machineName  the name of a Docker Machine machine
     *
     * @return a new {@link Docker}
     */
    public static Docker machine(String machineName)
    {
        return machine(machineName, DockerMachine.local());
    }


    /**
     * Configure Docker using the specified Docker Machine machine name.
     * <p>
     * Docker client commands will be executed with the correct settings
     * to connect to the Docker daemon on the specified Docker Machine
     * instance.
     *
     * @param machineName  the name of a Docker Machine machine
     * @param machine      the {@link DockerMachine} environment
     * @param options      the {@link Option}s
     *
     * @return a new {@link Docker}
     */
    public static Docker machine(String        machineName,
                                 DockerMachine machine,
                                 Option...     options)
    {
        OptionsByType             machineOptions       = OptionsByType.of(options);
        List<EnvironmentVariable> environmentVariables = machine.environmentFor(machineName);
        String dockerHost =
            environmentVariables.stream().filter((envVar) -> envVar.getName().equals(ENV_DOCKER_HOST)).findFirst()
            .map((envVar) -> String.valueOf(envVar.getValue())).orElse(null);

        Docker docker = machineOptions.get(Docker.class);

        return docker.withDaemonAddress(dockerHost).withEnvironmentVariables(environmentVariables)
        .withCommandOptions(Argument.of(ARG_HOST,
                                        '=',
                                        dockerHost));
    }


    /**
     * Manually configure Docker.
     * <p>
     * The Docker client commands will be executed with parameters
     * to connect to the Docker daemon at the specified address.
     *
     * @param address  the address of the Docker daemon
     *
     * @return a new {@link Docker}
     */
    public static Docker daemonAt(String address)
    {
        List<EnvironmentVariable> environmentVariables = new ArrayList<>();
        List<Argument>            arguments            = new ArrayList<>();

        arguments.add(Argument.of(ARG_HOST, '=', address));

        return new Docker(address,
                          DEFAULT_EXECUTABLE,
                          environmentVariables,
                          arguments,
                          DockerDefaultBaseImages.defaultImages());
    }
}
