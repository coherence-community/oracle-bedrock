/*
 * File: AbstractApplicationBuilder.java
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

package com.oracle.tools.runtime;

import com.oracle.tools.runtime.console.NullApplicationConsole;

import java.io.IOException;

import java.util.UUID;

/**
 * An {@link AbstractApplicationBuilder} is a base implementation of an {@link ApplicationBuilder}.
 *
 * @param <A>  the type of the {@link Application}s the {@link ApplicationBuilder} will realize
 * @param <S>  the type of the {@link ApplicationSchema} for the {@link Application}s
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public abstract class AbstractApplicationBuilder<A extends Application<A>, S extends ApplicationSchema<A, S>>
    implements ApplicationBuilder<A, S>
{
    /**
     * Should diagnostic information be enabled for the {@link Application}s
     * produced by this {@link ApplicationBuilder}.
     */
    protected boolean m_isDiagnosticsEnabled;


    /**
     * Constructs an {@link AbstractApplicationBuilder} with default {@link ApplicationSchema}.
     */
    public AbstractApplicationBuilder()
    {
        m_isDiagnosticsEnabled = false;
    }


    /**
     * Determines if diagnostics are enabled for the {@link ApplicationBuilder},
     * meaning that information concerning the construction of {@link Application}s
     * will be output/logged.
     *
     * @return <code>true</code> if diagnostics are enabled.
     */
    public boolean isDiagnosticsEnabled()
    {
        return m_isDiagnosticsEnabled;
    }


    /**
     * {@inheritDoc}
     */
    public A realize(S schema) throws IOException
    {
        return realize(schema, UUID.randomUUID().toString());
    }


    /**
     * {@inheritDoc}
     */
    public A realize(S      schema,
                     String applicationName) throws IOException
    {
        return realize(schema, applicationName, new NullApplicationConsole());
    }


    /**
     * Raises a specific type of {@link Application} {@link LifecycleEvent} for
     * the specified application.
     *
     * @param application  the application on which the event occurred
     * @param eventKind    the event type
     */
    protected void raiseApplicationLifecycleEvent(final A                     application,
                                                  final Application.EventKind eventKind)
    {
        // construct the LifecycleEvent
        @SuppressWarnings("rawtypes") LifecycleEvent event = new LifecycleEvent<A>()
        {
            @Override
            public Application.EventKind getType()
            {
                return eventKind;
            }

            @Override
            public A getObject()
            {
                return application;
            }
        };

        // raise the event
        for (LifecycleEventInterceptor<A> interceptor : application.getLifecycleInterceptors())
        {
            interceptor.onEvent(event);
        }
    }
}
