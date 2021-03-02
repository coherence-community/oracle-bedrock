/*
 * File: AbstractAssemblyResource.java
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

package com.oracle.bedrock.testsupport.junit;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;
import com.oracle.bedrock.runtime.Application;
import com.oracle.bedrock.runtime.Assembly;
import com.oracle.bedrock.runtime.AssemblyBuilder;
import com.oracle.bedrock.runtime.Infrastructure;
import com.oracle.bedrock.runtime.LocalPlatform;
import com.oracle.bedrock.runtime.Platform;
import com.oracle.bedrock.runtime.options.PlatformPredicate;
import com.oracle.bedrock.util.Triple;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract JUnit {@link ExternalResource} to configure and build an {@link Assembly} for use with JUnit tests.
 * <p>
 * This {@link ExternalResource} is essentially a wrapper around an {@link AssemblyBuilder}, that uses
 * JUnit life-cycle methods to create and destroy an {@link Assembly} for testing.
 * <p>
 * Copyright (c) 2017. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 *
 * @param <A>  the type of {@link Application} within the {@link Assembly}
 * @param <G>  the type of {@link Assembly} resource produced
 * @param <R>  the type of the {@link AbstractAssemblyResource} to permit fluent-style method calls
 *
 * @see AssemblyBuilder
 */
public abstract class AbstractAssemblyResource<A extends Application, G extends Assembly<A>,
                                               R extends AbstractAssemblyResource<A, G, R>>
        extends AbstractBaseAssembly<A, G, R>
        implements TestRule
{
    /**
     * Constructor for {@link AbstractAssemblyResource}.
     */
    protected AbstractAssemblyResource()
    {
    }

    public Statement apply(Statement base, Description description)
    {
        return statement(base);
    }

    private Statement statement(final Statement base)
    {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
            before();

            List<Throwable> errors = new ArrayList<Throwable>();
            try
            {
                base.evaluate();
            } catch (Throwable t)
            {
                errors.add(t);
            }
            finally
            {
                try
                {
                    after();
                }
                catch (Throwable t)
                {
                    errors.add(t);
                }
            }
            MultipleFailureException.assertEmpty(errors);
            }
        };
    }

    /**
     * Creates a new {@link AssemblyBuilder} for creating an {@link Assembly}.
     *
     * @return a new {@link AssemblyBuilder}
     */
    protected abstract AssemblyBuilder<A, G> createBuilder();

    protected void before() throws Throwable
    {
        start();
    }

    protected void after()
    {
        close();
    }
}
