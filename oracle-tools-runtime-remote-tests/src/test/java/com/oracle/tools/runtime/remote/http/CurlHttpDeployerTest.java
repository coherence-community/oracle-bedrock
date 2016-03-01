/*
 * File: CurlHttpDeployerTest.java
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

package com.oracle.tools.runtime.remote.http;

import com.oracle.tools.Options;

import com.oracle.tools.runtime.ApplicationConsole;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.SimpleApplication;
import com.oracle.tools.runtime.SimpleApplicationSchema;

import com.oracle.tools.runtime.console.SystemApplicationConsole;

import com.oracle.tools.runtime.java.SimpleJavaApplication;
import com.oracle.tools.runtime.java.SimpleJavaApplicationSchema;

import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.remote.DeploymentArtifact;
import com.oracle.tools.runtime.remote.RemotePlatform;
import com.oracle.tools.runtime.remote.java.applications.SleepingApplication;
import com.oracle.tools.runtime.remote.options.Deployer;

import org.junit.Assume;
import org.junit.Test;

import org.mockito.ArgumentCaptor;

import java.io.File;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Functional tests for the {@link CurlHttpDeployer}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class CurlHttpDeployerTest extends AbstractHttpDeployerTest
{
    @Test
    public void shouldFindInOptions() throws Exception
    {
        Deployer deployer = new CurlHttpDeployer();
        Options  options  = new Options(deployer);

        assertThat(options.get(Deployer.class), is(sameInstance(deployer)));
    }

    @Test
    public void shouldDeployEmptyArtifacts() throws Exception
    {
        Map<String,DeploymentArtifact> artifacts   = new HashMap<>();
        String                         destination = "/foo";
        Platform                       platform    = mock(Platform.class);
        InetSocketAddress              address     = new InetSocketAddress(InetAddress.getLocalHost(), 1234);
        Options                        options     = new Options();

        CurlHttpDeployer deploymentMethod = new CurlHttpDeployer();

        deploymentMethod.deployAllArtifacts(artifacts, destination, platform, address, options);

        verifyNoMoreInteractions(platform);
    }

    @Test
    public void shouldDeployNullArtifacts() throws Exception
    {
        String            destination = "/foo";
        Platform          platform    = mock(Platform.class);
        InetSocketAddress address     = new InetSocketAddress(InetAddress.getLocalHost(), 1234);
        Options           options     = new Options();

        CurlHttpDeployer deploymentMethod = new CurlHttpDeployer();

        deploymentMethod.deployAllArtifacts(null, destination, platform, address, options);
    }

    @Test
    public void shouldDeployArtifactWithDestination() throws Exception
    {
        Platform                       platform    = mock(Platform.class);
        int                            port        = 1234;
        String                         hostName    = InetAddress.getLocalHost().getCanonicalHostName();
        InetSocketAddress              address     = new InetSocketAddress(hostName, port);
        Options                        options     = new Options();
        SimpleApplication              application = mock(SimpleApplication.class, "1");

        File                           source      = new File("/test1/test2/source-1.txt");
        String                         urlPath     = URLEncoder.encode(source.getCanonicalPath(), "UTF-8");
        URL                            url         = new URL("http", hostName, port, urlPath);
        File                           target      = new File("/dest/destination-1.txt");
        DeploymentArtifact             artifact    = new DeploymentArtifact(source, target);

        Map<String,DeploymentArtifact> artifacts   = Collections.singletonMap(urlPath, artifact);

        when(application.waitFor()).thenReturn(0);
        when(application.exitValue()).thenReturn(0);
        when(platform.realize(anyString(), any(ApplicationSchema.class), any(ApplicationConsole.class)))
                .thenReturn(application);

        CurlHttpDeployer deploymentMethod = new CurlHttpDeployer();

        deploymentMethod.deployAllArtifacts(artifacts, "/foo", platform, address, options);

        ArgumentCaptor<SimpleApplicationSchema> schemaCaptor = ArgumentCaptor.forClass(SimpleApplicationSchema.class);
        verify(platform, times(1)).realize(anyString(), schemaCaptor.capture(), any(ApplicationConsole.class));

        SimpleApplicationSchema schema1 = schemaCaptor.getValue();

        assertThat(schema1.getExecutableName(),   is("curl"));

        List<String> argList = schema1.getOptions().get(Arguments.class).realize(platform, schema1);
        assertThat(argList.get(0), is(url.toExternalForm()));
        assertThat(argList.get(1), is("--create-dirs"));
        assertThat(argList.get(2), is("-o"));
        assertThat(argList.get(3), is(target.getCanonicalPath()));
    }

    @Test
    public void shouldDeployArtifactWithoutDestination() throws Exception
    {
        String                         destination = "/foo";
        Platform                       platform    = mock(Platform.class);
        int                            port        = 1234;
        String                         hostName    = InetAddress.getLocalHost().getCanonicalHostName();
        InetSocketAddress              address     = new InetSocketAddress(hostName, port);
        Options                        options     = new Options();
        SimpleApplication              application = mock(SimpleApplication.class, "1");

        File                           source      = new File("/test1/test2/source-2.txt");
        String                         urlPath     = URLEncoder.encode(source.getCanonicalPath(), "UTF-8");
        URL                            url         = new URL("http", hostName, port, urlPath);
        DeploymentArtifact             artifact    = new DeploymentArtifact(source);

        Map<String,DeploymentArtifact> artifacts   = Collections.singletonMap(urlPath, artifact);

        when(application.waitFor()).thenReturn(0);
        when(application.exitValue()).thenReturn(0);
        when(platform.realize(anyString(), any(ApplicationSchema.class), any(ApplicationConsole.class)))
                .thenReturn(application);

        CurlHttpDeployer deploymentMethod = new CurlHttpDeployer();

        deploymentMethod.deployAllArtifacts(artifacts, destination, platform, address, options);

        ArgumentCaptor<SimpleApplicationSchema> schemaCaptor = ArgumentCaptor.forClass(SimpleApplicationSchema.class);
        verify(platform, times(1)).realize(anyString(), schemaCaptor.capture(), any(ApplicationConsole.class));

        SimpleApplicationSchema schema2 = schemaCaptor.getValue();

        assertThat(schema2.getExecutableName(), is("curl"));

        List<String> argList = schema2.getOptions().get(Arguments.class).realize(platform, schema2);
        assertThat(argList.get(0), is(url.toExternalForm()));
        assertThat(argList.get(1), is("--create-dirs"));
        assertThat(argList.get(2), is("-o"));
        assertThat(argList.get(3), is(destination + "/" + source.getName()));
    }

    @Test
    public void shouldRunCurl() throws Exception
    {
        String curl = findCurl();
        Assume.assumeThat("Test ignored as curl does not exist", curl, is(notNullValue()));

        File                     tempDir           = temporaryFolder.newFolder();
        List<DeploymentArtifact> artifactsToDeploy = createArtifactList(2);

        CurlHttpDeployer deploymentMethod = new CurlHttpDeployer(curl);

        deploymentMethod.deploy(artifactsToDeploy, tempDir.getCanonicalPath(), LocalPlatform.getInstance());

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

        while(itSource.hasNext())
        {
            File source   = itSource.next();
            File deployed = itDeployed.next();

            assertThat(getMD5(deployed), is(getMD5(source)));
        }
    }

    @Test
    public void shouldRunApplicationUsingDeployer() throws Exception
    {
        String curl = findCurl();
        Assume.assumeThat("Test ignored as curl does not exist", curl, is(notNullValue()));

        SimpleJavaApplicationSchema schema = new SimpleJavaApplicationSchema(SleepingApplication.class.getName())
                                                .addArgument("3")
                                                .addOption(HttpDeployer.curlAt(curl));

        RemotePlatform platform = getRemotePlatform();

        try (SimpleJavaApplication application = platform.realize("Java", schema, new SystemApplicationConsole()))
        {
            assertThat(application.waitFor(), is(0));

            application.close();

            assertThat(application.exitValue(), is(0));
        }
    }


    /**
     * Determine where the Curl executable exists.
     *
     * @return the location of curl or null if it is not found
     *         on the system
     */
    public String findCurl()
    {
        String[] locations = {"/usr/bin/curl", "/usr/local/bin/curl"};

        for (String location : locations)
        {
            if (new File(location).exists())
            {
                return location;
            }
        }

        return null;
    }
}
