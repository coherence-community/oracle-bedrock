/*
 * File: JvmOptions.java
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

package com.oracle.tools.runtime.java.options;

import com.oracle.tools.ComposableOption;
import com.oracle.tools.Option;

import java.util.LinkedHashSet;

/**
 * A {@link ComposableOption} representing a set of Java Virtual Machine options.
 * <p>
 * Copyright (c) 2014. All Rights Reserved. Oracle Corporation.<br>
 * Oracle is a registered trademark of Oracle Corporation and/or its affiliates.
 *
 * @author Brian Oliver
 */
public class JvmOptions implements JvmOption, ComposableOption<JvmOptions>
{
    /**
     * The custom {@link JvmOption} strings.
     */
    private LinkedHashSet<String> options;


    private JvmOptions(String... options)
    {
        this.options = new LinkedHashSet<>();

        if (options != null)
        {
            for (String option : options)
            {
                this.options.add(option);
            }
        }
    }


    @Override
    public JvmOptions compose(JvmOptions other)
    {
        JvmOptions set = new JvmOptions();

        set.options.addAll(this.options);
        set.options.addAll(other.options);

        return set;
    }


    @Override
    public String get()
    {
        StringBuilder builder = new StringBuilder();

        for (String option : options)
        {
            if (builder.length() > 0)
            {
                builder.append(" ");
            }

            builder.append(option);
        }

        return builder.toString();
    }


    /**
     * Add all of the specified Java Virtual Machine options to the {@link JvmOptions}.
     *
     * @param options  the options to add
     *
     * @return  a new {@link JvmOptions}
     */
    public JvmOptions addAll(String... options)
    {
        JvmOptions set = new JvmOptions();

        set.options.addAll(this.options);

        if (options != null)
        {
            for (String option : options)
            {
                set.options.add(option);
            }
        }

        return set;
    }


    /**
     * Creates an {@link Option} representing a set of strings
     * representing Java Virtual Machine options.
     *
     * @param options  the options
     *
     * @return the {@link JvmOptions}
     */
    public static JvmOptions include(String... options)
    {
        return new JvmOptions(options);
    }
}
