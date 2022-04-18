/*
 * File: PowerShellHttpDeployerTest.java
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

package com.oracle.bedrock.runtime.remote.windows.http;

import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.remote.DeployedArtifacts;
import com.oracle.bedrock.runtime.remote.DeploymentArtifact;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.runtime.remote.windows.winrm.AbstractWindowsTest;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Functional tests for the {@link PowerShellHttpDeployer}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class PowerShellHttpDeployerTest extends AbstractWindowsHttpDeployerTest
{
    @Test
    public void shouldFindInOptions() throws Exception
    {
        Deployer      deployer      = new PowerShellHttpDeployer();
        OptionsByType optionsByType = OptionsByType.of(deployer);

        assertThat(optionsByType.get(Deployer.class), is(sameInstance(deployer)));
    }


    @Test
    public void shouldDeployEmptyArtifacts() throws Exception
    {
        Map<String, DeploymentArtifact> artifacts         = new HashMap<>();
        String                          destination       = "/foo";
        Platform                        platform          = mock(Platform.class);
        InetSocketAddress               address           = new InetSocketAddress(InetAddress.getLocalHost(), 1234);
        OptionsByType                   optionsByType     = OptionsByType.empty();

        PowerShellHttpDeployer          deploymentMethod  = new PowerShellHttpDeployer();
        DeployedArtifacts               deployedArtifacts = new DeployedArtifacts();

        deploymentMethod.deployAllArtifacts(artifacts,
                                            destination,
                                            platform,
                                            address,
                                            optionsByType,
                                            deployedArtifacts);

        verifyNoMoreInteractions(platform);
    }


    @Test
    public void shouldDeployNullArtifacts() throws Exception
    {
        String                 destination       = "/foo";
        Platform               platform          = mock(Platform.class);
        InetSocketAddress      address           = new InetSocketAddress(InetAddress.getLocalHost(), 1234);
        OptionsByType          optionsByType     = OptionsByType.empty();

        PowerShellHttpDeployer deploymentMethod  = new PowerShellHttpDeployer();
        DeployedArtifacts      deployedArtifacts = new DeployedArtifacts();

        deploymentMethod.deployAllArtifacts(null, destination, platform, address, optionsByType, deployedArtifacts);
    }


//  TODO: repair these if possible
//
//      @Test
//      public void shouldDeployArtifactWithDestination() throws Exception
//      {
//          Platform                        platform    = mock(Platform.class);
//          int                             port        = 1234;
//          String                          hostName    = InetAddress.getLocalHost().getCanonicalHostName();
//          InetSocketAddress               address     = new InetSocketAddress(hostName, port);
//          Options                         options     = new Options();
//          SimpleApplication               application = mock(SimpleApplication.class, "1");
//
//          File                            source      = new File("/test1/test2/source-1.txt");
//          String                          urlPath     = URLEncoder.encode(source.getCanonicalPath(), "UTF-8");
//          URL                             url         = new URL("http", hostName, port, urlPath);
//          File                            target      = new File("/dest/destination-1.txt");
//          DeploymentArtifact              artifact    = new DeploymentArtifact(source, target);
//
//          Map<String, DeploymentArtifact> artifacts   = Collections.singletonMap(urlPath, artifact);
//
//          when(application.waitFor()).thenReturn(0);
//          when(application.exitValue()).thenReturn(0);
//          when(platform.realize(anyString(), any(ApplicationSchema.class),
//                                any(ApplicationConsole.class))).thenReturn(application);
//
//          PowerShellHttpDeployer deploymentMethod = new PowerShellHttpDeployer();
//
//          deploymentMethod.deployAllArtifacts(artifacts, "/foo", platform, address, options);
//
//          ArgumentCaptor<SimpleApplicationSchema> schemaCaptor = ArgumentCaptor.forClass(SimpleApplicationSchema.class);
//
//          verify(platform, times(1)).realize(anyString(), schemaCaptor.capture(), any(ApplicationConsole.class));
//
//          SimpleApplicationSchema schema = schemaCaptor.getValue();
//
//          assertThat(schema.getExecutableName(), is("powershell"));
//
//          List<String> argList = schema.getOptions().get(Arguments.class).realize(platform, schema);
//          assertThat(argList.get(0), is("-Command"));
//          assertThat(argList.get(1), is("Invoke-WebRequest"));
//          assertThat(argList.get(2), is("-Uri"));
//          assertThat(argList.get(3), is(url.toExternalForm()));
//          assertThat(argList.get(4), is("-OutFile"));
//          assertThat(argList.get(5), is(target.toString()));
//      }
//
//
//      @Test
//      public void shouldDeployArtifactWithoutDestination() throws Exception
//      {
//          String                          destination = "/foo";
//          Platform                        platform    = mock(Platform.class);
//          int                             port        = 1234;
//          String                          hostName    = InetAddress.getLocalHost().getCanonicalHostName();
//          InetSocketAddress               address     = new InetSocketAddress(hostName, port);
//          Options                         options     = new Options();
//          SimpleApplication               application = mock(SimpleApplication.class, "1");
//
//          File                            source      = new File("/test1/test2/source-2.txt");
//          String                          urlPath     = URLEncoder.encode(source.getCanonicalPath(), "UTF-8");
//          URL                             url         = new URL("http", hostName, port, urlPath);
//          DeploymentArtifact              artifact    = new DeploymentArtifact(source);
//
//          Map<String, DeploymentArtifact> artifacts   = Collections.singletonMap(urlPath, artifact);
//
//          when(application.waitFor()).thenReturn(0);
//          when(application.exitValue()).thenReturn(0);
//          when(platform.realize(anyString(), any(ApplicationSchema.class),
//                                any(ApplicationConsole.class))).thenReturn(application);
//
//          PowerShellHttpDeployer deploymentMethod = new PowerShellHttpDeployer();
//
//          deploymentMethod.deployAllArtifacts(artifacts, destination, platform, address, options);
//
//          ArgumentCaptor<SimpleApplicationSchema> schemaCaptor = ArgumentCaptor.forClass(SimpleApplicationSchema.class);
//
//          verify(platform, times(1)).realize(anyString(), schemaCaptor.capture(), any(ApplicationConsole.class));
//
//          SimpleApplicationSchema schema = schemaCaptor.getValue();
//
//          assertThat(schema.getExecutableName(), is("powershell"));
//
//          List<String> argList = schema.getOptions().get(Arguments.class).realize(platform, schema);
//          assertThat(argList.get(0), is("-Command"));
//          assertThat(argList.get(1), is("Invoke-WebRequest"));
//          assertThat(argList.get(2), is("-Uri"));
//          assertThat(argList.get(3), is(url.toExternalForm()));
//          assertThat(argList.get(4), is("-OutFile"));
//          assertThat(argList.get(5), is(destination + File.separator + source.getName()));
//      }

    @Test
    public void shouldRunPowerShell() throws Exception
    {
        Assume.assumeThat("Test ignored as PowerShell does not exist or is not the required version",
                          AbstractWindowsTest.getPowershellVersion(LocalPlatform.get()),
                          is(greaterThanOrEqualTo(3.0)));

        File                     tempDir           = temporaryFolder.newFolder();
        List<DeploymentArtifact> artifactsToDeploy = createArtifactList(2);

        PowerShellHttpDeployer   deploymentMethod  = new PowerShellHttpDeployer();

        deploymentMethod.deploy(artifactsToDeploy, tempDir.getCanonicalPath(), LocalPlatform.get());

        SortedSet<File> sourceFiles   = new TreeSet<>();
        SortedSet<File> deployedFiles = new TreeSet<>();

        for (DeploymentArtifact artifact : artifactsToDeploy)
        {
            sourceFiles.add(artifact.getSourceFile());
        }

        Collections.addAll(deployedFiles, tempDir.listFiles());

        assertThat(deployedFiles.size(), is(sourceFiles.size()));

        Iterator<File> itSource   = sourceFiles.iterator();
        Iterator<File> itDeployed = deployedFiles.iterator();

        while (itSource.hasNext())
        {
            File source   = itSource.next();
            File deployed = itDeployed.next();

            assertThat(getMD5(deployed), is(getMD5(source)));
        }
    }
}
