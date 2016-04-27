/*
 * File: DockerMachine.java
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

package com.oracle.tools.runtime.docker.machine;

import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.lang.StringHelper;
import com.oracle.tools.options.Timeout;
import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.console.CapturingApplicationConsole;
import com.oracle.tools.runtime.console.Console;
import com.oracle.tools.runtime.docker.DockerPlatform;
import com.oracle.tools.runtime.options.Argument;
import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.EnvironmentVariable;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.File;
import java.io.StringReader;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * An encapsulation of a Docker Machine environment.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerMachine
{
    /**
     * The {@link Logger} to use for log messages.
     */
    private static Logger LOGGER = Logger.getLogger(DockerPlatform.class.getName());

    /**
     * The name of the executable to run Docker Machine client commands.
     */
    private final String command = "docker-machine";

    /**
     * The {@link Platform} to use to execute Docker Machine client commands.
     */
    private final Platform clientPlatform;

    /**
     * The Docker Machine command line options to use.
     */
    private final Arguments arguments;


    /**
     * Create a {@link DockerMachine} that uses the specified {@link Platform}
     * to execute client commands.
     *
     * @param clientPlatform  the {@link Platform} to use to execute client commands
     */
    private DockerMachine(Platform clientPlatform, Arguments arguments)
    {
        this.clientPlatform = clientPlatform;
        this.arguments      = arguments;
    }


    /**
     * Obtain the client {@link Platform} used to issue
     * Docker Machine commands.
     *
     * @return  Obtain the client {@link Platform} used to issue
     *          Docker Machine commands
     */
    public Platform getClientPlatform()
    {
        return clientPlatform;
    }


    /**
     * Add the specified Docker Machine command option.
     *
     * @param args  the command line option to add
     *
     * @return  a new {@link DockerMachine} instance that is a copy of this
     *          {@link DockerMachine} with the specified command line option
     *          added
     */
    public DockerMachine withCommandOptions(Argument... args)
    {
        return new DockerMachine(clientPlatform, arguments.with(args));
    }


    /**
     * Enable debug mode.
     * <p>
     * Equates to the <code>--debug</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code>--debug</code> option applied
     */
    public DockerMachine debug()
    {
        return withCommandOptions(Argument.of("--debug"));
    }


    /**
     * Specify the storage path
     * <p>
     * Equates to the <code>--storage-path</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code>--storage-path</code> option applied
     */
    public DockerMachine storagePath(File path)
    {
        return withCommandOptions(Argument.of("--storage-path", '=', path));
    }


    /**
     * Specify the CA to verify remotes against.
     * <p>
     * Equates to the <code>--tls-ca-cert</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code>--tls-ca-cert</code> option applied
     */
    public DockerMachine tlsCACert(Object cert)
    {
        return withCommandOptions(Argument.of("--tls-ca-cert", '=', cert));
    }


    /**
     * Specify the private key to generate certificates.
     * <p>
     * Equates to the <code>--tls-ca-key</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code>--tls-ca-key</code> option applied
     */
    public DockerMachine tlsCAKey(Object key)
    {
        return withCommandOptions(Argument.of("--tls-ca-key", '=', key));
    }


    /**
     * Specify the client cert to use for TLS
     * <p>
     * Equates to the <code>--tls-client-cert</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code--tls-client-cert></code> option applied
     */
    public DockerMachine tlsClientCert(Object cert)
    {
        return withCommandOptions(Argument.of("--tls-client-cert", '=', cert));
    }


    /**
     * Specify the private key used in client TLS auth
     * <p>
     * Equates to the <code>--tls-client-key</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code>--tls-client-key</code> option applied
     */
    public DockerMachine tlsPrivateKey(Object key)
    {
        return withCommandOptions(Argument.of("--tls-client-key", '=', key));
    }


    /**
     * Specify the token to use for requests to the Github API
     * <p>
     * Equates to the <code>--github-api-token</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code>--github-api-token</code> option applied
     */
    public DockerMachine githubToken(String token)
    {
        return withCommandOptions(Argument.of("--github-api-token", '=', token));
    }


    /**
     * Use the native (Go-based) SSH implementation
     * <p>
     * Equates to the <code>--native-ssh</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code>--native-ssh</code> option applied
     */
    public DockerMachine nativeSSH()
    {
        return withCommandOptions(Argument.of("--native-ssh"));
    }


    /**
     * Specify the BugSnag API token for crash reporting
     * <p>
     * Equates to the <code>--bugsnag-api-token</code> command option.
     *
     * @return  a {@link DockerMachine} that is a copy of this instance
     *          with the <code></code> option applied
     */
    public DockerMachine bugSnagToken(String token)
    {
        return withCommandOptions(Argument.of("--bugsnag-api-token", '=', token));
    }


    /**
     * Obtain the {@link InetAddress} of the specified Docker Machine.
     * <p>
     * The equivalent of running the Docker Machine <code>ip</code> command
     * and turning the resulting address into an {@link InetAddress}.
     *
     * @param machineName  the name of the Docker Machine to
     *                     obtain the status for
     *
     * @return  the status of the specified Docker Machine
     */
    public InetAddress getAddress(String machineName)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application application = launch("ip",
                                              Argument.of(machineName),
                                              Console.of(console)))
        {
            if (application.waitFor() != 0)
            {
                String msg = "Error obtaining IP address for docker-machine " + machineName;
                logError(msg, console);
                throw new RuntimeException(msg);
            }

            String address = console.getCapturedOutputLines().poll();

            return InetAddress.getByName(address);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error obtaining IP address for docker-machine", e);
        }
    }


    /**
     * Obtain a {@link DockerMachinePlatform} wrapping a new Docker Machine VM.
     *
     * @param machineName  the name of the Docker Machine instance to create
     * @param options      the {@link Option}s and {@link Argument}s to pass to the create command
     *
     * @return  a {@link DockerMachinePlatform} wrapping a new Docker Machine VM
     */
    public DockerMachinePlatform create(String machineName, Option... options)
    {
        Options createOptions = new Options(options);
        Timeout timeout       = createOptions.getOrDefault(Timeout.class, Timeout.after(5, TimeUnit.MINUTES));

        try (Application create = launch("create", createOptions.asArray()))
        {
            if (create.waitFor(timeout) != 0)
            {
                throw new RuntimeException("Error creating Docker Machine instance");
            }
        }

        return new DockerMachinePlatform(this, machineName, options);
    }


    /**
     * Execute the specified DockerMachine command.
     *
     * @param command  the command to execute
     * @param opts     the {@link Options} to use
     *
     * @return  the {@link Application} executing the command
     */
    public Application launch(String command, Option... opts)
    {
        Options   options   = new Options(opts);
        Arguments arguments = options.get(Arguments.class);

        Arguments launchArgs = this.arguments
                                   .with(Argument.of(command))
                                   .with(arguments);

        options.add(launchArgs);

        return clientPlatform.launch(this.command, options.asArray());
    }


    /**
     * Obtain the status of the specified Docker Machine.
     *
     * @param machineName  the name of the Docker Machine to
     *                     obtain the status for
     *
     * @return  the status of the specified Docker Machine
     */
    public String status(String machineName)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application application = launch("status",
                                              Argument.of(machineName),
                                              Console.of(console)))
        {
            if (application.waitFor() == 0)
            {
                return console.getCapturedOutputLines().poll();
            }
            else
            {
                logError("Error obtaining status for docker machine " + machineName, console);
            }

            return "Error";
        }
    }


    /**
     * Obtain the status of the specified Docker Machine.
     *
     * @param machineName  the name of the Docker Machine to
     *                     obtain the status for
     *
     * @return  a {@link JsonObject} with the Docker Machine information
     */
    public JsonObject inspect(String machineName)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application application = launch("inspect",
                                              Argument.of(machineName),
                                              Console.of(console)))
        {
            if (application.waitFor() != 0)
            {
                String msg = "Error inspecting docker machine " + machineName;
                logError(msg, console);

                throw new RuntimeException(msg);
            }

            String     json   = console.getCapturedOutputLines().stream().collect(Collectors.joining());
            JsonReader reader = Json.createReader(new StringReader(json));

            return (JsonObject) reader.read();
        }
    }


    /**
     * Restart the specified Docker Machine instances.
     *
     * @param machineNames  the name of the Docker Machine
     *                      instance to restart
     *
     * @return  the exit code from the restart command
     */
    public int restart(String... machineNames)
    {
        try (Application application = launch("restart", Arguments.of(Arrays.asList(machineNames))))
        {
            if (application.waitFor() != 0)
            {
                LOGGER.log(Level.SEVERE, "Error restarting docker machines " + Arrays.toString(machineNames));
            }

            return application.exitValue();
        }
    }


    /**
     * Remove the specified Docker Machine instances.
     *
     * @param machineNames  the name of the Docker Machine
     *                      instance to remove
     *
     * @return  the exit code from the remove command
     */
    public int remove(boolean force, String... machineNames)
    {
        Arguments arguments = Arguments.empty();

        if (force)
        {
            arguments = arguments.with(Argument.of("-y"), Argument.of("--force"));
        }

        arguments = arguments.with(Arrays.asList(machineNames));

        try (Application application = launch("rm", arguments))
        {
            if (application.waitFor() != 0)
            {
                LOGGER.log(Level.SEVERE, "Error removing docker machines " + Arrays.toString(machineNames));
            }

            return application.exitValue();
        }
    }


    /**
     * Stop the specified Docker Machine instances.
     *
     * @param machineNames  the name of the Docker Machine
     *                      instance to stop
     *
     * @return  the exit code from the stop command
     */
    public int stop(String... machineNames)
    {
        try (Application application = launch("stop", Arguments.of(Arrays.asList(machineNames))))
        {
            if (application.waitFor(Timeout.after(2, TimeUnit.MINUTES)) != 0)
            {
                LOGGER.log(Level.SEVERE, "Error stopping docker machines " + Arrays.toString(machineNames));
            }

            return application.exitValue();
        }
    }


    /**
     * Kill (abruptly force stop) the specified Docker Machine instances.
     *
     * @param machineNames  the name of the Docker Machine
     *                      instance to kill
     *
     * @return  the exit code from the kill command
     */
    public int kill(String... machineNames)
    {
        try (Application application = launch("kill", Arguments.of(Arrays.asList(machineNames))))
        {
            if (application.waitFor() != 0)
            {
                LOGGER.log(Level.SEVERE, "Error killing docker machines " + Arrays.toString(machineNames));
            }

            return application.exitValue();
        }
    }


    /**
     * Obtain the {@link EnvironmentVariable}s that can be applied to a Docker command
     * to make it execute against the specified Docker Machine.
     *
     * @param machineName  the name of the Docker Machine
     *
     * @return  the {@link EnvironmentVariable}s required to use the Docker Machine
     */
    public List<EnvironmentVariable> environmentFor(String machineName)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application application = launch("env",
                                              Argument.of(machineName),
                                              Console.of(console)))
        {
            if (application.waitFor() == 0)
            {
                return console.getCapturedOutputLines().stream()
                                .filter((line) -> line.startsWith("export"))
                                .map((line) -> line.substring(7))
                                .map((line -> {
                                    int index = line.indexOf('=');
                                    if (index >= 0)
                                    {
                                        String name  = line.substring(0, index);
                                        String value = StringHelper.unquote(line.substring(index + 1));

                                        return EnvironmentVariable.of(name, value);
                                    }

                                    return EnvironmentVariable.of(line);
                                }))
                                .collect(Collectors.toList());
            }

            String msg = "Error obtaining environment for docker-machine " + machineName;

            logError(msg, console);

            throw new RuntimeException(msg);
        }
    }


    private void logError(String msg, CapturingApplicationConsole console)
    {
        LOGGER.log(Level.SEVERE, msg);
        LOGGER.log(Level.SEVERE, Stream.concat(console.getCapturedOutputLines().stream(),
                                               console.getCapturedErrorLines().stream())
                                       .collect(Collectors.joining("\n")));
    }


    /**
     * Create a {@link DockerMachine} using the {@link LocalPlatform}
     * as the platform to execute Docker Machine commands.
     *
     * @return  a {@link DockerMachine} using the {@link LocalPlatform}
     */
    public static DockerMachine local()
    {
        return at(LocalPlatform.get());
    }


    /**
     * Create a {@link DockerMachine} using the specified {@link Platform}
     * as the platform to execute Docker Machine commands.
     *
     * @return  a {@link DockerMachine} using the specified client {@link Platform}
     */
    public static DockerMachine at(Platform platform)
    {
        return new DockerMachine(platform, Arguments.empty());
    }
}
