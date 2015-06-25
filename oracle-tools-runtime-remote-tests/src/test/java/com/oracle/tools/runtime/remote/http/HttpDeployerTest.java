/*
 * File: HttpDeployerTest.java
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

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.remote.DeploymentArtifact;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Functional tests for {@link HttpDeployer}.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HttpDeployerTest extends AbstractHttpDeployerTest
{
    @Test
    public void shouldHandleRequestForExistingArtifact() throws Exception
    {
        Map<String,DeploymentArtifact>            artifacts = createArtifactMap(1);
        Map.Entry<String,DeploymentArtifact>      entry     = artifacts.entrySet().iterator().next();
        String                                    urlPath   = entry.getKey();
        DeploymentArtifact                        artifact  = entry.getValue();
        HttpExchange                              exchange  = mock(HttpExchange.class);
        Headers                                   headers   = new Headers();
        File                                      file      = temporaryFolder.newFile();
        URI                                       uri       = new URI(urlPath);
        long                                      size      = artifact.getSourceFile().length();

        when(exchange.getRequestURI()).thenReturn(uri);
        when(exchange.getResponseHeaders()).thenReturn(headers);
        when(exchange.getResponseBody()).thenReturn(new FileOutputStream(file));

        HttpDeployer.ArtifactsHandler handler = new HttpDeployer.ArtifactsHandler(artifacts);
        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(200, 0);

        assertThat(headers.getFirst("Content-type"), is("application/octet-stream"));
        assertThat(headers.getFirst("Content-length"), is(String.valueOf(size)));
        assertThat(getMD5(file), is(getMD5(artifact.getSourceFile())));
    }

    @Test
    public void shouldHandleRequestForNonExistentArtifact() throws Exception
    {
        Map<String,DeploymentArtifact>            artifacts = createArtifactMap(1);
        HttpExchange                              exchange  = mock(HttpExchange.class);
        HttpDeployer.ArtifactsHandler             handler   = new HttpDeployer.ArtifactsHandler(artifacts);
        URI                                       uri       = new File("/foo.txt").toURI();

        when(exchange.getRequestURI()).thenReturn(uri);

        handler.handle(exchange);

        verify(exchange).sendResponseHeaders(404, 0);
    }

    @Test
    public void shouldDeployArtifacts() throws Exception
    {
        final File                     tempDir           = temporaryFolder.newFolder();
        final List<File>               artifactsDeployed = new ArrayList<>();
        final List<DeploymentArtifact> artifactsToDeploy = createArtifactList(5);

        HttpDeployer http = new HttpDeployer()
        {
            @Override
            protected void deployArtifact(URL sourceURL, String targetFileName, Platform platform)
            {
                pullArtifacts(sourceURL, targetFileName, artifactsDeployed);
            }
        };

        http.deploy(artifactsToDeploy, tempDir.getCanonicalPath(), LocalPlatform.getInstance());

        assertThat(artifactsDeployed.size(), is(artifactsToDeploy.size()));

        for (int i = 0; i < artifactsToDeploy.size(); i++)
        {
            DeploymentArtifact artifact = artifactsToDeploy.get(i);
            File               deployed = artifactsDeployed.get(i);
            File               source   = artifact.getSourceFile();

            assertThat("MD5 mismatch for " + source, getMD5(source), is(getMD5(deployed)));
        }
    }

    /**
     * Get the specified list of artifacts from the HTTP server
     * at the specified address.
     *
     * @param sourceURL         the URL to download the artifact from
     * @param targetFileName    the target file to save the artifact to
     * @param artifactsDeployed an array to store the retrieved {@link File}s in
     */
    public void pullArtifacts(URL sourceURL, String targetFileName, List<File> artifactsDeployed)
    {
        int    size   = 100000;
        byte[] buffer = new byte[size];

        try
        {
            HttpURLConnection urlConnection = (HttpURLConnection) sourceURL.openConnection();

            assertThat("Expected response code 200 for artifact " + sourceURL.toExternalForm(),
                       urlConnection.getResponseCode(),
                       is(200));

            File destination = new File(targetFileName);

            try (InputStream data = urlConnection.getInputStream();
                OutputStream os = new FileOutputStream(destination))
            {
                while (true)
                {
                    int read = data.read(buffer, 0, size);

                    if (read <= 0)
                    {
                        break;
                    }

                    os.write(buffer, 0, read);
                }
            }

            artifactsDeployed.add(destination);
        }
        catch (IOException e)
        {
            fail("Error getting " + sourceURL.toExternalForm() + " " + e.getMessage());
        }
    }

}
