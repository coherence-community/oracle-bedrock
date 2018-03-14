/*
 * File: K8sCluster.java
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

package com.oracle.bedrock.runtime.k8s;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.options.LaunchLogging;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.SimpleApplication;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.console.SystemApplicationConsole;
import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.DisplayName;
import com.oracle.bedrock.runtime.options.Executable;

import java.io.Closeable;
import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * A representation of a Kubernetes cluster.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class K8sCluster<K extends K8sCluster>
        implements Closeable
{
    /**
     * The default location of kubectl.
     */
    public static final File DEFAULT_KUBECTL = new File("/usr/local/bin/kubectl");

    /**
     * The kubectl executable.
     */
    private File kubectl = DEFAULT_KUBECTL;

    /**
     * The path to the kubeconfig file to use for CLI requests.
     */
    private File kubectlConfig;

    /**
     * The name of the kubeconfig context to use.
     */
    private String kubectlContext;

    /**
     * Flag to use with the kubectl --insecure-skip-tls-verify option
     */
    private boolean kubectlInsecure = false;
    
    /**
     * Create a {@link K8sCluster}.
     */
    public K8sCluster()
    {
    }

    /**
     * Set the location of the kubectl executable.
     *
     * @param kubectl  the location of kubectl
     *
     * @return  this {@link K8sCluster}
     */
    public K withKubectlAt(File kubectl)
    {
        this.kubectl = kubectl == null ? DEFAULT_KUBECTL : kubectl;

        return (K) this;
    }

    /**
     * Obtain the location of kubectl.
     *
     * @return  the location of kubectl
     */
    public File getKubectl()
    {
        return kubectl;
    }

    /**
     * Set the path to the kubeconfig file to use for CLI requests.
     *
     * @param config  path to the kubeconfig file to use for CLI requests
     *
     * @return  this {@link K8sCluster}
     */
    public K withKubectlConfig(File config)
    {
        this.kubectlConfig = config;

        return (K) this;
    }

    /**
     * Obtain the path to the kubeconfig file to use for CLI requests.
     *
     * @return  the path to the kubeconfig file to use for CLI requests
     */
    public File getKubectlConfig()
    {
        return kubectlConfig;
    }

    /**
     * Set the name of the kubeconfig context to use.
     *
     * @param context  the name of the kubeconfig context to use
     *
     * @return  this {@link K8sCluster}
     */
    public K withKubectlContext(String context)
    {
        this.kubectlContext = context;

        return (K) this;
    }

    /**
     * Obtain the name of the kubeconfig context to use.
     *
     * @return  the name of the kubeconfig context to use
     */
    public String getKubectlContext()
    {
        return kubectlContext;
    }

    /**
     * Set the value to apply to the kubectl {@code --insecure-skip-tls-verify}
     * option.
     *
     * @param insecure  the value to apply to
     *                  kubectl {@code --insecure-skip-tls-verify}
     *
     * @return  this {@link K8sCluster}
     */
    public K withKubectlInsecure(boolean insecure)
    {
        kubectlInsecure = insecure;

        return (K) this;
    }

    /**
     * Obtain the value to apply to the kubectl
     * {@code --insecure-skip-tls-verify}.
     *
     * @return  the value to apply to the kubectl
     *          {@code --insecure-skip-tls-verify}
     */
    public boolean isKubectlInsecure()
    {
        return kubectlInsecure;
    }

    /**
     * Determine whether the K8s Master ready.
     *
     * @return  {@code true} if the K8s Master ready
     */
    // must be public to be used in Eventually.assertThat
    public boolean isMasterReady()
    {
        CapturingApplicationConsole console = new CapturingApplicationConsole();

        try (Application application = kubectl(Arguments.of("get", "nodes"),
                                               Console.of(console),
                                               LaunchLogging.disabled()))
        {
            if (application.waitFor() == 0)
            {
                if (application.waitFor() == 0)
                {
                return console.getCapturedOutputLines().stream()
                              .filter(this::isMasterNodeLine)
                              .anyMatch(line -> "Ready".equalsIgnoreCase(getNodeStatus(line)));
                }

            }
        }
        catch (Exception e)
        {
            // ignored
        }

        return false;
    }

    /**
     * Execute a kubectl command against the k8s cluster
     * and wait for the command to complete.
     *
     * @param options  the options to use to run the kubectl command
     *
     * @return  the exit code from the kubectl command
     */
    public int kubectlAndWait(Option... options)
    {
        return kubectlAndWait(Timeout.autoDetect(), options);
    }

    /**
     * Execute a kubectl command against the k8s cluster
     * and wait for the command to complete.
     *
     * @param timeout  the time to wait for the command to complete
     * @param options  the options to use to run the kubectl command
     *
     * @return  the exit code from the kubectl command
     */
    public int kubectlAndWait(Timeout timeout, Option... options)
    {
        return waitFor(kubectl(options), timeout);
    }

    /**
     * Execute a kubectl command against the k8s cluster
     *
     * @param options  the options to use to run the kubectl command
     *
     * @return  the {@link Application} representing the running
     *          kubectl command
     */
    public Application kubectl(Option... options)
    {
        try
        {
            OptionsByType optionsByType = OptionsByType.empty();

            optionsByType.addAll(Executable.named(kubectl.getPath()),
                                 DisplayName.of("kubectl"),
                                 SystemApplicationConsole.builder());

            Arguments arguments = Arguments.empty();

            if (kubectlConfig != null)
            {
                arguments = arguments.with(Argument.of("--kubeconfig=" + kubectlConfig));
            }

            if (kubectlContext != null)
            {
                arguments = arguments.with(Argument.of("--context=" + kubectlContext));
            }

            arguments = arguments.with(Argument.of("--insecure-skip-tls-verify=" + kubectlInsecure));

            optionsByType.addAll(options);

            arguments = arguments.with(optionsByType.get(Arguments.class));

            optionsByType.add(arguments);

            return LocalPlatform.get().launch(SimpleApplication.class, optionsByType.asArray());
        }
        catch (Throwable e)
        {
            throw ensureRuntimeException(e);
        }
    }

    public void start()
    {
        // basically a no-op for an existing K8s cluster but to be safe we verify that the master is ready
        assertThat(isMasterReady(), is(true));
    }

    @Override
    public void close()
    {
        // no-op
    }

    private int waitFor(Application application, Timeout timeout)
    {
        try (Application app = application)
        {
            return app.waitFor(timeout);
        }
    }

    /**
     * Determine whether a kubectl get nodes output line is for a specific node
     *
     * @param name  the name of the node
     * @param line  the line to test
     *
     * @return  {@code true} if the line is for the specified node
     */
    public boolean isNodeLine(String name, String line)
    {
        if (line == null || line.trim().isEmpty())
        {
            return false;
        }

        String[] parts = line.trim().split("\\s+");

        return parts[0].equals(name);
    }

    /**
     * Determine whether a kubectl get nodes output line is for the master node.
     *
     * @param line  the line to test
     *
     * @return  {@code true} if the line is for the master node
     */
    public boolean isMasterNodeLine(String line)
    {
        if (line == null || line.trim().isEmpty())
        {
            return false;
        }

        String[] parts = line.trim().split("\\s+");

        return parts.length >= 3 && parts[2].equalsIgnoreCase("master");
    }

    /**
     * Obtain the node status from the node status line.
     *
     * @param line  the node status line
     *
     * @return  the node status from the node status line
     */
    public String getNodeStatus(String line)
    {
        if (line == null || line.trim().isEmpty())
        {
            return "NotReady";
        }

        String[] parts = line.trim().split("\\s+");

        return parts.length >= 2 ? parts[1] : "NotReady";
    }


    protected RuntimeException ensureRuntimeException(Throwable t)
    {
        if (t instanceof RuntimeException)
        {
            return (RuntimeException) t;
        }
        else
        {
            return new RuntimeException(t);
        }
    }
}
