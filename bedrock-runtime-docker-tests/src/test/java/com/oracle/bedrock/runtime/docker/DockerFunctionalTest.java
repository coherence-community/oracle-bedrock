/*
 * File: DockerFunctionalTest.java
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

import applications.SocketEchoServer;
import com.oracle.bedrock.Options;
import com.oracle.bedrock.deferred.Eventually;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.console.Console;
import com.oracle.bedrock.runtime.docker.commands.Build;
import com.oracle.bedrock.runtime.docker.commands.Run;
import com.oracle.bedrock.runtime.docker.machine.DockerMachine;
import com.oracle.bedrock.runtime.docker.machine.DockerMachinePlatform;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.JavaHome;
import com.oracle.bedrock.runtime.java.options.SystemProperties;
import com.oracle.bedrock.runtime.java.options.SystemProperty;
import com.oracle.bedrock.runtime.network.AvailablePortIterator;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Ports;
import com.oracle.bedrock.runtime.options.WorkingDirectory;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.json.JsonValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Functional tests for Docker
 * <p>
 * These tests require that Docker Machine is installed. If there is no local
 * install of Docker Machine then the tests will be skipped.
 * <p>
 * Some of these tests also require the ability to build a Java Docker Image.
 * This requires the <code>oracle.bedrock.container.tests.jre</code> System property
 * to be set to point to the file location of a valid Java JRE tar.gz file.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerFunctionalTest extends AbstractFunctionalTest
{
    /**
     * Field description
     */
    @ClassRule
    public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The {@link DockerMachine} environment to use
     */
    private static DockerMachine machine;

    /**
     * The name of the {@link DockerPlatform}.
     */
    private static String platformName;

    /**
     * The {@link DockerPlatform} to use.
     */
    private static DockerMachinePlatform platform;


    @BeforeClass
    public static void createPlatform()
    {
        // ----- skip this test if Docker Machine is not installed -----

        if (hasDockerMachine())
        {
            Docker docker = Docker.auto().withBaseImage(JavaApplication.class, javaImageName);

            platformName = "bedrock-" + System.currentTimeMillis();
            machine      = DockerMachine.local();
            platform = machine.create(platformName,
                                      docker,
                                      JavaHome.at("/java"),
                                      Argument.of("-d", "virtualbox"),
                                      Argument.of(platformName));

            // Pull the oraclelinux:7.1 base image now - it might take a while
            RemotePlatform remotePlatform = platform.getRemotePlatform();

            try (Application pull = remotePlatform.launch("docker",
                                                          Argument.of("pull"),
                                                          Argument.of("oraclelinux:7.1")))
            {
                pull.waitFor(Timeout.after(10, TimeUnit.MINUTES));
            }
        }
    }


    @AfterClass
    public static void cleanup()
    {
        // ----- skip this test if Docker Machine is not installed -----

        if (hasDockerMachine())
        {
            killMachine(platformName);
            removeMachine(platformName);
        }
    }


    @Test
    public void shouldRunApplicationInDocker() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        try (Application application = platform.launch("cat", Argument.of("/etc/hosts")))
        {
            assertThat(application.waitFor(), is(0));

            DockerImage     image     = application.get(DockerImage.class);
            DockerContainer container = application.get(DockerContainer.class);

            assertThat(image, is(notNullValue()));
            assertThat(container, is(notNullValue()));
        }
    }


    @Test
    public void shouldCreateImageAndRunContainer() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        // ----- create a temporary working directory -----

        File workingDirectory   = temporaryFolder.newFolder();
        File dockerFile         = new File(workingDirectory, "Dockerfile");
        Path dockerFileTemplate = Paths.get(getClass().getResource("/TestDockerFile").toURI());

        // ----- copy the Dockerfile template to the working directory -----

        Files.copy(dockerFileTemplate, dockerFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        String      message = "hello world!";
        String      tag     = "testimage:1.0";
        DockerImage image   = null;

        // ----- create a dummy text file that will be added to the image -----

        try (PrintWriter writer = new PrintWriter(new FileWriter(new File(workingDirectory, "test.txt")), true))
        {
            writer.print(message);
        }

        // ----- create the image using the Dockerfile in the working directory -----
        try (Application imageApp = platform.launch(Build.fromDockerFile().withTags(tag),
                                                    WorkingDirectory.at(workingDirectory)))
        {
            assertThat(imageApp.waitFor(), is(0));

            image = imageApp.get(DockerImage.class);

            assertThat(image, is(notNullValue()));
            assertThat(image.getFirstTag(), is(tag));

            JsonValue jsonImage = image.inspect();

            assertThat(jsonImage, is(notNullValue()));

            // ----- create a container from the image -----

            CapturingApplicationConsole console   = new CapturingApplicationConsole();
            DockerContainer             container = null;

            try (Application containerApp = platform.launch(Run.image(image).interactive(),
                                                            Argument.of("cat"),
                                                            Argument.of("test.txt"),
                                                            Console.of(console)))
            {
                containerApp.waitFor();

                console.getCapturedOutputLines().forEach(System.out::println);
                console.getCapturedErrorLines().forEach(System.err::println);

                assertThat(containerApp.exitValue(), is(0));

                container = containerApp.get(DockerContainer.class);

                assertThat(container, is(notNullValue()));

                JsonValue jsonContainer = container.inspect();

                assertThat(jsonContainer, is(notNullValue()));

                // ----- the command run in the container was to cat the dummy text file in the image
                // ----- which should contain the message we wrote to it

                boolean hasMessage =
                    console.getCapturedOutputLines().stream().filter((line) -> line.equals(message)).findFirst()
                    .isPresent();

                assertThat(hasMessage, is(true));
            }

            // ----- the container should have been removed on exiting the try block

            assertThat(container.inspect(), is(nullValue()));
        }

        // ----- the image should have been removed on exiting the try block

        assertThat(image.inspect(), is(nullValue()));
    }


    @Test
    public void shouldRunJavaApplicationThatListensOnSocketAndConnectToNATedPort() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        // ----- ensure that there is a Java image on the Docker Machine -----
        ensureJavaImage(platform, temporaryFolder);

        AvailablePortIterator availablePorts = LocalPlatform.get().getAvailablePorts();

        // ----- start a SocketEchoServer (create an image and run a container) -----
        try (JavaApplication server = platform.launch(JavaApplication.class, ClassName.of(SocketEchoServer.class),

            // --- we use Ports.capture() to capture the Docker port NAT'ing
            SystemProperty.of(SocketEchoServer.PROPERTY_SERVER_PORT, availablePorts, Ports.capture())))
        {
            // ----- assert that the Echo Server is listening -----
            Eventually.assertThat(server, SocketEchoServer.IS_LISTENING, is(true));

            // ----- obtain the server applications Options -----
            Options serverOptions = server.getOptions();

            // ----- obtain the server applications System properties -----
            SystemProperties serverProperties = serverOptions.get(SystemProperties.class);

            // ----- obtain the Ports Option that contains any port mappings -----
            Ports ports = serverOptions.get(Ports.class);

            // ----- use the Ports instance to copy the server properties, changing any port properties to the NAT'ed port
            SystemProperties clientProperties = ports.asMappedProperties(serverProperties);

            // ----- resolve the client System properties -----
            Properties properties = clientProperties.resolve(platform, serverOptions);

            // ----- obtain the port to connect to, this will be the NAT'ed port -----
            int portNumber = Integer.parseInt(properties.getProperty(SocketEchoServer.PROPERTY_SERVER_PORT));

            // ----- the host name to connect to is the host name of the Docker Machine platform
            // ----- as Docker is NAT'ing the port through the host
            String hostName = platform.getAddress().getHostName();

            // ----- connect to the server -----
            try (Socket echoSocket = new Socket(hostName, portNumber);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream())))
            {
                // ----- send a message and assert that it is echoed back -----

                String message = "hello world!";

                out.println(message);

                String response = in.readLine();

                assertThat(response, is(message));
            }
        }
    }
}
