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

package com.oracle.tools.runtime.virtual.vagrant;

import com.oracle.tools.deferred.Eventually;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Assembly;
import com.oracle.tools.runtime.Infrastructure;
import com.oracle.tools.runtime.InfrastructureAssemblyBuilder;
import com.oracle.tools.runtime.InfrastructureBuilder;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleAssembly;

import com.oracle.tools.runtime.coherence.CoherenceCacheServer;
import com.oracle.tools.runtime.coherence.CoherenceCacheServerSchema;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;

import com.oracle.tools.runtime.remote.Authentication;
import com.oracle.tools.runtime.remote.RemotePlatform;

import com.oracle.tools.runtime.virtual.HostAddressIterator;

import com.tangosol.util.Resources;

import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import org.junit.rules.TemporaryFolder;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import static org.junit.Assert.assertThat;

import java.io.Closeable;
import java.io.File;

import java.net.InetAddress;
import java.net.URL;

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
     * The {@link Infrastructure} to use in the test methods
     */
    public static Infrastructure<Platform> infrastructure;


    /**
     * Check Vagrant is installed.
     * If not installed then skip all further tests
     * If Vagrant is installed then build the VM infrastructure.
     */
    @BeforeClass
    public static void checkEnvironment() throws Exception
    {
        Assume.assumeTrue(VagrantChecker.vagrantExists());

        File                vmRoot    = temporaryFolder.newFolder();
        HostAddressIterator addresses = new HostAddressIterator("192.168.56.200");

        VagrantPlatformSchema schema = new VagrantPlatformSchema("VM",
                                                                 vmRoot,
                                                                 "rel65-java")
                                                                     .addNetworkAdapter(new VagrantHostOnlyNetworkSchema("eth1",
            addresses));

        InfrastructureBuilder<Platform> builder = new InfrastructureBuilder<Platform>();

        builder.addPlatform(VagrantPlatformBuilder.INSTANCE, schema, 2);

        infrastructure = builder.realize();
    }


    @AfterClass
    public static void closeInfrastructure() throws Exception
    {
        close(infrastructure);
    }


    @Test
    public void shouldRunEverything() throws Exception
    {
        InfrastructureAssemblyBuilder<Platform, Application, Assembly<Application>> assemblyBuilder =
            new InfrastructureAssemblyBuilder<Platform, Application, Assembly<Application>>();

        ApplicationSchema appSchema = new SimpleJavaApplicationSchema(DoStuff.class.getCanonicalName());

        assemblyBuilder.addApplication("Test", appSchema, 1);

        Assembly<Application> assembly = assemblyBuilder.realize(infrastructure, new SystemApplicationConsole());

        for (Application app : assembly)
        {
            try
            {
                app.waitFor();
            }
            catch (RuntimeException e)
            {
                // ignored
            }
        }

        close(assembly);
    }


    @Test
    public void shouldRunCluster() throws Exception
    {
        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().setCacheConfigURI("coherence-cache-config.xml");

        Platform             platform1 = infrastructure.getPlatform("VM-1");
        Platform             platform2 = infrastructure.getPlatform("VM-2");

        CoherenceCacheServer app1      = null;
        CoherenceCacheServer app2      = null;
        CoherenceCacheServer app3      = null;
        CoherenceCacheServer app4      = null;

        try
        {
            schema.setLocalHostAddress(platform1.getPublicInetAddress().getHostName());

            app1 = platform1.realize("Data-1@VM-1", schema, new SystemApplicationConsole());
            app2 = platform1.realize("Data-2@VM-1", schema, new SystemApplicationConsole());

            schema.setLocalHostAddress(platform2.getPublicInetAddress().getHostName());

            app3 = platform2.realize("Data-1@VM-2", schema, new SystemApplicationConsole());
            app4 = platform2.realize("Data-2@VM-2", schema, new SystemApplicationConsole());

            assertThat(app1, is(notNullValue()));
            Eventually.assertThat(invoking(app1).getClusterSize(), is(4));
        }
        finally
        {
            close(app1);
            close(app2);
            close(app3);
            close(app4);
        }
    }


    @Test
    public void shouldRunClusterUsingInfrastructure() throws Exception
    {
        SimpleAssembly<CoherenceCacheServer> assembly = startCluster(infrastructure, 2);

        assertThat(assembly, is(notNullValue()));

        try
        {
            CoherenceCacheServer cacheServer = assembly.get("Data-1@VM-1");

            Eventually.assertThat(invoking(cacheServer).getClusterSize(), is(4));
        }
        finally
        {
            close(assembly);
        }
    }


    @Test
    public void shouldAddLocalPlatformMemberToCluster() throws Exception
    {
        int                                  membersPerVM = 2;
        CoherenceCacheServer                 localMember  = null;
        SimpleAssembly<CoherenceCacheServer> assembly     = startCluster(infrastructure, membersPerVM);

        assertThat(assembly, is(notNullValue()));

        try
        {
            int                  expectedSize = membersPerVM * infrastructure.size();
            CoherenceCacheServer cacheServer  = assembly.get("Data-1@VM-1");

            Eventually.assertThat(invoking(cacheServer).getClusterSize(), is(expectedSize));

            CoherenceCacheServerSchema schema =
                new CoherenceCacheServerSchema().setCacheConfigURI("coherence-cache-config.xml")
                    .setLocalHostAddress("192.168.56.1");

            localMember = LocalPlatform.getInstance().realize("Data-1@Local", schema, new SystemApplicationConsole());
            Eventually.assertThat(invoking(localMember).getClusterSize(), is(expectedSize + 1));
        }
        finally
        {
            close(localMember);
            close(assembly);
        }
    }


    @Test
    public void shouldCreateVagrantPlatformFromFile() throws Exception
    {
        File vmRoot = temporaryFolder.newFolder();
        URL  url    = Resources.findFileOrResource("Single-VM-Vagrantfile.rb", null);
        VagrantFilePlatformSchema schema = new VagrantFilePlatformSchema("VM-3",
                                                                         vmRoot,
                                                                         url).setPublicHostName("192.168.56.210");

        InfrastructureBuilder<Platform> builder = new InfrastructureBuilder<Platform>();

        builder.addPlatform(VagrantPlatformBuilder.INSTANCE, schema);

        Infrastructure<Platform> infra = builder.realize();

        try
        {
            assertThat(infra.size(), is(1));

            Platform platform = infra.getPlatform("VM-3");

            assertThat(platform, is(notNullValue()));

            InetAddress address = platform.getPublicInetAddress();

            assertThat(address.getAddress(), is(InetAddress.getByName("192.168.56.210").getAddress()));
            assertThat(address.isReachable(20000), is(true));
        }
        finally
        {
            close(infra);
        }
    }


