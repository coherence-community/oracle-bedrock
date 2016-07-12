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

package com.oracle.bedrock.runtime.remote.http;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.io.NetworkHelper;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.options.PlatformSeparators;
import com.oracle.bedrock.runtime.remote.DeploymentArtifact;
import com.oracle.bedrock.runtime.remote.options.Deployer;
import com.oracle.bedrock.table.Table;
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
     * The {@link OptionsByType} controlling this {@link HttpDeployer}.
     */
    private OptionsByType optionsByType;


    /**
     * Create a new {@link HttpDeployer}.
     *
     * @param options the {@link Option}s controlling this {@link HttpDeployer}
     */
    public HttpDeployer(Option... options)
    {
        this.optionsByType = OptionsByType.of(options);
    }


    /**
     * Set the size of the byte buffer that will be used to
     * send the response of a HTTP request.
     *
     * @param size the size of the buffer in bytes
     *
     * @return this {@link HttpDeployer} to allow a fluent method style
     */
    public HttpDeployer withBufferSize(int size)
    {
        optionsByType.add(new BufferSize(size));

        return this;
    }


    /**
     * Set the size of the byte buffer that will be used to
     * send the response of a HTTP request.
     *
     * @param size the size of the buffer in kilo-bytes
     *
     * @return this {@link HttpDeployer} to allow a fluent method style
     */
    public HttpDeployer withBufferSizeInKB(int size)
    {
        return withBufferSize(size * 1024);
    }


    /**
     * Set the size of the byte buffer that will be used to
     * send the response of a HTTP request.
     *
     * @param size the size of the buffer in mega-bytes
     *
     * @return this {@link HttpDeployer} to allow a fluent method style
     */
    public HttpDeployer withBufferSizeInMB(int size)
    {
        return withBufferSize(size * 1024 * 1024);
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use wget to retrieve artifacts.
     *
     * @param options the {@link OptionsByType}
     *
     * @return a new {@link HttpDeployer}
     */
    public static HttpDeployer wget(Option... options)
    {
        return new WGetHttpDeployer(options);
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use wget to retrieve artifacts.
     *
     * @param wgetLocation the location of the wget executable
     *                     (for example "/usr/local/bin/wget")
     * @param options the {@link Option}s controlling the deployer
     *
     * @return a new {@link HttpDeployer}
     */
    public static HttpDeployer wgetAt(String    wgetLocation,
                                      Option... options)
    {
        return new WGetHttpDeployer(wgetLocation, options);
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use wget to retrieve artifacts.
     *
     * @param options the {@link Option}s controlling the deployer
     *
     * @return a new {@link HttpDeployer}
     */
    public static HttpDeployer curl(Option... options)
    {
        return new CurlHttpDeployer(options);
    }


    /**
     * A static helper method to return an {@link HttpDeployer}
     * that will use wget to retrieve artifacts.
     *
     * @param curlLocation the location of the curl executable
     *                     (for example "/usr/local/bin/curl")
     * @param options the {@link Option}s controlling the deployer
     *
     * @return a new {@link HttpDeployer}
     */
    public static HttpDeployer curlAt(String    curlLocation,
                                      Option... options)
    {
        return new CurlHttpDeployer(curlLocation, options);
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

        OptionsByType optionsByType = OptionsByType.empty();

        optionsByType.addAll(platform.getOptions());
        optionsByType.addAll(deploymentOptions);

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

            deployAllArtifacts(artifactMap, remoteDirectory, platform, server.getAddress(), optionsByType);
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
     * @param artifacts          a {@link Map} of {@link DeploymentArtifact}s to deploy
     *                           keyed by the URL path for each artifact
     * @param remoteDirectory    the remote directory to deploy to
     * @param platform           the remote {@link Platform} to deploy to
     * @param httpServerAddress  the {@link InetSocketAddress} the HTTP server is listening on
     * @param optionsByType      the {@link OptionsByType}s to use
     */
    protected void deployAllArtifacts(Map<String, DeploymentArtifact> artifacts,
                                      String                          remoteDirectory,
                                      Platform                        platform,
                                      InetSocketAddress               httpServerAddress,
                                      OptionsByType                   optionsByType)
    {
        if (artifacts == null || artifacts.isEmpty())
        {
            return;
        }

        try
        {
            PlatformSeparators separators      = optionsByType.get(PlatformSeparators.class);
            Table              deploymentTable = new Table();
            String             hostName        = httpServerAddress.getAddress().getCanonicalHostName();
            int                port            = httpServerAddress.getPort();

            for (Map.Entry<String, DeploymentArtifact> entry : artifacts.entrySet())
            {
                DeploymentArtifact artifact        = entry.getValue();
                File               sourceFile      = artifact.getSourceFile();
                URL                sourceURL       = new URL("http", hostName, port, entry.getKey());
                File               destinationFile = artifact.getDestinationFile();
                double             start           = System.currentTimeMillis();

                String             destinationParentFolder;
                String             destinationFileName;

                if (destinationFile == null)
                {
                    destinationParentFolder = remoteDirectory;
                    destinationFileName     = sourceFile.getName();
                }
                else
                {
                    destinationParentFolder = separators.asPlatformFileName(destinationFile.getParent());

                    if (destinationParentFolder == null)
                    {
                        destinationParentFolder = remoteDirectory;
                    }

                    destinationFileName = destinationFile.getName();
                }

                String targetFileName = destinationParentFolder + separators.getFileSeparator() + destinationFileName;

                deployArtifact(sourceURL, targetFileName, platform);

                double time = (System.currentTimeMillis() - start) / 1000.0d;

                deploymentTable.addRow(sourceFile.toString(), destinationFileName, String.format("%.3f s", time));
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
     * @return the {@link HttpServer}
     */
    protected HttpServer createServer(ExecutorService                 executor,
                                      Map<String, DeploymentArtifact> artifacts)
    {
        try
        {
            LocalPlatform platform = LocalPlatform.get();
            InetAddress   address  = NetworkHelper.getFeasibleLocalHost();
            int           port     = platform.getAvailablePorts().next();
            HttpServer    server   = HttpServer.create(new InetSocketAddress(address, port), 0);

            server.createContext("/", new ArtifactsHandler(artifacts, optionsByType.asArray()));
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
         * The options controlling this {@link ArtifactsHandler}.
         */
        private final OptionsByType optionsByType;


        /**
         * Create a {@link ArtifactsHandler} to serve the specified
         * {@link List} of {@link DeploymentArtifact}s.
         *
         * @param artifacts the {@link DeploymentArtifact}s to serve
         * @param options   the {@link Option}s controlling the {@link ArtifactsHandler}
         */
        public ArtifactsHandler(Map<String, DeploymentArtifact> artifacts,
                                Option...                       options)
        {
            this.artifacts     = artifacts;
            this.optionsByType = OptionsByType.of(options);
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

            BufferSize bufferSize  = optionsByType.getOrSetDefault(BufferSize.class, new BufferSize(1000000));
            int        bufferBytes = bufferSize.getBufferSize();

            byte[]     buff        = new byte[bufferBytes];

            try (InputStream data = sourceURI.toURL().openStream();
                OutputStream os = httpExchange.getResponseBody())
            {
                while (true)
                {
                    int read = data.read(buff, 0, bufferBytes);

                    if (read <= 0)
                    {
                        break;
                    }

                    os.write(buff, 0, read);
                }
            }
        }
    }


    /**
     * An {@link Option} for the {@link BufferSize}.
     */
    public static class BufferSize implements Option
    {
        private int bufferSize;


        /**
         * Constructs a {@link BufferSize} {@link Option}
         *
         * @param bufferSize  the size of the buffer (in bytes)
         */
        public BufferSize(int bufferSize)
        {
            this.bufferSize = bufferSize;
        }


        public int getBufferSize()
        {
            return bufferSize;
        }
    }
}
