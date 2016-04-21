/*
 * File: InfrastructureAssemblyBuilder.java
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

import com.oracle.tools.Option;
import com.oracle.tools.Options;
import com.oracle.tools.runtime.options.Discriminator;
import com.oracle.tools.util.Quadruple;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * A builder of {@link Assembly}s for a given {@link Infrastructure}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 *
 * @param <P>  the type of {@link Platform}s that this builder will build applications on
 * @param <A>  the type of {@link Application}s that will be in a realized {@link Assembly}
 * @param <G>  the type of {@link Assembly}s that will be realized by the {@link AssemblyBuilder}
 */
public class InfrastructureAssemblyBuilder<P extends Platform, A extends Application, G extends Assembly<A>>
{
    private LinkedList<Quadruple<Predicate<P>, Class<? extends A>, Option[], Integer>> applications;


    /**
     * Constructs an {@link InfrastructureAssemblyBuilder}
     */
    public InfrastructureAssemblyBuilder()
    {
        applications = new LinkedList<>();
    }


    /**
     * Includes necessary information for launching one or more {@link Application}s of a specified
     * type as part of an {@link Assembly} when {@link #realize(Infrastructure, Option...)} is called.
     * <p>
     * Multiple calls to this method are permitted, allowing an {@link Assembly} to be created containing
     * multiple different types of {@link Application}s.
     *
     * @param predicate         the {@link Predicate} that will be used to select which {@link Platform}s in
     *                          the {@link Infrastructure} will build the application
     * @param count             the number of instances of the {@link Application} that should be realized for
     *                          the {@link Assembly}
     * @param applicationClass  the class of {@link Application}
     * @param options           the {@link Option}s to use for realizing the {@link Application}s
     *
     * @see Platform#launch(String, Option...)
     */
    public void include(Predicate<P>       predicate,
                        int                count,
                        Class<? extends A> applicationClass,
                        Option...          options)
    {
        applications.add(new Quadruple(predicate, applicationClass, options, count));
    }


    /**
     * Realize the {@link Assembly} of {@link Application}s on the specified {@link Infrastructure}.
     *
     * @param infrastructure   the {@link Infrastructure} to build applications on
     * @param options          the {@link Option}s for overriding defined options
     *
     * @return an {@link Assembly} representing the collection of realized {@link Application}s.
     *
     * @throws RuntimeException Thrown if a problem occurs while realizing the application
     */
    @SuppressWarnings("unchecked")
    public G realize(Infrastructure<P> infrastructure,
                     Option...         options)
    {
        List<A> applications = new LinkedList<>();

        for (Quadruple<Predicate<P>, Class<? extends A>, Option[], Integer> entry : this.applications)
        {
            Predicate<P>       predicate        = entry.getA();
            Class<? extends A> applicationClass = entry.getB();
            Options            launchOptions    = new Options(entry.getC());
            int                count            = entry.getD();

            // override the launch options with those specified
            launchOptions.addAll(options);

            for (P platform : infrastructure)
            {
                if (predicate == null || predicate.test(platform))
                {
                    for (int i = 1; i <= count; i++)
                    {
                        // add a discriminator for the application being launched
                        launchOptions.add(Discriminator.of(String.format("%d@%s", i, platform.getName())));

                        // launch the application
                        A application = platform.launch(applicationClass, launchOptions.asArray());

                        // add the application to the assembly
                        applications.add(application);
                    }
                }
            }
        }

        return (G) new SimpleAssembly<>(applications, Options.from(options));
    }
}
