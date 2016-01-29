/*
 * File: LocalCoherenceClusterBuilderTest.java
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

package com.oracle.tools.runtime.coherence;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.concurrent.runnable.RuntimeExit;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.ClassPath;
import com.oracle.tools.runtime.java.LocalJavaApplicationBuilder;
import com.oracle.tools.runtime.java.options.JavaAgent;

import com.oracle.tools.runtime.network.AvailablePortIterator;

import com.oracle.tools.util.Capture;

import org.jacoco.agent.rt.RT;

import org.junit.Assert;
import org.junit.Test;

import static com.oracle.tools.deferred.DeferredHelper.invoking;

import static com.oracle.tools.deferred.Eventually.assertThat;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;

import java.util.HashSet;

/**
 * Functional Tests for {@link CoherenceClusterBuilder}
 * using a {@link LocalJavaApplicationBuilder}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class LocalCoherenceClusterBuilderTest extends AbstractCoherenceClusterBuilderTest
{
    @Override
    public Platform getPlatform()
    {
        return LocalPlatform.getInstance();
    }


    /**
     * Ensure we can build and close a {@link com.oracle.tools.runtime.coherence.CoherenceCluster}
     * of storage enabled members with a proxy server when using code-coverage tools.
     */
    @Test
    public void shouldEstablishStorageAndProxyClusterWithCodeCoverage() throws Exception
    {
        // determine the classpath of the JaCoCo runtime agent jar (should be something like jacocoagent-x.y.z.jar)
        ClassPath jacocoPath = ClassPath.ofClass(RT.class);

        // define a temp file name pattern for JaCoCo code coverage reports
        String jacocoDestinationFileName = "jacoco-${oracletools.runtime.id}.exec";
        File   destinationFile           = new File(System.getProperty("java.io.tmpdir"), jacocoDestinationFileName);

        // define the JavaAgent for JaCoCo
        JavaAgent javaAgent = JavaAgent.using(jacocoPath.toString(),
                                              "destfile=" + destinationFile
                                              + ",output=file,sessionid=${oracletools.runtime.id},dumponexit=true");

        AvailablePortIterator availablePorts = LocalPlatform.getInstance().getAvailablePorts();
        Capture<Integer>      clusterPort    = new Capture<Integer>(availablePorts);

        CoherenceCacheServerSchema storageSchema =
            new CoherenceCacheServerSchema().setClusterPort(clusterPort).setStorageEnabled(true)
            .setCacheConfigURI("test-cache-config.xml").useLocalHostMode().setClusterName("Storage-Proxy")
            .addOption(javaAgent).addOption(RuntimeExit.withExitCode(0));

        CoherenceCacheServerSchema extendSchema =
            new CoherenceCacheServerSchema().setStorageEnabled(false).setClusterPort(clusterPort)
            .setClusterName("Storage-Proxy").setCacheConfigURI("test-extend-proxy-config.xml")
            .setSystemProperty("coherence.extend.port",
                               availablePorts).useLocalHostMode().addOption(javaAgent)
                               .addOption(RuntimeExit.withExitCode(0));

        CoherenceClusterBuilder builder = new CoherenceClusterBuilder();

        builder.addSchema("storage", storageSchema, 2, getPlatform());
        builder.addSchema("extend", extendSchema, 1, getPlatform());

        try (CoherenceCluster cluster = builder.realize(new SystemApplicationConsole()))
        {
            // ensure the cluster size is as expected
            assertThat(invoking(cluster).getClusterSize(), is(3));

            // ensure the member id's are different
            HashSet<Integer> memberIds = new HashSet<Integer>();

            for (CoherenceClusterMember member : cluster)
            {
                memberIds.add(member.getLocalMemberId());
            }

            Assert.assertEquals(3, memberIds.size());

            CoherenceClusterMember extendMember = cluster.get("extend-1");

            assertThat(invoking(extendMember).isServiceRunning("ExtendTcpProxyService"), is(true));

            for (CoherenceClusterMember storageMember : cluster.getAll("storage"))
            {
                assertThat(invoking(storageMember).isServiceRunning("ExtendTcpProxyService"), is(false));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
