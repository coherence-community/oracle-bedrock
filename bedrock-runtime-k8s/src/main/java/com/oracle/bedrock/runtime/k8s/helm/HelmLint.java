/*
 * File: HelmLint.java
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

/**
 * A representation of the Helm lint command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmLint
        extends HelmCommand<HelmLint>
{
    /**
     * Create a Helm lint command.
     *
     * @param name  the name of the chart to check
     */
    HelmLint(String name)
    {
    this(DEFAULT_HELM, Arguments.of(Argument.of(name)), Arguments.empty(), EnvironmentVariables.custom());
    }

    /**
     * Create a Helm lint command.
     *
     * @param chart  the name of the chart to check
     */
    HelmLint(File chart)
    {
    this(DEFAULT_HELM, Arguments.of(Argument.of(chart)), Arguments.empty(), EnvironmentVariables.custom());
    }

    /**
     * Create a {@link HelmLint} command.
     *
     * @param arguments  the command arguments
     * @param flags      the command flags
     * @param env        the environment variables
     */
    HelmLint(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        super(helm, arguments, flags, env, true, "lint");
    }

    @Override
    public HelmLint newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmLint(helm, arguments, flags, env);
    }

    /**
     * Obtain a copy of this Helm create command that
     * fails on lint warnings.
     *
     * @return  a copy of this Helm create command that also
     *          fails on lint warnings
     */
    public HelmLint strict()
    {
        return withArguments(Arguments.of("--strict"));
    }
}
