/*
 * File: DockerMachineTest.java
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

package com.oracle.tools.runtime.containers.docker.machine;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.console.CapturingApplicationConsole;
import com.oracle.tools.runtime.console.Console;
import com.oracle.tools.runtime.options.Argument;
import org.junit.Assume;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link DockerMachine}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class DockerMachineTest
{
    @Test
    public void shouldCreateAndRemoveDockerMachineUsingDefaultCloseBehaviour() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        DockerMachine machine = DockerMachine.local();
        String        name    = "oracle-tools-" + System.currentTimeMillis();

        // --- verify the machine name IS NOT in the list of machines -----
        assertThat(listMachines().contains(name), is(false));

        try
        {
            // ----- create a new Docker Machine with the specified name -----
            try (DockerMachinePlatform platform = machine.create(name,
                                                                 Argument.of("-d", "virtualbox"),
                                                                 Argument.of(name)))
            {
                // --- verify the machine name IS in the list of machines -----
                assertThat(listMachines().contains(name), is(true));

                // ----- on exiting the try block the machine should be removed -----
            }

            // --- verify the machine name IS NOT in the list of machines -----
            assertThat(listMachines().contains(name), is(false));
        }
        finally
        {
            // clean up in case the test failed
            killMachine(name);
            removeMachine(name);
        }
    }


    @Test
    public void shouldCreateAndRemoveDockerMachine() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        DockerMachine machine = DockerMachine.local();
        String        name    = "oracle-tools-" + System.currentTimeMillis();

        // --- verify the machine name IS NOT in the list of machines -----
        assertThat(listMachines().contains(name), is(false));

        try
        {
            // ----- create a new Docker Machine with the specified name -----
            try (DockerMachinePlatform platform = machine.create(name,
                                                                 Argument.of("-d", "virtualbox"),
                                                                 Argument.of(name),
                                                                 MachineCloseBehaviour.remove()))
            {
                // --- verify the machine name IS in the list of machines -----
                assertThat(listMachines().contains(name), is(true));

                // ----- on exiting the try block the machine should be removed -----
            }

            // --- verify the machine name IS NOT in the list of machines -----
            assertThat(listMachines().contains(name), is(false));
        }
        finally
        {
            // clean up in case the test failed
            killMachine(name);
            removeMachine(name);
        }
    }


    @Test
    public void shouldCreateAndStopDockerMachine() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        DockerMachine machine = DockerMachine.local();
        String        name    = "oracle-tools-" + System.currentTimeMillis();

        // --- verify the machine name IS NOT in the list of machines -----
        assertThat(listMachines().contains(name), is(false));

        try
        {
            // ----- create a new Docker Machine with the specified name -----
            try (DockerMachinePlatform platform = machine.create(name,
                                                                 Argument.of("-d", "virtualbox"),
                                                                 Argument.of(name),
                                                                 MachineCloseBehaviour.stop()))
            {
                // --- verify the machine name IS in the list of machines -----
                assertThat(listMachines().contains(name), is(true));

                // ----- on exiting the try block the machine should be removed -----
            }

            // --- verify the machine name IS STILL in the list of machines -----
            assertThat(listMachines().contains(name), is(true));

            // --- verify the machine status is Stopped -----
            assertThat(status(name), is("Stopped"));
        }
        finally
        {
            // clean up in case the test failed
            killMachine(name);
            removeMachine(name);
        }
    }


    @Test
    public void shouldCreateAndLeaveDockerMachineRunning() throws Exception
    {
        // ----- skip this test if Docker Machine is not installed -----
        Assume.assumeThat("Test skipped, Docker Machine is not present", hasDockerMachine(), is(true));

        DockerMachine machine = DockerMachine.local();
        String        name    = "oracle-tools-" + System.currentTimeMillis();

        // --- verify the machine name IS NOT in the list of machines -----
        assertThat(listMachines().contains(name), is(false));

        try
        {
            // ----- create a new Docker Machine with the specified name -----
            try (DockerMachinePlatform platform = machine.create(name,
                                                                 Argument.of("-d", "virtualbox"),
                                                                 Argument.of(name),
                                                                 MachineCloseBehaviour.none()))
            {
                // --- verify the machine name IS in the list of machines -----
                assertThat(listMachines().contains(name), is(true));

                // ----- on exiting the try block the machine should be removed -----
            }

            // --- verify the machine name IS STILL in the list of machines -----
            assertThat(listMachines().contains(name), is(true));

            // --- verify the machine status is Stopped -----
            assertThat(status(name), is("Running"));
        }
        finally
        {
            // clean up in case the test failed
            killMachine(name);
            removeMachine(name);
        }
    }


    /**
     * Determine whether Docker Machine is installed.
     *
     * @return  true if Docker Machine is installed locally
     */
    public static boolean hasDockerMachine()
    {
        try (Application application = LocalPlatform.get().launch("docker-machine", Argument.of("--version")))
        {
            return application.waitFor() == 0;
        }
        catch (Exception e)
        {
            return false;
        }
    }


    public static void removeMachine(String name)
    {
        try (Application application = LocalPlatform.get().launch("docker-machine",
                                                                  Argument.of("rm"),
                                                                  Argument.of("--force"),
                                                                  Argument.of(name)))
        {
            application.waitFor();
        }
    }


    public static void killMachine(String name)
    {
        try (Application application = LocalPlatform.get().launch("docker-machine",
                                                                  Argument.of("kill"),
                                                                  Argument.of(name)))
        {
            application.waitFor();
        }
    }


    public static List<String> listMachines()
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application list = LocalPlatform.get().launch("docker-machine",
                                                           Argument.of("ls"),
                                                           Argument.of("--format"),
                                                           Argument.of("{{.Name}}"),
                                                           Console.of(console)))
        {
            assertThat(list.waitFor(), is(0));

            return console.getCapturedOutputLines().stream().filter((line) -> !"(terminated)".equals(line))
            .collect(Collectors.toList());
        }
    }


    public static String status(String name)
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application list = LocalPlatform.get().launch("docker-machine",
                                                           Argument.of("status"),
                                                           Argument.of(name),
                                                           Console.of(console)))
        {
            assertThat(list.waitFor(), is(0));

            return console.getCapturedOutputLines().poll();
        }
    }
}
