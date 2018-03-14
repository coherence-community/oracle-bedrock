/*
 * File: HelmFetch.java
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

/**
 * A representation of the Helm fetch command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmFetch
        extends HelmCommand<HelmFetch>
        implements CLI.WithCerts<HelmFetch>,
                   CLI.WithRepo<HelmFetch>
{
    /**
     * Create a {@link HelmFetch} command.
     *
     * @param charts  the chart(s) to fetch
     */
    HelmFetch(String... charts)
    {
    this(DEFAULT_HELM, Arguments.of(Arrays.asList(charts)), Arguments.empty(), EnvironmentVariables.custom());
    }

    /**
     * Create a {@link HelmFetch} command.
     *
     * @param arguments  the command arguments
     * @param flags      the command flags
     * @param env        the environment variables
     */
    HelmFetch(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        super(helm, arguments, flags, env, true, "fetch");
    }

    /**
     * Use development versions too. Equivalent to version '>0.0.0-a'.
     * If {@code --version} is set, this is ignored.
     *
     * @return  a copy of this command with the {@code --devel} flag appended
     */
    public HelmFetch development()
    {
        return withFlags(Argument.of("--devel"));
    }

    /**
     * Fetch the provenance file, but don't perform verification.
     *
     * @return  a copy of this command with the {@code --prov} flag appended
     */
    public HelmFetch prov()
    {
        return withFlags(Argument.of("--prov"));
    }

    /**
     * Untar the chart after downloading it.
     *
     * @return  a copy of this command with the {@code --untar} flag appended
     */
    public HelmFetch untar()
    {
        return withFlags(Argument.of("--untar", true));
    }

    /**
     * The name of the directory into which the chart should be expanded (default ".") - also sets
     * the {@code --untar} flag.
     *
     * @param directory  the repository url
     *
     * @return  a copy of this command with the {@code --untardir} and {@code --untar} flags appended
     */
    public HelmFetch untarInto(String directory)
    {
        return withFlags(Argument.of("--untardir", directory));
    }

    /**
     * The name of the directory into which the chart should be expanded (default ".") - also sets
     * the {@code --untar} flag.
     *
     * @param directory  the repository url
     *
     * @return  a copy of this command with the {@code --untardir} and {@code --untar} flags appended
     */
    public HelmFetch untarInto(File directory)
    {
        return withFlags(Argument.of("--untardir", directory));
    }

    @Override
    public HelmFetch newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmFetch(helm, arguments, flags, env);
    }
}
