/*
 * File: Pod.java
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
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.ApplicationConsole;
import com.oracle.bedrock.runtime.console.CapturingApplicationConsole;
import com.oracle.bedrock.runtime.console.FileWriterApplicationConsole;
import com.oracle.bedrock.runtime.console.InputFromIntputStreamRedirector;
import com.oracle.bedrock.runtime.console.OutputToOutputStreamRedirector;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.Console;
import com.oracle.bedrock.runtime.options.ConsoleInputRedirector;
import com.oracle.bedrock.runtime.options.ConsoleOutputRedirector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A representation of a Kubernetes Pod.
 * <p>
 * Copyright (c) 2019. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class Pod
{
    /**
     * The {@link K8sCluster} containing this {@link Pod}.
     */
    private K8sCluster k8s;

    /**
     * The name of this {@link Pod}.
     */
    private String podName;

    /**
     * The namespace that this {@link Pod} is running in.
     */
    private String namespace;

    /**
     * The containers in this Pod.
     */
    private Map<String, PodContainer> containers;

    /**
     * Create a {@link Pod} in the default namespace.
     *
     * @param k8s      the {@link K8sCluster} containing this {@link Pod}
     * @param podName  the name of this {@link Pod}
     */
    public Pod(K8sCluster k8s, String podName)
    {
        this(k8s, podName, null);
    }


    /**
     * Create a {@link Pod}.
     *
     * @param k8s        the {@link K8sCluster} containing this {@link Pod}
     * @param podName    the name of this {@link Pod}
     * @param namespace  the namespace that this {@link Pod} is running in
     */
    public Pod(K8sCluster k8s, String podName, String namespace)
    {
        if (podName == null || podName.trim().isEmpty())
        {
            throw new IllegalArgumentException("Pod name cannot be null or blank");
        }

        this.k8s = k8s == null ? new K8sCluster() : k8s;
        this.podName = podName;
        this.namespace = namespace == null || namespace.trim().isEmpty() ? K8sCluster.DEFAULT_NAMESPACE : namespace;
    }


    /**
     * Obtain the {@link K8sCluster} containing this {@link Pod}
     *
     * @return  the {@link K8sCluster} containing this {@link Pod}
     */
    public K8sCluster getK8sCluster()
    {
        return k8s;
    }


    /**
     * Obtain the name of this {@link Pod}.
     *
     * @return   the name of this {@link Pod}
     */
    public String getPodName()
    {
        return podName;
    }


    /**
     * Obtain the namespace that this {@link Pod} is running in.
     *
     * @return  the namespace that this {@link Pod} is running in
     */
    public String getNamespace()
    {
        return namespace;
    }


    /**
     * Copy a local file to this Pod.
     *
     * @param source       the location of the file to copy
     * @param destination  the destination to copy to in the Pod
     *
     * @throws Exception  if an error occurs copying the file
     */
    public void copyTo(Path source, Path destination) throws Exception
    {
        if (!Files.exists(source))
        {
            throw new IOException("Source does not exists " + source);
        }

        List<String> listArgs = getExecArgs(true, "cat > " + destination.toString());

        try (FileInputStream in = new FileInputStream(source.toFile()))
        {
            InputFromIntputStreamRedirector redirector = new InputFromIntputStreamRedirector(in);

            k8s.kubectl(Arguments.of(listArgs),
                        LaunchLogging.disabled(),
                        ConsoleInputRedirector.of(redirector));
            
            redirector.join();
        }
    }


    /**
     * Copy a file from this Pod to a local destination.
     *
     * @param source       the source file to copy from this Pod
     * @param destination  the destination to copy to
     *
     * @throws Exception  if an error occurs copying the file
     */
    public void copyFrom(Path source, Path destination) throws Exception
    {
        List<String> listArgs = getExecArgs(false, "cat " + source.toString());

        try (OutputStream out = new FileOutputStream(destination.toFile()))
        {
            OutputToOutputStreamRedirector redirector = new OutputToOutputStreamRedirector(out);
            try (Application app = k8s.kubectl(Arguments.of(listArgs),
                                               LaunchLogging.disabled(),
                                               ConsoleOutputRedirector.of(redirector)))
            {
                redirector.join();
            }
        }
    }


    /**
     * Write this Pod's log to the specified file.
     *
     * @param file  the file to write the log to
     *
     * @throws IOException  if an error occurs obtaining the log
     */
    public void log(File file) throws IOException
    {
        try(FileWriter writer = new FileWriter(file))
        {
            log(new FileWriterApplicationConsole(writer));
        }
    }


    /**
     * Capture this Pod's log.
     *
     * @param console  the {@link ApplicationConsole} to write the log to
     *
     * @throws IOException  if an error occurs obtaining the log
     */
    public void log(ApplicationConsole console) throws IOException
    {
        List<String> listArgs = getExecArgs(false, "log");

        if (k8s.kubectlAndWait(Arguments.of(listArgs), Console.of(console), LaunchLogging.disabled()) != 0)
        {
            throw new IOException("Error obtaining logs for Pod " + podName);
        }
    }


    /**
     * Tail and follow this Pod's log.
     *
     * @param file  the file to write the log to
     *
     * @return  the {@link Application} wrapping the kubectl log command that allows
     *          stopping of log following by calling {@link Application#close()}
     *
     * @throws IOException  if an error occurs obtaining the log
     */
    public Application followLog(File file) throws IOException
    {
        return followLog(new FileWriterApplicationConsole(new FileWriter(file)));
    }


    /**
     * Tail and follow this Pod's log.
     *
     * @param console  the {@link ApplicationConsole} to write the log to
     *
     * @return  the {@link Application} wrapping the kubectl log command that allows
     *          stopping of log following by calling {@link Application#close()}
     */
    public Application followLog(ApplicationConsole console)
    {
        List<String> listArgs = getExecArgs(false, "log");

        return k8s.kubectl(Arguments.of(listArgs), Console.of(console), LaunchLogging.disabled());
    }


    /**
     * Exec a command on this Pod.
     *
     * @param args     the arguments of the command to exec
     * @param options  extra {@link Option}s to use when exec'ing the command
     */
    public Application exec(Arguments args, Option... options)
    {
        List<String>  listArgs = getExecArgs(false, "log");
        OptionsByType opts     = OptionsByType.of(options).add(Arguments.of(listArgs).with(args));
        
        return k8s.kubectl(opts.asArray());
    }


    /**
     * Execute the ls command in this Pod and return the list of files.
     *
     * @return  the list of files in the path
     *
     * @throws IOException  if an error occurs executing the ls command
     */
    public List<String> ls() throws IOException
    {
        return ls(null);
    }


    /**
     * Execute the ls command in this Pod and return the list of files.
     *
     * @param path  the path to perform the ls command in, or null to use the root directory
     *
     * @return  the list of files in the path
     *
     * @throws IOException  if an error occurs executing the ls command
     */
    public List<String> ls(Path path) throws IOException
    {
        String                      dir      = path == null ? "/" : path.toString();
        List<String>                listArgs = getExecArgs(true, "ls " + dir);
        CapturingApplicationConsole console  = new CapturingApplicationConsole();

        if (k8s.kubectlAndWait(Arguments.of(listArgs), Console.of(console), LaunchLogging.disabled()) != 0)
        {
            throw new IOException("Error executing ls " + dir + "\n"
                    + String.join("", console.getCapturedErrorLines()));
        }

        List<String> list = new ArrayList<>(console.getCapturedOutputLines());

        list.remove(list.size() - 1);

        return list;
    }


    /**
     * Execute the mkdir command in this Pod to create a directory or directory path
     *
     * @param path  the path of the directory to create
     *
     * @throws IOException  if an error occurs executing the ls command
     */
    public void mkdir(Path path) throws IOException
    {
        List<String>                listArgs = getExecArgs(true, "mkdir -p " + path);
        CapturingApplicationConsole console  = new CapturingApplicationConsole();

        if (k8s.kubectlAndWait(Arguments.of(listArgs), Console.of(console), LaunchLogging.disabled()) != 0)
        {
            throw new IOException("Error executing mkdir -p " + path + "\n"
                    + String.join("", console.getCapturedErrorLines()));
        }
    }


    /**
     * Execute the rm command in this Pod to remove a file or directory
     *
     * @param path  the path to the file or directory to remove
     *
     * @throws IOException  if an error occurs executing the ls command
     */
    public void rm(Path path) throws IOException
    {
        List<String>                listArgs = getExecArgs(true, "rm -rf " + path);
        CapturingApplicationConsole console  = new CapturingApplicationConsole();

        if (k8s.kubectlAndWait(Arguments.of(listArgs), Console.of(console), LaunchLogging.disabled()) != 0)
        {
            throw new IOException("Error executing rm " + path + "\n"
                    + String.join("", console.getCapturedErrorLines()));
        }
    }


    /**
     * Obtain the containers for this Pod.
     *
     * @return  a {@link Map} of containers for this Pod keyed by container name
     */
    public Map<String, PodContainer> getContainers()
    {
        if (containers == null)
        {
            synchronized (this)
            {
                if (containers == null)
                {
                    CapturingApplicationConsole console = new CapturingApplicationConsole();
                    List<String>                args    = getArgs("get");

                    args.add("pod");
                    args.add(podName);
                    args.add("-o");
                    args.add("jsonpath={range .spec.containers[*]}{.name}{\"\\n\"}{end}");

                    int exitCode = k8s.kubectlAndWait(Arguments.of(args), Console.of(console), LaunchLogging.disabled());

                    if (exitCode == 0)
                    {
                    containers = console.getCapturedOutputLines()
                                        .stream()
                                        .filter(s -> !"(terminated)".equals(s))
                                        .filter(s -> !s.trim().isEmpty())
                                        .map(name -> new PodContainer(k8s, podName, name, namespace))
                                        .collect(Collectors.toMap(PodContainer::getContainerName, container -> container));
                    }
                    else
                    {
                        String msg = String.join("\n", console.getCapturedErrorLines());
                        throw new RuntimeException("Error obtaining Pod containers\n" + msg);
                    }

                }
            }
        }

        return Collections.unmodifiableMap(containers);
    }

    /**
     * Obtain the kubectl exec command line to use for this Pod.
     *
     * @param termainal  {@link true} to include {@code -i} in the command
     * @param args       the additional arguments to append to the command
     *
     * @return  the kubectl exec command line to use for this Pod
     */
    protected List<String> getExecArgs(boolean termainal, String... args)
    {
        List<String> list = getArgs("exec");

        if (termainal)
        {
            list.add("-i");
        }

        list.add(podName);
        list.add("--");
        list.add("/bin/sh");
        list.add("-c");

        list.addAll(Arrays.asList(args));

        return list;
    }

    /**
     * Obtain the base kubectl arguments.
     *
     * @param command  the kubectl command
     *
     * @return  the base kubectl arguments
     */
    protected List<String> getArgs(String command)
    {
        List<String> list = new ArrayList<>();

        if (namespace != null)
        {
            list.add("-n");
            list.add(namespace);
        }

        list.add(command);

        return list;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        Pod pod = (Pod) o;
        return Objects.equals(podName, pod.podName) &&
               Objects.equals(namespace, pod.namespace);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(podName, namespace);
    }
}
