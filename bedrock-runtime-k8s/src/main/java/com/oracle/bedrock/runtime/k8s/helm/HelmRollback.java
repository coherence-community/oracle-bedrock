/*
 * File: HelmRollback.java
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

package com.oracle.bedrock.runtime.k8s.helm;

import com.oracle.bedrock.runtime.options.Argument;
import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A representation of the Helm rollback command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmRollback
        extends HelmCommand<HelmRollback>
        implements CLI.WithTLS<HelmRollback>
{
    /**
     * Create a {@link HelmRollback} command.
     *
     * @param release  the name of the release to be upgraded
     * @param revision  the revision to rollback to
     */
    HelmRollback(String release, String revision)
    {
    this(DEFAULT_HELM, Arguments.of(release, revision), Arguments.empty(), EnvironmentVariables.custom());
    }

    /**
     * Create a {@link HelmRollback} command.
     *
     * @param arguments  the command arguments
     * @param flags      the command flags
     * @param env        the environment variables
     */
    HelmRollback(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        super(helm, arguments, flags, env, true, "rollback");
    }

    /**
     * Perform a dry run install.
     *
     * @return  a copy of this command with the {@code --dry-run} flag appended
     */
    public HelmRollback dryRun()
    {
        return withFlags(Argument.of("--dry-run"));
    }

    /**
     * Force resource update through delete/recreate if needed.
     *
     * @return  a copy of this command with the {@code --force} flag appended
     */
    public HelmRollback force()
    {
        return withFlags(Argument.of("--force"));
    }

    /**
     * Prevent hooks from running during install.
     *
     * @return  a copy of this command with the {@code --no-hooks} flag appended
     */
    public HelmRollback noHooks()
    {
        return withFlags(Argument.of("--no-hooks"));
    }

    /**
     * Performs pods restart for the resource if applicable.
     *
     * @return  a copy of this command with the {@code --recreate-pods} flag appended
     */
    public HelmRollback recreatePods()
    {
        return withFlags(Argument.of("--recreate-pods"));
    }

    /**
     * Set the time in seconds to wait for any individual Kubernetes
     * operation (like Jobs for hooks) (default 300)
     *
     * @param seconds  the timeout in seconds
     *
     * @return  a copy of this command with the {@code --values} flag appended
     */
    public HelmRollback timeout(int seconds)
    {
        return withFlags(Argument.of("--timeout", seconds));
    }

    /**
     * Wait until all Pods, PVCs, Services, and minimum number of Pods of a
     * Deployment are in a ready state before marking the release as successful.
     * Will wait for the number of seconds specified with the {@code --timeout}
     * option {@link #timeout(int)} or the default of 300 seconds.
     *
     * @return  a copy of this command with the {@code --wait} flag appended
     */
    public HelmRollback waitForK8s()
    {
        return withFlags(Argument.of("--wait"));
    }

    /**
     * Wait until all Pods, PVCs, Services, and minimum number of Pods of a
     * Deployment are in a ready state before marking the release as successful.
     *
     * @param  seconds  timeout in seconds
     *
     * @return  a copy of this command with the {@code --wait} and {@code --timeout} flags appended
     */
    public HelmRollback waitForK8s(int seconds)
    {
        return withFlags(Argument.of("--wait"), Argument.of("--timeout", seconds));
    }

    @Override
    public HelmRollback newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmRollback(helm, arguments, flags, env);
    }
}
