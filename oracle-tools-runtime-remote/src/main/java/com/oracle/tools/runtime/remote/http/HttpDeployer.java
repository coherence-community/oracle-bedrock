/*
 * File: HttpDeployer.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.LocalPlatform;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.options.PlatformSeparators;

import com.oracle.tools.runtime.remote.DeploymentArtifact;
import com.oracle.tools.runtime.remote.options.Deployer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * A base class for {@link Deployer}s that use HTTP
 * as a file transfer mechanism.
 * <p>
 * Copyright (c) 2015. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public abstract class HttpDeployer implements Deployer
{
    /**
     * Create a new {@link HttpDeployer}.
     */
    public HttpDeployer()
    {
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use wget to retrieve artifacts.
     */
    public static HttpDeployer wget()
    {
        return new WGetHttpDeployer();
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use wget to retrieve artifacts.
     *
     * @param wgetLocation the location of the wget executable
     *                     (for example "/usr/local/bin/wget")
     */
    public static HttpDeployer wgetAt(String wgetLocation)
    {
        return new WGetHttpDeployer(wgetLocation);
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use wget to retrieve artifacts.
     */
    public static HttpDeployer curl()
    {
        return new CurlHttpDeployer();
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use wget to retrieve artifacts.
     *
     * @param curlLocation the location of the curl executable
     *                     (for example "/usr/local/bin/curl")
     */
    public static HttpDeployer curlAt(String curlLocation)
    {
        return new CurlHttpDeployer(curlLocation);
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use PowerShell Invoke-WebRequest to retrieve
     * artifacts.
     */
    public static HttpDeployer powerShell()
    {
        return new PowerShellHttpDeployer();
    }


    @Override
    public void deploy(List<DeploymentArtifact> artifactsToDeploy,
                       String                   remoteDirectory,
                       Platform                 platform,
                       Option...                deploymentOptions)
    {
        if (artifactsToDeploy == null || artifactsToDeploy.isEmpty())
        {
            return;
        }

        Options options = new Options();

        options.addAll(platform.getOptions().asArray());
        options.addAll(deploymentOptions);

        Map<String, DeploymentArtifact> artifactMap = new LinkedHashMap<>();
        ExecutorService                 executor    = createExecutor();
        HttpServer                      server      = null;

        try
        {
            for (DeploymentArtifact artifact : artifactsToDeploy)
            {
                artifactMap.put("/" + UUID.randomUUID().toString(), artifact);
            }

            server = createServer(executor, artifactMap);

            deployAllArtifacts(artifactMap, remoteDirectory, platform, server.getAddress(), options);
        }
        finally
        {
            if (server != null)
            {
                server.stop(0);

            }

            if (executor != null)
            {
                executor.shutdownNow();
            }
        }
    }


    /**
     * Deploy the all of the specified {@link DeploymentArtifact}s.
     *
     * @param artifacts         a {@link Map} of {@link DeploymentArtifact}s to deploy
     *                          keyed by the URL path for each artifact
     * @param remoteDirectory   the remote directory to deploy to
     * @param platform          the remote {@link Platform} to deploy to
     * @param httpServerAddress the {@link InetSocketAddress} the HTTP server is listening on
     * @param options           the {@link Option}s to use
     */
    protected void deployAllArtifacts(Map<String, DeploymentArtifact> artifacts,
                                      String                          remoteDirectory,
                                      Platform                        platform,
                                      InetSocketAddress               httpServerAddress,
                                      Options                         options)
    {
        if (artifacts == null || artifacts.isEmpty())
        {
            return;
        }

        try
        {
            PlatformSeparators separators = options.get(PlatformSeparators.class, PlatformSeparators.autoDetect());
            String             hostName   = httpServerAddress.getAddress().getHostAddress();
            int                port       = httpServerAddress.getPort();

            for (Map.Entry<String, DeploymentArtifact> entry : artifacts.entrySet())
            {
                DeploymentArtifact artifact        = entry.getValue();
                File               sourceFile      = artifact.getSourceFile();
                URL                sourceURL       = new URL("http", hostName, port, entry.getKey());
                File               destinationFile = artifact.getDestinationFile();

                String             destinationParentFolder;
                String             destinationFileName;

                if (destinationFile == null)
                {
                    destinationParentFolder = remoteDirectory;
                    destinationFileName     = sourceFile.getName();
                }
                else
                {
                    destinationParentFolder = separators.asRemotePlatformFileName(destinationFile.getParent());

                    if (destinationParentFolder == null)
                    {
                        destinationParentFolder = remoteDirectory;
                    }

                    destinationFileName = destinationFile.getName();
                }

                String targetFileName = destinationParentFolder + separators.getFileSeparator() + destinationFileName;

                deployArtifact(sourceURL, targetFileName, platform);
            }
        }
        catch (Exception e)
        {
            throw new RuntimeException("Error deploying artifacts", e);
        }
    }


    /**
     * Create the {@link Executor} that will be used by the {@link HttpServer}
     * that this deployer runs.
     * If a specific implementation of {@link HttpDeployer} will perform
     * parallel HTTP requests then it should use an {@link Executor} to provide
     * multiple threads to the {@link HttpServer}. If the deployer only deploys
     * artifacts one at a time then it can return null from this method.
     *
     * @return an {@link Executor} to use for parallel deployments or null
     *         if performing single threaded deployments.
     */
    protected ExecutorService createExecutor()
    {
        return null;
    }

    /**
     * Deploy the specified artifact.
     *
     * @param sourceURL       the HTTP URL to download the artifact from
     * @param targetFileName  the target file name to download the artifact to
     * @param platform        the remote {@link Platform} to download the artifact on
     */
    protected abstract void deployArtifact(URL      sourceURL,
                                           String   targetFileName,
                                           Platform platform);


    /**
     * Start the HTTP server that will be used to serve the artifacts
     * to be deployed.
     *
     * @param executor  the {@link Executor} to pass to the {@link HttpServer}
     * @param artifacts a {@link Map} of {@link DeploymentArtifact}s to deploy
     *                  keyed by the URL path for each artifact
     *
     */
    protected HttpServer createServer(ExecutorService                 executor,
                                      Map<String, DeploymentArtifact> artifacts)
    {
        try
        {
            LocalPlatform platform = LocalPlatform.getInstance();
            InetAddress   address  = InetAddress.getLocalHost();
            int           port     = platform.getAvailablePorts().next();
            HttpServer    server   = HttpServer.create(new InetSocketAddress(address, port), 0);

            server.createContext("/", new ArtifactsHandler(artifacts));
            server.setExecutor(executor);
            server.start();

            return server;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable to create HTTP server", e);
        }
    }


    /**
     * A {@link HttpHandler} implementation that serves a list
     * of {@link DeploymentArtifact}s as files.
     */
    public static class ArtifactsHandler implements HttpHandler
    {
        /**
         * The {@link List} of {@link DeploymentArtifact}s that can
         * be served by the HTTP server
         */
        private final Map<String, DeploymentArtifact> artifacts;


        /**
         * Create a {@link ArtifactsHandler} to serve the specified
         * {@link List} of {@link DeploymentArtifact}s.
         *
         * @param artifacts the {@link DeploymentArtifact}s to serve
         */
        public ArtifactsHandler(Map<String, DeploymentArtifact> artifacts)
        {
            this.artifacts = artifacts;
        }


        @Override
        public void handle(HttpExchange httpExchange) throws IOException
        {
            URI                requestedURI      = httpExchange.getRequestURI();
            String             requestedPath     = requestedURI.toString();
            DeploymentArtifact requestedArtifact = artifacts.get(requestedPath);

            if (requestedArtifact == null)
            {
                httpExchange.sendResponseHeaders(404, 0);

                return;
            }

            URI  sourceURI = requestedArtifact.getSourceFile().toURI();
            Path path      = Paths.get(sourceURI);

            httpExchange.getResponseHeaders().set("Content-length", String.valueOf(Files.size(path)));
            httpExchange.getResponseHeaders().set("Content-type", "application/octet-stream");
            httpExchange.sendResponseHeaders(200, 0);

            byte[] buff = new byte[100000];

            try (InputStream data = sourceURI.toURL().openStream();
                OutputStream os = httpExchange.getResponseBody())
            {
                while (true)
                {
                    int read = data.read(buff, 0, 100000);

                    if (read <= 0)
                    {
                        break;
                    }

                    os.write(buff, 0, read);
                }
            }
        }
    }
}
