/*
 * File: JavaAgent.java
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

package com.oracle.bedrock.runtime.java.options;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.lang.ExpressionEvaluator;
import com.oracle.bedrock.runtime.java.ClassPath;

import java.util.Collections;

/**
 * A {@link JvmOption} for Java-based Agents.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JavaAgent implements JvmOption, Option.Collectable
{
    /**
     * The singleton {@link ClassPath} to the Java Agent (containing a single path element)
     */
    private ClassPath path;

    /**
     * The {@link JavaAgent} command-line parameters (options)
     */
    private String parameters;


    /**
     * Constructs a {@link JavaAgent} based on the path to the Java Agent (without parameters)
     *
     * @param path        path to the Java Agent
     */
    private JavaAgent(String path)
    {
        this(path, "");
    }


    /**
     * Constructs a {@link JavaAgent} based on the path to the Java Agent and parameters.
     *
     * @param path        path to the Java Agent
     * @param parameters  the command-line parameters
     */
    private JavaAgent(String path,
                      String parameters)
    {
        this.path       = new ClassPath(path);
        this.parameters = parameters == null ? "" : parameters.trim();

        if (this.path.size() != 1)
        {
            throw new IllegalArgumentException("JavaAgent path [" + path
                                               + "] may only contain a single path entry, to a Java Archive");
        }
    }


    /**
     * Constructs a {@link JavaAgent} using the specified path, including the Java Agent.
     *
     * @param path  the path to the Java Agent jar
     *
     * @return  the {@link JavaAgent}
     */
    public static JavaAgent using(String path)
    {
        return new JavaAgent(path);
    }


    /**
     * Constructs a {@link JavaAgent} using the specified path, including the Java Agent,
     * with the specified parameters.
     *
     * @param path  the path to the Java Agent jar
     * @param parameters  the command-line parameters
     *
     * @return  the {@link JavaAgent}
     */
    public static JavaAgent using(String path,
                                  String parameters)
    {
        return new JavaAgent(path, parameters);
    }


    @Override
    public Class<? extends Collector> getCollectorClass()
    {
        return JavaAgents.class;
    }


    @Override
    public Iterable<String> resolve(OptionsByType optionsByType)
    {
        String resolvedParameters;

        if (parameters.isEmpty())
        {
            resolvedParameters = "";
        }
        else
        {
            ExpressionEvaluator evaluator = new ExpressionEvaluator(optionsByType);

            resolvedParameters = "=" + evaluator.evaluate(parameters, String.class);
        }

        String agent = "-javaagent:" + path.toString(optionsByType) + resolvedParameters;

        return Collections.singletonList(agent);
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof JavaAgent))
        {
            return false;
        }

        JavaAgent javaAgent = (JavaAgent) o;

        if (path != null ? !path.equals(javaAgent.path) : javaAgent.path != null)
        {
            return false;
        }

        return parameters != null ? parameters.equals(javaAgent.parameters) : javaAgent.parameters == null;

    }


    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;

        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);

        return result;
    }
}
