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

import com.oracle.tools.Options;

import com.oracle.tools.lang.ExpressionEvaluator;

import com.oracle.tools.options.Variables;

import com.oracle.tools.runtime.Application;
import com.oracle.tools.runtime.ApplicationSchema;
import com.oracle.tools.runtime.Platform;

import com.oracle.tools.runtime.Profile;
import com.oracle.tools.runtime.java.JavaApplication;

import com.oracle.tools.runtime.options.Arguments;
import com.oracle.tools.runtime.options.EnvironmentVariables;

import java.net.InetAddress;

import java.util.ArrayList;
import java.util.List;
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
     * The {@link Options} for the remote {@link JavaApplication}.
     */
    protected Options options;


    /**
     * Constructs an {@link AbstractRemoteApplicationEnvironment}.
     *
     * @param schema    the {@link com.oracle.tools.runtime.ApplicationSchema}
     * @param platform  the {@link Platform} representing the remote O/S
     * @param options   the {@link Options} for the remote O/S
     */
    protected AbstractRemoteApplicationEnvironment(S        schema,
                                                   Platform platform,
                                                   Options  options)
    {
        this.schema   = schema;
        this.platform = platform;
        this.options  = options;

        // ----- notify the Profiles that the application is about to be realized -----

        for (Profile profile : options.getInstancesOf(Profile.class))
        {
            profile.onBeforeRealize(platform, schema, options);
        }
    }


    @Override
    public String getRemoteCommandToExecute()
    {
        return schema.getExecutableName();
    }


    @Override
    public List<String> getRemoteCommandArguments(InetAddress remoteExecutorAddress)
    {
        ArrayList<String> arguments = new ArrayList<>();

        // ----- add oracletools.runtime.inherit.xxx values to the arguments -----

        for (String propertyName : System.getProperties().stringPropertyNames())
        {
            if (propertyName.startsWith("oracletools.runtime.inherit."))
            {
                // resolve the property value
                String propertyValue = System.getProperty(propertyName);

                // evaluate any expressions in the property value
                ExpressionEvaluator evaluator = new ExpressionEvaluator(options.get(Variables.class));

                propertyValue = evaluator.evaluate(propertyValue, String.class);

                arguments.add(propertyValue);
            }
        }

        List<String> argList = schema.getOptions().get(Arguments.class)
                .realize(platform, schema, options.asArray());

        arguments.addAll(argList);

        return arguments;
    }


    @Override
    public Properties getRemoteEnvironmentVariables()
    {
        EnvironmentVariables environmentVariables = options.get(EnvironmentVariables.class,
                                                                EnvironmentVariables.of(EnvironmentVariables.Source
                                                                    .TargetPlatform));

        Properties variables = new Properties();

        switch (environmentVariables.getSource())
        {
        case Custom :
            break;

        case ThisApplication :
            variables.putAll(System.getenv());
            break;

        case TargetPlatform :
            break;
        }

        // add the optionally defined environment variables
        variables.putAll(environmentVariables.realize(platform, schema, options.asArray()));

        return variables;
    }


    @Override
    public void close()
    {
        // nothing to do by default
    }
}
