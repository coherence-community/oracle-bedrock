/*
 * File: ApplicationClass.java
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

package com.oracle.tools.runtime.options;

import com.oracle.tools.Option;
import com.oracle.tools.Options;

import com.oracle.tools.runtime.Application;

import com.oracle.tools.runtime.annotations.PreferredMetaClass;

import com.oracle.tools.util.ReflectionHelper;


/**
 * An {@link Option} to hold the {@link Class} of an {@link Application}
 * being launched.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
public class ApplicationType<A extends Application> implements Option
{
    /**
     * the {@link Class} of the {@link Application} to launch.
     */
    private final Class<A> applicationClass;


    /**
     * Create a {@link ApplicationType}.
     *
     * @param applicationClass  the {@link Class} of the {@link Application}
     */
    public ApplicationType(Class<A> applicationClass)
    {
        if (applicationClass == null)
        {
            throw new IllegalArgumentException("The application class cannot be null");
        }

        this.applicationClass = applicationClass;
    }


    /**
     * Obtain the {@link Class} of the {@link Application}.
     *
     * @return  the {@link Class} of the {@link Application}
     */
    public Class<A> getType()
    {
        return applicationClass;
    }


    /**
     * Obtain the {@link MetaClass} to use when launching an
     * application og this {@link ApplicationType}.
     * <p>
     * If the specified {@link Options} contains a {@link MetaClass}
     * then that will be returned otherwise the preferred {@link MetaClass}
     * for this application {@link Class} will be returned.
     *
     * @param options  the {@link Options} to use.
     *
     * @return  the {@link MetaClass} to use when launching an
     *          application og this {@link ApplicationType}
     */
    public MetaClass<A> getMetaClass(Options options)
    {
        // attempt to locate the meta-class using the options
        MetaClass<A> metaClass = options.get(MetaClass.class);

        if (metaClass == null)
        {
            // attempt to find the meta-class for the application based on the @PreferredMetaClass annotation
            PreferredMetaClass preferredMetaClass = ReflectionHelper.getAnnotation(applicationClass,
                                                                                   PreferredMetaClass.class);

            if (preferredMetaClass == null)
            {
                throw new UnsupportedOperationException("Failed to locate the MetaClass option and PreferredMetaClass annotation for "
                                                        + applicationClass);
            }
            else
            {
                // establish a new instance of the MetaClass
                Class<? extends MetaClass> metaClassClass = preferredMetaClass.value();

                try
                {
                    metaClass = metaClassClass.newInstance();
                }
                catch (Exception e)
                {
                    throw new UnsupportedOperationException("Failed to create MetaClass instance for "
                                                            + applicationClass,
                                                            e);
                }
            }
        }

        return metaClass;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        ApplicationType that = (ApplicationType) o;

        return applicationClass.equals(that.applicationClass);

    }


    @Override
    public int hashCode()
    {
        return applicationClass.hashCode();
    }

    @Override
    public String toString()
    {
        return "ApplicationType(class=" + applicationClass + ')';
    }


    /**
     * Create the default {@link ApplicationType} that uses the
     * {@link Application} class.
     *
     * @return  the default {@link ApplicationType} that uses the
     *          {@link Application} class
     */
    @Options.Default
    public static ApplicationType<Application> ofApplication()
    {
        return new ApplicationType<>(Application.class);
    }


    /**
     * Create a {@link ApplicationType}.
     *
     * @param applicationClass  the {@link Class} of the {@link Application}
     */
    public static <A extends Application> ApplicationType<A> of(Class<A> applicationClass)
    {
        return new ApplicationType<>(applicationClass);
    }
}
