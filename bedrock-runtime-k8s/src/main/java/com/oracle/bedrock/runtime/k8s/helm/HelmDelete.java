/*
 * File: HelmDelete.java
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

/**
 * A representation of the Helm delete command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmDelete
        extends HelmCommand<HelmDelete>
        implements CLI.WithTLS<HelmDelete>
{
    /**
     * Create a {@link HelmDelete} command.
     *
     * @param name  the release to delete
     */
    HelmDelete(String name)
    {
    this(DEFAULT_HELM, Arguments.of(Argument.of(name)), Arguments.empty(), EnvironmentVariables.custom());
    }

    /**
     * Create a {@link HelmDelete} command.
     *
     * @param arguments  the command arguments
     * @param flags      the command flags
     * @param env        the environment variables
     */
    HelmDelete(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        super(helm, arguments, flags, env, true, "delete");
    }

    /**
     * Simulate a delete.
     *
     * @return  a copy of this command with the {@code --dry-run} flag appended
     */
    public HelmDelete dryRun()
    {
        return withFlags(Argument.of("--dry-run"));
    }

    /**
     * Prevent hooks from running during deletion.
     *
     * @return  a copy of this command with the {@code --no-hooks} flag appended
     */
    public HelmDelete noHooks()
    {
        return withFlags(Argument.of("--no-hooks"));
    }

    /**
     * Remove the release from the store and make its name free for later use.
     *
     * @return  a copy of this command with the {@code --purge} flag appended
     */
    public HelmDelete purge()
    {
        return withFlags(Argument.of("--purge"));
    }

    /**
     * Set the time in seconds to wait for any individual Kubernetes operation
     * (like Jobs for hooks) (default 300).
     *
     * @return  a copy of this command with the {@code --timeout} flag appended
     */
    public HelmDelete timeout(int seconds)
    {
        return withFlags(Argument.of("--timeout", seconds));
    }

    @Override
    public HelmDelete newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmDelete(helm, arguments, flags, env);
    }
}
