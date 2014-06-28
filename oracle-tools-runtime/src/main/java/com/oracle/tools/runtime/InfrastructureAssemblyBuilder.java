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

import com.oracle.tools.predicate.Predicate;
import com.oracle.tools.runtime.console.NullApplicationConsole;
import com.oracle.tools.runtime.console.SingletonApplicationConsoleBuilder;
import com.oracle.tools.util.Quadruple;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private Map<String, Quadruple<Predicate<P>, ApplicationSchema<A>, Integer, ApplicationConsoleBuilder>> apps;


    /**
     * Constructs an {@link InfrastructureAssemblyBuilder}
     *
     */
    public InfrastructureAssemblyBuilder()
    {
        apps = new LinkedHashMap<String,
                                 Quadruple<Predicate<P>, ApplicationSchema<A>, Integer, ApplicationConsoleBuilder>>();
    }


    /**
     * Add an application based on the specified {@link ApplicationSchema} that will
     * be realized when the {@link Assembly} is realized.
     *
     * @param applicationSchema      the {@link ApplicationSchema} that defines the application to be realized
     * @param applicationNamePrefix  the prefix for the application name
     * @param instancesPerPlatform   the number of instances of the application to realize per {@link Platform} in the
     *                               {@link Infrastructure}
     *
     * @param <T>  the type of {@link Application} realized from the {@link ApplicationSchema}
     * @param <S>  the type of {@link ApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> void addApplication(S      applicationSchema,
                                                                             String applicationNamePrefix,
                                                                             int    instancesPerPlatform)
    {
        addApplication(null,
                       applicationSchema,
                       applicationNamePrefix,
                       instancesPerPlatform,
                       (ApplicationConsoleBuilder) null);
    }


    /**
     * Add an application based on the specified {@link ApplicationSchema} that will
     * be realized when the {@link Assembly} is realized.
     *
     * @param applicationSchema      the {@link ApplicationSchema} that defines the application to be realized
     * @param applicationNamePrefix  the prefix for the application name
     * @param instancesPerPlatform   the number of instances of the application to realize per {@link Platform} in the
     *                               {@link Infrastructure}
     *
     * @param <T>  the type of {@link Application} realized from the {@link ApplicationSchema}
     * @param <S>  the type of {@link ApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> void addApplication(Predicate<P> predicate,
                                                                             S            applicationSchema,
                                                                             String       applicationNamePrefix,
                                                                             int          instancesPerPlatform)
    {
        addApplication(predicate,
                       applicationSchema,
                       applicationNamePrefix,
                       instancesPerPlatform,
                       (ApplicationConsoleBuilder) null);
    }


    /**
     * Add an application based on the specified {@link ApplicationSchema} that will
     * be realized when the {@link Assembly} is realized.
     *
     * @param applicationSchema      the {@link ApplicationSchema} that defines the application to be realized
     * @param applicationNamePrefix  the prefix for the application name
     * @param instancesPerPlatform   the number of instances of the application to realize per {@link Platform} in the
     *                               {@link Infrastructure}
     * @param consoleBuilder         the {@link ApplicationConsoleBuilder} to be used to provide
     *                               {@link ApplicationConsole}s for realized {@link Application}s.
     *
     * @param <T>  the type of {@link Application} realized from the {@link ApplicationSchema}
     * @param <S>  the type of {@link ApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> void addApplication(S                         applicationSchema,
                                                                             String                    applicationNamePrefix,
                                                                             int                       instancesPerPlatform,
                                                                             ApplicationConsoleBuilder consoleBuilder)
    {
        apps.put(applicationNamePrefix, new Quadruple(null, applicationSchema, instancesPerPlatform, consoleBuilder));
    }


    /**
     * Add an application based on the specified {@link ApplicationSchema} that will
     * be realized when the {@link Assembly} is realized.
     *
     * @param predicate              the {@link Predicate} that will be used to select which {@link Platform}s in
     *                               the {@link Infrastructure} will realize the application
     * @param applicationSchema      the {@link ApplicationSchema} that defines the application to be realized
     * @param applicationNamePrefix  the prefix for the application name
     * @param instancesPerPlatform   the number of instances of the application to realize per {@link Platform} in the
     *                               {@link Infrastructure}
     * @param console                the {@link ApplicationConsole} to be used to provide
     *                               {@link ApplicationConsole}s for realized {@link Application}s.
     *
     * @param <T>  the type of {@link Application} realized from the {@link ApplicationSchema}
     * @param <S>  the type of {@link ApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> void addApplication(Predicate<P>       predicate,
                                                                             S                  applicationSchema,
                                                                             String             applicationNamePrefix,
                                                                             int                instancesPerPlatform,
                                                                             ApplicationConsole console)
    {
        apps.put(applicationNamePrefix, new Quadruple(predicate,
                                                      applicationSchema,
                                                      instancesPerPlatform,
                                                      new SingletonApplicationConsoleBuilder(console)));
    }


    /**
     * Add an application based on the specified {@link ApplicationSchema} that will
     * be realized when the {@link Assembly} is realized.
     *
     * @param predicate              the {@link Predicate} that will be used to select which {@link Platform}s in
     *                               the {@link Infrastructure} will realize the application
     * @param applicationSchema      the {@link ApplicationSchema} that defines the application to be realized
     * @param applicationNamePrefix  the prefix for the application name
     * @param instancesPerPlatform   the number of instances of the application to realize per {@link Platform} in the
     *                               {@link Infrastructure}
     * @param consoleBuilder         the {@link ApplicationConsoleBuilder} to be used to provide
     *                               {@link ApplicationConsole}s for realized {@link Application}s.
     *
     * @param <T>  the type of {@link Application} realized from the {@link ApplicationSchema}
     * @param <S>  the type of {@link ApplicationSchema}
     */
    @SuppressWarnings("unchecked")
    public <T extends A, S extends ApplicationSchema<T>> void addApplication(Predicate<P>              predicate,
                                                                             S                         applicationSchema,
                                                                             String                    applicationNamePrefix,
                                                                             int                       instancesPerPlatform,
                                                                             ApplicationConsoleBuilder consoleBuilder)
    {
        apps.put(applicationNamePrefix, new Quadruple(predicate, applicationSchema, instancesPerPlatform, consoleBuilder));
    }


    /**
     * Realize the {@link Assembly} of {@link Application}s on the specified {@link Infrastructure}
     *
     * @param infrastructure            the {@link Infrastructure} to realize applications on
     * @param overridingConsoleBuilder  the {@link ApplicationConsoleBuilder} that will be used to create
     *                                  {@link ApplicationConsole}s for each of the realized {@link Application}s
     *                                  in the {@link Assembly}, overriding those that had a specific
     *                                  {@link ApplicationConsoleBuilder} specified for them using
     *                                  {@link #addApplication(ApplicationSchema, String, int, ApplicationConsoleBuilder)}
     *                                  When this is <code>null</code> the defined {@link ApplicationConsole}
     *                                  will be used for each {@link Application} in the {@link Assembly}
     *
     * @return an {@link Assembly} representing the collection of realized {@link Application}s.
     *
     * @throws RuntimeException Thrown if a problem occurs while realizing the application
     */
    @SuppressWarnings("unchecked")
    public G realize(Infrastructure<P>         infrastructure,
                     ApplicationConsoleBuilder overridingConsoleBuilder)
    {
        List<A> applications = new LinkedList<A>();

        for (Map.Entry<String,
                       Quadruple<Predicate<P>, ApplicationSchema<A>, Integer, ApplicationConsoleBuilder>> entry :
            apps.entrySet())
        {
            Quadruple<Predicate<P>, ApplicationSchema<A>, Integer, ApplicationConsoleBuilder> quad = entry.getValue();

            String                    prefix         = entry.getKey();
            Predicate<P>              predicate      = quad.getA();
            ApplicationSchema<A>      schema         = quad.getB();
            int                       count          = quad.getC();
            ApplicationConsoleBuilder consoleBuilder = quad.getD();

            if (overridingConsoleBuilder != null)
            {
                consoleBuilder = overridingConsoleBuilder;
            }

            for (P platform : infrastructure)
            {
                if (predicate == null || predicate.evaluate(platform))
                {
                    for (int i = 1; i <= count; i++)
                    {
                        String             name    = String.format("%s-%d@%s", prefix, i, platform.getName());

                        ApplicationConsole console = consoleBuilder == null ? null : consoleBuilder.realize(name);

                        if (console == null)
                        {
                            console = new NullApplicationConsole();
                        }

                        A application = platform.realize(schema, name, console);

                        applications.add(application);
                    }
                }
            }
        }

        return (G) new SimpleAssembly<A>(applications);
    }

    /**
     * Realize the {@link Assembly} of {@link Application}s on the specified {@link Infrastructure}
     *
     * @param infrastructure     the {@link Infrastructure} to realize applications on
     * @param overridingConsole  the {@link ApplicationConsole} that will be used for I/O by all of the
     *                           {@link Application}s realized in the {@link Assembly}, including
     *                           those that had a specific {@link ApplicationConsoleBuilder} specified for
     *                           them using {@link #addApplication(ApplicationSchema, String, int, ApplicationConsoleBuilder)}
     *                           When this is <code>null</code> the defined {@link ApplicationConsole}
     *                           will be used for each {@link Application} in the {@link Assembly}
     *
     * @return an {@link Assembly} representing the collection of realized {@link Application}s.
     *
     * @throws RuntimeException Thrown if a problem occurs while realizing the application
     */
    public G realize(Infrastructure<P>  infrastructure,
                     ApplicationConsole overridingConsole)
    {
        return realize(infrastructure,
                       overridingConsole == null ? null : new SingletonApplicationConsoleBuilder(overridingConsole));
    }


    /**
     * Realize the {@link Assembly} of {@link Application}s on the specified {@link Infrastructure}
     *
     * @param infrastructure  the {@link Infrastructure} to realize applications on
     *
     * @return an {@link Assembly} representing the collection of realized {@link Application}s.
     *
     * @throws IOException Thrown if a problem occurs while realizing the application
     */
    public G realize(Infrastructure<P> infrastructure) throws IOException
    {
        return realize(infrastructure, (ApplicationConsoleBuilder) null);
    }
}
