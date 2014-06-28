/*
 * File: AbstractRemoteApplicationEnvironment.java
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

package com.oracle.tools.runtime.remote;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;
import com.oracle.tools.runtime.PropertiesBuilder;

import java.util.Collections;
import java.util.Properties;

/**
 * A base implementation of a {@link RemoteApplicationEnvironment}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @param <A>  the type of the {@link Application}s the {@link RemoteApplicationBuilder} will realize
 * @param <S>  the type of the {@link ApplicationSchema} for the {@link Application}s
 *
 * @author Brian Oliver
 */
public abstract class AbstractRemoteApplicationEnvironment<A extends Application, S extends ApplicationSchema<A>>
    implements RemoteApplicationEnvironment
{
    /**
     * The {@link ApplicationSchema} for the {@link RemoteApplicationEnvironment}.
     */
    protected S schema;

    /**
     * The {@link Platform} representing the remoteO/S
     */
    protected Platform platform;

    /**
     * Constructs an {@link AbstractRemoteApplicationEnvironment}.
     *
     * @param schema    the {@link com.oracle.tools.runtime.ApplicationSchema}
     * @param platform  the {@link Platform} representing the remoteO/S
     */
    protected AbstractRemoteApplicationEnvironment(S schema, Platform platform)
    {
        this.schema   = schema;
        this.platform = platform;
    }


    @Override
    public String getRemoteCommandToExecute()
    {
        return schema.getExecutableName();
    }


    @Override
    public Iterable<DeploymentArtifact> getRemoteDeploymentArtifacts()
    {
        return Collections.EMPTY_LIST;
    }


    @Override
    public Properties getRemoteEnvironmentVariables()
    {
        // when environment variables are inherited (from the current environment), we need to
        // set our current environment variables in the remote application
        if (schema.isEnvironmentInherited())
        {
            PropertiesBuilder environmentBuilder = PropertiesBuilder.fromCurrentEnvironmentVariables();

            return environmentBuilder.realize(schema.getEnvironmentVariablesBuilder());
        }
        else
        {
            return schema.getEnvironmentVariables(platform);
        }
    }


    @Override
    public void close()
    {
        // nothing to do by default
    }
}
