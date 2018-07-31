/*
 * File: MetaClass.java
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

package com.oracle.bedrock.runtime;

import com.oracle.bedrock.Option;
import com.oracle.bedrock.OptionsByType;

/**
 * Defines meta-information about the {@link Class} of an {@link Application}.
 * <p>
 * Copyright (c) 2016. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @param <A> the type of the {@link Application}
 *
 * @author Brian Oliver
 * @author Jonathan Knight
 */
public interface MetaClass<A extends Application>
{
    /**
     * Obtains the concrete implementation {@link Class} for presenting {@link Application}s
     * launched on the specified {@link Platform} with the provided {@link Option}s.
     * <p>
     * This is the type of {@link Class} that will be returned by {@link Platform#launch(Class, Option...)}
     * for the {@link Application}.
     *
     * @param platform       the {@link Platform} on which the {@link Application} will be launched
     * @param optionsByType  the {@link OptionsByType} provided for launching the {@link Application}
     *
     * @return the {@link Class} of {@link Application}
     */
    Class<? extends A> getImplementationClass(Platform      platform,
                                              OptionsByType optionsByType);


    /**
     * Invoked by a {@link Platform} prior to an {@link Application} being launched,
     * allowing a {@link MetaClass} to prepare and customize the specified {@link OptionsByType}.
     *
     * @param platform       the {@link Platform}
     * @param optionsByType  the {@link OptionsByType}
     */
    void onLaunching(Platform      platform,
                     OptionsByType optionsByType);


    /**
     * Invoked by a {@link Platform} prior to an {@link Application} being launched,
     * but <strong>after</strong> all {@link Option} preparations have taken place, eg: by previous
     * calls to {@link #onLaunching(Platform, OptionsByType)} and other onLaunching methods, thus
     * allowing a {@link MetaClass} one final chance to customize launch configuration and behavior
     * of an {@link Application}.
     *
     * @param platform       the {@link Platform}
     * @param optionsByType  the final set of {@link OptionsByType} that will be used to launch
     *                       the {@link Application}.
     */
    void onLaunch(Platform      platform,
                  OptionsByType optionsByType);


    /**
     * Invoked by a {@link Platform} after an {@link Application} has been launched using the
     * specified {@link OptionsByType}, but before the {@link Application} is returned to the
     * {@link Thread} that requested the {@link Application}.
     *
     * @param platform       the {@link Platform}
     * @param application    the {@link Application}
     * @param optionsByType  the {@link OptionsByType}
     */
    void onLaunched(Platform      platform,
                    A             application,
                    OptionsByType optionsByType);


    /**
     * Detect the {@link MetaClass} for the specified {@link Class} of {@link Application}.
     *
     * @param applicationClass  the {@link Class} of {@link Application}
     *
     * @param <A>               the type of the {@link Application}
     *
     * @return  the {@link MetaClass} of the {@link Application} or <code>null</code> if not defined
     */
    @SuppressWarnings("unchecked")
    static <A extends Application> MetaClass<A> of(Class<?> applicationClass)
    {
        Class<? extends MetaClass> metaClassClass = null;

        // ----- search the classes and interfaces directly on the application class -----
        // (to see if the class directly provides a MetaClass)
        Class<?>[] declaredClasses = applicationClass.getDeclaredClasses();

        for (int i = 0; i < declaredClasses.length && metaClassClass == null; i++)
        {
            if (MetaClass.class.isAssignableFrom(declaredClasses[i]))
            {
                metaClassClass = (Class<? extends MetaClass>) declaredClasses[i];
            }
        }

        if (metaClassClass == null)
        {
            MetaClass<A> metaClass = null;

            // ----- search the interfaces implemented by the application class -----
            Class<?>[] interfaces = applicationClass.getInterfaces();

            for (int i = 0; i < interfaces.length && metaClass == null; i++)
            {
                metaClass = MetaClass.of(interfaces[i]);
            }

            if (metaClass == null)
            {
                // ----- search the super class of the application class -----
                Class<?> superClass = applicationClass.getSuperclass();

                if (superClass != null &&!superClass.equals(Object.class))
                {
                    metaClass = MetaClass.of(superClass);
                }
            }

            return metaClass;
        }
        else
        {
            try
            {
                // try to create the MetaClass instance
                return metaClassClass.getDeclaredConstructor().newInstance();
            }
            catch (Exception e)
            {
                throw new UnsupportedOperationException("Failed to create MetaClass instance for " + applicationClass,
                                                        e);
            }
        }
    }
}
