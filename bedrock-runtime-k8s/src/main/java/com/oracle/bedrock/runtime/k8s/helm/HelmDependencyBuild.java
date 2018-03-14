/*
 * File: HelmDependencyBuild.java
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
 * A representation of the Helm dependency build command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmDependencyBuild
        extends HelmCommand<HelmDependencyBuild>
{
    /**
     * Create a {@link HelmDependencyBuild}.
     *
     * @param chart  the chart name
     */
    HelmDependencyBuild(String chart)
    {
        this(DEFAULT_HELM,
             Arguments.of(chart),
             Arguments.empty(),
             EnvironmentVariables.custom());
    }

    private HelmDependencyBuild(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        super(helm, arguments, flags, env, true, "dependency", "build");
    }

    /**
     * Keyring containing public keys (default ~/.gnupg/pubring.gpg).
     *
     * @param file  keyring file
     *
     * @return  a copy of this command with the {@code --keyring} flag appended
     */
    public HelmDependencyBuild keyRing(String file)
    {
        return withFlags(Argument.of("--keyring", file));
    }

    /**
     * Verify the package against its signature.
     *
     * @return  a copy of this command with the {@code --verify} flag appended
     */
    public HelmDependencyBuild verify()
    {
        return withFlags(Argument.of("--verify"));
    }

    @Override
    public HelmDependencyBuild newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmDependencyBuild(helm, arguments, flags, env);
    }
}
