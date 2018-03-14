/*
 * File: ContainerdCommandInterceptor.java
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

package com.oracle.bedrock.runtime.k8s.linuxkit;

import com.oracle.bedrock.runtime.options.CommandInterceptor;

import java.io.File;
import java.util.List;
import java.util.Properties;

/**
 * A {@link CommandInterceptor} that wraps commands inside a containerd
 * ctr task exec command with a specified namespace and container.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ContainerdCommandInterceptor implements CommandInterceptor
{
    /**
     * Th etemplate for executing commands on a LinuxKit VM.
     */
    public static final String CMD_TEMPLATE = "ctr -n %s tasks exec -cwd %s --exec-id bedrock %s %s";

    /**
     * The default linuxkit namespace.
     */
    public static final String DEFAULT_NAMESPACE = "services.linuxkit";

    /**
     * The default container to execute commands in.
     */
    public static final String DEFAULT_CONTAINER = "kubelet";

    /**
     * The namespace to execute the task in.
     */
    private final String namespace;

    /**
     * The container to execute the task in.
     */
    private final String container;

    /**
     * Create a {@link ContainerdCommandInterceptor}.
     *
     * @param namespace  the namespace to execute the task in
     * @param container  the container to execute the task in
     */
    private ContainerdCommandInterceptor(String namespace, String container)
    {
        this.namespace = namespace;
        this.container = container;
    }

    @Override
    public String onExecute(String executableName, List<String> arguments, Properties envVariables, File workingDirectory)
    {
        // determine the command to execute remotely
        StringBuilder command = new StringBuilder(executableName);

        // add the arguments
        for (String arg : arguments)
        {
            command.append(" ").append(arg);
        }

        return String.format(CMD_TEMPLATE, namespace, workingDirectory, container, command);
    }

    /**
     * Create a {@link ContainerdCommandInterceptor} that will
     * use a default namespace and container.
     *
     * @return  a {@link ContainerdCommandInterceptor} that will
     *          use a default namespace and container
     */
    public static ContainerdCommandInterceptor instance()
    {
        return new ContainerdCommandInterceptor(DEFAULT_NAMESPACE, DEFAULT_CONTAINER);
    }

    /**
     * Create a {@link ContainerdCommandInterceptor}.
     *
     * @param namespace  the namespace to execute the task in
     * @param container  the container to execute the task in
     *
     * @return  a {@link ContainerdCommandInterceptor}.
     */
    public static ContainerdCommandInterceptor instance(String namespace, String container)
    {
        return new ContainerdCommandInterceptor(namespace, container);
    }
}
