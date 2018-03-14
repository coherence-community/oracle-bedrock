/*
 * File: HelmList.java
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
 * A representation of the Helm list command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmList
        extends HelmCommand<HelmList>
        implements CLI.WithTLS<HelmList>
{
    /**
     * Create a {@link HelmList} command.
     */
    HelmList()
    {
        this(DEFAULT_HELM,
             Arguments.empty(),
             Arguments.empty(),
             EnvironmentVariables.custom());
    }

    /**
     * Create a {@link HelmList} command.
     *
     * @param filter  the filter string
     */
    HelmList(String filter)
    {
        this(DEFAULT_HELM,
             Arguments.of(Argument.of(filter)),
             Arguments.empty(),
             EnvironmentVariables.custom());
    }

    /**
     * Create a {@link HelmList} command.
     *
     * @param arguments  the command arguments
     * @param flags      the command flags
     * @param env        the environment variables
     */
    HelmList(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        super(helm, arguments, flags, env, true, "list");
    }

    /**
     * Show all releases, not just the ones marked DEPLOYED.
     *
     * @return  a copy of this command with the {@code --all} flag appended
     */
    public HelmList all()
    {
        return withFlags(Argument.of("--all"));
    }

    /**
     * Sort output by release date.
     *
     * @return  a copy of this command with the {@code --date} flag appended
     */
    public HelmList sortByDate()
    {
        return withFlags(Argument.of("--date"));
    }

    /**
     * Show deleted releases
     *
     * @return  a copy of this command with the {@code --deleted} flag appended
     */
    public HelmList deleted()
    {
        return withFlags(Argument.of("--deleted"));
    }

    /**
     * Show faideletingled releases
     *
     * @return  a copy of this command with the {@code --deleting} flag appended
     */
    public HelmList deleting()
    {
        return withFlags(Argument.of("--deleting"));
    }

    /**
     * Show deployed releases
     *
     * @return  a copy of this command with the {@code --deployed} flag appended
     */
    public HelmList deployed()
    {
        return withFlags(Argument.of("--deployed"));
    }

    /**
     * Show failed releases
     *
     * @return  a copy of this command with the {@code --failed} flag appended
     */
    public HelmList failed()
    {
        return withFlags(Argument.of("--failed"));
    }

    /**
     * Set the maximum number of revision to include in
     * the list (default 256)
     *
     * @param max  the maximum number of revision to include
     *
     * @return  a copy of this command with the {@code --max} flag appended
     */
    public HelmList max(int max)
    {
        return withFlags(Argument.of("--max", max));
    }

    /**
     * Show releases within a specific namespace.
     *
     * @param namespace  the namespace
     *
     * @return  a copy of this command with the {@code --max} flag appended
     */
    public HelmList namespace(String namespace)
    {
        return withFlags(Argument.of("--namespace", namespace));
    }

    /**
     * The next release name in the list, used to offset from start value
     *
     * @param release  the release name
     *
     * @return  a copy of this command with the {@code --offset} flag appended
     */
    public HelmList offset(String release)
    {
        return withFlags(Argument.of("--offset", release));
    }

    /**
     * Show pending releases.
     *
     * @return  a copy of this command with the {@code --pending} flag appended
     */
    public HelmList pending()
    {
        return withFlags(Argument.of("--pending"));
    }

    /**
     * Reverse the sort order.
     *
     * @return  a copy of this command with the {@code --reverse} flag appended
     */
    public HelmList reverse()
    {
        return withFlags(Argument.of("--reverse"));
    }

    /**
     * Output short (quiet) listing format.
     *
     * @return  a copy of this command with the {@code --short} flag appended
     */
    public HelmList quiet()
    {
        return withFlags(Argument.of("--short"));
    }

    @Override
    public HelmList newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmList(helm, arguments, flags, env);
    }
}
