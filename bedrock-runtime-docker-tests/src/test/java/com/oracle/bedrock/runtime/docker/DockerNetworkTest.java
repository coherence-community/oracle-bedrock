/*
 * File: DockerNetworkTest.java
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

import applications.SleepingApplication;
import callables.FindNetworks;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.console.Console;
import com.oracle.bedrock.runtime.docker.commands.Network;
import com.oracle.bedrock.runtime.docker.commands.Run;
import com.oracle.bedrock.runtime.docker.machine.DockerMachine;
import com.oracle.bedrock.runtime.docker.machine.DockerMachinePlatform;
import com.oracle.bedrock.runtime.docker.options.ContainerCloseBehaviour;
import com.oracle.bedrock.runtime.java.JavaApplication;
import com.oracle.bedrock.runtime.java.options.ClassName;
import com.oracle.bedrock.runtime.java.options.JavaHome;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.remote.RemotePlatform;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Functional tests for Docker networks
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerNetworkTest extends AbstractFunctionalTest
{
    /**
     * Field description
     */
    @ClassRule
    public static final TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * The name to use for the Java base image.
     */
    private static final String javaImageName = "bedrockjava:1.0";

    /**
     * The {@link DockerMachine} environment to use
     */
    private static DockerMachine machine;

    /**
     * The name of the {@link DockerPlatform}.
     */
    private static String keystorePlatformName;

    /**
     * The {@link DockerPlatform} to use.
     */
    private static DockerMachinePlatform keystorePlatform;

    /**
     * The name of the {@link DockerPlatform}.
     */
    private static String platformOneName;

    /**
     * The {@link DockerPlatform} to use.
     */
    private static DockerMachinePlatform platformOne;

    /**
     * The name of the {@link DockerPlatform}.
     */
    private static String platformTwoName;

    /**
     * The {@link DockerPlatform} to use.
     */
    private static DockerMachinePlatform platformTwo;


    @BeforeClass
    public static void createPlatform() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----

        if (hasDockerMachine())
        {
            // ----- verify that there is a Java installer -----

            findJavaInstaller();

            Docker docker = Docker.auto().withBaseImage(JavaApplication.class, javaImageName);

            keystorePlatformName = "keystore-" + System.currentTimeMillis();
            platformOneName      = "platformOne-" + System.currentTimeMillis();
            platformTwoName      = "platformTwo-" + System.currentTimeMillis();

            machine              = DockerMachine.local();
            keystorePlatform     = createKeystorePlatform(docker, machine, keystorePlatformName);

            String keyStoreAddress = keystorePlatform.getAddress().getHostAddress();

            platformOne = createPlatform(docker, machine, keyStoreAddress, platformOneName);
            platformTwo = createPlatform(docker, machine, keyStoreAddress, platformTwoName);
        }
    }


    @AfterClass
    public static void cleanup()
    {
        // ----- skip this test if Docker Machine is not installed -----

        if (hasDockerMachine())
        {
            killMachine(keystorePlatformName);
            removeMachine(keystorePlatformName);

            killMachine(platformOneName);
            removeMachine(platformOneName);

            killMachine(platformTwoName);
            removeMachine(platformTwoName);
        }
    }


    @Test
    public void shouldCreateAndRemoveNetwork() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        // create an overlay network on platform one
        Application create = platformOne.launch(Network.createOverlay("my-net"));

        // assert that the create command ran
        assertThat(create.waitFor(), is(0));

        // inspect the network on platform one
        JsonValue json1 = Network.inspect(platformOne, platformOne.getDocker(), "my-net");

        // assert that the inspection returned valid json - i.e. the network exists
        assertThat(json1, is(notNullValue()));

        // inspect the network on platform two
        JsonValue json2 = Network.inspect(platformTwo, platformTwo.getDocker(), "my-net");

        // assert that the inspection returned valid json - i.e. the network also exists on platform two
        assertThat(json2, is(notNullValue()));

        // list the networks on platform one and assert that my-net is in the list
        CapturingApplicationConsole console = new CapturingApplicationConsole();
        Application                 list = platformOne.launch(Network.list().filter("name=my-net"),
                                                              Console.of(console));

        assertThat(list.waitFor(), is(0));

        String line =
            console.getCapturedOutputLines().stream().filter((l) -> l.contains("my-net")).findFirst().orElse(null);

        assertThat(line, is(notNullValue()));

        // remove the network from platform two
        Application remove = platformTwo.launch(Network.remove("my-net"));

        assertThat(remove.waitFor(), is(0));

        // inspect the network on platform two
        json2 = Network.inspect(platformTwo, platformTwo.getDocker(), "my-net");

        // assert that the inspection returned valid json - i.e. the network was removed from platform two
        assertThat(json2, is(nullValue()));

        // inspect the network on platform one
        json1 = Network.inspect(platformOne, platformOne.getDocker(), "my-net");

        // assert that the inspection returned valid json - i.e. the network was removed from platform one
        assertThat(json1, is(nullValue()));
    }


    @Test
    public void shouldConnectAndDisconnect() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        // create an overlay network on platform one
        String      networkName = "test-net-1";
        Application create      = platformOne.launch(Network.createOverlay(networkName));

        // assert that the create command ran
        assertThat(create.waitFor(), is(0));

        try (JavaApplication application = platformOne.launch(JavaApplication.class,
                                                              ClassName.of(SleepingApplication.class),
                                                              Argument.of(60)))
        {
            DockerContainer container = application.get(DockerContainer.class);

            assertThat(container, is(notNullValue()));

            Map<String, List<InetAddress>> networksBefore = application.submit(FindNetworks.INSTANCE).get();

            Application connect = platformOne.launch(Network.connect(networkName, container.getName()));

            assertThat(connect.waitFor(), is(0));

            Map<String, List<InetAddress>> networksConnect = application.submit(FindNetworks.INSTANCE).get();

            Application disconnect = platformOne.launch(Network.disconnect(networkName, container.getName()));

            assertThat(disconnect.waitFor(), is(0));

            Map<String, List<InetAddress>> networksAfter = application.submit(FindNetworks.INSTANCE).get();

            System.out.println("======= before =========");
            networksBefore.entrySet().stream().forEach(System.out::println);
            System.out.println("======= connected ======");
            networksConnect.entrySet().stream().forEach(System.out::println);
            System.out.println("======= after ==========");
            networksAfter.entrySet().stream().forEach(System.out::println);
            System.out.println("========================");

            assertThat(networksBefore, is(networksAfter));
            assertThat(networksConnect.size(), is(networksBefore.size() + 1));
        }
    }


    @Test
    public void shouldRunApplicationAndConnectToDefaultNetwork() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        // create an overlay network on platform one
        String      networkName = "test-net-2";
        Application create      = platformOne.launch(Network.createOverlay(networkName));

        // assert that the create command ran
        assertThat(create.waitFor(), is(0));

        // Configure Docker environment to use the network for default connections
        Docker docker = platformOne.getDocker().withDefaultNetwork(networkName);

        try (JavaApplication application = platformOne.launch(JavaApplication.class,
                                                              docker,
                                                              ClassName.of(SleepingApplication.class),
                                                              Argument.of(60)))
        {
            DockerContainer container = application.get(DockerContainer.class);

            assertThat(container, is(notNullValue()));

            // inspect the network on platform one
            JsonArray  json           = (JsonArray) Network.inspect(platformOne, platformOne.getDocker(), networkName);
            JsonObject jsonContainers = json.getJsonObject(0).getJsonObject("Containers");

            // get the names of the containers connected to the network
            List<String> names =
                jsonContainers.values().stream().map((value) -> ((JsonObject) value).getString("Name"))
                .collect(Collectors.toList());

            // assert that the container created is one of the containers connected to the network
            assertThat(names.contains(container.getName()), is(true));
        }
    }


    /**
     * Crete the {@link DockerMachinePlatform} that will run the Consul keystore
     * required by Docker for overlay networks.
     *
     * @param docker   the Docker environment
     * @param machine  the Docker Machine environment
     * @param name     the name of the {@link DockerMachinePlatform} to create
     *
     * @return  a new {@link DockerMachinePlatform} running the Consul container
     */
    public static DockerMachinePlatform createKeystorePlatform(Docker        docker,
                                                               DockerMachine machine,
                                                               String        name)
    {
        // create the platform
        DockerMachinePlatform platform = machine.create(name,
                                                        docker,
                                                        Argument.of("-d", "virtualbox"),
                                                        Argument.of(name));

        // pull the Consul base image
        DockerNetworkTest.pullBaseImage(platform, "progrium/consul");

        // run consul listening on port 8500
        try (Application keystoreContainer = platform.launch(Run.image("progrium/consul",
                                                                       "consul").detached().hostName("consul")
                                                                       .publish("8500:8500"),
                                                             ContainerCloseBehaviour.none(),
                                                             Argument.of("-server"),
                                                             Argument.of("-bootstrap")))
        {
            // wait for the container to be started
            assertThat(keystoreContainer.waitFor(), is(0));
        }

        return platform;
    }


    /**
     * Create a {@link DockerMachinePlatform} to use to run applications on and create
     * networks on.
     * <p>
     * The keystore platform must already have been created.
     *
     * @param docker           the Docker environment
     * @param machine          the Docker Machine environment
     * @param keystoreAddress  the address of the Consul keystore
     * @param name             the name of the {@link DockerMachinePlatform} to create
     *
     * @return  a new {@link DockerMachinePlatform}
     *
     * @throws Exception  if platform creation fails
     */
    public static DockerMachinePlatform createPlatform(Docker        docker,
                                                       DockerMachine machine,
                                                       String        keystoreAddress,
                                                       String        name) throws Exception
    {
        String address = "consul://" + keystoreAddress + ":8500";

        DockerMachinePlatform platform = machine.create(name,
                                                        docker,
                                                        JavaHome.at("/java"),
                                                        Argument.of("-d", "virtualbox"),
                                                        Argument.of("--engine-opt", "cluster-store=" + address),
                                                        Argument.of("--engine-opt", "cluster-advertise=eth1:2376"),
                                                        Argument.of(name));

        pullBaseImage(platform, "oraclelinux:7.1");

        ensureJavaImage(platform, temporaryFolder);

        return platform;
    }


    /**
     * Pull the specified image on the given platform.
     *
     * @param platform   the platform to use to pul the image
     * @param imageName  the image to pull
     */
    public static void pullBaseImage(DockerMachinePlatform platform,
                                     String                imageName)
    {
        RemotePlatform remotePlatform = platform.getRemotePlatform();

        try (Application pull = remotePlatform.launch("docker", Argument.of("pull"), Argument.of(imageName)))
        {
            pull.waitFor(Timeout.after(10, TimeUnit.MINUTES));
        }
    }
}
