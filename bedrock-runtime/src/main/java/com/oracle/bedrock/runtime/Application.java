/*
 * File: Application.java
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
import com.oracle.bedrock.extensible.Extensible;
import com.oracle.bedrock.options.Timeout;
import com.oracle.bedrock.runtime.options.ApplicationClosingBehavior;

import java.io.Closeable;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A platform and location independent mechanism to represent, access and
 * control a running application.
 * <p>
 * Copyright (c) 2011. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public interface Application extends Extensible, Closeable
{
    /**
     * Obtains the name of the {@link Application}.
     *
     * @return The name of the {@link Application}
     */
    String getName();


    /**
     * Obtains the {@link Platform} that this {@link Application}
     * is running on.
     *
     * @return the {@link Platform} that this {@link Application}
     *         is running on
     */
    Platform getPlatform();


    /**
     * Attempts to close and terminate the running {@link Application}.
     * <p>
     * Upon returning it is safe to assume that the {@link Application}
     * is no longer running.   All resources, including input and output streams
     * used by the {@link Application} are no longer available to be used.
     * <p>
     * To determine the exit value of the terminated application use {@link #exitValue()}.
     */
    @Override
    void close();


    /**
     * Attempts to close and terminate the running {@link Application} using the
     * specified {@link Option}s.  When no {@link Option}s are specified
     * a regular {@link #close} is performed.
     * <p>
     * Upon returning it is safe to assume that the {@link Application}
     * is no longer running.   All resources, including input and output streams
     * used by the {@link Application} are no longer available to be used.
     * <p>
     * To determine the exit value of the terminated application use {@link #exitValue()}.
     *
     * @param options  the {@link Option}s indicating how to close the application.
     *
     * @see ApplicationClosingBehavior
     */
    void close(Option... options);


    /**
     * Determines if the {@link Application} is operational, has not been closed
     * and closing has not been commenced.
     *
     * @return <code>true</code>  if operational,
     *         <code>false</code> if the {@link Application} has been closed or
     *                            has commenced closing
     */
    boolean isOperational();


    /**
     * Causes the calling {@link Thread} to block and wait until the
     * {@link Application} has terminated, either naturally or due to a call
     * to {@link #close()} by another {@link Thread}.
     * <p>
     * This method returns immediately if the {@link Application} has already
     * been terminated.
     *
     * @param options  the {@link Option}s for waiting, including possible
     *                 {@link Timeout}
     *
     * @return the exit value of the {@link Application}
     *         (by convention <code>0</code> indicates normal termination)
     *
     * @throws RuntimeException  if it wasn't possible to wait for the
     *                           termination.
     */
    int waitFor(Option... options);


    /**
     * Obtains the exit value for the terminated {@link Application}.
     *
     * @return the exit value of the {@link Application}
     *
     * @throws IllegalThreadStateException if the {@link Application} has not
     *                                     yet terminated
     */
    int exitValue();


    /**
     * Obtain the identity for the {@link Application}.  This is typically
     * the underlying process id (pid), but in some circumstances this may be
     * an environment specific identifier.  eg: in a cloud/container/multi-tenant
     * environment this may not be an operating system pid.
     *
     * @return The unique identity or -1 if it can't be determined.
     */
    long getId();


    /**
     * Obtains the default duration to used by the {@link Application}
     * for timeouts.
     *
     * @return a {@link Timeout}
     */
    Timeout getDefaultTimeout();


    /**
     * Obtains the configured {@link OptionsByType} for the {@link Application}.
     * <p>
     * <strong>Changes to the {@link OptionsByType} may not be recognized
     * or used by the {@link Application} after it was realized.</strong>
     *
     * @return the {@link OptionsByType}
     */
    OptionsByType getOptions();


    /**
     * The {@link com.oracle.bedrock.runtime.MetaClass} for generic {@link Application}s.
     */
    class MetaClass implements com.oracle.bedrock.runtime.MetaClass<Application>
    {
        /**
         * Constructs a {@link MetaClass} for a {@link Application}.
         */
        @OptionsByType.Default
        public MetaClass()
        {
        }


        @Override
        public Class<? extends Application> getImplementationClass(Platform      platform,
                                                                   OptionsByType optionsByType)
        {
            return SimpleApplication.class;
        }


        @Override
        public void onLaunching(Platform      platform,
                                OptionsByType optionsByType)
        {
            // there's nothing to do before launching the application
        }


        @Override
        public void onLaunch(Platform      platform,
                             OptionsByType optionsByType)
        {
            // there's nothing to do before launching the application
        }


        @Override
        public void onLaunched(Platform      platform,
                               Application   application,
                               OptionsByType optionsByType)
        {
            // there's nothing to do after launching the application
        }
    }


    static <A extends Application> Predicate<A> withName(String name)
    {
        String regex = Pattern.quote(name);
        Pattern pattern = Pattern.compile(regex);
        return new Predicate<A>() {
            @Override
            public boolean test(A a) {
                return pattern.matcher(a.getName()).matches();
            }
        };
    }

    static <A extends Application> Predicate<A> withNameMatching(String regex)
    {
        Pattern pattern = Pattern.compile(regex);
        return a -> pattern.matcher(a.getName()).matches();
    }

    static <A extends Application> Predicate<A> withNamePrefix(String name)
    {
        String regex = Pattern.quote(name);
        Pattern pattern = Pattern.compile(regex);
        return a -> pattern.matcher(a.getName()).lookingAt();
    }

    static <A extends Application> Predicate<A> withNameContaining(String name)
    {
        String regex = Pattern.quote(name);
        Pattern pattern = Pattern.compile(regex);
        return a -> pattern.matcher(a.getName()).find();
    }
}
