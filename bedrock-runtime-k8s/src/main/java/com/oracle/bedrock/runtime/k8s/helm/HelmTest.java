/*
 * File: HelmTest.java
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
 * A representation of the Helm test command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmTest
        extends HelmCommand<HelmTest>
        implements CLI.WithTLS<HelmTest>
{
    /**
     * Create a {@link HelmTest} command.
     *
     * @param releaseName  the release to get
     */
    HelmTest(String releaseName)
    {
        this(DEFAULT_HELM,
             Arguments.of(Argument.of(releaseName)),
             Arguments.empty(),
             EnvironmentVariables.custom());
    }

    /**
     * Create a {@link HelmTest} command.
     *
     * @param arguments  the command arguments
     * @param flags      the command flags
     * @param env        the environment variables
     */
    HelmTest(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        super(helm, arguments, flags, env, false, "test");
    }

    /**
     * Delete test pods upon completion.
     *
     * @return  a copy of this command with the {@code --cleanup} flag appended
     */
    public HelmTest cleanup()
    {
        return withFlags(Argument.of("--cleanup"));
    }

    /**
     * Set the time in seconds to wait for any individual Kubernetes
     * operation (like Jobs for hooks) (default 300).
     *
     * @param seconds  the number of seconds to wait
     *
     * @return  a copy of this command with the {@code --timeout} flag appended
     */
    public HelmTest timeout(int seconds)
    {
        return withFlags(Argument.of("--timeout", seconds));
    }

    @Override
    public HelmTest newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmTest(helm, arguments, flags, env);
    }
}