//  TODO: re-enable this test when Platforms support "default" options
//     @Test
//     public void shouldBuildInfrastructureFromExistingRemoteHosts() throws Exception
//     {
//         VagrantPlatform vagrant1        = (VagrantPlatform) infrastructure.getPlatform("VM-1");
//         InetAddress     address1        = vagrant1.getPrivateInetAddress();
//         int             port1           = vagrant1.getPort();
//         String          userName1       = vagrant1.getUserName();
//         Authentication  auth1           = vagrant1.getAuthentication();
//
//         VagrantPlatform vagrant2        = (VagrantPlatform) infrastructure.getPlatform("VM-2");
//         InetAddress     address2        = vagrant2.getPrivateInetAddress();
//         int             port2           = vagrant2.getPort();
//         String          userName2       = vagrant2.getUserName();
//         Authentication  auth2           = vagrant2.getAuthentication();
//
//         RemotePlatform  remotePlatform1 = new RemotePlatform("Remote-1", address1, port1, userName1, auth1);
//         RemotePlatform  remotePlatform2 = new RemotePlatform("Remote-2", address2, port2, userName2, auth2);
//
//         remotePlatform1.setPublicAddress(vagrant1.getPublicInetAddress());
//         remotePlatform1.setStrictHostChecking(false);
//         remotePlatform2.setPublicAddress(vagrant2.getPublicInetAddress());
//         remotePlatform2.setStrictHostChecking(false);
//
//         InfrastructureBuilder<Platform> builder = new InfrastructureBuilder<Platform>();
//
//         builder.addPlatform(remotePlatform1);
//         builder.addPlatform(remotePlatform2);
//
//         Infrastructure<Platform> infra = builder.realize();
//
//         assertThat(infra.size(), is(2));
//
//         SimpleAssembly<CoherenceCacheServer> assembly = startCluster(infra, 2);
//
//         assertThat(assembly, is(notNullValue()));
//
//         try
//         {
//             CoherenceCacheServer cacheServer = assembly.get("Data-1@Remote-1");
//
//             assertThat(cacheServer, is(notNullValue()));
//
//             Eventually.assertThat(invoking(cacheServer).getClusterSize(), is(4));
//         }
//         finally
//         {
//             close(assembly);
//         }
//
//     }

    /**
     * Start two Coherence cache server members on each of the {@link Platform}s
     * in the specified {@link Infrastructure}.
     *
     * @param infra        the {@link Infrastructure} to start the storage nodes on
     * @param memberCount  the number of Coherence members to start per {@link Platform}
     *
     * @return a {@link SimpleAssembly} containing the realized storage nodes
     */
    protected SimpleAssembly<CoherenceCacheServer> startCluster(Infrastructure<Platform> infra,
                                                                int                      memberCount)
    {
        CoherenceCacheServerSchema schema =
            new CoherenceCacheServerSchema().setCacheConfigURI("coherence-cache-config.xml");

        InfrastructureAssemblyBuilder<Platform, CoherenceCacheServer, SimpleAssembly<CoherenceCacheServer>> builder =
            new InfrastructureAssemblyBuilder<Platform, CoherenceCacheServer, SimpleAssembly<CoherenceCacheServer>>();

        builder.addApplication("Data", schema, memberCount);

        return builder.realize(infra, new SystemApplicationConsole());
    }


    /**
     * Close a {@link java.io.Closeable} and catch any exception
     *
     * @param closeable  the {@link java.io.Closeable} to close
     */
    public static void close(Closeable closeable)
    {
        if (closeable == null)
        {
            return;
        }

        try
        {
            closeable.close();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
}
