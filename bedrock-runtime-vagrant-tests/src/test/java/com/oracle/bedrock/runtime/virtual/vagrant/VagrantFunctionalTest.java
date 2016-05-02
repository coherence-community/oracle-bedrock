/*
 * File: VagrantFunctionalTest.java
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

package com.oracle.bedrock.runtime.virtual.vagrant;

import com.oracle.bedrock.runtime.options.WorkingDirectory;
import com.oracle.bedrock.runtime.remote.options.HostName;
import com.oracle.bedrock.runtime.virtual.vagrant.options.BoxName;
import com.oracle.bedrock.runtime.virtual.vagrant.options.HostOnlyNetwork;
import com.oracle.bedrock.runtime.virtual.vagrant.options.UpdateBox;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Functional tests for the Vagrant platform.
 *
 * NOTE: To run these tests Vagrant must be installed on the machine
 * running these tests. If Vagrant is not installed the tests will be skipped.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class VagrantFunctionalTest
{
    /**
     * A JUnit rule to create temporary folders for use in tests
     */
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    /**
     * Check Vagrant is installed.
     * If not installed then skip all further tests
     * If Vagrant is installed then build the VM infrastructure.
     */
    @BeforeClass
    public static void checkEnvironment() throws Exception
    {
        String boxName = System.getProperty("oracle.bedrock.test.vagrant.box", "oracle/java8");

        Assume.assumeThat("Vagrant does not exist, skipping tests", VagrantChecker.vagrantExists(), is(true));
        Assume.assumeThat("Vagrant box " + boxName + " does not exist, skipping tests",
                          VagrantChecker.vagrantExistsWithBox(boxName),
                          is(true));
    }


    @Test
    public void shouldCreateVagrantPlatformFromOptions() throws Exception
    {
        File               vmRoot  = temporaryFolder.newFolder();
        VagrantFileBuilder builder = VagrantFileBuilder.from(BoxName.of("oracle/java8"),
                                                             UpdateBox.no(),
                                                             HostOnlyNetwork.of("192.168.56.211"));

        try (VagrantPlatform platform = new VagrantPlatform("VM-1",
                                                            builder,
                                                            WorkingDirectory.at(vmRoot)))
        {
            InetAddress address = platform.getAddress();

            assertThat(address.getAddress(), is(InetAddress.getByName("192.168.56.211").getAddress()));
            assertThat(address.isReachable(20000), is(true));
        }
    }


    @Test
    public void shouldCreateMultipleVagrantPlatformsFromOptions() throws Exception
    {
        File               vmRoot  = temporaryFolder.newFolder();
        VagrantFileBuilder builder = VagrantFileBuilder.from(BoxName.of("oracle/java8"),
                                                             UpdateBox.no(),
                                                             HostOnlyNetwork.startingAt("192.168.56.220"));

        try (VagrantPlatform platform1 = new VagrantPlatform("VM-2", builder, WorkingDirectory.subDirectoryOf(vmRoot));
             VagrantPlatform platform2 = new VagrantPlatform("VM-3", builder, WorkingDirectory.subDirectoryOf(vmRoot)))
        {
            HostName hostName1 = platform1.getOptions().get(HostName.class);
            HostName hostName2 = platform2.getOptions().get(HostName.class);

            assertThat(hostName1, is(notNullValue()));
            assertThat(hostName2, is(notNullValue()));

            assertThat(hostName1.get(), is(not(hostName2.get())));

            InetAddress address1 = platform1.getAddress();

            assertThat(address1.getAddress(), is(InetAddress.getByName(hostName1.get()).getAddress()));
            assertThat(address1.isReachable(20000), is(true));

            InetAddress address2 = platform2.getAddress();

            assertThat(address2.getHostAddress(), is(InetAddress.getByName(hostName2.get()).getHostAddress()));
            assertThat(address2.isReachable(20000), is(true));
        }
    }


    @Test
    public void shouldCreateVagrantPlatformFromFile() throws Exception
    {
        File vmRoot = temporaryFolder.newFolder();
        URL  url    = getClass().getResource("/Single-VM-Vagrantfile.rb");

        assertThat(url, is(notNullValue()));

        VagrantFileBuilder builder = VagrantFileBuilder.from(url);

        try (VagrantPlatform platform = new VagrantPlatform("VM-4",
                                                            builder,
                                                            WorkingDirectory.at(vmRoot),
                                                            HostName.of("192.168.56.210")))
        {
            InetAddress address = platform.getAddress();

            assertThat(address.getAddress(), is(InetAddress.getByName("192.168.56.210").getAddress()));
            assertThat(address.isReachable(20000), is(true));
        }
    }
}
