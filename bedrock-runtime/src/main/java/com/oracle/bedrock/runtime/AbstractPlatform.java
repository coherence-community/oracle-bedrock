/*
 * File: AbstractPlatform.java
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
import com.oracle.bedrock.annotations.Internal;
import com.oracle.bedrock.extensible.AbstractExtensible;

import java.util.List;

/**
 * An abstract implementation of a {@link Platform}.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Jonathan Knight
 */
@Internal
public abstract class AbstractPlatform extends AbstractExtensible implements Platform
{
    /**
     * The name of this {@link Platform}.
     */
    private String name;

    /**
     * The {@link OptionsByType} for the {@link Platform}.
     */
    private OptionsByType optionsByType;


    /**
     * Construct an {@link AbstractPlatform} with the specified name.
     *
     * @param name     the name of this {@link Platform}
     * @param options  the {@link Option}s for the {@link Platform}
     */
    public AbstractPlatform(String    name,
                            Option... options)
    {
        super();

        this.name          = name;
        this.optionsByType = OptionsByType.of(options);
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    public OptionsByType getOptions()
    {
        return optionsByType;
    }


    @Override
    @SuppressWarnings("unchecked")
    public <A extends Application> A launch(MetaClass<A> metaClass,
                                            Option...    options)
    {
        // establish the initial launch options based on those defined by the platform
        OptionsByType launchOptions = OptionsByType.of(getOptions().asArray());

        // include the options specified when this method was called
        launchOptions.addAll(options);

        // obtain the application launcher for the class of application
        // first, check the options to see if a launcher was specified
        ApplicationLauncher<A> launcher = launchOptions.get(ApplicationLauncher.class);

        if (launcher == null)
            {
            // no launcher option, sp check the Profiles to see if a launcher was specified
            // the first profile that specifies an option is the one we use
            List<Profile> profiles = Profiles.getOrderedProfiles();
            for (Profile profile : profiles)
                {
                launcher = profile.getLauncher(metaClass);
                if (launcher != null)
                    {
                    break;
                    }
                }
            }

        launcher = getApplicationLauncher(metaClass, launchOptions);

    if (launcher == null)
        {
            throw new IllegalArgumentException("Can't determine ApplicationLauncher for " + metaClass + " using "
                                               + launchOptions);
        }
        else
        {
            // now launch the application
            return launcher.launch(this, metaClass, launchOptions);
        }
    }


    /**
     * Obtains the {@link ApplicationLauncher} for the given {@link Application} {@link MetaClass} and launch
     * {@link OptionsByType}.
     *
     * @param metaClass      the {@link MetaClass} for the {@link Application}
     * @param optionsByType  the launch {@link OptionsByType} for the {@link Application}
     *
     * @param <A>            the type of the {@link Application}
     * @param <B>            the type of the {@link ApplicationLauncher}
     *
     * @throws UnsupportedOperationException  when an {@link ApplicationLauncher} for the specified {@link Application}
     *                                        {@link Class} and {@link MetaClass} with the provided {@link OptionsByType}
     *                                        is unavailable and/or unsupported
     * @return  an {@link ApplicationLauncher} capable of launching the {@link Class} of {@link Application}
     */
    abstract protected <A extends Application,
                        B extends ApplicationLauncher<A>> B getApplicationLauncher(MetaClass<A>  metaClass,
                                                                                   OptionsByType optionsByType)
                                                                                   throws UnsupportedOperationException;
}
