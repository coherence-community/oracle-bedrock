/*
 * File: HelmInspect.java
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

import com.oracle.bedrock.runtime.options.Arguments;
import com.oracle.bedrock.runtime.options.EnvironmentVariables;

import java.io.File;
import java.net.URL;

/**
 * A representation of the Helm inspect command.
 * <p>
 * Copyright (c) 2018. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class HelmInspect
        extends HelmCommand<HelmInspect>
        implements CLI.WithCerts<HelmInspect>,
                   CLI.WithRepo<HelmInspect>
{
    /**
     * Create a {@link HelmInspect} command.
     *
     * @param chart  the chart(s) to inspect
     */
    HelmInspect(String chart)
    {
    this(DEFAULT_HELM, Arguments.of(chart), Arguments.empty(), EnvironmentVariables.custom(), "fetch");
    }

    /**
     * Create a {@link HelmInspect} command.
     *
     * @param chart  the chart(s) to inspect
     */
    HelmInspect(String subCommand, Object chart)
    {
    this(DEFAULT_HELM, Arguments.of(chart), Arguments.empty(), EnvironmentVariables.custom(), "fetch", subCommand);
    }

    /**
     * Create a {@link HelmInspect} command.
     *
     * @param arguments  the command arguments
     * @param flags      the command flags
     * @param env        the environment variables
     */
    HelmInspect(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env, String... commands)
    {
        super(helm, arguments, flags, env, false, ensureCommands(commands));
    }

    /**
     * A factory method to create a Helm inspect chart command.
     *
     * @param chart  the name of the chart to inspect
     *
     * @return  a Helm inspect chart command
     */
    public static HelmInspect chart(String chart)
    {
        return new HelmInspect("chart", chart);
    }

    /**
     * A factory method to create a Helm inspect chart command.
     *
     * @param chart  the name of the chart to inspect
     *
     * @return  a Helm inspect chart command
     */
    public static HelmInspect chart(File chart)
    {
        return new HelmInspect("chart", chart);
    }

    /**
     * A factory method to create a Helm inspect chart command.
     *
     * @param chart  the name of the chart to inspect
     *
     * @return  a Helm inspect chart command
     */
    public static HelmInspect chart(URL chart)
    {
        return new HelmInspect("chart", chart);
    }

    /**
     * A factory method to create a Helm inspect values command.
     *
     * @param chart  the name of the chart to inspect
     *
     * @return  a Helm inspect chart command
     */
    public static HelmInspect values(String chart)
    {
        return new HelmInspect("values", chart);
    }

    private static String[] ensureCommands(String... commands)
    {
        if (commands == null || commands.length == 0)
        {
            commands = new String[]{"inspect"};
        }

        return commands;
    }

    @Override
    public HelmInspect newInstance(String helm, Arguments arguments, Arguments flags, EnvironmentVariables env)
    {
        return new HelmInspect(helm, arguments, flags, env, getCommands());
    }
}
